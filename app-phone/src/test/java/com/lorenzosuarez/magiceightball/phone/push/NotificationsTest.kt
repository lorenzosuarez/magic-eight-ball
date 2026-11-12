package com.lorenzosuarez.magiceightball.phone.push

import com.example.magiceightball.core.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsTest {

    private fun shouldAsk(
        sdk: Int = 34,
        granted: Boolean = false,
        alreadyAsked: Boolean = false,
        bestStreak: Int = 3,
        askAfterDays: Int = 3
    ) = Notifications.shouldAsk(sdk, granted, alreadyAsked, bestStreak, askAfterDays)

    @Test
    fun `the question is put to someone who has come back for the days set`() {
        assertTrue(shouldAsk())
        assertTrue(shouldAsk(bestStreak = 9))
        assertFalse(shouldAsk(bestStreak = 2))
    }

    @Test
    fun `it is put once, and never to someone who already allows them`() {
        assertFalse(shouldAsk(alreadyAsked = true))
        assertFalse(shouldAsk(granted = true))
    }

    @Test
    fun `zero days means it is never put, and an older system does not ask at all`() {
        assertFalse(shouldAsk(askAfterDays = 0, bestStreak = 100))
        assertFalse(shouldAsk(sdk = 32))
    }

    @Test
    fun `every language has a topic of its own`() {
        assertEquals("lang_es", Notifications.topicOf(AppLanguage.SPANISH))
        assertEquals(AppLanguage.entries.size, AppLanguage.entries.map(Notifications::topicOf).distinct().size)
    }
}
