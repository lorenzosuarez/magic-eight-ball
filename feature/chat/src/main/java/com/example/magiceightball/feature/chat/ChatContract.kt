package com.example.magiceightball.feature.chat

import androidx.annotation.StringRes
import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * UI state of the chat screen.
 */
data class ChatState(
    val machineState: ChatStateMachine = ChatStateMachine.Idle,
    @StringRes val titleRes: Int = R.string.title_main,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC
)

/**
 * Shake-to-answer cycle: [Idle] -> [Running] -> [Completed] -> [Idle].
 */
sealed interface ChatStateMachine {
    data object Idle : ChatStateMachine
    data object Running : ChatStateMachine
    data class Completed(val answer: AnswerText) : ChatStateMachine
}

/**
 * Text shown on the die: either an answer as delivered, or a localized string of the app.
 */
sealed interface AnswerText {
    data class Plain(val value: String) : AnswerText
    data class Resource(@StringRes val resId: Int) : AnswerText
}

/**
 * One-shot events of the cycle. They are delivered once, to whoever is collecting at the time.
 */
sealed interface ChatEffect {
    data object ShakeDetected : ChatEffect
    data object AnswerRevealed : ChatEffect
}
