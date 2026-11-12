package com.lorenzosuarez.magiceightball.phone.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class DailyVerdictTest {

    private fun at(hour: Int, minute: Int = 0) = LocalDateTime.of(2026, 11, 12, hour, minute)

    @Test
    fun `before the hour it comes later today, and after it tomorrow`() {
        assertEquals(Duration.ofHours(2), DailyVerdict.delayUntil(10, at(8)))
        assertEquals(Duration.ofMinutes(30), DailyVerdict.delayUntil(10, at(9, 30)))
        assertEquals(Duration.ofHours(23), DailyVerdict.delayUntil(10, at(11)))
        // On the hour itself, the next one is a day away: it is never due at once.
        assertEquals(Duration.ofHours(24), DailyVerdict.delayUntil(10, at(10)))
    }

    @Test
    fun `an hour that makes no sense is taken as the nearest that does`() {
        assertEquals(Duration.ofHours(15), DailyVerdict.delayUntil(99, at(8)))
        assertEquals(Duration.ofHours(16), DailyVerdict.delayUntil(-3, at(8)))
    }

    private fun isDue(
        enabledByUser: Boolean = true,
        enabledFromAfar: Boolean = true,
        mayNotify: Boolean = true,
        lastAskedDay: Long? = 99,
        today: Long = 100
    ) = DailyVerdict.isDue(enabledByUser, enabledFromAfar, mayNotify, lastAskedDay, today)

    @Test
    fun `a verdict is due to someone who allows it and has not asked the ball today`() {
        assertTrue(isDue())
        assertTrue(isDue(lastAskedDay = null))
        assertFalse(isDue(lastAskedDay = 100))
    }

    @Test
    fun `none is given when the user, the developer or the system says no`() {
        assertFalse(isDue(enabledByUser = false))
        assertFalse(isDue(enabledFromAfar = false))
        assertFalse(isDue(mayNotify = false))
    }
}
