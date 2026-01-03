package com.example.magiceightball.core.domain.policy

import javax.inject.Inject

class DefaultPromptPolicy @Inject constructor() : PromptPolicy {
    
    // Updated persona: Creative, funny, rarely optimistic, max 6 words.
    private val basePrompt = "You are a sarcastic, witty Magic 8 Ball. Be creative, funny, and rarely optimistic. Use dark humor. Max 6 words."

    override fun getSystemPrompt(languageCode: String): String {
        val languageInstruction = when (languageCode) {
            "es" -> " Respond in Spanish."
            else -> " Respond in English."
        }
        return basePrompt + languageInstruction
    }
}
