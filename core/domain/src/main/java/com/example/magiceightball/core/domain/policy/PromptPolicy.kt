package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

interface PromptPolicy {
    fun getSystemPrompt(languageCode: String, personality: Magic8BallPersonality): String
}
