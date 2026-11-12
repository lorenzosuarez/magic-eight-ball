package com.example.magiceightball.core.data.settings

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.magiceightball.core.domain.model.Streak
import com.example.magiceightball.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Keeps the [Streak] in a Preferences DataStore file of its own on the device. A file that
 * cannot be read counts as no streak, and one that cannot be written loses a day at worst.
 */
class DataStoreStreakRepository(
    private val dataStore: DataStore<Preferences>
) : StreakRepository {

    override val streak: Flow<Streak> = dataStore.data
        .catch { e ->
            if (e !is IOException) throw e
            Log.w(TAG, "Could not read the streak", e)
            emit(emptyPreferences())
        }
        .map { it.toStreak() }

    override suspend fun recordAnswer(epochDay: Long) {
        try {
            dataStore.edit { preferences ->
                val next = preferences.toStreak().after(epochDay)
                preferences[DAYS] = next.days
                preferences[BEST] = next.best
                next.lastDay?.let { preferences[LAST_DAY] = it }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Could not save the streak", e)
        }
    }

    private fun Preferences.toStreak() = Streak(
        days = (this[DAYS] ?: 0).coerceAtLeast(0),
        best = (this[BEST] ?: 0).coerceAtLeast(0),
        lastDay = this[LAST_DAY]
    )

    private companion object {
        const val TAG = "StreakRepository"
        val DAYS = intPreferencesKey("days")
        val BEST = intPreferencesKey("best")
        val LAST_DAY = longPreferencesKey("last_day")
    }
}
