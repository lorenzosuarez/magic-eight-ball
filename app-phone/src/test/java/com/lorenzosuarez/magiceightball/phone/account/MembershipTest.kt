package com.lorenzosuarez.magiceightball.phone.account

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Zodiac
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** An account, its profile and the device's marks, kept in step: see [Membership]. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class MembershipTest {

    private val host: Context = ApplicationProvider.getApplicationContext()
    private val account = FakeAccount(initial = FakeAccount.ANA)
    private val profiles = FakeProfiles(initial = ProfileState.Missing)
    private val marks = FakeDeviceMarks()
    private val membership = Membership(account, profiles, marks)

    @Test
    fun `an adult's profile is saved as typed, tidied, and the account stays`() = runTest {
        val outcome = membership.saveProfile("  lorena   maría ", ADULT, TODAY, host)

        assertEquals(ProfileOutcome.SAVED, outcome)
        assertEquals(Profile("lorena maría", ADULT), profiles.saved.single())
        assertEquals(FakeAccount.ANA, account.state.value)
        assertFalse(marks.underAge.value)
    }

    @Test
    fun `eighteen today is an adult, and eighteen tomorrow is not`() = runTest {
        assertEquals(ProfileOutcome.SAVED, membership.saveProfile("Ana", TODAY.minusYears(18), TODAY, host))
        assertEquals(ProfileOutcome.UNDER_AGE, membership.saveProfile("Ana", TODAY.minusYears(18).plusDays(1), TODAY, host))
    }

    @Test
    fun `an under-age date of birth marks the device, keeps nothing typed and takes the account away`() = runTest {
        val outcome = membership.saveProfile("Lorena", MINOR, TODAY, host)

        assertEquals(ProfileOutcome.UNDER_AGE, outcome)
        assertTrue(marks.underAge.value)
        assertTrue(profiles.saved.isEmpty())
        assertEquals(1, account.deleted)
        val who = membership.who.first()
        assertEquals(AccountState.SignedOut, who.account)
        assertTrue(who.underAge)
        assertTrue(who.ageNotice)

        membership.ageNoticeSeen()
        assertFalse(membership.who.first().ageNotice)
        // The mark is not a notice: it stays.
        assertTrue(membership.who.first().underAge)
    }

    @Test
    fun `an under-age date typed over a profile already kept deletes that profile too`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))

        membership.saveProfile("Lorena", MINOR, TODAY, host)

        assertEquals(1, profiles.deleted)
        assertEquals(1, account.deleted)
    }

    @Test
    fun `when the account cannot be deleted for an under-age date the device is signed out and still marked`() = runTest {
        account.deletes = AccountOutcome.FAILED

        membership.saveProfile("Lorena", MINOR, TODAY, host)

        assertEquals(0, account.deleted)
        assertEquals(1, account.signedOut)
        assertTrue(marks.underAge.value)
    }

    @Test
    fun `a profile that cannot be saved says so and leaves the account as it was`() = runTest {
        profiles.works = false

        assertEquals(ProfileOutcome.FAILED, membership.saveProfile("Lorena", ADULT, TODAY, host))
        assertEquals(FakeAccount.ANA, account.state.value)
        assertEquals(0, account.deleted)
    }

    @Test
    fun `a name with no letters in it is not saved`() = runTest {
        assertEquals(ProfileOutcome.FAILED, membership.saveProfile(" 123 ", ADULT, TODAY, host))
        assertTrue(profiles.saved.isEmpty())
    }

    @Test
    fun `not now means no account`() = runTest {
        membership.declineProfile(host)

        assertEquals(1, account.deleted)
        assertEquals(AccountState.SignedOut, account.state.value)
        assertFalse(marks.underAge.value)
    }

    @Test
    fun `deleting the account deletes its profile first, and keeps the account if that fails`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        profiles.works = false
        assertEquals(AccountOutcome.FAILED, membership.deleteAccount(host))
        assertEquals(0, account.deleted)

        profiles.works = true
        assertEquals(AccountOutcome.DONE, membership.deleteAccount(host))
        assertEquals(1, profiles.deleted)
        assertEquals(1, account.deleted)
    }

    @Test
    fun `nothing is told about the asker without an account, without a profile or on a marked device`() = runTest {
        assertNull(membership.asker(TODAY))

        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        assertEquals(AskerFacts("Lorena", Zodiac.PISCES, birthdayToday = false), membership.asker(TODAY))

        account.state.value = AccountState.SignedOut
        assertNull(membership.asker(TODAY))

        account.state.value = FakeAccount.ANA
        marks.underAge.value = true
        assertNull(membership.asker(TODAY))
    }

    @Test
    fun `a birthday is told on the day, until it has been wished, and again a year later`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        val birthday = ADULT.withYear(TODAY.year)

        assertEquals(false, membership.asker(birthday.minusDays(1))?.birthdayToday)
        assertEquals(true, membership.asker(birthday)?.birthdayToday)

        membership.birthdayWished(birthday)
        assertEquals(false, membership.asker(birthday)?.birthdayToday)
        assertEquals(true, membership.asker(birthday.plusYears(1))?.birthdayToday)
    }

    @Test
    fun `someone born on a 29 February has a birthday every year`() {
        val leapling = Profile("Bisi", LocalDate.of(2000, 2, 29))
        assertTrue(leapling.isBirthday(LocalDate.of(2028, 2, 29)))
        assertTrue(leapling.isBirthday(LocalDate.of(2027, 2, 28)))
        assertFalse(leapling.isBirthday(LocalDate.of(2028, 2, 28)))
        assertEquals(Zodiac.PISCES, leapling.zodiac)
    }

    @Test
    fun `a date has to be a real day on which someone alive was born`() {
        assertEquals(LocalDate.of(1990, 3, 12), ProfileRules.dateOrNull("12", " 3", "1990", TODAY))
        assertNull(ProfileRules.dateOrNull("31", "2", "1990", TODAY))
        assertNull(ProfileRules.dateOrNull("12", "13", "1990", TODAY))
        assertNull(ProfileRules.dateOrNull("", "3", "1990", TODAY))
        assertNull(ProfileRules.dateOrNull("12", "3", "90", TODAY))
        assertNull(ProfileRules.dateOrNull("12", "3", "1850", TODAY))
        assertNull(ProfileRules.dateOrNull("1", "1", (TODAY.year + 1).toString(), TODAY))
    }

    @Test
    fun `what the server holds is a profile only if it is whole and was agreed to`() {
        val whole = mapOf(
            "firstName" to "Lorena",
            "birthDate" to "1990-03-12",
            "consent" to mapOf("version" to 1L, "at" to "whenever")
        )
        assertEquals(
            ProfileState.Present(Profile("Lorena", ADULT)),
            FirestoreProfiles.profileOf(exists = true, fromCache = false, data = whole)
        )
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole - "consent"))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole + ("birthDate" to "12/03/1990")))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole + ("firstName" to 7)))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, null))
    }

    @Test
    fun `a profile this device has no copy of is unknown until the server has spoken`() {
        assertEquals(ProfileState.Unknown, FirestoreProfiles.profileOf(exists = false, fromCache = true, data = null))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(exists = false, fromCache = false, data = null))
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 11, 13)
        val ADULT: LocalDate = LocalDate.of(1990, 3, 12)
        val MINOR: LocalDate = LocalDate.of(2012, 6, 1)
    }
}
