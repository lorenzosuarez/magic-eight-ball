package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
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
 * @property characters the characters they made up; null while those are not known.
 * @property premium whether they have what is sold: see [Entitlements].
 * @property premiumKnown whether that has been heard from the server, and is not only assumed.
 * @property offer what they can buy and for how much; null while the store has not said, or
 * they cannot: see [Shop].
 */
data class Who(
    val account: AccountState = AccountState.Unavailable,
    val profile: ProfileState = ProfileState.Unknown,
    val underAge: Boolean = false,
    val ageNotice: Boolean = false,
    val removing: Boolean = false,
    val characters: List<CustomCharacter>? = null,
    val premium: Boolean = false,
    val offer: Offer? = null,
    val premiumKnown: Boolean = true
)

/**
 * The account, what is kept with it and what the device remembers, kept in step with one
 * another: an account has a profile or is not kept, its characters and its profile are deleted
 * before it is, and nobody under [ProfileRules.ADULT_AGE] has any of them.
 *
 * Taking an account away is never left half done because the screen that asked for it went
 * away: those steps run to their end whatever happens to whoever called.
 */
class Membership @Inject constructor(
    private val account: Account,
    private val profiles: Profiles,
    private val marks: DeviceMarks,
    private val characters: CustomCharacters,
    entitlements: Entitlements,
    private val shop: Shop
) {
    /** With nothing to sign in to, and nothing sold: for tests, and for whatever has no account at all. */
    constructor(
        account: Account = Account.None,
        profiles: Profiles = Profiles.None,
        marks: DeviceMarks = DeviceMarks.None,
        characters: CustomCharacters = CustomCharacters.None,
        entitlements: Entitlements = Entitlements.Nobody
    ) : this(account, profiles, marks, characters, entitlements, Shop.None)

    private val ageNotice = MutableStateFlow(false)
    private val removing = MutableStateFlow(false)

    /** Up from the moment an under-age date is given, whether or not the device could write it down. */
    private val markedNow = MutableStateFlow(false)
    private val underAge: Flow<Boolean> = combine(marks.underAge, markedNow) { written, now -> written || now }

    val who: Flow<Who> = combine(
        combine(account.state, profiles.state, characters.state, ::Triple),
        underAge,
        ageNotice,
        removing,
        combine(entitlements.premium, entitlements.known, shop.offer, ::Triple)
    ) { (account, profile, characters), underAge, ageNotice, removing, (premium, known, offer) ->
        Who(account, profile, underAge, ageNotice, removing, characters, premium, offer, known)
    }

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
     * Saves a character of the user's own, as typed: over [replacing], the one that was opened
     * to be corrected, or as a new one when that is null. Only for someone signed in with a
     * profile, which is where they agreed to what is sent to the AI service.
     */
    suspend fun saveCharacter(
        replacing: CustomCharacter?,
        name: String,
        description: String,
        colour: Magic8BallPersonality,
        emblem: Magic8BallPersonality
    ): CharacterSave {
        if (account.state.value !is AccountState.SignedIn || profiles.state.value !is ProfileState.Present || isUnderAge()) {
            return CharacterSave.Failed
        }
        val character = CustomCharacter(
            id = replacing?.id.orEmpty(),
            name = CustomCharacter.nameOrNull(name) ?: return CharacterSave.Refused,
            description = CustomCharacter.descriptionOrNull(description) ?: return CharacterSave.Refused,
            colour = colour.takeIf { it in CustomCharacter.LOOKS } ?: Magic8BallPersonality.NEUTRAL,
            emblem = emblem.takeIf { it in CustomCharacter.LOOKS } ?: Magic8BallPersonality.NEUTRAL
        )
        return characters.save(character, replacing)
    }

    /** Deletes the character in slot [id], for good. */
    suspend fun deleteCharacter(id: String): Boolean = characters.delete(id)

    /**
     * Buys what is sold: see [Shop.buy]. Only for whoever could use it: signed in, with a
     * profile, and not on a device that was given an under-age date of birth.
     */
    suspend fun buyPremium(host: Context): Bought {
        if (account.state.value !is AccountState.SignedIn || profiles.state.value !is ProfileState.Present || isUnderAge()) {
            return Bought.FAILED
        }
        return shop.buy(host)
    }

    /**
     * What is kept with the account, its profile and its characters at once, then the account:
     * in that order, to the end, and with nothing asked for in between. It is deleted whether
     * or not this device knows of any, since what it does not know of may still be there.
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
