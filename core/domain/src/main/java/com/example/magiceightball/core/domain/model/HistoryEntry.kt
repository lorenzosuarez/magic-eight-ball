package com.example.magiceightball.core.domain.model

/**
 * One answer the ball gave.
 *
 * @property id unique among the saved entries, and larger for a later answer.
 * @property question what was asked out loud; empty when the ball was asked without a question.
 * @property answer the answer as shown; null when none could be obtained and the app gave its own.
 * @property fromAi true when the AI service wrote the answer, false for a bundled one.
 */
data class HistoryEntry(
    val id: Long,
    val question: String,
    val answer: String?,
    val askedAtMillis: Long,
    val fromAi: Boolean,
    /** The style that gave the answer; null when none did, or for an entry kept before styles were. */
    val style: Magic8BallPersonality? = null,
    /**
     * The name of the character that gave it, when it was one of the user's own: kept here so
     * that the answer stays signed after the character, or the account, is gone.
     */
    val customName: String? = null
)
