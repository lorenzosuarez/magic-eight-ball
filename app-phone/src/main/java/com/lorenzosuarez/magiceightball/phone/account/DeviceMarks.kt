package com.lorenzosuarez.magiceightball.phone.account

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * What this device remembers about whoever uses it, and tells nobody: it stays in the app's own
 * files, out of any backup, until the app's data is cleared.
 */
interface DeviceMarks {
    /**
     * Whether a date of birth of someone under [ProfileRules.ADULT_AGE] was entered here. Only
     * that it was: nothing of what was typed. From then on the app on this device offers no
     * account and asks the AI service for nothing.
     */
    val underAge: Flow<Boolean>

    suspend fun markUnderAge()

    /**
     * The year in which a birthday was last wished, so that it is wished once. Only the year:
     * the day would be the user's birthday, written down where it outlives their account.
     */
    val birthdayWishedIn: Flow<Int?>

    suspend fun markBirthdayWished(year: Int)

    /** For tests, and for anything that has no device to remember on. */
    object None : DeviceMarks {
        override val underAge: Flow<Boolean> = flowOf(false)
        override suspend fun markUnderAge() = Unit
        override val birthdayWishedIn: Flow<Int?> = flowOf(null)
        override suspend fun markBirthdayWished(year: Int) = Unit
    }
}

/** [DeviceMarks] in a Preferences DataStore file of its own. A file that cannot be read holds no marks. */
class DataStoreDeviceMarks(private val dataStore: DataStore<Preferences>) : DeviceMarks {

    private val marks: Flow<Preferences> = dataStore.data.catch { e ->
        if (e !is IOException) throw e
        Log.w(TAG, "Could not read the device's marks", e)
        emit(emptyPreferences())
    }

    override val underAge: Flow<Boolean> = marks.map { it[UNDER_AGE] == true }
    override val birthdayWishedIn: Flow<Int?> = marks.map { it[BIRTHDAY_WISHED_IN] }

    override suspend fun markUnderAge() = write { it[UNDER_AGE] = true }

    override suspend fun markBirthdayWished(year: Int) = write { it[BIRTHDAY_WISHED_IN] = year }

    private suspend fun write(change: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        try {
            dataStore.edit(change)
        } catch (e: IOException) {
            Log.w(TAG, "Could not save the device's marks", e)
        }
    }

    private companion object {
        const val TAG = "DeviceMarks"
        val UNDER_AGE = booleanPreferencesKey("under_age")
        val BIRTHDAY_WISHED_IN = intPreferencesKey("birthday_wished_in")
    }
}
