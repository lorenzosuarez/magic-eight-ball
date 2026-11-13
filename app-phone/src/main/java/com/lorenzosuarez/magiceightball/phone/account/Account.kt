package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Who is using the app, as far as the app knows. Nobody has to be: everything but answers made
 * personal and characters of one's own works with no account at all.
 */
sealed interface AccountState {
    /** Signing in is not offered: this build has nothing to sign in to. */
    data object Unavailable : AccountState

    data object SignedOut : AccountState

    /**
     * @property id the account's identifier, which never changes and says nothing about the person.
     * @property name as the account gives it; may be empty.
     * @property email shown so that the user can tell which account this is, and for nothing else.
     */
    data class SignedIn(val id: String, val name: String, val email: String) : AccountState
}

/** How an attempt to sign in, or to delete the account, ended. */
enum class AccountOutcome {
    DONE,

    /** The user backed out: nothing to explain. */
    CANCELLED,

    /** The device has no Google account to sign in with: the user has to add one first. */
    NO_GOOGLE_ACCOUNT,

    /** It could not be done, for want of a connection or of anything else: worth saying, and trying again. */
    FAILED
}

/**
 * The user's account. Signing in is the user's own Google account and one touch; there is no
 * password to make or to forget, and signing up is the same touch.
 */
interface Account {
    val state: StateFlow<AccountState>

    /**
     * Asks the user which Google account to use and signs in with it.
     *
     * @param host the activity the system's account picker opens over; held only for the call.
     */
    suspend fun signIn(host: Context): AccountOutcome

    /** Forgets the account on this device. What the account holds stays where it is. */
    suspend fun signOut()

    /**
     * Makes sure it is the account's owner who is here: someone who signed in a while ago is
     * asked to pick the account once more. Done before anything that cannot be taken back, so
     * that backing out of it leaves everything as it was.
     *
     * @param host as for [signIn].
     */
    suspend fun confirmIdentity(host: Context): AccountOutcome

    /**
     * Deletes the account, for good. What is kept with it is deleted first, by whoever calls.
     *
     * @param host as for [signIn], for the case the service still wants the account picked
     * again; null to delete without asking anything, which fails where it would have asked.
     */
    suspend fun delete(host: Context?): AccountOutcome

    /** For builds with nothing to sign in to, and for tests. */
    object None : Account {
        override val state: StateFlow<AccountState> = MutableStateFlow(AccountState.Unavailable)
        override suspend fun signIn(host: Context) = AccountOutcome.FAILED
        override suspend fun signOut() = Unit
        override suspend fun confirmIdentity(host: Context) = AccountOutcome.FAILED
        override suspend fun delete(host: Context?) = AccountOutcome.FAILED
    }
}
