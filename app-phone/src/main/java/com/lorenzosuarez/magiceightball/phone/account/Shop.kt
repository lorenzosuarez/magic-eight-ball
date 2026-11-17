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
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResult
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
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.time.Period
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * @property price what each payment comes to, as the store words it for them, in their
 * currency: never worked out here.
 * @property period how often it is paid, as the store has it: never assumed here.
 */
data class Offer(val price: String, val period: BillingPeriod)

/** How often a subscription is paid for: every [count] of [unit]. */
data class BillingPeriod(val count: Int, val unit: Unit) {
    enum class Unit { WEEK, MONTH, YEAR }

    companion object {
        /**
         * From the store's own wording of it, as in "P1M", "P4W" or "P1Y". Null for whatever is
         * not a whole number of weeks, of months or of years: the store sells by nothing else,
         * and what cannot be said plainly is not offered.
         */
        fun of(text: String): BillingPeriod? {
            val period = try {
                Period.parse(text)
            } catch (_: DateTimeParseException) {
                return null
            }
            val (years, months, days) = Triple(period.years, period.months, period.days)
            return when {
                years > 0 && months == 0 && days == 0 -> BillingPeriod(years, Unit.YEAR)
                months > 0 && years == 0 && days == 0 -> BillingPeriod(months, Unit.MONTH)
                days > 0 && days % 7 == 0 && years == 0 && months == 0 -> BillingPeriod(days / 7, Unit.WEEK)
                else -> null
            }
        }
    }
}

/**
 * One way of paying for the subscription, as the store lists it, down to what choosing among
 * them needs.
 *
 * @property offer the discount it is sold at; null for the base plan at its own price.
 * @property token what the store is handed to sell this one.
 */
internal data class Plan(val basePlan: String, val offer: String?, val token: String, val phases: List<Phase>)

/** A stretch of a plan: what a payment comes to, how often, and whether it goes on until cancelled. */
internal data class Phase(val price: String, val period: String, val renews: Boolean)

/**
 * The plan that is sold, with its terms: [BASE_PLAN] at its own price, paid again each period
 * until it is cancelled. A trial or a discount the store may list beside it is passed over, and
 * so is a plan that ends by itself: the sheet that sells Premium says one price and that it
 * renews, and nothing is sold on other terms than those said.
 */
internal fun List<Plan>.sold(): Pair<Plan, Offer>? {
    val plan = firstOrNull { it.basePlan == BASE_PLAN && it.offer == null } ?: return null
    val phase = plan.phases.singleOrNull()?.takeIf { it.renews } ?: return null
    return BillingPeriod.of(phase.period)?.let { plan to Offer(phase.price, it) }
}

/**
 * Google Play's own page for the subscription, where it is cancelled or its payment changed, at
 * the address Google gives for it.
 */
fun subscriptionPage(packageName: String): String =
    "https://play.google.com/store/account/subscriptions?sku=$SUBSCRIPTION&package=$packageName"

/**
 * The subscription and its one base plan in Google Play, made in Play Console under these ids:
 * see docs/REMOTE_CONFIG.md. The first is also what the server looks a token up as.
 */
internal const val SUBSCRIPTION = "premium_subscription"
internal const val BASE_PLAN = "monthly"

/** What Premium was first sold as, bought once. It is sold no more, and whoever bought it keeps it. */
internal const val ONE_TIME = "premium"

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
 * Where what is sold is bought. One thing is: Premium, a subscription paid for again each
 * period until it is cancelled in Google Play.
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

    /**
     * The app came to the front. What Google Play holds is asked again then, and not only when
     * the process starts: a subscription whose payment was put right in Google Play, or a
     * purchase that could not be checked when it was made, is theirs when they come back.
     * And Google Play is let say, over [host], what it has to say of a payment that failed:
     * Premium going with no word of why is the one thing a subscriber cannot work out alone.
     */
    fun opened(host: Activity) = Unit

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
 * give the money back for after three days. That goes for Premium bought once too, from when it
 * was sold that way. Whoever does not have it yet is told the price too.
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

    /**
     * Each coming to the front, with the screen that came: only the latest matters, and none is
     * waited for. With no screen, it is only what is held that is asked again.
     */
    private val openings =
        MutableSharedFlow<WeakReference<Activity>?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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
                    // So is asking Google Play what is held: a subscription that came back from
                    // being on hold or paused is theirs again only once it is claimed here.
                    launch { until { held()?.let { settle(it) != Bought.UNCONFIRMED } == true } }
                    // Until the store has said: without a price nothing can be bought at all.
                    if (!premium) {
                        launch {
                            until {
                                offered.value = details()?.plans()?.sold()?.second
                                offered.value != null
                            }
                        }
                    }
                }
            }
        }
        scope.launch {
            // Settling what is already theirs asks the server for nothing: see [settle].
            openings.collectLatest { host ->
                val open = config.config.value.premiumEnabled && account.state.value is AccountState.SignedIn
                if (!open) return@collectLatest
                host?.get()?.let { remind(it) }
                until { held()?.let { settle(it) != Bought.UNCONFIRMED } == true }
            }
        }
    }

    override fun opened(host: Activity) {
        openings.tryEmit(WeakReference(host))
    }

    /**
     * Google Play's own message over [host] when a subscription's payment was refused, with its
     * own way to put the payment right: Google Play decides whether there is anything to say,
     * and how often to say it. Put right from there, what is held is asked again.
     */
    private suspend fun remind(host: Activity) {
        if (!connected() || host.isFinishing || host.isDestroyed) return
        val which = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
            .build()
        client.showInAppMessages(host, which) { result ->
            if (result.responseCode == InAppMessageResult.InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED) {
                openings.tryEmit(null)
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
        val (plan, _) = details.plans().sold() ?: return Bought.FAILED
        // Whatever ended before this one started is not this one's ending.
        while (ended.tryReceive().isSuccess) Unit
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(plan.token)
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
    private suspend fun restore(): Bought = held()?.let { settle(it) } ?: Bought.FAILED

    /** What Google Play holds as bought on this device; null when it could not be asked. */
    private suspend fun held(): List<Purchase>? {
        if (!connected()) return null
        // A subscription on hold or paused is not among them: it gives nothing until it is back.
        return listOf(ProductType.SUBS, ProductType.INAPP).flatMap { type ->
            val asked = client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(type).build())
            if (asked.billingResult.responseCode != BillingResponseCode.OK) return null
            asked.purchasesList
        }
    }

    /**
     * Bought once first, since that one is theirs for good; and if that does not make Premium
     * theirs, as when another account holds it, the subscription is not left unclaimed for it.
     */
    private suspend fun settle(purchases: List<Purchase>): Bought {
        val outcomes = listOf(ONE_TIME, SUBSCRIPTION).mapNotNull { product ->
            val purchase = purchases.firstOrNull { product in it.products } ?: return@mapNotNull null
            settle(purchase, product).also { if (it == Bought.DONE) return it }
        }
        return outcomes.firstOrNull() ?: Bought.FAILED
    }

    private suspend fun settle(purchase: Purchase, product: String): Bought {
        return when (purchase.purchaseState) {
            // Theirs already, and Google Play knows it was delivered: nothing is left to do. Until
            // Google Play knows, the server is asked again, which is what tells it.
            Purchase.PurchaseState.PURCHASED ->
                if (entitlements.premium.value && purchase.isAcknowledged) Bought.DONE else claim(purchase.purchaseToken, product)
            Purchase.PurchaseState.PENDING -> Bought.PENDING
            else -> Bought.FAILED
        }
    }

    /**
     * Has the server check a purchase of [product] and write it down for whoever is signed in. An answer that
     * does not come in time, or is not one of those agreed, leaves it as Google Play has it:
     * paid for, and not yet checked.
     */
    private suspend fun claim(token: String, product: String): Bought = try {
        val answer = withTimeoutOrNull(CLAIM_TIMEOUT_MS) {
            FirebaseFunctions.getInstance(REGION).getHttpsCallable(CLAIM).call(mapOf(TOKEN to token, PRODUCT to product)).await()
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
                            .setProductId(SUBSCRIPTION)
                            .setProductType(ProductType.SUBS)
                            .build()
                    )
                )
                .build()
        )
        if (asked.billingResult.responseCode != BillingResponseCode.OK) {
            Log.w(TAG, "Could not ask what is sold: ${asked.billingResult.responseCode}")
            return null
        }
        return asked.productDetailsList?.firstOrNull { it.productId == SUBSCRIPTION }
    }

    private fun ProductDetails.plans(): List<Plan> = subscriptionOfferDetails.orEmpty().map { plan ->
        Plan(
            basePlan = plan.basePlanId,
            offer = plan.offerId,
            token = plan.offerToken,
            phases = plan.pricingPhases.pricingPhaseList.map {
                Phase(it.formattedPrice, it.billingPeriod, it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING)
            }
        )
    }

    /** True once Google Play answers. After the first time it finds its own way back. */
    private suspend fun connected(): Boolean = connecting.withLock {
        // A try that whoever asked for it gave up on may still be on its way: it is waited for,
        // since starting another meanwhile is answered at once with a refusal.
        withTimeoutOrNull(CONNECTING_WAIT_MS) {
            while (client.connectionState == BillingClient.ConnectionState.CONNECTING) delay(CONNECTING_POLL_MS)
        }
        if (client.isReady) return@withLock true
        // Not waited for without end: with the store being updated, or turned off, it may never answer,
        // and everything that asks after it would wait behind this one.
        withTimeoutOrNull(CONNECTING_WAIT_MS) {
            suspendCancellableCoroutine { waiting ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        // Called again on every reconnection, when nobody is waiting any more.
                        if (waiting.isActive) waiting.resume(result.responseCode == BillingResponseCode.OK)
                    }

                    override fun onBillingServiceDisconnected() = Unit
                })
            }
        } ?: false
    }

    private companion object {
        const val TAG = "Shop"

        /** Where the project's functions run, next to its data. */
        const val REGION = "europe-west1"
        const val CLAIM = "claimPremium"
        const val TOKEN = "token"
        const val PRODUCT = "product"
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
