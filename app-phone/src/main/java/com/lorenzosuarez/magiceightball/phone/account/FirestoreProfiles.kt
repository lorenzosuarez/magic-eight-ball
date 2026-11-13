package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The profile, kept by Cloud Firestore in a document of the account's own, `users/{id}`, which
 * the project's rules let nobody else read or write. Firestore keeps a copy on the device, so
 * the profile is there without a connection.
 *
 * Where there is no Firebase project, or nobody is signed in, there is no profile to know of.
 */
@Singleton
class FirestoreProfiles @Inject constructor(
    @ApplicationContext context: Context,
    account: Account
) : Profiles {

    private val store: FirebaseFirestore? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseFirestore.getInstance() else null

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

    private fun profileIn(document: DocumentReference): Flow<ProfileState> = callbackFlow {
        // Told of every change, the news from the server included: an account new to this
        // device has nothing here yet, and that says nothing about what the server holds.
        val listening = document.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, failure ->
            if (failure != null || snapshot == null) {
                Log.w(TAG, "Could not read the profile", failure)
                trySend(ProfileState.Unknown)
            } else {
                trySend(profileOf(snapshot.exists(), snapshot.metadata.isFromCache, snapshot.data))
            }
        }
        awaitClose { listening.remove() }
    }

    override suspend fun save(profile: Profile): Boolean = write {
        it.set(
            mapOf(
                FIRST_NAME to profile.firstName,
                BIRTH_DATE to profile.birthDate.toString(),
                // The record of what the user agreed to: which wording, and when, by the server's clock.
                CONSENT to mapOf(CONSENT_VERSION_FIELD to CONSENT_VERSION, CONSENT_AT to FieldValue.serverTimestamp())
            )
        )
    }

    override suspend fun delete(): Boolean = write { it.delete() }

    /**
     * Without a connection Firestore would keep the change for later and never say how it went:
     * a change that has not reached the server in [WRITE_TIMEOUT_MS] counts as not made.
     */
    private suspend fun write(change: (DocumentReference) -> com.google.android.gms.tasks.Task<Void>): Boolean {
        val document = document ?: return false
        return try {
            withTimeoutOrNull(WRITE_TIMEOUT_MS) { change(document).await() } != null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Could not write the profile", e)
            false
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

        /**
         * What a document says of the profile. One that is not there counts as [ProfileState.Missing]
         * only on the server's word: the device's own copy may simply not have it yet. One that
         * is there without the user's agreement, or that cannot be read, is no profile either.
         */
        fun profileOf(exists: Boolean, fromCache: Boolean, data: Map<String, Any?>?): ProfileState {
            if (!exists) return if (fromCache) ProfileState.Unknown else ProfileState.Missing
            val name = (data?.get(FIRST_NAME) as? String)?.let(ProfileRules::nameOrNull)
            val date = (data?.get(BIRTH_DATE) as? String)?.let {
                try {
                    LocalDate.parse(it)
                } catch (_: DateTimeParseException) {
                    null
                }
            }
            val agreed = data?.get(CONSENT) is Map<*, *>
            return if (name != null && date != null && agreed) ProfileState.Present(Profile(name, date)) else ProfileState.Missing
        }
    }
}
