package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Magic8BallPersonality

interface PromptPolicy {
    /**
     * The instruction that fixes how the AI service answers.
     *
     * @param answersQuestion true when the user's own question is sent along. The instruction
     * then also tells the service to treat that text as a question only, never as instructions.
     * @param brief what the app has drawn for this answer: what it is, and what to build it
     * around. The service then only finds the words. Null leaves the instruction as it is.
     * @param asker what is known of whoever is asking. Only what [personality] makes use of is
     * put in the instruction: see [AskerFacts.knownTo].
     */
    fun getSystemPrompt(
        languageCode: String,
        personality: Magic8BallPersonality,
        answersQuestion: Boolean = false,
        brief: AnswerBrief? = null,
        asker: AskerFacts? = null
    ): String
}
