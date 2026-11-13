package com.lorenzosuarez.magiceightball.phone.account

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

/** An account driven by the test: signing in makes [who], and what was asked of it is counted. */
class FakeAccount(
    initial: AccountState = AccountState.SignedOut,
    private val who: AccountState.SignedIn = ANA
) : Account {
    override val state = MutableStateFlow(initial)
    var deletes = AccountOutcome.DONE
    var deleted = 0
    var signedOut = 0

    override suspend fun signIn(host: Context): AccountOutcome {
        state.value = who
        return AccountOutcome.DONE
    }

    override suspend fun signOut() {
        signedOut++
        state.value = AccountState.SignedOut
    }

    override suspend fun delete(host: Context): AccountOutcome {
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

/** Stands for the profile's document: what is saved here is what the next read finds. */
class FakeProfiles(initial: ProfileState = ProfileState.Unknown) : Profiles {
    override val state = MutableStateFlow(initial)
    var works = true
    val saved = mutableListOf<Profile>()
    var deleted = 0

    override suspend fun save(profile: Profile): Boolean {
        if (works) {
            saved += profile
            state.value = ProfileState.Present(profile)
        }
        return works
    }

    override suspend fun delete(): Boolean {
        if (works) {
            deleted++
            state.value = ProfileState.Missing
        }
        return works
    }
}

/** Stands for the device's own file of marks. */
class FakeDeviceMarks : DeviceMarks {
    override val underAge = MutableStateFlow(false)
    override val birthdayWishedOn = MutableStateFlow<Long?>(null)

    override suspend fun markUnderAge() {
        underAge.value = true
    }

    override suspend fun markBirthdayWished(epochDay: Long) {
        birthdayWishedOn.value = epochDay
    }
}
