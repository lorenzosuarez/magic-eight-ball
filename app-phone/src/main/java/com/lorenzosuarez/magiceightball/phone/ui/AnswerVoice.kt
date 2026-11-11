package com.lorenzosuarez.magiceightball.phone.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Reads answers out loud with the device's own voice. The speech service takes a moment to
 * start, so an answer asked for before then is said as soon as it has.
 */
internal class AnswerVoice(context: Context) {
    private var ready = false
    private var waiting: Pair<String, Locale>? = null
    private val speech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        waiting?.let { (text, locale) -> say(text, locale) }
        waiting = null
    }

    /** Says [text] in [locale]'s voice, in place of whatever was being said. */
    fun say(text: String, locale: Locale) {
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
        speech.stop()
    }

    fun release() {
        speech.stop()
        speech.shutdown()
    }

    private companion object {
        const val UTTERANCE_ID = "answer"
    }
}

/** The voice while [enabled], released when it no longer is: nobody else pays for the speech service. */
@Composable
internal fun rememberAnswerVoice(enabled: Boolean): AnswerVoice? {
    val context = LocalContext.current
    val voice = remember(enabled, context) { if (enabled) AnswerVoice(context) else null }
    DisposableEffect(voice) { onDispose { voice?.release() } }
    return voice
}
