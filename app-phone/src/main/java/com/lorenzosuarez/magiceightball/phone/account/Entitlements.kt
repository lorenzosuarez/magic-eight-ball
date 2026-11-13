package com.lorenzosuarez.magiceightball.phone.account

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn

/**
 * What whoever is using the app has paid for. One thing is sold: Premium, which is making
 * characters of one's own, more answers from the AI in a day, and no ads.
 *
 * The app never takes its own word for it, nor Google Play's as the device tells it: an app
 * that did could be told anything. What counts is what the project's server wrote down after
 * checking the purchase with Google: see [Shop].
 */
interface Entitlements {
    /** Whether whoever is using the app has what is sold. False while that is not known. */
    val premium: StateFlow<Boolean>

    /**
     * Whether [premium] is what the server said, and not only what is assumed until it has been
     * heard from. Nobody is offered what they may already have: that waits for this.
     */
    val known: StateFlow<Boolean> get() = HEARD

    /** For builds with nothing to sign in to, and for tests: nobody has paid for anything. */
    object Nobody : Entitlements {
        override val premium: StateFlow<Boolean> = MutableStateFlow(false)
    }

    private companion object {
        /** Where there is no server to hear from, what is said is all there is to know. */
        val HEARD: StateFlow<Boolean> = MutableStateFlow(true)
    }
}

/**
 * [Entitlements] as the server keeps them: `users/{id}/grants/premium` is there for an account
 * that holds a purchase, and is not for any other. The project's rules let the account read it
 * and let nobody write it; only the server does.
 */
@Singleton
class FirestoreEntitlements @Inject constructor(store: AccountStore, account: Account) : Entitlements {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** What the server says of whoever is signed in: null until it has been heard from. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val heard: StateFlow<Boolean?> = account.state
        .map { (it as? AccountState.SignedIn)?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            val grant = id?.let {
                store.firestore?.collection(AccountStore.USERS)?.document(it)?.collection(GRANTS)?.document(PREMIUM)
            }
            // Signed out, nothing is theirs, and that much is known: what the last account had
            // is not shown to the next. Of an account just signed in, nothing is known yet.
            grant?.let { held(it).onStart { emit(null) } } ?: flowOf(false)
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val premium: StateFlow<Boolean> =
        heard.map { it == true }.stateIn(scope, SharingStarted.Eagerly, false)

    override val known: StateFlow<Boolean> =
        heard.map { it != null }.stateIn(scope, SharingStarted.Eagerly, false)

    private fun held(grant: DocumentReference): Flow<Boolean?> = callbackFlow<Boolean?> {
        val listening = grant.addSnapshotListener { snapshot, failure ->
            if (failure != null || snapshot == null) {
                close(failure ?: IllegalStateException("No grant and no reason"))
            } else {
                trySend(snapshot.exists())
            }
        }
        awaitClose { listening.remove() }
    }.retryWhen { failure, attempt ->
        // What was known stays as it was: a connection that blinked takes nothing from anyone.
        Log.w(TAG, "Could not read what is held: ${(failure as? FirebaseFirestoreException)?.code ?: failure.javaClass.simpleName}")
        delay(RETRY_FIRST_MS shl attempt.toInt().coerceAtMost(RETRY_DOUBLINGS))
        true
    }

    private companion object {
        const val TAG = "Entitlements"
        const val GRANTS = "grants"
        const val PREMIUM = "premium"
        const val RETRY_FIRST_MS = 2_000L
        const val RETRY_DOUBLINGS = 5
    }
}
