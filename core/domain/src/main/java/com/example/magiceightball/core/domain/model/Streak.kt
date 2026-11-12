package com.example.magiceightball.core.domain.model

/**
 * How many days in a row the ball has been asked something.
 *
 * @property days the length of the run that ended on [lastDay].
 * @property best the longest run there has been: what was earned stays earned.
 * @property lastDay the last day the ball answered, counted in days since 1970 on the device's
 * own calendar; null before the first answer.
 */
data class Streak(val days: Int = 0, val best: Int = 0, val lastDay: Long? = null) {

    /**
     * The run as it stands on [today]: still alive if the ball answered today or yesterday,
     * over if a day went by without it.
     */
    fun daysOn(today: Long): Int = if (lastDay != null && today - lastDay in 0..1) days else 0

    /** The run after an answer on [today]. A clock set back changes nothing. */
    fun after(today: Long): Streak {
        val last = lastDay
        val run = when {
            last == null -> 1
            today == last -> days
            today == last + 1 -> days + 1
            today > last -> 1
            else -> return this
        }
        return Streak(days = run, best = maxOf(best, run), lastDay = today)
    }
}
