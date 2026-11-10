package com.lorenzosuarez.magiceightball.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.model.UserSettings
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import com.lorenzosuarez.magiceightball.phone.speech.SpeechEvent
import com.lorenzosuarez.magiceightball.phone.speech.SpeechListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class EightBallViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    aiAnswersAvailability: AiAnswersAvailability,
    private val speechListener: SpeechListener,
    private val history: AnswerHistoryRepository,
    private val clock: Clock,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val phase = MutableStateFlow<BallPhase>(BallPhase.Idle)
    private val notice = MutableStateFlow<Notice?>(null)
    private val resumed = MutableStateFlow(false)
    private val speechAvailable = MutableStateFlow(speechListener.isAvailable())
    private val microphone = MutableStateFlow(MicrophoneAccess.NotAsked)

    /** The saved choices; null until they have been read, right after launch. */
    private val settings: StateFlow<UserSettings?> = userSettingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val voiceInputs = combine(speechAvailable, microphone, ::Pair)
    private val noticeAndHistory = combine(notice, history.entries, ::Pair)

    /**
     * Null until the saved choices have been read, so the screen never shows a default that is
     * about to be replaced. Kept up to date without subscribers: the activity reads it to hold
     * its first frame until then.
     */
    val state: StateFlow<EightBallUiState?> = combine(
        phase,
        settings,
        aiAnswersAvailability.isAvailable,
        voiceInputs,
        noticeAndHistory
    ) { phase, saved, aiAvailable, (speech, microphone), (notice, history) ->
        saved?.let {
            EightBallUiState(
                phase = phase,
                settings = it,
                aiAnswersAvailable = aiAvailable,
                voice = voiceStatus(it, speech, microphone),
                notice = notice,
                history = history
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _voiceLevel = MutableStateFlow(0f)

    /**
     * How loud the voice is while listening, from 0 to 1. Kept out of [state] because it changes
     * many times a second and only ever drives a drawing.
     */
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

    private val _effects = Channel<EightBallEffect>(Channel.BUFFERED)
    val effects: Flow<EightBallEffect> = _effects.receiveAsFlow()

    /** The question in progress, from the first word heard to the answer on the ball. */
    private var cycleJob: Job? = null

    /** Completed when the user lets go of the screen while the app is listening. */
    private var released: CompletableDeferred<Unit>? = null

    /** True while the hold in progress belongs to a spoken question, so its end asks nothing more. */
    private var holdIsQuestion = false
    private var awaitingPermission = false
    private var noticeJob: Job? = null
    private val noticesGiven = mutableSetOf<Notice>()
    private var nextAnswerId = 0L

    init {
        observeShakes()
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { userSettingsRepository.setLanguage(language) }
    }

    fun setPersonality(personality: Magic8BallPersonality) {
        viewModelScope.launch { userSettingsRepository.setPersonality(personality) }
    }

    fun setAiAnswersEnabled(enabled: Boolean) {
        viewModelScope.launch { userSettingsRepository.setAiAnswersEnabled(enabled) }
    }

    fun setVoiceQuestionsEnabled(enabled: Boolean) {
        viewModelScope.launch { userSettingsRepository.setVoiceQuestionsEnabled(enabled) }
    }

    /**
     * Tells whether the screen is the one the user is looking at. Shakes are detected only then,
     * and the microphone is released as soon as it is not; an answer on its way still arrives.
     */
    fun setResumed(isResumed: Boolean) {
        resumed.value = isResumed
        if (isResumed) {
            speechAvailable.value = speechListener.isAvailable()
        } else {
            abandonListening()
        }
    }

    /** Reports what the system says about the microphone permission, outside of a request. */
    fun onMicrophonePermissionChecked(granted: Boolean) {
        microphone.value = when {
            granted -> MicrophoneAccess.Granted
            // Taken away in the system settings: it can be asked for again.
            microphone.value == MicrophoneAccess.Granted -> MicrophoneAccess.NotAsked
            else -> microphone.value
        }
    }

    /** Reports the user's answer to the permission request. Never blocks: no means tap and shake. */
    fun onMicrophonePermissionResult(granted: Boolean) {
        awaitingPermission = false
        microphone.value = if (granted) MicrophoneAccess.Granted else MicrophoneAccess.Denied
        if (!granted) giveNoticeOnce(Notice.MicrophoneDenied)
    }

    /** A tap or a shake: asks the ball without a question. Ignored while one is in progress. */
    fun ask() {
        if (isBusy()) return
        val settings = settings.value ?: return

        cycleJob = viewModelScope.launch {
            _effects.trySend(EightBallEffect.AskStarted)
            answer(settings) { "" }
        }
    }

    /** The finger has stayed on the screen: listen to the question, when the app may. */
    fun onHoldStart() {
        holdIsQuestion = startListening(handsFree = false)
    }

    /**
     * The finger has left the screen after a hold: the question, if one was being listened to,
     * is over and gets its answer. A hold that listened to nothing is a long tap.
     */
    fun onHoldEnd() {
        if (holdIsQuestion) {
            holdIsQuestion = false
            released?.complete(Unit)
        } else {
            ask()
        }
    }

    /** The hold was interrupted by the system rather than ended by the user: nothing is asked. */
    fun onHoldCancel() {
        holdIsQuestion = false
        abandonListening()
    }

    /**
     * The equivalent of holding for users who cannot hold: the first call starts listening and
     * the next one ends the question. The question also ends by itself after a pause.
     */
    fun toggleVoiceQuestion() {
        if (phase.value is BallPhase.Listening) {
            released?.complete(Unit)
        } else {
            startListening(handsFree = true)
        }
    }

    /** Closes whatever is on screen, a question in progress included, and goes back to rest. */
    fun dismiss() {
        if (phase.value is BallPhase.Idle) return
        val cycle = cycleJob?.also { it.cancel() }
        viewModelScope.launch {
            // An abandoned listening puts back what was there before; rest comes after that.
            cycle?.join()
            if (cycleJob === cycle) phase.value = BallPhase.Idle
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch { history.remove(id) }
    }

    /** Undoes [deleteHistoryEntry]. */
    fun restoreHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch { history.restore(entry) }
    }

    fun clearHistory() {
        viewModelScope.launch { history.clear() }
    }

    /** A question is in progress, even one whose coroutine has not shown its first state yet. */
    private fun isBusy(): Boolean =
        cycleJob?.isActive == true || phase.value is BallPhase.Listening || phase.value is BallPhase.Thinking

    /**
     * @param handsFree true when nothing is being held, so a question that ends in silence must
     * end the listening too instead of waiting for a release that will never come.
     * @return true when the gesture was taken as a spoken question: the app listens, or is
     * asking for the permission to. False when voice questions are off, denied or impossible.
     */
    private fun startListening(handsFree: Boolean): Boolean {
        if (awaitingPermission) return true
        if (isBusy()) return false
        val settings = settings.value ?: return false
        if (!settings.voiceQuestionsEnabled) return false
        if (!speechAvailable.value) {
            giveNoticeOnce(Notice.VoiceUnsupported)
            return false
        }
        return when (microphone.value) {
            MicrophoneAccess.Granted -> {
                listenAndAnswer(settings, handsFree)
                true
            }
            MicrophoneAccess.Denied -> false
            MicrophoneAccess.NotAsked -> {
                awaitingPermission = true
                _effects.trySend(EightBallEffect.RequestMicrophonePermission)
                true
            }
        }
    }

    private fun listenAndAnswer(settings: UserSettings, handsFree: Boolean) {
        val before = phase.value
        val release = CompletableDeferred<Unit>()
        released = release

        cycleJob = viewModelScope.launch {
            clearNotice()
            phase.value = BallPhase.Listening(handsFree = handsFree)
            _effects.trySend(EightBallEffect.ListeningStarted)

            val transcript = MutableStateFlow("")
            val heard = async { hear(settings.language, release, transcript) }
            try {
                // The question ends when the user lets go, or when the recognizer has heard
                // the end of it. A recognizer that gives up empty-handed while the finger is
                // still down changes nothing: the release then asks without a question.
                val early: String? = select {
                    release.onAwait { null }
                    heard.onAwait { it }
                }
                if (early != null && early.isEmpty()) {
                    if (handsFree) {
                        phase.value = before
                        return@launch
                    }
                    release.await()
                }
            } catch (e: CancellationException) {
                phase.value = before
                throw e
            }

            _effects.trySend(EightBallEffect.ListeningStopped)
            answer(settings) {
                // The last words can take a moment to be recognized after the release.
                withTimeoutOrNull(FINAL_TRANSCRIPT_TIMEOUT_MS) { heard.await() }
                    ?: transcript.value.also { heard.cancel() }
            }
        }
    }

    /**
     * Listens until the recognizer is done and returns the question it understood: the final
     * transcript, or the latest partial one when no final one came. Empty when nothing was
     * understood. Publishes the words, to [transcript] and to the screen, and the voice level
     * as they arrive.
     */
    private suspend fun hear(
        language: AppLanguage,
        release: CompletableDeferred<Unit>,
        transcript: MutableStateFlow<String>
    ): String {
        try {
            speechListener.listen(language, release).collect { event ->
                when (event) {
                    is SpeechEvent.Level -> _voiceLevel.value = event.value
                    is SpeechEvent.Transcript -> {
                        transcript.value = event.text.trim()
                        (phase.value as? BallPhase.Listening)?.let {
                            phase.value = it.copy(transcript = transcript.value)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // A recognizer that breaks is a question nobody heard, not an error to show.
        } finally {
            _voiceLevel.value = 0f
        }
        return transcript.value
    }

    /** Stops listening without asking anything and puts the ball back as it was. */
    private fun abandonListening() {
        if (phase.value is BallPhase.Listening) cycleJob?.cancel()
    }

    /**
     * Shows the ball thinking, gets the answer to [awaitQuestion] and reveals it. The thinking
     * lasts at least [QueryConfig.minThinkingMs], so its animation never flashes by.
     */
    private suspend fun answer(settings: UserSettings, awaitQuestion: suspend () -> String) = coroutineScope {
        clearNotice()
        val minimumThinking = launch { delay(queryConfig.minThinkingMs) }
        phase.value = BallPhase.Thinking((phase.value as? BallPhase.Listening)?.transcript.orEmpty())

        val question = awaitQuestion()
        phase.value = BallPhase.Thinking(question)
        val answer = requestAnswer(settings, question)
        minimumThinking.join()

        phase.value = BallPhase.Answered(id = nextAnswerId++, question = question, answer = answer)
        // Outside this question's own job: closing the answer at once must not lose its entry.
        viewModelScope.launch {
            val plain = answer as? AnswerText.Plain
            history.add(question, plain?.value, clock.millis(), plain?.fromAi == true)
        }
        _effects.trySend(EightBallEffect.AnswerRevealed)
    }

    /**
     * Asks for an answer and waits at most [QueryConfig.maxThinkingMs] for it. Whatever goes
     * wrong ends in [AnswerText.Fallback]: the user never sees an error.
     *
     * Privacy: a question the app heard is passed on so the AI service can answer it; the data
     * layer sends it only when AI answers are in use, capped in length. It is kept, with its
     * answer, in the history on this device only.
     * A tap or a shake carries no question, and then only the fixed [ASK_TRIGGER] is sent.
     */
    private suspend fun requestAnswer(settings: UserSettings, question: String): AnswerText {
        val result = try {
            withTimeoutOrNull(queryConfig.maxThinkingMs) {
                sendMessageUseCase(
                    trigger = ASK_TRIGGER,
                    languageCode = settings.language.code,
                    personality = settings.personality,
                    useAi = settings.aiAnswersEnabled,
                    question = question.takeIf { it.isNotBlank() }
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return when (result) {
            is Magic8BallResult.Success ->
                result.message.takeIf { it.isNotBlank() }?.let { AnswerText.Plain(it, result.fromAi) }
            is Magic8BallResult.Failure, null -> null
        } ?: AnswerText.Fallback
    }

    /**
     * Listens to the motion sensor only while the screen is in front of the user and the ball
     * is at rest. After an answer the sensor stays released for [QueryConfig.cooldownMs], so
     * the tail of the shake that asked does not ask again.
     */
    private fun observeShakes() {
        viewModelScope.launch {
            combine(resumed, phase) { resumed, phase ->
                when {
                    !resumed -> null
                    phase is BallPhase.Idle -> 0L
                    phase is BallPhase.Answered -> queryConfig.cooldownMs
                    else -> null
                }
            }
                .distinctUntilChanged()
                .collectLatest { armIn ->
                    if (armIn != null) {
                        delay(armIn)
                        observeShakeUseCase().collect { event ->
                            if (event == ShakeEvent.Started) ask()
                        }
                    }
                }
        }
    }

    private fun giveNoticeOnce(what: Notice) {
        if (!noticesGiven.add(what)) return
        noticeJob?.cancel()
        noticeJob = viewModelScope.launch {
            notice.value = what
            delay(NOTICE_DURATION_MS)
            notice.value = null
        }
    }

    private fun clearNotice() {
        noticeJob?.cancel()
        notice.value = null
    }

    private fun voiceStatus(
        settings: UserSettings,
        speechAvailable: Boolean,
        microphone: MicrophoneAccess
    ): VoiceStatus = when {
        !speechAvailable -> VoiceStatus.Unsupported
        !settings.voiceQuestionsEnabled -> VoiceStatus.Off
        microphone == MicrophoneAccess.Denied -> VoiceStatus.PermissionDenied
        else -> VoiceStatus.Ready
    }

    private enum class MicrophoneAccess { NotAsked, Granted, Denied }

    companion object {
        /** The fixed trigger the watch app sends as well. */
        const val ASK_TRIGGER = "shake"

        /** How long a notice replaces the hint. */
        const val NOTICE_DURATION_MS = 6_000L

        /** How long the last words may take to be recognized once the user has let go. */
        const val FINAL_TRANSCRIPT_TIMEOUT_MS = 1_500L
    }
}
