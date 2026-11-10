package com.example.magiceightball.core.domain.model

/**
 * Timing of one shake-to-answer cycle.
 *
 * @property processingDelayMs artificial latency of the mock gateway.
 * @property minThinkingMs shortest time the "thinking" state is shown, so the loading animation
 * always plays through even when the answer is available instantly (offline answers).
 * @property maxThinkingMs longest time to wait for an answer before giving up on it.
 * @property completionHoldTimeMs how long the answer stays on screen.
 * @property cooldownMs extra time after the hold before a new shake is accepted.
 */
data class QueryConfig(
    val processingDelayMs: Long = 2000L,
    val minThinkingMs: Long = 1200L,
    val maxThinkingMs: Long = 6000L,
    val completionHoldTimeMs: Long = 3000L,
    val cooldownMs: Long = 1000L
)
