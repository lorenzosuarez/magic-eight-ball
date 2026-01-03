package com.example.magiceightball.core.domain.policy

import javax.inject.Inject

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

class DefaultPromptPolicy @Inject constructor() : PromptPolicy {
    
    private val sarcasticPrompt = "You are a sarcastic, witty Magic 8 Ball. Be creative, funny, and rarely optimistic. Use dark humor. Max 6 words."
    private val mysticalPrompt = "You are a mystical, ancient Magic 8 Ball. Speak in riddles, wise sayings, and be mysterious. Max 6 words."

    override fun getSystemPrompt(languageCode: String, personality: Magic8BallPersonality): String {
        val basePrompt = when (personality) {
            Magic8BallPersonality.SARCASTIC -> sarcasticPrompt
            Magic8BallPersonality.MYSTICAL -> mysticalPrompt
        }
        
        val languageInstruction = when (languageCode) {
            "es" -> " Respond in Spanish."
            else -> " Respond in English."
        }
        return basePrompt + languageInstruction
    }
}
