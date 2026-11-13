package com.lorenzosuarez.magiceightball.phone.account

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Zodiac
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
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
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class MembershipTest {

    private val host: Context = ApplicationProvider.getApplicationContext()
    private val account = FakeAccount(initial = FakeAccount.ANA)
    private val profiles = FakeProfiles(initial = ProfileState.Missing)
    private val marks = FakeDeviceMarks()
    private val characters = FakeCustomCharacters()
    private val membership = Membership(account, profiles, marks, characters)

    @Test
    fun `an adult's profile is saved as typed, tidied, and the account stays`() = runTest {
        val outcome = membership.saveProfile("  lorena   maría ", ADULT, TODAY, host)

        assertEquals(ProfileOutcome.SAVED, outcome)
        assertEquals(Profile("lorena maría", ADULT), profiles.saved.single())
        assertEquals(FakeAccount.ANA, account.state.value)
        assertFalse(membership.isUnderAge())
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
    fun `the account goes even when the screen that gave the under-age date goes first`() = runTest {
        // Marking the device takes that screen, and whatever it started, away.
        val reachedTheServer = CompletableDeferred<Unit>()
        account.beforeDeleting = { reachedTheServer.await() }
        val screen = launch { membership.saveProfile("Lorena", MINOR, TODAY, host) }
        runCurrent()
        assertTrue(membership.who.first().removing)

        screen.cancel()
        reachedTheServer.complete(Unit)
        screen.join()

        assertEquals(1, account.deleted)
        assertTrue(marks.underAge.value)
        assertTrue(membership.who.first().ageNotice)
        assertFalse(membership.who.first().removing)
    }

    @Test
    fun `an account that cannot be reached for an under-age date stays signed in, to be deleted at the next start`() = runTest {
        account.deletes = AccountOutcome.FAILED
        membership.saveProfile("Lorena", MINOR, TODAY, host)

        // Signing out would leave an account nobody could reach to delete.
        assertEquals(0, account.signedOut)
        assertEquals(FakeAccount.ANA, account.state.value)
        assertTrue(marks.underAge.value)
        assertNull(membership.asker(TODAY))

        account.deletes = AccountOutcome.DONE
        Membership(account, profiles, marks, characters).enforceAge()

        assertEquals(1, account.deleted)
        // Nothing is asked of anyone at start.
        assertNull(account.deletedWith.last())
    }

    @Test
    fun `at start nothing is done to an account on a device that is not marked`() = runTest {
        membership.enforceAge()

        assertEquals(0, account.deleted)
        assertEquals(0, profiles.deleted)
    }

    @Test
    fun `a device whose file cannot be written is still under age for as long as the app runs`() = runTest {
        marks.writes = false

        membership.saveProfile("Lorena", MINOR, TODAY, host)

        assertFalse(marks.underAge.value)
        assertTrue(membership.isUnderAge())
        assertTrue(membership.who.first().underAge)
    }

    @Test
    fun `a profile that cannot be saved, or that the server refuses, says so and leaves the account as it was`() = runTest {
        profiles.saves = SaveOutcome.FAILED
        assertEquals(ProfileOutcome.FAILED, membership.saveProfile("Lorena", ADULT, TODAY, host))

        profiles.saves = SaveOutcome.REFUSED
        assertEquals(ProfileOutcome.REFUSED, membership.saveProfile("Lorena", ADULT, TODAY, host))

        assertEquals(FakeAccount.ANA, account.state.value)
        assertEquals(0, account.deleted)
        assertFalse(membership.isUnderAge())
    }

    @Test
    fun `a name with no letters in it is not saved`() = runTest {
        assertEquals(ProfileOutcome.FAILED, membership.saveProfile(" 123 ", ADULT, TODAY, host))
        assertTrue(profiles.saved.isEmpty())
    }

    @Test
    fun `going on without an account deletes the one just made`() = runTest {
        assertEquals(AccountOutcome.DONE, membership.declineProfile(host))

        assertEquals(1, account.deleted)
        assertEquals(AccountState.SignedOut, account.state.value)
        assertFalse(membership.isUnderAge())
    }

    @Test
    fun `an account that cannot be deleted on the way out is kept signed in, and said to be`() = runTest {
        account.deletes = AccountOutcome.FAILED

        assertEquals(AccountOutcome.FAILED, membership.declineProfile(host))
        assertEquals(0, account.signedOut)
        assertEquals(FakeAccount.ANA, account.state.value)
    }

    @Test
    fun `deleting the account asks who it is before anything is touched`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        account.confirms = AccountOutcome.CANCELLED

        assertEquals(AccountOutcome.CANCELLED, membership.deleteAccount(host))

        assertEquals(0, profiles.deleted)
        assertEquals(0, account.deleted)
        assertEquals(ProfileState.Present(Profile("Lorena", ADULT)), profiles.state.value)
    }

    @Test
    fun `deleting the account deletes its profile first, and keeps the account if that fails`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        profiles.deletes = false
        assertEquals(AccountOutcome.FAILED, membership.deleteAccount(host))
        assertEquals(0, account.deleted)

        profiles.deletes = true
        assertEquals(AccountOutcome.DONE, membership.deleteAccount(host))
        assertEquals(1, profiles.deleted)
        assertEquals(1, account.deleted)
    }

    @Test
    fun `a profile this device does not know of is deleted with its account all the same`() = runTest {
        // Not heard of here, and there all the same: a listener that failed, a document that does not read.
        profiles.state.value = ProfileState.Unknown

        assertEquals(AccountOutcome.DONE, membership.deleteAccount(host))

        assertEquals(1, profiles.deleted)
        assertEquals(1, account.deleted)
    }

    @Test
    fun `a character is saved as typed, tidied, in the first free slot, and then over itself`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))

        val first = membership.saveCharacter(null, "  Capitán\nNemo ", "  Un capitán gruñón del mar. ", Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT)
        val second = membership.saveCharacter(null, "Doña Rosa", "Una vecina que lo sabe todo.", Magic8BallPersonality.GRANDMA, Magic8BallPersonality.NEUTRAL)

        val nemo = CustomCharacter("1", "Capitán Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT)
        assertEquals(CharacterSave.Saved(nemo), first)
        assertEquals("2", (second as CharacterSave.Saved).character.id)

        membership.saveCharacter("1", "Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT)
        assertEquals(listOf("Nemo", "Doña Rosa"), characters.state.value?.map { it.name })
        assertEquals(characters.state.value, membership.who.first().characters)
    }

    @Test
    fun `a sixth character has nowhere to go, and one with no name or nothing said of it is not saved`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        repeat(CustomCharacter.MAX_COUNT) { membership.saveCharacter(null, "Nemo $it", "Un capitán gruñón del mar.", Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT) }

        assertEquals(CharacterSave.Full, membership.saveCharacter(null, "Otro", "Uno más, que ya no cabe.", Magic8BallPersonality.CAT, Magic8BallPersonality.CAT))
        assertEquals(CharacterSave.Refused, membership.saveCharacter("1", " -- ", "Un capitán gruñón del mar.", Magic8BallPersonality.CAT, Magic8BallPersonality.CAT))
        assertEquals(CharacterSave.Refused, membership.saveCharacter("1", "Nemo", "ok", Magic8BallPersonality.CAT, Magic8BallPersonality.CAT))
        assertEquals(CustomCharacter.MAX_COUNT, characters.state.value?.size)
    }

    @Test
    fun `nobody without an account and a profile makes a character, nor does a marked device`() = runTest {
        suspend fun save() = membership.saveCharacter(null, "Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.CAT, Magic8BallPersonality.CAT)
        // Signed in, and not yet asked who they are: nothing was agreed to.
        assertEquals(CharacterSave.Failed, save())

        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        marks.underAge.value = true
        assertEquals(CharacterSave.Failed, save())

        marks.underAge.value = false
        account.state.value = AccountState.SignedOut
        assertEquals(CharacterSave.Failed, save())
        assertTrue(characters.state.value.orEmpty().isEmpty())
    }

    @Test
    fun `one of the app's characters lends a look, and the user's own kind lends none`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))

        val saved = membership.saveCharacter(null, "Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.CUSTOM, Magic8BallPersonality.CUSTOM)

        assertEquals(Magic8BallPersonality.NEUTRAL, (saved as CharacterSave.Saved).character.colour)
        assertEquals(Magic8BallPersonality.NEUTRAL, saved.character.emblem)
    }

    @Test
    fun `an account goes with its characters first, then its profile, and stays if either cannot be deleted`() = runTest {
        profiles.state.value = ProfileState.Present(Profile("Lorena", ADULT))
        membership.saveCharacter(null, "Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.CAT, Magic8BallPersonality.CAT)

        characters.deletes = false
        assertEquals(AccountOutcome.FAILED, membership.deleteAccount(host))
        assertEquals(0, profiles.deleted)
        assertEquals(0, account.deleted)

        characters.deletes = true
        assertEquals(AccountOutcome.DONE, membership.deleteAccount(host))
        assertEquals(1, characters.deletedAll)
        assertEquals(1, profiles.deleted)
        assertEquals(1, account.deleted)
    }

    @Test
    fun `what the server holds is a character only in a slot, with a name and something said of it`() {
        val whole = mapOf("name" to "Capitán Nemo", "description" to "Un capitán gruñón del mar.", "colour" to "pirate", "emblem" to "cat")
        assertEquals(
            CustomCharacter("3", "Capitán Nemo", "Un capitán gruñón del mar.", Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT),
            FirestoreCustomCharacters.characterOf("3", whole)
        )
        assertEquals(whole, FirestoreCustomCharacters.fieldsOf(FirestoreCustomCharacters.characterOf("3", whole)!!))
        assertNull(FirestoreCustomCharacters.characterOf("6", whole))
        assertNull(FirestoreCustomCharacters.characterOf("3", whole - "description"))
        assertNull(FirestoreCustomCharacters.characterOf("3", whole + ("name" to 7)))
        assertNull(FirestoreCustomCharacters.characterOf("3", null))
        // A look this app does not know, or the user's own kind, is no look.
        val odd = FirestoreCustomCharacters.characterOf("3", whole + ("colour" to "unicorn") + ("emblem" to "custom"))
        assertEquals(Magic8BallPersonality.NEUTRAL, odd?.colour)
        assertEquals(Magic8BallPersonality.NEUTRAL, odd?.emblem)
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
        // What the device keeps of it is the year, which is nobody's birthday.
        assertEquals(TODAY.year, marks.birthdayWishedIn.value)
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
    fun `what the server holds is a profile only if it is whole and was agreed to as worded today`() {
        val whole = mapOf(
            "firstName" to "Lorena",
            "birthDate" to "1990-03-12",
            "consent" to mapOf("version" to FirestoreProfiles.CONSENT_VERSION, "at" to "whenever")
        )
        assertEquals(
            ProfileState.Present(Profile("Lorena", ADULT)),
            FirestoreProfiles.profileOf(exists = true, unconfirmed = false, data = whole)
        )
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole - "consent"))
        // Agreed to an earlier wording: asked again.
        val earlier = whole + ("consent" to mapOf("version" to FirestoreProfiles.CONSENT_VERSION - 1, "at" to "whenever"))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, earlier))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole + ("birthDate" to "12/03/1990")))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, whole + ("firstName" to 7)))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(true, false, null))
    }

    @Test
    fun `a profile this device has no copy of is unknown until the server has spoken`() {
        assertEquals(ProfileState.Unknown, FirestoreProfiles.profileOf(exists = false, unconfirmed = true, data = null))
        assertEquals(ProfileState.Missing, FirestoreProfiles.profileOf(exists = false, unconfirmed = false, data = null))
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 11, 13)
        val ADULT: LocalDate = LocalDate.of(1990, 3, 12)
        val MINOR: LocalDate = LocalDate.of(2012, 6, 1)
    }
}
