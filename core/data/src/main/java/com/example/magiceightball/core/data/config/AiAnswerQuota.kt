package com.example.magiceightball.core.data.config

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** How many answers this device may still ask the AI service for today. */
interface AiAnswerQuota {
    /**
     * Takes one answer out of today's allowance of [dailyLimit]. False when today's allowance
     * is already used up, and then nothing is taken.
     */
    fun tryTake(dailyLimit: Int): Boolean
}

/**
 * Counts the day's AI answers in a small private file on the device. The count starts again
 * with each new day on the device's own calendar.
 *
 * It keeps an honest user from running up the bill by accident; it is not a defence against a
 * dishonest one, who can clear the app's data. The hard ceiling is the quota set on the API key.
 */
@Singleton
class DeviceAiAnswerQuota(
    private val store: Store,
    private val today: () -> String
) : AiAnswerQuota {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        store = PreferencesStore(context),
        today = { LocalDate.now(Clock.systemDefaultZone()).toString() }
    )

    @Synchronized
    override fun tryTake(dailyLimit: Int): Boolean {
        val day = today()
        val used = if (store.day == day) store.used else 0
        if (used >= dailyLimit) return false
        store.day = day
        store.used = used + 1
        return true
    }

    /** Where the count is kept. */
    interface Store {
        var day: String?
        var used: Int
    }

    private class PreferencesStore(context: Context) : Store {
        private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

        override var day: String?
            get() = preferences.getString(KEY_DAY, null)
            set(value) = preferences.edit().putString(KEY_DAY, value).apply()

        override var used: Int
            get() = preferences.getInt(KEY_USED, 0)
            set(value) = preferences.edit().putInt(KEY_USED, value).apply()

        private companion object {
            const val FILE = "ai_answer_quota"
            const val KEY_DAY = "day"
            const val KEY_USED = "used"
        }
    }
}
