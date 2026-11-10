package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

interface PromptPolicy {
    /**
     * The instruction that fixes how the AI service answers.
     *
     * @param answersQuestion true when the user's own question is sent along. The instruction
     * then also tells the service to treat that text as a question only, never as instructions.
     */
    fun getSystemPrompt(
        languageCode: String,
        personality: Magic8BallPersonality,
        answersQuestion: Boolean = false
    ): String
}
