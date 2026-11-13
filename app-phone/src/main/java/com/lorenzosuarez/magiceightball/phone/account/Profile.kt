package com.lorenzosuarez.magiceightball.phone.account

import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Zodiac
import java.time.DateTimeException
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * What the user tells the app about themselves so that answers can be made for them. There is
 * no account without one, and none is kept without the user having agreed, on the screen that
 * asks for it, to what is sent of it to the AI service.
 *
 * @property firstName as the user typed it, trimmed and at most [AskerFacts.MAX_NAME_LENGTH] long.
 * @property birthDate kept with the account and never sent to the AI service: only the sign it
 * falls under and, on the day, that it is the user's birthday.
 */
data class Profile(val firstName: String, val birthDate: LocalDate) {

    val zodiac: Zodiac get() = Zodiac.of(birthDate.monthValue, birthDate.dayOfMonth)

    /** Whether [today] is the user's birthday; someone born on a 29 February has it on the 28th in other years. */
    fun isBirthday(today: LocalDate): Boolean =
        birthDate.withYear(today.year).let { it.monthValue == today.monthValue && it.dayOfMonth == today.dayOfMonth }
}

/** The profile of whoever is signed in, as far as the app knows it. */
sealed interface ProfileState {
    /** Nobody is signed in, or the account's profile has not been heard of yet. */
    data object Unknown : ProfileState

    /** The account has no profile: it was just made, and is not kept unless it gets one. */
    data object Missing : ProfileState

    data class Present(val profile: Profile) : ProfileState
}

/** How saving a profile ended. */
enum class ProfileOutcome {
    SAVED,

    /** The date of birth is that of someone under [ProfileRules.ADULT_AGE]: no account for them. */
    UNDER_AGE,

    /** It could not be saved, for want of a connection or of anything else: worth trying again. */
    FAILED
}

/** Where the profile of whoever is signed in is kept. */
interface Profiles {
    val state: StateFlow<ProfileState>

    /** Saves [profile] for the account signed in, with the record of the user's agreement. */
    suspend fun save(profile: Profile): Boolean

    /** Deletes the profile of the account signed in: done before the account itself is deleted. */
    suspend fun delete(): Boolean

    /** For builds with nothing to sign in to, and for tests. */
    object None : Profiles {
        override val state: StateFlow<ProfileState> = MutableStateFlow(ProfileState.Unknown)
        override suspend fun save(profile: Profile) = false
        override suspend fun delete() = true
    }
}

/** What a profile has to be, checked on the device before anything is saved. */
object ProfileRules {
    /** The age the AI service's terms set for whoever uses an app built on it. */
    const val ADULT_AGE = 18

    /** Nobody is older than this: a year further back is a slip of the finger. */
    private const val LONGEST_LIFE = 120

    /** [typed] as it is kept, or null when there is no name in it. */
    fun nameOrNull(typed: String): String? =
        typed.trim().replace(Regex("\\s+"), " ").take(AskerFacts.MAX_NAME_LENGTH).trim()
            .takeIf { AskerFacts(firstName = it).nameForAi != null }

    /** The date typed as three numbers, or null when there is no such day or nobody was born on it. */
    fun dateOrNull(day: String, month: String, year: String, today: LocalDate): LocalDate? {
        val date = try {
            LocalDate.of(year.trim().toInt(), month.trim().toInt(), day.trim().toInt())
        } catch (_: NumberFormatException) {
            return null
        } catch (_: DateTimeException) {
            return null
        }
        return date.takeIf { !it.isAfter(today) && it.year >= today.year - LONGEST_LIFE }
    }

    fun isAdult(birthDate: LocalDate, today: LocalDate): Boolean = !birthDate.plusYears(ADULT_AGE.toLong()).isAfter(today)
}
