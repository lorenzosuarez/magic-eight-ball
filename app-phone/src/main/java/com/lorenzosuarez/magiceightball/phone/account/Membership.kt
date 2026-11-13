package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import com.example.magiceightball.core.domain.model.AskerFacts
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Who is using the app, as far as the account, its profile and this device say.
 *
 * @property underAge whether this device was given the date of birth of someone under
 * [ProfileRules.ADULT_AGE]: see [DeviceMarks.underAge].
 * @property ageNotice true from the moment that date was given until the user has been told
 * that there is no account for them.
 */
data class Who(
    val account: AccountState = AccountState.Unavailable,
    val profile: ProfileState = ProfileState.Unknown,
    val underAge: Boolean = false,
    val ageNotice: Boolean = false
)

/**
 * The account, its profile and what the device remembers, kept in step with one another: an
 * account has a profile or is not kept, a profile is deleted before its account is, and nobody
 * under [ProfileRules.ADULT_AGE] has either.
 */
class Membership @Inject constructor(
    private val account: Account,
    private val profiles: Profiles,
    private val marks: DeviceMarks
) {
    /** With nothing to sign in to: for tests, and for whatever has no account at all. */
    constructor(account: Account = Account.None) : this(account, Profiles.None, DeviceMarks.None)

    private val ageNotice = MutableStateFlow(false)

    val who: Flow<Who> = combine(account.state, profiles.state, marks.underAge, ageNotice, ::Who)

    /** See [Account.signIn]. Whoever signs in for the first time has no profile yet, and is asked for one. */
    suspend fun signIn(host: Context): AccountOutcome = account.signIn(host)

    /** See [Account.signOut]. */
    suspend fun signOut() = account.signOut()

    /**
     * Deletes the account for good, and first what is kept with it: an account gone with its
     * profile left behind would leave that profile out of anyone's reach. See [Account.delete].
     */
    suspend fun deleteAccount(host: Context): AccountOutcome =
        if (clearProfile()) account.delete(host) else AccountOutcome.FAILED

    /**
     * Saves who the user says they are, which is also their agreement to what the screen asking
     * for it says is sent to the AI service.
     *
     * A date of birth of someone under [ProfileRules.ADULT_AGE] ends it there: the device is
     * marked, nothing typed is kept, and the account goes.
     *
     * @param host as for [signIn]: deleting an account may ask which one it is.
     */
    suspend fun saveProfile(firstName: String, birthDate: LocalDate, today: LocalDate, host: Context): ProfileOutcome {
        if (!ProfileRules.isAdult(birthDate, today)) {
            // Marked first: whatever fails after this, the device offers no account again.
            marks.markUnderAge()
            ageNotice.value = true
            removeAccount(host)
            return ProfileOutcome.UNDER_AGE
        }
        val name = ProfileRules.nameOrNull(firstName) ?: return ProfileOutcome.FAILED
        return if (profiles.save(Profile(name, birthDate))) ProfileOutcome.SAVED else ProfileOutcome.FAILED
    }

    /** "Not now" on the screen that asks for a profile: no profile, no account. */
    suspend fun declineProfile(host: Context) = removeAccount(host)

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
        if (account.state.value !is AccountState.SignedIn || marks.underAge.first()) return null
        val profile = (profiles.state.value as? ProfileState.Present)?.profile ?: return null
        return AskerFacts(
            firstName = profile.firstName,
            zodiac = profile.zodiac,
            birthdayToday = profile.isBirthday(today) && marks.birthdayWishedOn.first() != today.toEpochDay()
        )
    }

    /** An answer has been told that [today] is the asker's birthday: the day's other answers are not. */
    suspend fun birthdayWished(today: LocalDate) = marks.markBirthdayWished(today.toEpochDay())

    /** True once nothing is kept with the account: there was no profile, or it has been deleted. */
    private suspend fun clearProfile(): Boolean =
        profiles.state.value !is ProfileState.Present || profiles.delete()

    /** Takes the account away, or at the least signs this device out of it. */
    private suspend fun removeAccount(host: Context) {
        if (!clearProfile() || account.delete(host) != AccountOutcome.DONE) account.signOut()
    }
}
