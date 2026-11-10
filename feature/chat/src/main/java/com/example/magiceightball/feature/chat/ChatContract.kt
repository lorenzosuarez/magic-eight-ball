package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.domain.model.UserSettings

/**
 * UI state of the chat screen.
 *
 * @property aiAnswersAvailable whether AI answers can be offered at all; when false the user's
 * choice in [settings] has no effect and answers are the bundled ones.
 * @property canAskByVoice whether holding the screen listens to a question. Only when the AI
 * service is the one answering, since nothing else can answer a question, and the watch can
 * listen. Without the microphone permission a hold asks for it, after a refusal too.
 */
data class ChatState(
    val machineState: ChatStateMachine = ChatStateMachine.Idle,
    val settings: UserSettings = UserSettings(),
    val aiAnswersAvailable: Boolean = false,
    val canAskByVoice: Boolean = false
) {
    /** Whether a shake asks the AI service for the answer. */
    val usesAiAnswers: Boolean get() = settings.aiAnswersEnabled && aiAnswersAvailable
}

/**
 * The cycle of one answer: [Idle] -> [Running] -> [Completed] -> [Idle] for a shake, with
 * [Listening] before [Running] for a question asked out loud.
 */
sealed interface ChatStateMachine {
    data object Idle : ChatStateMachine

    /**
     * @property transcript what has been understood so far; empty until the first words.
     * @property finished true once the question is over and [transcript] is all of it: it stays
     * on screen for a moment, so the user sees what the ball is about to answer.
     */
    data class Listening(val transcript: String = "", val finished: Boolean = false) : ChatStateMachine
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
    data object ListeningStarted : ChatEffect
    data object ListeningStopped : ChatEffect
    data object AnswerRevealed : ChatEffect

    /** The microphone permission has to be asked for before the app can listen. */
    data object RequestMicrophonePermission : ChatEffect
}
