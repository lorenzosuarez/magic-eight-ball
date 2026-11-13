package com.lorenzosuarez.magiceightball.phone.account

import android.util.Log
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
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

/** How saving a character ended. */
sealed interface CharacterSave {
    /** @property character as it is kept, in the slot it was given. */
    data class Saved(val character: CustomCharacter) : CharacterSave

    /** Every slot is taken: one has to go before another comes. */
    data object Full : CharacterSave

    /** The server would not have it. */
    data object Refused : CharacterSave

    /** It could not be saved, for want of a connection or of anything else: worth trying again. */
    data object Failed : CharacterSave
}

/** Where the characters the user made up are kept: with their account, and nowhere else. */
interface CustomCharacters {
    /**
     * Those of whoever is signed in, in the order of their slots. Null while they are not
     * known: nobody is signed in, or the server has not been heard from yet.
     */
    val state: StateFlow<List<CustomCharacter>?>

    /**
     * Saves [character]: over the one in its slot when [CustomCharacter.id] names one, and
     * otherwise in the first slot that is free. Saved means the server has it.
     */
    suspend fun save(character: CustomCharacter): CharacterSave

    /** Deletes the character in slot [id]. True once the server holds none there. */
    suspend fun delete(id: String): Boolean

    /** Deletes every character of the account signed in: done before the account is deleted. */
    suspend fun deleteAll(): Boolean

    /** For builds with nothing to sign in to, and for tests. */
    object None : CustomCharacters {
        override val state: StateFlow<List<CustomCharacter>?> = MutableStateFlow(null)
        override suspend fun save(character: CustomCharacter) = CharacterSave.Failed
        override suspend fun delete(id: String) = true
        override suspend fun deleteAll() = true
    }
}

/**
 * The user's characters, kept by Cloud Firestore under their account: `users/{id}/characters`,
 * one document to a slot, named "1" to "5". The project's rules allow no other name, which is
 * what keeps an account to that many, and let nobody but the account read or write them.
 */
@Singleton
class FirestoreCustomCharacters @Inject constructor(
    private val store: AccountStore,
    account: Account
) : CustomCharacters {

    /** Where the characters of whoever is signed in are; null when nobody is. */
    @Volatile
    private var collection: CollectionReference? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<List<CustomCharacter>?> = account.state
        .map { (it as? AccountState.SignedIn)?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            collection = id?.let {
                store.firestore?.collection(AccountStore.USERS)?.document(it)?.collection(AccountStore.CHARACTERS)
            }
            collection?.let(::charactersIn) ?: flowOf(null)
        }
        .stateIn(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate), SharingStarted.Eagerly, null)

    private fun charactersIn(collection: CollectionReference): Flow<List<CustomCharacter>?> = callbackFlow {
        // What this device holds of them says nothing until the server has been heard from once.
        var heardFromServer = false
        val listening = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, failure ->
            if (failure != null || snapshot == null) {
                close(failure ?: IllegalStateException("No characters and no reason"))
                return@addSnapshotListener
            }
            if (!snapshot.metadata.isFromCache) heardFromServer = true
            trySend(
                if (heardFromServer) {
                    snapshot.documents.mapNotNull { characterOf(it.id, it.data) }.sortedBy { it.id }
                } else null
            )
        }
        awaitClose { listening.remove() }
    }.retryWhen { failure, attempt ->
        Log.w(TAG, "Could not read the characters: ${(failure as? FirebaseFirestoreException)?.code ?: failure.javaClass.simpleName}")
        emit(null)
        delay(RETRY_FIRST_MS shl attempt.toInt().coerceAtMost(RETRY_DOUBLINGS))
        true
    }.onStart { emit(null) }

    override suspend fun save(character: CustomCharacter): CharacterSave {
        val collection = collection ?: return CharacterSave.Failed
        return store.write<CharacterSave>(refused = CharacterSave.Refused) { transaction, _ ->
            // Its own slot, or the first that is free: read before anything is written.
            val slot = character.id.takeIf { it in CustomCharacter.SLOTS }
                ?: CustomCharacter.SLOTS.firstOrNull { !transaction.get(collection.document(it)).exists() }
                ?: return@write CharacterSave.Full
            val kept = character.copy(id = slot)
            transaction.set(collection.document(slot), fieldsOf(kept))
            CharacterSave.Saved(kept)
        } ?: CharacterSave.Failed
    }

    override suspend fun delete(id: String): Boolean {
        val collection = collection ?: return false
        if (id !in CustomCharacter.SLOTS) return true
        return store.write(refused = false) { transaction, _ ->
            transaction.delete(collection.document(id))
            true
        } == true
    }

    override suspend fun deleteAll(): Boolean {
        val collection = collection ?: return false
        return store.write(refused = false) { transaction, _ ->
            CustomCharacter.SLOTS.forEach { transaction.delete(collection.document(it)) }
            true
        } == true
    }

    internal companion object {
        private const val TAG = "CustomCharacters"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val COLOUR = "colour"
        const val EMBLEM = "emblem"
        private const val RETRY_FIRST_MS = 2_000L
        private const val RETRY_DOUBLINGS = 5

        fun fieldsOf(character: CustomCharacter): Map<String, Any> = mapOf(
            NAME to character.name,
            DESCRIPTION to character.description,
            COLOUR to character.colour.name.lowercase(),
            EMBLEM to character.emblem.name.lowercase()
        )

        /**
         * What a document says of a character; null when it is not in a slot, or its name or
         * description does not read as one: it is then as if it were not there.
         */
        fun characterOf(id: String, data: Map<String, Any?>?): CustomCharacter? {
            if (id !in CustomCharacter.SLOTS) return null
            val name = (data?.get(NAME) as? String)?.let(CustomCharacter::nameOrNull) ?: return null
            val description = (data[DESCRIPTION] as? String)?.let(CustomCharacter::descriptionOrNull) ?: return null
            return CustomCharacter(id, name, description, lookOf(data[COLOUR]), lookOf(data[EMBLEM]))
        }

        private fun lookOf(name: Any?): Magic8BallPersonality =
            CustomCharacter.LOOKS.find { it.name.lowercase() == name } ?: Magic8BallPersonality.NEUTRAL
    }
}
