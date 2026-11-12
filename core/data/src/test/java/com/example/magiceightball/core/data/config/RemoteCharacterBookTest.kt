package com.example.magiceightball.core.data.config

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.policy.AppCharacters
import com.example.magiceightball.core.domain.policy.PromptRules
import com.example.magiceightball.core.domain.policy.SampleRemark
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RemoteCharacterBookTest {

    private var file = ""
    private val book = RemoteCharacterBook(
        remote = object : RemoteCharacters {
            override fun charactersJson(): String = file
        },
        json = Json { ignoreUnknownKeys = true }
    )

    private val pirate = Magic8BallPersonality.PIRATE

    @Test
    fun `with no file the characters are the app's own`() {
        Magic8BallPersonality.entries.forEach { personality ->
            assertEquals(AppCharacters.sheet(personality), book.sheet(personality))
            assertEquals(AppCharacters.lines(personality, "es"), book.lines(personality, "es"))
        }
        assertEquals(PromptRules(), book.rules)
    }

    @Test
    fun `what the file holds takes the place of the app's, and what it leaves out stays`() {
        file = """
            {
              "rules": { "form": "Answer in three words." },
              "characters": {
                "pirate": {
                  "wants": "Every coin the asker has.",
                  "angles": ["a debt", "a storm coming", "the cook's stew", "a debt"],
                  "lines": {
                    "es": {
                      "no": ["No. Ni por todo el ron.", "Jamás, grumete."],
                      "remarks": [{ "q": "¿Zarpo hoy?", "a": "Hoy solo zarpa quien paga su parte." }]
                    }
                  }
                }
              }
            }
        """.trimIndent()
        val own = AppCharacters.sheet(pirate)!!
        val ownLines = AppCharacters.lines(pirate, "es")!!

        val sheet = book.sheet(pirate)!!
        assertEquals("Every coin the asker has.", sheet.wants)
        assertEquals(listOf("a debt", "a storm coming", "the cook's stew"), sheet.angles)
        assertEquals(own.who, sheet.who)
        assertEquals(own.never, sheet.never)

        val lines = book.lines(pirate, "es")!!
        assertEquals(listOf("No. Ni por todo el ron.", "Jamás, grumete."), lines.no)
        assertEquals(listOf(SampleRemark("¿Zarpo hoy?", "Hoy solo zarpa quien paga su parte.")), lines.remarks)
        assertEquals(ownLines.yes, lines.yes)
        assertEquals(ownLines.maybe, lines.maybe)

        // Another language, another character and the other rules are untouched.
        assertEquals(AppCharacters.lines(pirate, "en"), book.lines(pirate, "en"))
        assertEquals(AppCharacters.sheet(Magic8BallPersonality.CAT), book.sheet(Magic8BallPersonality.CAT))
        assertEquals(PromptRules().copy(form = "Answer in three words."), book.rules)
    }

    @Test
    fun `what the file gets wrong is left out, and the app's own stays in its place`() {
        file = """
            {
              "rules": { "craft": "   ", "remark": "${"x".repeat(1300)}" },
              "characters": {
                "pirate": {
                  "who": "",
                  "wants": "${"long ".repeat(200)}",
                  "angles": ["only one"],
                  "lines": {
                    "es": {
                      "yes": ["Esta respuesta tiene bastante más de seis palabras", "Sí."],
                      "maybe": ["  ", ""],
                      "remarks": [{ "q": "", "a": "Sin pregunta." }, { "q": "¿Sí?", "a": "${"palabra ".repeat(20)}" }]
                    }
                  }
                },
                "neutral": { "who": "Somebody." },
                "wizard": { "who": "Nobody the app knows." }
              }
            }
        """.trimIndent()

        assertEquals(AppCharacters.sheet(pirate), book.sheet(pirate))
        assertEquals(AppCharacters.lines(pirate, "es"), book.lines(pirate, "es"))
        assertEquals(PromptRules(), book.rules)
        // The classic style has no character, whatever the file says.
        assertEquals(null, book.sheet(Magic8BallPersonality.NEUTRAL))
    }

    @Test
    fun `a file that cannot be read changes nothing, and a corrected one is read again`() {
        file = "{ \"characters\": { \"pirate\": { \"angles\": \"not a list\" } } "
        assertEquals(AppCharacters.sheet(pirate), book.sheet(pirate))

        file = "{ \"characters\": { \"pirate\": { \"who\": \"A cook who was never a pirate.\" } } }"
        assertEquals("A cook who was never a pirate.", book.sheet(pirate)!!.who)
        assertNotEquals(AppCharacters.sheet(pirate), book.sheet(pirate))

        file = ""
        assertEquals(AppCharacters.sheet(pirate), book.sheet(pirate))
    }
}
