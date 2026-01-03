package com.example.magiceightball.core.domain.policy

import javax.inject.Inject

class DefaultPromptPolicy @Inject constructor() : PromptPolicy {
    
    private val basePrompt = "Generate a Magic 8 Ball style message, brief, max 6 words."

    override fun getSystemPrompt(languageCode: String): String {
        val languageInstruction = when (languageCode) {
            "es" -> " You are a Magic 8 Ball. Respond in Spanish."
            else -> " You are a Magic 8 Ball. Respond in English."
        }
        return basePrompt + languageInstruction
    }
}
