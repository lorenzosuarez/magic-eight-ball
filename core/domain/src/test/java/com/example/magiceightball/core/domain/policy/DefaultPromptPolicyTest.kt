package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultPromptPolicyTest {

    private val policy = DefaultPromptPolicy()

    @Test
    fun `every personality has its own prompt`() {
        val prompts = Magic8BallPersonality.entries.map { policy.getSystemPrompt("en", it) }

        assertTrue(prompts.all { it.isNotBlank() })
        assertTrue(prompts.distinct().size == prompts.size)
    }

    @Test
    fun `the prompt names the personality`() {
        assertTrue("sarcastic" in policy.getSystemPrompt("en", Magic8BallPersonality.SARCASTIC))
        assertTrue("mystical" in policy.getSystemPrompt("en", Magic8BallPersonality.MYSTICAL))
    }

    @Test
    fun `every prompt limits the answer to six words`() {
        Magic8BallPersonality.entries.forEach { personality ->
            assertTrue("Max 6 words." in policy.getSystemPrompt("en", personality))
        }
    }

    @Test
    fun `the answer language follows the language code`() {
        Magic8BallPersonality.entries.forEach { personality ->
            val english = policy.getSystemPrompt("en", personality)
            val spanish = policy.getSystemPrompt("es", personality)

            assertTrue(english.endsWith(" Respond in English."))
            assertTrue(spanish.endsWith(" Respond in Spanish."))
            assertNotEquals(english, spanish)
        }
    }

    @Test
    fun `an unknown language is answered in English`() {
        assertTrue(
            policy.getSystemPrompt("fr", Magic8BallPersonality.SARCASTIC).endsWith(" Respond in English.")
        )
    }
}
