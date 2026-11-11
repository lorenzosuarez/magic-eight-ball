package com.lorenzosuarez.magiceightball.phone.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Reads answers out loud with the device's own voice. The speech service is only started the
 * first time something is to be said, and takes a moment to: what was asked for meanwhile is
 * said as soon as it has.
 */
internal class AnswerVoice(context: Context) {
    private val context = context.applicationContext
    private var speech: TextToSpeech? = null
    private var ready = false
    private var waiting: Pair<String, Locale>? = null

    /** Says [text] in [locale]'s voice, in place of whatever was being said. */
    fun say(text: String, locale: Locale) {
        val speech = speech ?: TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            waiting?.let { (text, locale) -> if (ready) say(text, locale) }
            waiting = null
        }.also { this.speech = it }
        if (!ready) {
            waiting = text to locale
            return
        }
        // A device without a voice for the language keeps the one it has.
        speech.setLanguage(locale)
        speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun hush() {
        waiting = null
        speech?.stop()
    }

    fun release() {
        speech?.stop()
        speech?.shutdown()
        speech = null
        ready = false
    }

    private companion object {
        const val UTTERANCE_ID = "answer"
    }
}

/** The voice, for as long as the screen is there. It costs nothing until it first speaks. */
@Composable
internal fun rememberAnswerVoice(): AnswerVoice {
    val context = LocalContext.current
    val voice = remember(context) { AnswerVoice(context) }
    DisposableEffect(voice) { onDispose { voice.release() } }
    return voice
}
