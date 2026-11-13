package com.lorenzosuarez.magiceightball.phone.account

import android.util.Log
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
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

/**
 * The profile, kept by Cloud Firestore in a document of the account's own, `users/{id}`, which
 * the project's rules let nobody else read or write.
 *
 * Nothing of it is written to this device, and every change is made on the server or not at
 * all: see [AccountStore]. Where there is no Firebase project, or nobody is signed in, there is
 * no profile to know of.
 */
@Singleton
class FirestoreProfiles @Inject constructor(
    private val store: AccountStore,
    account: Account
) : Profiles {

    /** The document of whoever is signed in; null when nobody is. */
    @Volatile
    private var document: DocumentReference? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ProfileState> = account.state
        .map { (it as? AccountState.SignedIn)?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            document = id?.let { store.firestore?.collection(AccountStore.USERS)?.document(it) }
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

    override suspend fun save(profile: Profile): SaveOutcome {
        val document = document ?: return SaveOutcome.FAILED
        return store.write(refused = SaveOutcome.REFUSED) { transaction, _ ->
            val name = mapOf<String, Any>(FIRST_NAME to profile.firstName, BIRTH_DATE to profile.birthDate.toString())
            if (agreedTo(transaction.get(document).data)) {
                // A correction: the record of the user's agreement stays as it was made.
                transaction.update(document, name)
            } else {
                // Which wording was agreed to, and when by the server's clock.
                val consent = mapOf(CONSENT_VERSION_FIELD to CONSENT_VERSION, CONSENT_AT to FieldValue.serverTimestamp())
                transaction.set(document, name + (CONSENT to consent))
            }
            SaveOutcome.SAVED
        } ?: SaveOutcome.FAILED
    }

    override suspend fun delete(): Boolean {
        val document = document ?: return false
        return store.write(refused = false) { transaction, _ ->
            // In one change: characters left under a profile that is gone could be reached by
            // nobody, and the project's rules refuse a profile deleted without them.
            val characters = document.collection(AccountStore.CHARACTERS)
            CustomCharacter.SLOTS.forEach { transaction.delete(characters.document(it)) }
            transaction.delete(document)
            true
        } == true
    }

    internal companion object {
        private const val TAG = "Profiles"
        const val FIRST_NAME = "firstName"
        const val BIRTH_DATE = "birthDate"
        const val CONSENT = "consent"
        const val CONSENT_VERSION_FIELD = "version"
        const val CONSENT_AT = "at"

        /** The wording of the agreement the profile screen shows; raised when it says something new. */
        const val CONSENT_VERSION = 1L
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
