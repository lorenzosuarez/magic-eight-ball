package com.example.magiceightball.core.data.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.magiceightball.core.domain.gateway.SpeechEvent
import com.example.magiceightball.core.domain.gateway.SpeechListener
import com.example.magiceightball.core.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * [SpeechListener] backed by the platform [SpeechRecognizer].
 *
 * The on-device recognizer is preferred where the device has one (Android 12+), so the audio
 * does not leave the device; otherwise, or when it lacks the language, the device's default
 * recognition service is used. A recognizer lives exactly as long as one [listen] collection
 * and is created, used and destroyed on the main thread, as the platform requires.
 */
class AndroidSpeechListener @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechListener {

    override fun isAvailable(): Boolean = recognizerFactories().isNotEmpty()

    override fun listen(language: AppLanguage, stop: Deferred<Unit>): Flow<SpeechEvent> = callbackFlow {
        val factories = recognizerFactories()
        val intent = recognitionIntent(language)
        var recognizer: SpeechRecognizer? = null
        var next = 0
        var finished = false

        /**
         * Starts the next recognizer in line that can be started; false when there is none
         * left. One that cannot be created or refuses to start is passed over for the next.
         */
        fun startNext(listener: RecognitionListener): Boolean {
            recognizer?.destroy()
            recognizer = null
            while (true) {
                val factory = factories.getOrNull(next++) ?: return false
                try {
                    recognizer = factory().also {
                        it.setRecognitionListener(listener)
                        it.startListening(intent)
                    }
                    return true
                } catch (_: RuntimeException) {
                    recognizer?.destroy()
                    recognizer = null
                }
            }
        }

        val listener = object : RecognitionListener {
            override fun onRmsChanged(rmsdB: Float) {
                trySend(SpeechEvent.Level(((rmsdB - RMS_FLOOR_DB) / RMS_RANGE_DB).coerceIn(0f, 1f)))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.bestMatch()?.let { trySend(SpeechEvent.Transcript(it, isFinal = false)) }
            }

            override fun onResults(results: Bundle?) {
                results.bestMatch()?.let { trySend(SpeechEvent.Transcript(it, isFinal = true)) }
                close()
            }

            override fun onError(error: Int) {
                // A recognizer without the language hands over to the next one, unless the
                // user has already finished talking; every other failure ends the question.
                // A callback that was already on its way when the collection ended starts nothing.
                val canRetry = !finished && error in LANGUAGE_ERRORS && !stop.isCompleted
                if (!canRetry || !startNext(this)) close()
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }

        if (!startNext(listener)) close()

        val stopWatcher = launch {
            stop.await()
            recognizer?.stopListening()
        }

        awaitClose {
            finished = true
            stopWatcher.cancel()
            recognizer?.destroy()
            recognizer = null
        }
    }.flowOn(Dispatchers.Main.immediate)

    /** The recognizers this device offers, the on-device one first. */
    private fun recognizerFactories(): List<() -> SpeechRecognizer> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            add { SpeechRecognizer.createOnDeviceSpeechRecognizer(context) }
        }
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            add { SpeechRecognizer.createSpeechRecognizer(context) }
        }
    }

    private fun recognitionIntent(language: AppLanguage): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            .apply {
                // Where the recognizer can punctuate what it hears, a question comes with its mark.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, RecognizerIntent.FORMATTING_OPTIMIZE_QUALITY)
                }
            }

    /**
     * The tag the recognizer is asked for: the device's own variant of the app language when
     * they match (for example es-MX), a common one otherwise.
     */
    private fun AppLanguage.speechTag(): String {
        val device = Locale.getDefault()
        if (device.language == code && device.country.isNotEmpty()) return device.toLanguageTag()
        return when (this) {
            AppLanguage.ENGLISH -> "en-US"
            AppLanguage.SPANISH -> "es-ES"
        }
    }

    private fun Bundle?.bestMatch(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private companion object {
        /** The sound level the platform reports for silence, and the span up to a clear voice. */
        const val RMS_FLOOR_DB = -2f
        const val RMS_RANGE_DB = 12f

        /** ERROR_LANGUAGE_NOT_SUPPORTED and ERROR_LANGUAGE_UNAVAILABLE (constants are API 31+). */
        val LANGUAGE_ERRORS = setOf(12, 13)
    }
}
