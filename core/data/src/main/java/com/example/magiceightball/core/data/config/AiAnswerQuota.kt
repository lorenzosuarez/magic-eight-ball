package com.example.magiceightball.core.data.config

import android.content.Context
import com.example.magiceightball.core.domain.gateway.AiAllowance
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

    /** Adds [answers] to today's allowance, on top of whatever the daily limit is. */
    fun grant(answers: Int)
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
) : AiAnswerQuota, AiAllowance {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        store = PreferencesStore(context),
        today = { LocalDate.now(Clock.systemDefaultZone()).toString() }
    )

    @Synchronized
    override fun tryTake(dailyLimit: Int): Boolean {
        val day = today()
        startDay(day)
        if (store.used >= dailyLimit + store.bonus) return false
        store.used += 1
        return true
    }

    @Synchronized
    override fun grant(answers: Int) {
        if (answers <= 0) return
        startDay(today())
        // Kept within reason whatever asks for it: the count is a guard on the bill.
        store.bonus = (store.bonus + answers).coerceAtMost(MAX_BONUS)
    }

    /** A new day starts from nothing: neither what was used nor what was added carries over. */
    private fun startDay(day: String) {
        if (store.day == day) return
        store.day = day
        store.used = 0
        store.bonus = 0
    }

    /** Where the count is kept. */
    interface Store {
        var day: String?
        var used: Int

        /** Answers added to today's allowance. */
        var bonus: Int
    }

    private class PreferencesStore(context: Context) : Store {
        private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

        override var day: String?
            get() = preferences.getString(KEY_DAY, null)
            set(value) = preferences.edit().putString(KEY_DAY, value).apply()

        override var used: Int
            get() = preferences.getInt(KEY_USED, 0)
            set(value) = preferences.edit().putInt(KEY_USED, value).apply()

        override var bonus: Int
            get() = preferences.getInt(KEY_BONUS, 0)
            set(value) = preferences.edit().putInt(KEY_BONUS, value).apply()

        private companion object {
            const val KEY_BONUS = "bonus"
            const val FILE = "ai_answer_quota"
            const val KEY_DAY = "day"
            const val KEY_USED = "used"
        }
    }

    private companion object {
        const val MAX_BONUS = 100
    }
}
