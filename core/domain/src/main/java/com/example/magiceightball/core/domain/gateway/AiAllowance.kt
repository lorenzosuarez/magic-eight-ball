package com.example.magiceightball.core.domain.gateway

/** The day's answers from the AI service, as far as the user can add to them. */
interface AiAllowance {
    /** Adds [answers] to what this device may still ask the AI service for today. */
    fun grant(answers: Int)
}
