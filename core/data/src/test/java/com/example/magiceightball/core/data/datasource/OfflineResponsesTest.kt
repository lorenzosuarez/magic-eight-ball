package com.example.magiceightball.core.data.datasource

import com.example.magiceightball.core.data.model.Magic8BallDataDto
import com.example.magiceightball.core.data.model.PersonalityResponsesDto
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.MYSTICAL
import com.example.magiceightball.core.domain.model.Magic8BallPersonality.SARCASTIC
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfflineResponsesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val data = Magic8BallDataDto(
        en = PersonalityResponsesDto(sarcastic = listOf("en-s"), mystical = listOf("en-m")),
        es = PersonalityResponsesDto(sarcastic = listOf("es-s"), mystical = listOf("es-m"))
    )

    @Test
    fun `personality and language select their own list`() {
        assertEquals(listOf("en-s"), data.messagesFor("en", SARCASTIC))
        assertEquals(listOf("en-m"), data.messagesFor("en", MYSTICAL))
        assertEquals(listOf("es-s"), data.messagesFor("es", SARCASTIC))
        assertEquals(listOf("es-m"), data.messagesFor("es-MX", MYSTICAL))
        assertEquals(listOf("en-m"), data.messagesFor("fr", MYSTICAL))
    }

    @Test
    fun `missing list falls back without failing`() {
        val noSpanishMystical = data.copy(es = PersonalityResponsesDto(sarcastic = listOf("es-s")))
        assertEquals(listOf("en-m"), noSpanishMystical.messagesFor("es", MYSTICAL))

        val onlySpanishSarcastic = Magic8BallDataDto(es = PersonalityResponsesDto(sarcastic = listOf("es-s")))
        assertEquals(listOf("es-s"), onlySpanishSarcastic.messagesFor("es", MYSTICAL))

        assertTrue(Magic8BallDataDto().messagesFor("es", MYSTICAL).isEmpty())
        assertEquals(listOf("en-s"), json.decodeFromString<Magic8BallDataDto>("""{"en":{"sarcastic":["en-s"," "]}}""").messagesFor("es", MYSTICAL))
    }

    @Test
    fun `pick never repeats the previous answer when there is an alternative`() {
        repeat(200) {
            assertNotEquals("a", JsonLocalDataSource.pickAvoidingRepeat(listOf("a", "b", "c"), listOf("a")))
            // Everything was seen recently: the oldest comes back, never the latest.
            assertEquals("a", JsonLocalDataSource.pickAvoidingRepeat(listOf("a", "b", "c"), listOf("a", "b", "c")))
            assertEquals("c", JsonLocalDataSource.pickAvoidingRepeat(listOf("a", "b", "c"), listOf("a", "b")))
        }
        assertEquals("a", JsonLocalDataSource.pickAvoidingRepeat(listOf("a"), listOf("a")))
        assertEquals("a", JsonLocalDataSource.pickAvoidingRepeat(listOf("a"), emptyList()))
        assertNull(JsonLocalDataSource.pickAvoidingRepeat(emptyList(), listOf("a")))
    }

    @Test
    fun `bundled answers fit the watch face`() {
        val asset = File("src/main/assets/magic_8_ball_responses.json")
        val bundled = json.decodeFromString<Magic8BallDataDto>(asset.readText())
        // Caps come from the triangle text area: (max characters, max words).
        val caps = mapOf("en" to (28 to 6), "es" to (23 to 6))
        val all = mutableListOf<String>()
        for ((language, cap) in caps) {
            for (personality in listOf(SARCASTIC, MYSTICAL)) {
                val messages = bundled.messagesFor(language, personality)
                assertTrue("$language/$personality has ${messages.size}", messages.size >= 40)
                messages.forEach { message ->
                    assertTrue(message, message.length <= cap.first)
                    assertTrue(message, message.trim().split(Regex("\\s+")).size <= cap.second)
                }
                all += messages
            }
        }
        assertEquals(all.size, all.map { it.lowercase() }.toSet().size)
    }
}
