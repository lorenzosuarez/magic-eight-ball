package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.domain.model.UserSettings

/**
 * UI state of the chat screen.
 *
 * @property aiAnswersAvailable whether AI answers can be offered at all; when false the user's
 * choice in [settings] has no effect and answers are the bundled ones.
 */
data class ChatState(
    val machineState: ChatStateMachine = ChatStateMachine.Idle,
    val settings: UserSettings = UserSettings(),
    val aiAnswersAvailable: Boolean = false
) {
    /** Whether a shake asks the AI service for the answer. */
    val usesAiAnswers: Boolean get() = settings.aiAnswersEnabled && aiAnswersAvailable
}

/**
 * Shake-to-answer cycle: [Idle] -> [Running] -> [Completed] -> [Idle].
 */
sealed interface ChatStateMachine {
    data object Idle : ChatStateMachine
    data object Running : ChatStateMachine
    data class Completed(val answer: AnswerText) : ChatStateMachine
}

/**
 * Text shown on the die: either an answer as delivered, or the app's own answer for when none
 * could be obtained.
 */
sealed interface AnswerText {
    data class Plain(val value: String) : AnswerText
    data object Fallback : AnswerText
}

/**
 * One-shot events of the cycle. They are delivered once, to whoever is collecting at the time.
 */
sealed interface ChatEffect {
    data object ShakeDetected : ChatEffect
    data object AnswerRevealed : ChatEffect
}
