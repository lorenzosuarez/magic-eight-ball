package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings

/**
 * UI state of the chat screen.
 *
 * @property aiAnswersAvailable whether AI answers can be offered at all; when false the user's
 * choice in [settings] has no effect and answers are the bundled ones.
 * @property aiAllowanceSpent whether the last answer was a bundled one only because the day's
 * answers from the AI service are used up.
 * @property canListen whether the watch can hear a question: it has a recognizer, and the
 * microphone has not been refused.
 */
data class ChatState(
    val machineState: ChatStateMachine = ChatStateMachine.Idle,
    val settings: UserSettings = UserSettings(),
    val aiAnswersAvailable: Boolean = false,
    val aiAllowanceSpent: Boolean = false,
    val canListen: Boolean = false
) {
    /**
     * Whether the AI service is the one that answers next. Everything the screen says or offers
     * about it goes by this alone: the classic style has its own answers, and so has a day
     * whose answers from the service are used up.
     */
    val usesAiAnswers: Boolean
        get() = settings.aiAnswersEnabled && aiAnswersAvailable &&
            settings.personality != Magic8BallPersonality.NEUTRAL && !aiAllowanceSpent

    /**
     * Whether holding the screen listens to a question. Only when the AI service is the one
     * answering, since nothing else can answer a question, and the watch can listen.
     */
    val canAskByVoice: Boolean get() = usesAiAnswers && canListen
}

/**
 * The cycle of one answer: [Idle] -> [Running] -> [Completed] -> [Idle] for a shake or a tap,
 * with [Listening] before [Running] for a question asked out loud.
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

    /** @property reported true once the user has said this answer should not have been given. */
    data class Completed(val answer: AnswerText, val reported: Boolean = false) : ChatStateMachine
}

/**
 * Text shown on the die: either an answer as delivered, or the app's own answer for when none
 * could be obtained.
 */
sealed interface AnswerText {
    /** @property fromAi true when the AI service wrote it; false for a bundled answer. */
    data class Plain(val value: String, val fromAi: Boolean = false) : AnswerText
    data object Fallback : AnswerText
}

/**
 * One-shot events of the cycle. They are delivered once, to whoever is collecting at the time.
 */
sealed interface ChatEffect {
    /** A shake, or the tap that stands for one, has asked. */
    data object ShakeDetected : ChatEffect
    data object ListeningStarted : ChatEffect
    data object ListeningStopped : ChatEffect
    data object AnswerRevealed : ChatEffect

    /** A hold ended without a question the watch could make out: nothing will be answered. */
    data object NothingHeard : ChatEffect

    /** The microphone permission has to be asked for before the app can listen. */
    data object RequestMicrophonePermission : ChatEffect
}
