package com.lorenzosuarez.magiceightball.phone

import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.UserSettings

/**
 * UI state of the single screen.
 *
 * @property aiAnswersAvailable whether AI answers can be offered at all; when false the user's
 * choice in [settings] has no effect and answers are the bundled ones.
 * @property voice whether holding the screen listens to a question, and if not, why.
 * @property notice a short explanation shown in place of the hint for a moment.
 * @property history the latest answers, newest first.
 */
data class EightBallUiState(
    val phase: BallPhase = BallPhase.Idle,
    val settings: UserSettings = UserSettings(),
    val aiAnswersAvailable: Boolean = false,
    val voice: VoiceStatus = VoiceStatus.Ready,
    val notice: Notice? = null,
    val history: List<HistoryEntry> = emptyList()
) {
    /** Whether a question the app hears is sent to the AI service to be answered. */
    val usesAiAnswers: Boolean get() = settings.aiAnswersEnabled && aiAnswersAvailable
}

/**
 * What the ball is doing. A question goes [Listening] -> [Thinking] -> [Answered]; a tap or a
 * shake skips [Listening]. The answer stays until the next question or until it is dismissed.
 */
sealed interface BallPhase {
    data object Idle : BallPhase

    /**
     * @property transcript what has been understood so far; empty until the first words.
     * @property handsFree true when nothing is being held: the question ends with a tap or a pause.
     */
    data class Listening(val transcript: String = "", val handsFree: Boolean = false) : BallPhase

    /** @property question the question being answered; empty when the ball was just asked. */
    data class Thinking(val question: String = "") : BallPhase

    /** @property id grows with every answer, so two identical answers are still told apart. */
    data class Answered(
        val id: Long,
        val question: String,
        val answer: AnswerText
    ) : BallPhase
}

/**
 * Text shown on the die: either an answer as delivered, or the app's own answer for when none
 * could be obtained.
 */
sealed interface AnswerText {
    /** @property fromAi true when the AI service wrote it, false for a bundled answer. */
    data class Plain(val value: String, val fromAi: Boolean = false) : AnswerText
    data object Fallback : AnswerText
}

/** Why holding the screen does, or does not, listen to a question. */
enum class VoiceStatus {
    /** Holding listens. The microphone permission is asked for on the first hold if needed. */
    Ready,

    /** The user turned voice questions off. */
    Off,

    /** The user did not grant the microphone permission. */
    PermissionDenied,

    /** The device has no speech recognition service. */
    Unsupported;

    val isReady: Boolean get() = this == Ready
}

/** Explanations the screen gives once, without getting in the way. */
enum class Notice {
    MicrophoneDenied,
    VoiceUnsupported
}

/** One-shot events, delivered once to whoever is collecting at the time. */
sealed interface EightBallEffect {
    data object ListeningStarted : EightBallEffect
    data object ListeningStopped : EightBallEffect
    data object AskStarted : EightBallEffect
    data object AnswerRevealed : EightBallEffect

    /** The microphone permission has to be asked for before the app can listen. */
    data object RequestMicrophonePermission : EightBallEffect
}
