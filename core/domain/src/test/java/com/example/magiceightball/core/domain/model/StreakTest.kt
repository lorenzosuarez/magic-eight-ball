package com.example.magiceightball.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakTest {

    @Test
    fun `the first answer starts a run of one day`() {
        assertEquals(Streak(days = 1, best = 1, lastDay = 100), Streak().after(100))
    }

    @Test
    fun `more answers on the same day do not make it longer`() {
        val today = Streak().after(100)

        assertEquals(today, today.after(100))
    }

    @Test
    fun `an answer the next day makes the run a day longer`() {
        val run = Streak().after(100).after(101).after(102)

        assertEquals(Streak(days = 3, best = 3, lastDay = 102), run)
    }

    @Test
    fun `a day without an answer ends the run, and the best one is remembered`() {
        val run = Streak().after(100).after(101).after(102).after(104)

        assertEquals(Streak(days = 1, best = 3, lastDay = 104), run)
    }

    @Test
    fun `a run is alive today and the day after, and over the day after that`() {
        val run = Streak().after(100).after(101)

        assertEquals(2, run.daysOn(101))
        assertEquals(2, run.daysOn(102))
        assertEquals(0, run.daysOn(103))
        assertEquals(0, Streak().daysOn(100))
    }

    @Test
    fun `a clock set back changes nothing`() {
        val run = Streak().after(100).after(101)

        assertEquals(run, run.after(90))
        assertEquals(0, run.daysOn(90))
    }
}
