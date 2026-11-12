package com.example.magiceightball.core.domain.gateway

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * Where an answer goes when the user says it should not have been given. What the AI service
 * writes is not read by anyone before it is shown: this is how the developer gets to see it.
 */
interface AnswerReports {
    /**
     * Sends what the ball said, as it was written. Never what was asked, and nothing about who
     * asked: the report is about the answer.
     */
    fun report(answer: String, remark: String?, style: Magic8BallPersonality?, languageCode: String)

    /** For where there is nowhere to send them: tests, and builds without the service. */
    object None : AnswerReports {
        override fun report(answer: String, remark: String?, style: Magic8BallPersonality?, languageCode: String) = Unit
    }
}
