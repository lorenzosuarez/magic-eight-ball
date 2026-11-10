package com.lorenzosuarez.magiceightball.phone

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings

/**
 * UI state of the single screen.
 *
 * @property history the latest answers, newest first, at most [MAX_ENTRIES]. Kept in memory only.
 */
data class EightBallUiState(
    val phase: BallPhase = BallPhase.Idle,
    val personality: Magic8BallPersonality = UserSettings().personality,
    val history: List<HistoryEntry> = emptyList()
) {
    companion object {
        const val MAX_ENTRIES = 5
    }
}

/**
 * What the ball is doing: [Idle] until the first question, then [Thinking] -> [Answered] for
 * every question. The answer stays on the ball until the next one is asked.
 */
sealed interface BallPhase {
    data object Idle : BallPhase
    data object Thinking : BallPhase
    data class Answered(val entry: HistoryEntry) : BallPhase
}

/**
 * One answered question.
 *
 * @property id grows with every answer, so two identical answers are still told apart.
 * @property question what the user typed, or empty when the ball was asked without a question.
 */
data class HistoryEntry(
    val id: Long,
    val question: String,
    val answer: AnswerText
)

/**
 * Text shown as the answer: either an answer as delivered, or the app's own answer for when
 * none could be obtained.
 */
sealed interface AnswerText {
    data class Plain(val value: String) : AnswerText
    data object Fallback : AnswerText
}

/** One-shot events, delivered once to whoever is collecting at the time. */
sealed interface EightBallEffect {
    data object AskStarted : EightBallEffect
    data object AnswerRevealed : EightBallEffect
}
