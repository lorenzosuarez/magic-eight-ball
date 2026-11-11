package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

interface PromptPolicy {
    /**
     * The instruction that fixes how the AI service answers.
     *
     * @param answersQuestion true when the user's own question is sent along. The instruction
     * then also tells the service to treat that text as a question only, never as instructions.
     * @param verdict what the answer is this time, when the app has drawn it: the service then
     * only finds the words for it.
     */
    fun getSystemPrompt(
        languageCode: String,
        personality: Magic8BallPersonality,
        answersQuestion: Boolean = false,
        verdict: Verdict? = null
    ): String
}
