package com.example.magiceightball.core.domain.gateway

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageEventTest {

    private val events = listOf(
        UsageEvent.Asked(UsageEvent.Asked.How.VOICE, Magic8BallPersonality.PIRATE, fromAi = true, withRemark = true),
        UsageEvent.Asked(
            UsageEvent.Asked.How.VOICE,
            Magic8BallPersonality.VILLAIN,
            fromAi = false,
            withRemark = false,
            tryAgain = true,
            aiMiss = "safety_blocked"
        ),
        UsageEvent.AnswerShared(Magic8BallPersonality.CAT, fromAi = false),
        UsageEvent.AppShared,
        UsageEvent.StyleChosen(Magic8BallPersonality.THERAPIST),
        UsageEvent.SettingChanged(UsageEvent.SettingChanged.Setting.SPOKEN_ANSWERS, on = true),
        UsageEvent.PartySet(players = 4)
    )

    @Test
    fun `an answer is counted by how it was asked, its style and where it came from`() {
        val asked = events.first()

        assertEquals("ask", asked.name)
        assertEquals(mapOf("how" to "voice", "style" to "pirate", "source" to "ai", "remark" to "yes"), asked.params)
    }

    @Test
    fun `an answer the AI service did not write says why, and whether the ball said to try again`() {
        assertEquals(
            mapOf("how" to "voice", "style" to "villain", "source" to "try_again", "remark" to "no", "ai_miss" to "safety_blocked"),
            events[1].params
        )
        assertEquals(
            mapOf("how" to "tap", "style" to "cat", "source" to "bundled", "remark" to "no", "ai_miss" to "timeout"),
            UsageEvent.Asked(UsageEvent.Asked.How.TAP, Magic8BallPersonality.CAT, fromAi = false, withRemark = false, aiMiss = "timeout").params
        )
    }

    @Test
    fun `a party is counted by how many play, never by who`() {
        assertEquals(mapOf("players" to "4"), UsageEvent.PartySet(players = 4).params)
    }

    /** What the measuring service accepts: names and keys of letters and underscores, short values. */
    @Test
    fun `every event is a short name with short words for values`() {
        val word = Regex("[a-z][a-z0-9_]{0,39}")
        events.forEach { event ->
            assertTrue(event.name, word.matches(event.name))
            event.params.forEach { (key, value) ->
                assertTrue(key, word.matches(key))
                assertTrue(value, value.length <= 36 && Regex("[a-z0-9_]+").matches(value))
            }
        }
    }
}
