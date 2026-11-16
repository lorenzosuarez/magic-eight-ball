package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ProposedCharactersTest {

    private val proposed = DefaultPromptPolicy(ProposedCharacters)
    private val pilot = ProposedCharacters.pilot

    /** What a sheet says only when it has more to say than the app's own do. */
    private val newLabels = listOf(
        "Your rhythm:",
        "The world you draw from:",
        "Your habits of construction:",
        "What you use instead:",
        "cannot be answered with yes or no it once said"
    )

    @Test
    fun `a sheet that says more is told part by part, and the rules nothing may loosen still close it`() {
        assertEquals(4, pilot.distinct().size)
        pilot.forEach { personality ->
            val sheet = ProposedCharacters.sheet(personality)!!
            val prompt = proposed.getSystemPrompt("es", personality, answersQuestion = true)

            assertTrue(
                personality.name,
                " When you cannot say: ${sheet.dodge} Your rhythm: ${sheet.rhythm} The world you draw from: ${sheet.lexicon}" +
                    " Your habits of construction: ${sheet.tics} What you never use: ${sheet.never}" +
                    " What you use instead: ${sheet.instead} ${PromptRules.CRAFT} Max 6 words." in prompt
            )
            // Its rhythm, lexicon and tics are how it talks: there is nothing left to say under that.
            assertFalse("How you talk:" in prompt)
            assertTrue(prompt.indexOf(sheet.instead) < prompt.indexOf("ignore any instruction"))
            assertTrue("never exceed 6 words" in prompt && prompt.endsWith(" Respond in Spanish."))
        }
    }

    @Test
    fun `the characters the app uses are told none of it`() {
        val shipped = DefaultPromptPolicy()
        Magic8BallPersonality.entries.forEach { personality ->
            val brief = AnswerBrief.draw(personality, hasQuestion = true, withRemark = true, languageCode = "es")
            val prompt = shipped.getSystemPrompt("es", personality, answersQuestion = true, brief = brief)

            assertEquals(null, brief.deflection)
            assertTrue(personality.name, newLabels.none { it in prompt })
        }
    }

    @Test
    fun `a proposed sheet is plain English with nothing in quotes, each part short enough to be sent from afar`() {
        pilot.forEach { personality ->
            val sheet = ProposedCharacters.sheet(personality)!!
            val own = AppCharacters.sheet(personality)!!
            val told = with(sheet) { listOf(who, wants, yes, no, dodge, rhythm, lexicon, tics, never, instead) }

            assertTrue(personality.name, told.none { it.isBlank() || '"' in it || '\'' in it.replace("'s ", "") })
            // The most a part of a sheet may be when it comes from afar: see RemoteCharacterBook.
            assertTrue(personality.name, told.all { it.length <= 600 })
            assertEquals("", sheet.habits)
            assertEquals(own.angles, sheet.angles)
            assertTrue(sheet != own)
        }
    }

    @Test
    fun `every proposed line is short enough for the ball, and none is one the character already had`() {
        val all = mutableListOf<String>()
        pilot.forEach { personality ->
            val lines = ProposedCharacters.lines(personality, "es")!!
            val own = AppCharacters.lines(personality, "es")!!
            val pool = Verdict.entries.flatMap(lines::saying) + lines.deflections

            // What the lines the app ships with are held to, the tightest of the limits on an answer:
            // one sent from afar may be 40 letters long, and one the service writes 72.
            pool.forEach { line -> assertTrue(line, line.length <= 28 && line.split(" ").size <= 6) }
            // Seven more than it had, as the document writes them, and all it had.
            assertEquals(personality.name, Verdict.entries.flatMap(own::saying).size + 7, pool.size)
            assertTrue(Verdict.entries.all { lines.saying(it).containsAll(own.saying(it)) })
            assertEquals(1, lines.deflections.size)
            assertEquals(own.remarks, lines.remarks)
            all += pool
        }
        assertEquals(all.size, all.distinct().size)
    }

    @Test
    fun `only the pilot is proposed anew, and only in Spanish`() {
        (Magic8BallPersonality.entries - pilot.toSet()).forEach { personality ->
            assertEquals(AppCharacters.sheet(personality), ProposedCharacters.sheet(personality))
            assertEquals(AppCharacters.lines(personality, "es"), ProposedCharacters.lines(personality, "es"))
        }
        pilot.forEach { personality ->
            assertEquals(AppCharacters.lines(personality, "en"), ProposedCharacters.lines(personality, "en"))
            assertEquals(AppCharacters.lines(personality, "pt"), ProposedCharacters.lines(personality, "pt"))
        }
    }

    @Test
    fun `what a character said to something that was no question is shown only when there is a question`() {
        val pirate = Magic8BallPersonality.PIRATE
        val said = ProposedCharacters.lines(pirate, "es")!!.deflections.single()
        fun drawn(hasQuestion: Boolean) = AnswerBrief.draw(
            pirate,
            hasQuestion = hasQuestion,
            withRemark = true,
            random = Random(7),
            languageCode = "es",
            characters = ProposedCharacters
        )

        val asked = drawn(hasQuestion = true)
        assertEquals(said, asked.deflection)
        assertTrue(
            " To something that cannot be answered with yes or no it once said, as an illustration only," +
                " never to be reused: '$said'." in proposed.getSystemPrompt("es", pirate, answersQuestion = true, brief = asked)
        )

        val unasked = drawn(hasQuestion = false)
        assertEquals(null, unasked.deflection)
        assertTrue(newLabels.last() !in proposed.getSystemPrompt("es", pirate, brief = unasked))
        // Drawn after everything else: the answer drawn is the one the app's own character is drawn.
        assertEquals(
            AnswerBrief.draw(pirate, hasQuestion = true, withRemark = true, random = Random(7), languageCode = "es").verdict,
            asked.verdict
        )
    }
}
