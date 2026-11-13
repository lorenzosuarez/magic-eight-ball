package com.lorenzosuarez.magiceightball.phone.account

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.lorenzosuarez.magiceightball.phone.config.AppConfigSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

/**
 * What is sold, as the store has it for whoever is using the app.
 *
 * @property price as the store words it for them, in their currency: never worked out here.
 */
data class Offer(val price: String)

/** How buying ended. */
enum class Bought {
    /** Paid for, checked, and theirs. */
    DONE,

    /** Google Play is still waiting for the money. It is theirs once that comes, with nothing more to do. */
    PENDING,

    /** They backed out. Nothing to say. */
    CANCELLED,

    /** It was bought before, and is held by another account of the app. */
    TAKEN,

    /** Paid for, and not yet checked: asking again checks it, and never charges twice. */
    UNCONFIRMED,

    /** Nothing was bought. */
    FAILED
}

/**
 * Where what is sold is bought. One thing is: Premium, paid for once.
 *
 * A purchase is the store's word for it, and counts only once the project's own server has
 * checked that word with the store and written it down: see [Entitlements]. What the server
 * writes down is held by the account that is signed in, so nobody buys without one.
 */
interface Shop {
    /** What can be bought and for how much; null while the store has not said, or nothing can. */
    val offer: StateFlow<Offer?>

    /**
     * Shows Google Play's own screen for buying it, and waits for how that ends.
     *
     * @param host the activity it is shown over.
     */
    suspend fun buy(host: Context): Bought

    /** For builds and tests with no store. */
    object None : Shop {
        override val offer: StateFlow<Offer?> = MutableStateFlow(null)
        override suspend fun buy(host: Context) = Bought.FAILED
    }
}

/**
 * [Shop] over Google Play's billing, with the project's Cloud Functions as the server that
 * checks a purchase: `claimPremium` is given the token Google Play hands the app, asks Google
 * whether it is paid for, and writes down which account holds it.
 *
 * Google Play is not spoken to at all while nothing is sold or nobody is signed in. Otherwise
 * anything bought before is checked at once, and again until it can be: what this account does
 * not hold yet, after a new phone, a payment that was pending or a check that could not be made
 * when it was paid for, and what Google Play was never told had been delivered, which it would
 * give the money back for after three days. Whoever does not have it yet is told the price too.
 */
@Singleton
class PlayShop @Inject constructor(
    @ApplicationContext private val context: Context,
    private val account: Account,
    private val entitlements: Entitlements,
    config: AppConfigSource
) : Shop, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val offered = MutableStateFlow<Offer?>(null)
    override val offer: StateFlow<Offer?> = offered.asStateFlow()

    /** How the purchase Google Play last told of ended, for [buy] to hear. */
    private val ended = Channel<Bought>(Channel.CONFLATED)

    private val connecting = Mutex()

    private val client: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(this)
            // A payment in cash, or one a parent has to approve, is told of before it is made.
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    }

    init {
        scope.launch {
            combine(config.config, account.state, entitlements.premium) { config, account, premium ->
                (config.premiumEnabled && account is AccountState.SignedIn) to premium
            }.distinctUntilChanged().collectLatest { (open, premium) ->
                offered.value = null
                if (!open) return@collectLatest
                coroutineScope {
                    // What was paid for and could not be checked is tried until it can be.
                    launch { until { restore() != Bought.UNCONFIRMED } }
                    // Until the store has said: without a price nothing can be bought at all.
                    if (!premium) {
                        launch {
                            until {
                                offered.value = details()?.oneTimePurchaseOfferDetails?.let { Offer(it.formattedPrice) }
                                offered.value != null
                            }
                        }
                    }
                }
            }
        }
    }

    /** Tries [done] again, a little later each time, until it says it is. */
    private suspend fun until(done: suspend () -> Boolean) {
        var wait = RETRY_FIRST_MS
        while (!done()) {
            delay(wait)
            wait = (wait * 2).coerceAtMost(RETRY_LONGEST_MS)
        }
    }

    override suspend fun buy(host: Context): Bought {
        val activity = host.activity() ?: return Bought.FAILED
        val buyer = (account.state.value as? AccountState.SignedIn)?.id ?: return Bought.FAILED
        // Paid for already, and not yet theirs here: that is settled, and nothing is sold twice.
        restore().takeIf { it != Bought.FAILED }?.let { return it }
        // Asked for afresh: what the store said a while ago is not what it sells now.
        val details = details() ?: return Bought.FAILED
        val price = details.oneTimePurchaseOfferDetails ?: return Bought.FAILED
        // Whatever ended before this one started is not this one's ending.
        while (ended.tryReceive().isSuccess) Unit
        val product = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        price.offerToken.orEmpty().takeIf { it.isNotEmpty() }?.let(product::setOfferToken)
        val shown = client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(product.build()))
                // Not who they are: a mark Google Play can tell two buyers apart by.
                .setObfuscatedAccountId(digest(buyer))
                .build()
        )
        return when (shown.responseCode) {
            BillingResponseCode.OK -> ended.receive()
            BillingResponseCode.ITEM_ALREADY_OWNED -> restore()
            BillingResponseCode.USER_CANCELED -> Bought.CANCELLED
            else -> {
                Log.w(TAG, "Could not show the purchase: ${shown.responseCode}")
                Bought.FAILED
            }
        }
    }

    /**
     * Google Play tells of a purchase here whoever is listening, and whether or not the screen
     * that started it is still there: it is checked either way.
     */
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        scope.launch {
            val outcome = try {
                when (result.responseCode) {
                    BillingResponseCode.OK -> settle(purchases.orEmpty())
                    BillingResponseCode.USER_CANCELED -> Bought.CANCELLED
                    BillingResponseCode.ITEM_ALREADY_OWNED -> restore()
                    else -> {
                        Log.w(TAG, "The purchase was not made: ${result.responseCode}")
                        Bought.FAILED
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Whoever is waiting is always told how it ended: here, that it is not known yet.
                Log.w(TAG, "Could not settle the purchase: ${e.javaClass.simpleName}")
                Bought.UNCONFIRMED
            }
            ended.send(outcome)
        }
    }

    /** What Google Play holds as bought on this device, checked for this account. */
    private suspend fun restore(): Bought {
        if (!connected()) return Bought.FAILED
        val held = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build())
        if (held.billingResult.responseCode != BillingResponseCode.OK) return Bought.FAILED
        return settle(held.purchasesList)
    }

    private suspend fun settle(purchases: List<Purchase>): Bought {
        val purchase = purchases.firstOrNull { PRODUCT in it.products } ?: return Bought.FAILED
        return when (purchase.purchaseState) {
            // Theirs already, and Google Play knows it was delivered: nothing is left to do. Until
            // Google Play knows, the server is asked again, which is what tells it.
            Purchase.PurchaseState.PURCHASED ->
                if (entitlements.premium.value && purchase.isAcknowledged) Bought.DONE else claim(purchase.purchaseToken)
            Purchase.PurchaseState.PENDING -> Bought.PENDING
            else -> Bought.FAILED
        }
    }

    /**
     * Has the server check a purchase and write it down for whoever is signed in. An answer that
     * does not come in time, or is not one of those agreed, leaves it as Google Play has it:
     * paid for, and not yet checked.
     */
    private suspend fun claim(token: String): Bought = try {
        val answer = withTimeoutOrNull(CLAIM_TIMEOUT_MS) {
            FirebaseFunctions.getInstance(REGION).getHttpsCallable(CLAIM).call(mapOf(TOKEN to token)).await()
        }
        when ((answer?.data as? Map<*, *>)?.get(OUTCOME)) {
            SAID_GRANTED -> {
                // Done once this device has seen what the server wrote, so that whoever is told
                // it is theirs can use it; and done all the same if that is slow to come.
                withTimeoutOrNull(SEEN_TIMEOUT_MS) { entitlements.premium.first { it } }
                Bought.DONE
            }
            SAID_PENDING -> Bought.PENDING
            SAID_TAKEN -> Bought.TAKEN
            SAID_INVALID -> Bought.FAILED
            else -> Bought.UNCONFIRMED
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Never the token: only how the server took it.
        Log.w(TAG, "Could not check the purchase: ${(e as? FirebaseFunctionsException)?.code ?: e.javaClass.simpleName}")
        Bought.UNCONFIRMED
    }

    private suspend fun details(): ProductDetails? {
        if (!connected()) return null
        val asked = client.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT)
                            .setProductType(ProductType.INAPP)
                            .build()
                    )
                )
                .build()
        )
        if (asked.billingResult.responseCode != BillingResponseCode.OK) {
            Log.w(TAG, "Could not ask what is sold: ${asked.billingResult.responseCode}")
            return null
        }
        return asked.productDetailsList?.firstOrNull { it.productId == PRODUCT }
    }

    /** True once Google Play answers. After the first time it finds its own way back. */
    private suspend fun connected(): Boolean = connecting.withLock {
        // A try that whoever asked for it gave up on may still be on its way: it is waited for,
        // since starting another meanwhile is answered at once with a refusal.
        withTimeoutOrNull(CONNECTING_WAIT_MS) {
            while (client.connectionState == BillingClient.ConnectionState.CONNECTING) delay(CONNECTING_POLL_MS)
        }
        if (client.isReady) return@withLock true
        suspendCancellableCoroutine { waiting ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    // Called again on every reconnection, when nobody is waiting any more.
                    if (waiting.isActive) waiting.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    private companion object {
        const val TAG = "Shop"

        /** The product in Google Play, and what the server looks a token up as. */
        const val PRODUCT = "premium"

        /** Where the project's functions run, next to its data. */
        const val REGION = "europe-west1"
        const val CLAIM = "claimPremium"
        const val TOKEN = "token"
        const val OUTCOME = "outcome"
        const val SAID_GRANTED = "granted"
        const val SAID_PENDING = "pending"
        const val SAID_TAKEN = "taken"
        const val SAID_INVALID = "invalid"
        const val CLAIM_TIMEOUT_MS = 20_000L
        const val SEEN_TIMEOUT_MS = 5_000L
        const val RETRY_FIRST_MS = 2_000L
        const val RETRY_LONGEST_MS = 60_000L
        const val CONNECTING_WAIT_MS = 5_000L
        const val CONNECTING_POLL_MS = 100L

        fun digest(text: String): String =
            MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

        tailrec fun Context.activity(): Activity? = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.activity()
            else -> null
        }
    }
}
