package com.example.magiceightball.core.domain.policy

interface PromptPolicy {
    fun getSystemPrompt(languageCode: String): String
}
