package com.lorenzosuarez.magiceightball.phone.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class AdPacingTest {

    private val pacing = AdPacing(minAnswers = 3, minIntervalMillis = 1_000L)

    @Test
    fun `no ad before enough answers have been closed`() {
        assertEquals(listOf(false, false, true), List(3) { pacing.answerClosed(nowMillis = 0L) })
    }

    @Test
    fun `after an ad both the answers and the time have to pass again`() {
        repeat(3) { pacing.answerClosed(0L) }
        pacing.adShown(0L)

        // Enough answers, too soon.
        assertEquals(listOf(false, false, false), List(3) { pacing.answerClosed(500L) })
        // Late enough now, and the answers already counted still count.
        assertEquals(true, pacing.answerClosed(1_000L))
    }

    @Test
    fun `an ad that could not be shown stays due`() {
        repeat(3) { pacing.answerClosed(0L) }

        assertEquals(true, pacing.answerClosed(0L))
    }
}
