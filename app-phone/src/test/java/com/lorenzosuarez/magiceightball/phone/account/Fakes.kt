package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

/** An account driven by the test: signing in makes [who], and what was asked of it is counted. */
class FakeAccount(
    initial: AccountState = AccountState.SignedOut,
    private val who: AccountState.SignedIn = ANA
) : Account {
    override val state = MutableStateFlow(initial)

    /** How being asked who it is ends: done at once for someone who signed in a moment ago. */
    var confirms = AccountOutcome.DONE
    var confirmed = 0
    var deletes = AccountOutcome.DONE
    var deleted = 0
    var signedOut = 0

    /** The hosts deletion was asked with: null where nothing may be asked of the user. */
    val deletedWith = mutableListOf<Context?>()

    /** Something for a deletion to wait on, as a slow connection would make it. */
    var beforeDeleting: suspend () -> Unit = {}

    override suspend fun signIn(host: Context): AccountOutcome {
        state.value = who
        return AccountOutcome.DONE
    }

    override suspend fun signOut() {
        signedOut++
        state.value = AccountState.SignedOut
    }

    override suspend fun confirmIdentity(host: Context): AccountOutcome {
        confirmed++
        return confirms
    }

    override suspend fun delete(host: Context?): AccountOutcome {
        beforeDeleting()
        deletedWith += host
        if (deletes == AccountOutcome.DONE) {
            deleted++
            state.value = AccountState.SignedOut
        }
        return deletes
    }

    companion object {
        val ANA = AccountState.SignedIn("1", "Ana García", "ana@example.com")
    }
}

/** Stands for the profile's document on the server: what is saved here is what the next read finds. */
class FakeProfiles(initial: ProfileState = ProfileState.Unknown) : Profiles {
    override val state = MutableStateFlow(initial)
    var saves = SaveOutcome.SAVED
    var deletes = true
    val saved = mutableListOf<Profile>()
    var deleted = 0

    override suspend fun save(profile: Profile): SaveOutcome {
        if (saves == SaveOutcome.SAVED) {
            saved += profile
            state.value = ProfileState.Present(profile)
        }
        return saves
    }

    override suspend fun delete(): Boolean {
        if (deletes) {
            deleted++
            state.value = ProfileState.Missing
        }
        return deletes
    }
}

/** Stands for the device's own file of marks. */
class FakeDeviceMarks : DeviceMarks {
    override val underAge = MutableStateFlow(false)
    override val birthdayWishedIn = MutableStateFlow<Int?>(null)

    /** False for a file that cannot be written: the mark is asked for and does not stay. */
    var writes = true

    override suspend fun markUnderAge() {
        if (writes) underAge.value = true
    }

    override suspend fun markBirthdayWished(year: Int) {
        birthdayWishedIn.value = year
    }
}
