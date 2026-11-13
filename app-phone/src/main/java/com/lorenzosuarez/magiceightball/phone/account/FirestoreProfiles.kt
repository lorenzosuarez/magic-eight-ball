package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The profile, kept by Cloud Firestore in a document of the account's own, `users/{id}`, which
 * the project's rules let nobody else read or write.
 *
 * Nothing of it is written to this device: the copy Firestore keeps is in memory only, and
 * every change is made on the server or not at all, so that a profile is never saved or deleted
 * behind the back of a screen that said it could not be.
 *
 * Where there is no Firebase project, or nobody is signed in, there is no profile to know of.
 */
@Singleton
class FirestoreProfiles @Inject constructor(
    @ApplicationContext context: Context,
    account: Account
) : Profiles {

    private val store: FirebaseFirestore? =
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseFirestore.getInstance().apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build()
            }
        } else null

    /** The document of whoever is signed in; null when nobody is. */
    @Volatile
    private var document: DocumentReference? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ProfileState> = account.state
        .map { (it as? AccountState.SignedIn)?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            document = id?.let { store?.collection(USERS)?.document(it) }
            document?.let(::profileIn) ?: flowOf(ProfileState.Unknown)
        }
        .stateIn(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate), SharingStarted.Eagerly, ProfileState.Unknown)

    /**
     * What the document says, for as long as someone listens. A listener that fails is put back
     * after a while, a little longer each time: until then the profile is not known.
     */
    private fun profileIn(document: DocumentReference): Flow<ProfileState> = callbackFlow {
        // Once the server has said there is no profile, losing the connection does not unsay it.
        var missingOnTheServer = false
        // Told of every change, the news from the server included: an account new to this
        // device has nothing here yet, and that says nothing about what the server holds.
        val listening = document.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, failure ->
            if (failure != null || snapshot == null) {
                close(failure ?: IllegalStateException("No profile and no reason"))
                return@addSnapshotListener
            }
            val fromCache = snapshot.metadata.isFromCache
            if (!fromCache) missingOnTheServer = !snapshot.exists()
            trySend(profileOf(snapshot.exists(), fromCache && !missingOnTheServer, snapshot.data))
        }
        awaitClose { listening.remove() }
    }.retryWhen { failure, attempt ->
        // Never what the document holds: only that it could not be read.
        Log.w(TAG, "Could not read the profile: ${(failure as? FirebaseFirestoreException)?.code ?: failure.javaClass.simpleName}")
        emit(ProfileState.Unknown)
        delay((RETRY_FIRST_MS shl attempt.toInt().coerceAtMost(RETRY_DOUBLINGS)))
        true
    }.onStart { emit(ProfileState.Unknown) }

    override suspend fun save(profile: Profile): SaveOutcome = when (
        write { transaction, document ->
            val name = mapOf<String, Any>(FIRST_NAME to profile.firstName, BIRTH_DATE to profile.birthDate.toString())
            if (agreedTo(transaction.get(document).data)) {
                // A correction: the record of the user's agreement stays as it was made.
                transaction.update(document, name)
            } else {
                // Which wording was agreed to, and when by the server's clock.
                val consent = mapOf(CONSENT_VERSION_FIELD to CONSENT_VERSION, CONSENT_AT to FieldValue.serverTimestamp())
                transaction.set(document, name + (CONSENT to consent))
            }
        }
    ) {
        true -> SaveOutcome.SAVED
        false -> SaveOutcome.REFUSED
        null -> SaveOutcome.FAILED
    }

    override suspend fun delete(): Boolean = write { transaction, document -> transaction.delete(document) } == true

    /**
     * Makes a change on the server, as a transaction: it is made there or not at all, and
     * nothing waits on the device to be sent later. True when made, false when the project's
     * rules refused it, null when it could not be made within [WRITE_TIMEOUT_MS].
     */
    private suspend fun write(change: (Transaction, DocumentReference) -> Unit): Boolean? {
        val store = store ?: return null
        val document = document ?: return null
        return try {
            withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                store.runTransaction { transaction ->
                    change(transaction, document)
                    null
                }.await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.w(TAG, "Could not write the profile: ${e.code}")
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) false else null
        } catch (e: Exception) {
            Log.w(TAG, "Could not write the profile: ${e.javaClass.simpleName}")
            null
        }
    }

    internal companion object {
        private const val TAG = "Profiles"
        private const val USERS = "users"
        const val FIRST_NAME = "firstName"
        const val BIRTH_DATE = "birthDate"
        const val CONSENT = "consent"
        const val CONSENT_VERSION_FIELD = "version"
        const val CONSENT_AT = "at"

        /** The wording of the agreement the profile screen shows; raised when it says something new. */
        const val CONSENT_VERSION = 1L
        private const val WRITE_TIMEOUT_MS = 10_000L
        private const val RETRY_FIRST_MS = 2_000L
        private const val RETRY_DOUBLINGS = 5

        /** Whether what a document holds carries the user's agreement to the wording shown today. */
        fun agreedTo(data: Map<String, Any?>?): Boolean {
            val version = ((data?.get(CONSENT) as? Map<*, *>)?.get(CONSENT_VERSION_FIELD) as? Number)?.toLong()
            return version != null && version >= CONSENT_VERSION
        }

        /**
         * What a document says of the profile. One that is not there counts as [ProfileState.Missing]
         * only on the server's word: the device's own copy may simply not have it yet. One that
         * is there without the user's agreement to today's wording, or that cannot be read, is
         * no profile either: the user is asked again.
         *
         * @param unconfirmed whether this is the device's own word and not the server's.
         */
        fun profileOf(exists: Boolean, unconfirmed: Boolean, data: Map<String, Any?>?): ProfileState {
            if (!exists) return if (unconfirmed) ProfileState.Unknown else ProfileState.Missing
            val name = (data?.get(FIRST_NAME) as? String)?.let(ProfileRules::nameOrNull)
            val date = (data?.get(BIRTH_DATE) as? String)?.let {
                try {
                    LocalDate.parse(it)
                } catch (_: DateTimeParseException) {
                    null
                }
            }
            return if (name != null && date != null && agreedTo(data)) ProfileState.Present(Profile(name, date)) else ProfileState.Missing
        }
    }
}
