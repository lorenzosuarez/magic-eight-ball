package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import com.example.magiceightball.core.domain.model.AskerFacts
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Who is using the app, as far as the account, its profile and this device say.
 *
 * @property underAge whether this device was given the date of birth of someone under
 * [ProfileRules.ADULT_AGE]: see [DeviceMarks.underAge].
 * @property ageNotice true from the moment that date was given until the user has been told
 * that there is no account for them.
 * @property removing true while an account is being taken away: between its profile going and
 * the account itself, it is not an account that still needs a profile.
 */
data class Who(
    val account: AccountState = AccountState.Unavailable,
    val profile: ProfileState = ProfileState.Unknown,
    val underAge: Boolean = false,
    val ageNotice: Boolean = false,
    val removing: Boolean = false
)

/**
 * The account, its profile and what the device remembers, kept in step with one another: an
 * account has a profile or is not kept, a profile is deleted before its account is, and nobody
 * under [ProfileRules.ADULT_AGE] has either.
 *
 * Taking an account away is never left half done because the screen that asked for it went
 * away: those steps run to their end whatever happens to whoever called.
 */
class Membership @Inject constructor(
    private val account: Account,
    private val profiles: Profiles,
    private val marks: DeviceMarks
) {
    /** With nothing to sign in to: for tests, and for whatever has no account at all. */
    constructor(account: Account = Account.None) : this(account, Profiles.None, DeviceMarks.None)

    private val ageNotice = MutableStateFlow(false)
    private val removing = MutableStateFlow(false)

    /** Up from the moment an under-age date is given, whether or not the device could write it down. */
    private val markedNow = MutableStateFlow(false)
    private val underAge: Flow<Boolean> = combine(marks.underAge, markedNow) { written, now -> written || now }

    val who: Flow<Who> = combine(account.state, profiles.state, underAge, ageNotice, removing, ::Who)

    /** Whether this device may ask the AI service for nothing: read where it is kept, never guessed. */
    suspend fun isUnderAge(): Boolean = underAge.first()

    /** See [Account.signIn]. Whoever signs in for the first time has no profile yet, and is asked for one. */
    suspend fun signIn(host: Context): AccountOutcome = account.signIn(host)

    /** See [Account.signOut]. */
    suspend fun signOut() = account.signOut()

    /**
     * Deletes the account for good, and first what is kept with it: an account gone with its
     * profile left behind would leave that profile out of anyone's reach.
     *
     * The user is asked which account it is, if they have to be, before anything is touched:
     * backing out of that leaves the profile where it was.
     */
    suspend fun deleteAccount(host: Context): AccountOutcome {
        val confirmed = account.confirmIdentity(host)
        return if (confirmed == AccountOutcome.DONE) remove(host) else confirmed
    }

    /**
     * Saves who the user says they are, which is also their agreement to what the screen asking
     * for it says is sent to the AI service.
     *
     * A date of birth of someone under [ProfileRules.ADULT_AGE] ends it there: the device is
     * marked, nothing typed is kept, and the account goes, now or, if it cannot be reached,
     * the next time the app starts: see [enforceAge].
     *
     * @param host as for [signIn]: deleting an account may ask which one it is.
     */
    suspend fun saveProfile(firstName: String, birthDate: LocalDate, today: LocalDate, host: Context): ProfileOutcome {
        if (!ProfileRules.isAdult(birthDate, today)) {
            // To the end, even though marking the device takes the screen that asked for this away.
            withContext(NonCancellable) {
                // Marked first: whatever fails after this, the device offers no account again.
                markedNow.value = true
                marks.markUnderAge()
                remove(host)
                ageNotice.value = true
            }
            return ProfileOutcome.UNDER_AGE
        }
        val name = ProfileRules.nameOrNull(firstName) ?: return ProfileOutcome.FAILED
        return when (profiles.save(Profile(name, birthDate))) {
            SaveOutcome.SAVED -> ProfileOutcome.SAVED
            SaveOutcome.REFUSED -> ProfileOutcome.REFUSED
            SaveOutcome.FAILED -> ProfileOutcome.FAILED
        }
    }

    /**
     * Going on without an account, on the screen that asks for a profile: no profile, no
     * account. When it cannot be deleted the user stays signed in and is told, to try again:
     * signing out instead would leave an account nobody could reach to delete.
     */
    suspend fun declineProfile(host: Context): AccountOutcome = remove(host)

    /**
     * Finishes what an under-age date started and could not finish then: an account that could
     * not be reached to be deleted. Done at start, without asking anything of anyone.
     */
    suspend fun enforceAge() {
        if (isUnderAge() && account.state.value is AccountState.SignedIn) remove(host = null)
    }

    /** The user has read that there is no account for them. */
    fun ageNoticeSeen() {
        ageNotice.value = false
    }

    /**
     * What a character may be told about whoever is asking on [today]: nothing unless they are
     * signed in and have a profile, which is their agreement to it. A birthday is among it only
     * until it has been wished: see [birthdayWished].
     */
    suspend fun asker(today: LocalDate): AskerFacts? {
        if (account.state.value !is AccountState.SignedIn || isUnderAge()) return null
        val profile = (profiles.state.value as? ProfileState.Present)?.profile ?: return null
        return AskerFacts(
            firstName = profile.firstName,
            zodiac = profile.zodiac,
            birthdayToday = profile.isBirthday(today) && marks.birthdayWishedIn.first() != today.year
        )
    }

    /** An answer has been told that [today] is the asker's birthday: the day's other answers are not. */
    suspend fun birthdayWished(today: LocalDate) = marks.markBirthdayWished(today.year)

    /**
     * The profile, then the account: in that order, to the end, and with nothing asked for in
     * between. The profile is deleted whether or not this device knows of one, since what it
     * does not know of may still be there.
     */
    private suspend fun remove(host: Context?): AccountOutcome = withContext(NonCancellable) {
        removing.value = true
        try {
            if (profiles.delete()) account.delete(host) else AccountOutcome.FAILED
        } finally {
            removing.value = false
        }
    }
}
