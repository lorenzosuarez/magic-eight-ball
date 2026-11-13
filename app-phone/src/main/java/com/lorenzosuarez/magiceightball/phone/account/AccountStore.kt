package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Cloud Firestore as everything kept with an account uses it: nothing of it is written to this
 * device, and every change is made on the server or not at all.
 *
 * The copy Firestore keeps is in memory only, and changes are transactions, which are never
 * left on the device to be sent later: a screen that said something could not be saved or
 * deleted is never made a liar of once the connection comes back.
 */
@Singleton
class AccountStore @Inject constructor(@ApplicationContext context: Context) {

    /** Null where there is no Firebase project: nothing is kept, and nothing is known. */
    val firestore: FirebaseFirestore? =
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseFirestore.getInstance().apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build()
            }
        } else null

    /**
     * Makes [change] on the server. Its answer when made, [refused] when the project's rules
     * would not have it, and null when it could not be made within [WRITE_TIMEOUT_MS].
     */
    suspend fun <T : Any> write(refused: T, change: (Transaction, FirebaseFirestore) -> T): T? {
        val store = firestore ?: return null
        return try {
            withTimeoutOrNull(WRITE_TIMEOUT_MS) { store.runTransaction { change(it, store) }.await() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            // Never what was being written: only how the server took it.
            Log.w(TAG, "Could not write: ${e.code}")
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) refused else null
        } catch (e: Exception) {
            Log.w(TAG, "Could not write: ${e.javaClass.simpleName}")
            null
        }
    }

    internal companion object {
        private const val TAG = "AccountStore"
        private const val WRITE_TIMEOUT_MS = 10_000L

        /** Where everything of an account is kept: its profile, and under it its characters. */
        const val USERS = "users"
        const val CHARACTERS = "characters"
    }
}
