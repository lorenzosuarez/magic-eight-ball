package com.example.magiceightball.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AskerFactsTest {

    @Test
    fun `each sign starts and ends on the days the horoscope pages give`() {
        val firstAndLast = mapOf(
            Zodiac.ARIES to ((3 to 21) to (4 to 19)),
            Zodiac.TAURUS to ((4 to 20) to (5 to 20)),
            Zodiac.GEMINI to ((5 to 21) to (6 to 20)),
            Zodiac.CANCER to ((6 to 21) to (7 to 22)),
            Zodiac.LEO to ((7 to 23) to (8 to 22)),
            Zodiac.VIRGO to ((8 to 23) to (9 to 22)),
            Zodiac.LIBRA to ((9 to 23) to (10 to 22)),
            Zodiac.SCORPIO to ((10 to 23) to (11 to 21)),
            Zodiac.SAGITTARIUS to ((11 to 22) to (12 to 21)),
            Zodiac.CAPRICORN to ((12 to 22) to (1 to 19)),
            Zodiac.AQUARIUS to ((1 to 20) to (2 to 18)),
            Zodiac.PISCES to ((2 to 19) to (3 to 20))
        )
        firstAndLast.forEach { (sign, days) ->
            val (first, last) = days
            assertEquals(sign, Zodiac.of(first.first, first.second))
            assertEquals(sign, Zodiac.of(last.first, last.second))
        }
        assertEquals(Zodiac.CAPRICORN, Zodiac.of(1, 1))
        assertEquals(Zodiac.CAPRICORN, Zodiac.of(12, 31))
        assertEquals(Zodiac.PISCES, Zodiac.of(2, 29))
    }

    @Test
    fun `a name leaves the device as its letters, two words at most`() {
        assertEquals("Lorena", AskerFacts(firstName = "  Lorena ").nameForAi)
        assertEquals("María José", AskerFacts(firstName = "María José de la Cruz").nameForAi)
        assertEquals("Jean-Luc", AskerFacts(firstName = "Jean-Luc").nameForAi)
        assertEquals("D'Angelo", AskerFacts(firstName = "D'Angelo").nameForAi)
        assertEquals("Ana", AskerFacts(firstName = "Ana\n\"); DROP 123").nameForAi?.substringBefore(" "))
        assertEquals(AskerFacts.MAX_NAME_LENGTH, AskerFacts(firstName = "A".repeat(80)).nameForAi?.length)
    }

    @Test
    fun `a name with no letters in it is no name`() {
        assertNull(AskerFacts(firstName = "").nameForAi)
        assertNull(AskerFacts(firstName = " 1234 !! -- ''").nameForAi)
        assertNull(AskerFacts(firstName = null).nameForAi)
    }

    @Test
    fun `a character is handed only what it makes use of`() {
        val all = AskerFacts(firstName = "Lorena", zodiac = Zodiac.LEO, birthdayToday = false)
        assertEquals(AskerFacts(firstName = "Lorena"), all.knownTo(Magic8BallPersonality.GRANDMA))
        assertEquals(AskerFacts(zodiac = Zodiac.LEO), all.knownTo(Magic8BallPersonality.ASTRO))
        assertNull(all.knownTo(Magic8BallPersonality.PIRATE))
        assertNull(all.copy(birthdayToday = true).knownTo(Magic8BallPersonality.NEUTRAL))
        assertEquals(AskerFacts(birthdayToday = true), all.copy(birthdayToday = true).knownTo(Magic8BallPersonality.PIRATE))
    }
}
