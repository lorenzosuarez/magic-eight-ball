package com.lorenzosuarez.magiceightball.phone.account

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The account, kept by Firebase Authentication and signed in to with the user's Google account
 * through the system's own account picker.
 *
 * It is [AccountState.Unavailable] wherever it cannot work: a build without Firebase, or one
 * whose Firebase project has no Google sign-in set up, which shows as a missing client id. The
 * app then offers no account and is otherwise the same.
 */
@Singleton
class FirebaseAccount @Inject constructor(
    @ApplicationContext private val context: Context
) : Account {

    private val auth: FirebaseAuth? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance() else null

    /** Written into the app's resources by the Google services plugin once the project has sign-in. */
    @SuppressLint("DiscouragedApi")
    private val serverClientId: String? = context.resources
        .getIdentifier(CLIENT_ID_RESOURCE, "string", context.packageName)
        .takeIf { it != 0 }
        ?.let(context::getString)
        ?.takeIf { it.isNotBlank() }

    private val current = MutableStateFlow(read())
    override val state: StateFlow<AccountState> = current.asStateFlow()

    init {
        auth?.addAuthStateListener { current.value = read() }
    }

    private fun read(): AccountState {
        val auth = auth
        if (auth == null || serverClientId == null) return AccountState.Unavailable
        val user = auth.currentUser ?: return AccountState.SignedOut
        return AccountState.SignedIn(
            id = user.uid,
            name = user.displayName.orEmpty(),
            email = user.email.orEmpty()
        )
    }

    override suspend fun signIn(host: Context): AccountOutcome {
        val auth = auth ?: return AccountOutcome.FAILED
        return attempt {
            auth.signInWithCredential(GoogleAuthProvider.getCredential(googleIdToken(host), null)).await()
        }
    }

    override suspend fun signOut() {
        auth?.signOut()
        forgetPickedAccount()
    }

    override suspend fun delete(host: Context): AccountOutcome {
        val user = auth?.currentUser ?: return AccountOutcome.DONE
        val outcome = attempt {
            try {
                user.delete().await()
            } catch (_: FirebaseAuthRecentLoginRequiredException) {
                // Deleting is only allowed soon after signing in: the user picks the account
                // once more, and then it goes.
                user.reauthenticate(GoogleAuthProvider.getCredential(googleIdToken(host), null)).await()
                user.delete().await()
            }
        }
        if (outcome == AccountOutcome.DONE) forgetPickedAccount()
        return outcome
    }

    /** Runs [work] and says how it went, never failing itself: the user is told, and can try again. */
    private suspend fun attempt(work: suspend () -> Unit): AccountOutcome = try {
        work()
        AccountOutcome.DONE
    } catch (e: CancellationException) {
        throw e
    } catch (_: GetCredentialCancellationException) {
        AccountOutcome.CANCELLED
    } catch (_: NoCredentialException) {
        AccountOutcome.NO_GOOGLE_ACCOUNT
    } catch (_: Exception) {
        AccountOutcome.FAILED
    }

    /** Has the user pick a Google account in the system's sheet, and returns the proof of it. */
    private suspend fun googleIdToken(host: Context): String {
        val clientId = serverClientId ?: throw IllegalStateException("No Google sign-in in this build")
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        val credential = CredentialManager.create(host).getCredential(host, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Not a Google account")
    }

    /** So that the next sign-in asks which account again instead of taking the last one. */
    private suspend fun forgetPickedAccount() {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Nothing depends on it: the account is signed out of the app either way.
        }
    }

    private companion object {
        const val CLIENT_ID_RESOURCE = "default_web_client_id"
    }
}
