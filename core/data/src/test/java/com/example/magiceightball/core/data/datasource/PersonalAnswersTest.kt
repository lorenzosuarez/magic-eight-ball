package com.example.magiceightball.core.data.datasource

import android.content.Context
import android.content.res.AssetManager
import com.example.magiceightball.core.data.config.RemoteAnswers
import com.example.magiceightball.core.data.datasource.JsonLocalDataSource.Companion.RECENT_WINDOW
import com.example.magiceightball.core.data.datasource.JsonLocalDataSource.Companion.namesOneOf
import com.example.magiceightball.core.data.datasource.JsonLocalDataSource.Companion.pickAvoidingRepeat
import com.example.magiceightball.core.data.model.Magic8BallDataDto
import com.example.magiceightball.core.data.model.PersonalAnswersDto
import com.example.magiceightball.core.data.model.PersonalLinesDto
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.ASTRO
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.COACH
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.CUSTOM
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.GRANDMA
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.LOVESTRUCK
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.MYSTICAL
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.NEUTRAL
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.PIRATE
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.THERAPIST
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.Zodiac
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class PersonalAnswersTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val ordinaryText = File("src/main/assets/$ORDINARY").readText()
    private val personalText = File("src/main/assets/$PERSONAL").readText()
    private val ordinary = json.decodeFromString<Magic8BallDataDto>(ordinaryText)
    private val personal = json.decodeFromString<Map<String, PersonalAnswersDto>>(personalText)
    private val assetManager = mockk<AssetManager>()

    private val everything = AskerFacts(firstName = "Lorenzo", zodiac = Zodiac.LEO)

    /** The data source over the app's own files, unless a test writes them, and with chance that repeats. */
    private fun dataSource(ordinary: String = ordinaryText, personal: String = personalText): JsonLocalDataSource {
        every { assetManager.open(ORDINARY) } answers { ordinary.byteInputStream() }
        every { assetManager.open(PERSONAL) } answers { personal.byteInputStream() }
        val context = mockk<Context> { every { assets } returns assetManager }
        val noRemoteAnswers = object : RemoteAnswers {
            override fun json() = ""
        }
        return JsonLocalDataSource(context, json, noRemoteAnswers, Random(SEED))
    }

    private suspend fun JsonLocalDataSource.draw(
        times: Int,
        language: String,
        personality: Magic8BallPersonality,
        asker: AskerFacts?
    ): List<Magic8BallResult.Success> = List(times) { getRandomMessage(language, personality, asker) }

    private fun knowsName(personality: Magic8BallPersonality) = everything.knownTo(personality)?.firstName != null

    private fun knowsSign(personality: Magic8BallPersonality) = everything.knownTo(personality)?.zodiac != null

    /** (max characters, max words): the same as for the ordinary lines. */
    private fun fitsTheBall(text: String) =
        text.codePointCount(0, text.length) <= 28 && text.trim().split(Regex("\\s+")).size <= 6

    @Test
    fun `with nobody to speak to the answers are the ordinary ones, drawn as they always were`() = runTest {
        // The draw as it was before any line spoke to anyone: the same chance, used the same way.
        val written = ordinary.messagesFor("es", GRANDMA)
        val chance = Random(SEED)
        val recent = ArrayDeque<String>()
        val before = List(300) {
            pickAvoidingRepeat(written, recent, chance)!!.also {
                recent.addLast(it)
                if (recent.size > RECENT_WINDOW) recent.removeFirst()
            }
        }

        // Nobody is signed in, which is always so on the watch: the other file is not even opened.
        assertEquals(before.map { Magic8BallResult.Success(it) }, dataSource().draw(300, "es", GRANDMA, null))
        verify(exactly = 0) { assetManager.open(PERSONAL) }

        // Somebody of whom the character may use nothing is nobody to it: a grandmother reads no
        // stars, a pirate calls no one by name, and the classic style knows no one at all.
        assertEquals(before, dataSource().draw(300, "es", GRANDMA, AskerFacts(zodiac = Zodiac.LEO)).map { it.message })
        assertEquals(dataSource().draw(300, "pt", PIRATE, null), dataSource().draw(300, "pt", PIRATE, everything))
        assertEquals(
            dataSource().draw(300, "en", NEUTRAL, null),
            dataSource().draw(300, "en", NEUTRAL, everything.copy(birthdayToday = true))
        )
    }

    @Test
    fun `about one answer in three calls the asker by name, and only from a character that knows it`() = runTest {
        val asker = AskerFacts(firstName = "Lorenzo Suárez", zodiac = Zodiac.LEO)
        for (personality in listOf(THERAPIST, GRANDMA, COACH, LOVESTRUCK)) {
            for (language in LANGUAGES) {
                val answers = dataSource().draw(DRAWS, language, personality, asker).map { it.message }
                val byName = answers.count { "Lorenzo" in it }
                assertTrue("$language/$personality: $byName", byName in DRAWS * 28 / 100..DRAWS * 39 / 100)
                // By the first name alone, and in a line that still fits the ball.
                assertTrue(answers.none { "Suárez" in it })
                assertTrue(answers.all(::fitsTheBall))
                // The rest are the ordinary ones, as they are written.
                assertTrue(answers.filterNot { "Lorenzo" in it }.all { it in ordinary.messagesFor(language, personality) })
            }
        }
        // A pirate and an astrologer were not told the name, so they cannot say it.
        assertTrue(dataSource().draw(DRAWS, "en", PIRATE, asker).none { "Lorenzo" in it.message })
        assertTrue(dataSource().draw(DRAWS, "en", ASTRO, asker).none { "Lorenzo" in it.message })
    }

    @Test
    fun `a name typed without its capital is said with one`() = runTest {
        val answers = dataSource().draw(DRAWS, "es", GRANDMA, AskerFacts(firstName = "lorenzo")).map { it.message }

        assertTrue(answers.any { "Lorenzo" in it })
        assertTrue(answers.none { "lorenzo" in it })
    }

    @Test
    fun `the sign spoken of is the asker's own, and no ordinary line guesses at another`() = runTest {
        for (language in LANGUAGES) {
            val signs = personal.getValue(language).signs
            for ((personality, share) in listOf(ASTRO to 50, MYSTICAL to 25)) {
                var ofTheirSign = 0
                for (zodiac in Zodiac.entries) {
                    val own = signs.getValue(zodiac.name.lowercase())
                    val answers = dataSource().draw(300, language, personality, everything.copy(zodiac = zodiac))
                        .map { it.message }
                    // An ordinary line that names their sign is a guess too, even a lucky one:
                    // with those gone, the sign is only in the lines written for it.
                    ofTheirSign += answers.count { own in it }
                    val others = signs.values - own
                    assertTrue("$language/$personality/$zodiac", answers.none { answer -> others.any { it in answer } })
                    assertTrue(answers.all(::fitsTheBall))
                }
                // Every other answer of the astrologer, whose trade it is, and one in four of the oracle.
                val asked = 300 * Zodiac.entries.size
                val about = asked * (share - 4) / 100..asked * (share + 4) / 100
                assertTrue("$language/$personality: $ofTheirSign", ofTheirSign in about)
            }
            // Asked by nobody, the astrologer goes on guessing: those lines are still its own.
            val unasked = dataSource().draw(300, language, ASTRO, null).map { it.message }
            assertTrue(language, unasked.any { answer -> signs.values.any { it in answer } })
        }
    }

    @Test
    fun `a sign is named by a whole word, in capitals or not, and never by part of one`() {
        val signs = listOf("Leo", "Virgo", "Libra", "Géminis")
        assertTrue(namesOneOf("Eres Leo, ¿verdad? Pues sí.", signs))
        assertTrue(namesOneOf("QUÉ GÉMINIS ERES", signs))
        // Said of all of them it is as much of a guess.
        assertTrue(namesOneOf("For sure. Virgos always do.", signs))
        assertFalse(namesOneOf("No. Return the library book.", signs))
        assertFalse(namesOneOf("Pregúntale a Leonor.", signs))
        assertFalse(namesOneOf("Sí. Te tocó carta bonita.", signs))
    }

    @Test
    fun `on the birthday the answer is a line for the day, and says so`() = runTest {
        val today = AskerFacts(firstName = "Lorenzo", zodiac = Zodiac.CAPRICORN, birthdayToday = true)
        for (language in LANGUAGES) {
            for (personality in Magic8BallPersonality.entries) {
                val source = dataSource()
                val answers = source.draw(6, language, personality, today)
                // The classic style knows nobody, and nothing is written for a character of the user's own.
                if (personality == NEUTRAL || personality == CUSTOM) {
                    assertTrue(answers.all { !it.birthdayLine && it.message in ordinary.messagesFor(language, NEUTRAL) })
                    continue
                }
                val forTheDay = personal.getValue(language).styles.getValue(personality.name.lowercase()).birthday
                    .map { it.replace("{name}", "Lorenzo") }
                // Before any line by name or of the sign, for as long as the wish is owed.
                assertTrue("$language/$personality", answers.all { it.birthdayLine && it.message in forTheDay })
                assertTrue(answers.zipWithNext().none { (first, second) -> first == second })
                // Once wished the asker comes without the birthday, and the answers go back to the others.
                val after = source.draw(6, language, personality, today.copy(birthdayToday = false))
                assertTrue(after.none { it.birthdayLine || it.message in forTheDay })
            }
        }
    }

    @Test
    fun `a name is said only in the lines it still fits, and in none when it fits none`() = runTest {
        val lines = """{"en":{"styles":{"coach":{"name":["Go for it, {name}!","Well done today, {name}!"]}}}}"""
        suspend fun byName(name: String): Set<String> =
            dataSource(personal = lines).draw(300, "en", COACH, AskerFacts(firstName = name))
                .map { it.message }
                .filterNot { it in ordinary.messagesFor("en", COACH) }
                .toSet()

        assertEquals(setOf("Go for it, Lorenzo!", "Well done today, Lorenzo!"), byName("Lorenzo"))
        // Eleven letters are one too many for the longer line: it is left out, and the name is not cut.
        assertEquals(setOf("Go for it, Maximiliano!"), byName("Maximiliano"))
        // Twenty fit neither: every answer is an ordinary one.
        assertEquals(emptySet<String>(), byName("Wolfeschlegelsteinha"))
        // Characters are counted, not units of text: ten of these letters are twenty units.
        val gothic = "\uD800\uDF30".repeat(10)
        assertEquals(setOf("Go for it, $gothic!", "Well done today, $gothic!"), byName(gothic))
    }

    @Test
    fun `no answer is said twice running, whoever it speaks to`() = runTest {
        // One line by name and two ordinary ones: chance alone would often give the name twice in a row.
        val few = dataSource(
            ordinary = """{"en":{"grandma":["Yes, dear.","No, dear."]}}""",
            personal = """{"en":{"styles":{"grandma":{"name":["Eat, {name}."]}}}}"""
        ).draw(600, "en", GRANDMA, everything).map { it.message }
        assertEquals(setOf("Yes, dear.", "No, dear.", "Eat, Lorenzo."), few.toSet())
        assertTrue(few.zipWithNext().none { (first, second) -> first == second })

        for (personality in listOf(THERAPIST, GRANDMA, COACH, LOVESTRUCK, ASTRO, MYSTICAL)) {
            val answers = dataSource().draw(DRAWS, "es", personality, everything).map { it.message }
            assertTrue("$personality", answers.zipWithNext().none { (first, second) -> first == second })
        }
    }

    @Test
    fun `the lines that speak to the asker are written for the same characters in the three languages`() {
        assertEquals(LANGUAGES.toSet(), personal.keys)
        val voiced = Magic8BallPersonality.entries - NEUTRAL - CUSTOM
        personal.forEach { (language, written) ->
            // Twelve signs, each under the name the app knows it by.
            assertEquals(language, Zodiac.entries.map { it.name.lowercase() }.toSet(), written.signs.keys)
            assertEquals(language, 12, written.signs.values.toSet().size)
            assertEquals(language, voiced.map { it.name.lowercase() }.toSet(), written.styles.keys)
            for (personality in voiced) {
                val lines = written.styles.getValue(personality.name.lowercase())
                val where = "$language/$personality"
                // A list for each thing the character may know, and none for what it may not.
                assertEquals(where, knowsName(personality), lines.name.isNotEmpty())
                assertEquals(where, knowsSign(personality), lines.sign.isNotEmpty())
                if (knowsName(personality)) assertTrue(where, lines.name.size >= 8)
                if (knowsSign(personality)) assertTrue(where, lines.sign.size >= 8)
                assertTrue(where, lines.birthday.size >= 2)
            }
        }
    }

    @Test
    fun `every line fits the ball with a name of seven letters and the longest sign`() {
        personal.forEach { (language, written) ->
            val longestSign = written.signs.values.maxBy { it.length }
            val all = written.styles.values.flatMap(PersonalLinesDto::all)
            assertEquals(language, all.size, all.map { it.lowercase() }.toSet().size)
            written.styles.forEach { (style, lines) ->
                val personality = Magic8BallPersonality.valueOf(style.uppercase())
                val mayAsk = setOfNotNull(
                    "{name}".takeIf { knowsName(personality) },
                    "{sign}".takeIf { knowsSign(personality) }
                )
                lines.all().forEach { line ->
                    val where = "$language/$style: $line"
                    // Nothing the character may not know, and nothing the app would not fill.
                    assertTrue(where, mayAsk.containsAll(PLACEHOLDER.findAll(line).map { it.value }.toList()))
                    assertEquals(where, line.trim(), line)
                    assertTrue(where, fitsTheBall(line.replace("{name}", "Lorenzo").replace("{sign}", longestSign)))
                }
                // Each list is of what it says, so that its share of the answers is the one meant.
                assertTrue("$language/$style", lines.name.all { "{name}" in it } && lines.sign.all { "{sign}" in it })
                // A longer name still has lines to be said in.
                if (lines.name.isNotEmpty()) {
                    val stillSaid = lines.name.count { fitsTheBall(it.replace("{name}", "Maximiliano")) }
                    assertTrue("$language/$style: $stillSaid", stillSaid >= 4)
                }
                // Whoever gave no name is wished a happy birthday all the same.
                assertTrue("$language/$style", lines.birthday.any { "{name}" !in it })
            }
        }
    }

    private companion object {
        const val ORDINARY = "magic_8_ball_responses.json"
        const val PERSONAL = "magic_8_ball_personal.json"
        const val SEED = 8
        const val DRAWS = 1200
        val LANGUAGES = listOf("en", "es", "pt")
        val PLACEHOLDER = Regex("\\{[^}]*\\}")
    }
}

private fun PersonalLinesDto.all(): List<String> = name + sign + birthday
