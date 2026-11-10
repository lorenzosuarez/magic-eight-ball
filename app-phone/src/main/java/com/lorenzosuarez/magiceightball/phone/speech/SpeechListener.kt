package com.lorenzosuarez.magiceightball.phone.speech

import com.example.magiceightball.core.domain.model.AppLanguage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

/** Turns what the user says into text. */
interface SpeechListener {
    /** Whether the device has a speech recognition service at all. */
    fun isAvailable(): Boolean

    /**
     * Listens to one question in [language] for as long as the returned flow is collected.
     *
     * Completing [stop] tells the listener the user has finished: it stops recording, delivers
     * what it understood as a final [SpeechEvent.Transcript] if it understood anything, and the
     * flow completes. The flow also completes on its own when the service decides the user has
     * finished, and, without a final transcript, when it fails for any reason; it never throws.
     * Cancelling the collection abandons the question and releases the microphone at once.
     */
    fun listen(language: AppLanguage, stop: Deferred<Unit>): Flow<SpeechEvent>
}

sealed interface SpeechEvent {
    /** How loud the voice is right now, from 0 (silence) to 1. */
    data class Level(val value: Float) : SpeechEvent

    /** What has been understood so far; [isFinal] on the last one. */
    data class Transcript(val text: String, val isFinal: Boolean) : SpeechEvent
}
