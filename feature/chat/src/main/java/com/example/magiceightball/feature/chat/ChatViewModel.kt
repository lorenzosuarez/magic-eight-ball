package com.example.magiceightball.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.gateway.SpeechEvent
import com.example.magiceightball.core.domain.gateway.SpeechListener
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.model.UserSettings
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    aiAnswersAvailability: AiAnswersAvailability,
    private val speechListener: SpeechListener,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val shakeDetectionEnabled = MutableStateFlow(false)
    private val speechAvailable = MutableStateFlow(speechListener.isAvailable())
    private val microphone = MutableStateFlow(MicrophoneAccess.NotAsked)

    /** The saved choices; null until they have been read, right after launch. */
    private val settings: StateFlow<UserSettings?> = userSettingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Null until the saved choices have been read, so the screen never shows a default that is
     * about to be replaced. Kept up to date without subscribers: the activity reads it to hold
     * the splash screen until then.
     */
    val state: StateFlow<ChatState?> = combine(
        machineState,
        settings,
        aiAnswersAvailability.isAvailable,
        speechAvailable,
        microphone
    ) { machine, saved, aiAvailable, speech, microphone ->
        saved?.let {
            ChatState(
                machineState = machine,
                settings = it,
                aiAnswersAvailable = aiAvailable,
                // A question is only worth hearing when the AI service is there to answer it.
                canAskByVoice = it.aiAnswersEnabled && aiAvailable && speech &&
                    microphone != MicrophoneAccess.Denied
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects: Flow<ChatEffect> = _effects.receiveAsFlow()

    private var cycleJob: Job? = null

    /** Completed when the user lets go of the screen while the app is listening. */
    private var released: CompletableDeferred<Unit>? = null
    private var awaitingPermission = false

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

    /**
     * Turns shake detection on while the prediction screen is the one the user is looking at.
     * Turning it off also abandons a cycle in progress, so nothing runs or changes unseen.
     */
    fun setShakeDetectionEnabled(enabled: Boolean) {
        shakeDetectionEnabled.value = enabled
        if (enabled) {
            speechAvailable.value = speechListener.isAvailable()
        } else {
            cycleJob?.cancel()
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

    /** Reports the user's answer to the permission request. Never blocks: no means shake. */
    fun onMicrophonePermissionResult(granted: Boolean) {
        awaitingPermission = false
        microphone.value = if (granted) MicrophoneAccess.Granted else MicrophoneAccess.Denied
    }

    /**
     * The finger has stayed on the screen: listen to the question, where the app may. The
     * first hold asks for the microphone instead, and the next one listens.
     */
    fun onHoldStart() {
        val state = state.value ?: return
        if (!state.canAskByVoice || awaitingPermission || cycleJob?.isActive == true) return
        if (microphone.value == MicrophoneAccess.Granted) {
            listenAndAnswer(state.settings)
        } else {
            awaitingPermission = true
            _effects.trySend(ChatEffect.RequestMicrophonePermission)
        }
    }

    /** The finger has left the screen: the question, if one was being listened to, is over. */
    fun onHoldEnd() {
        released?.complete(Unit)
    }

    /** The hold was taken away rather than ended, by a swipe for instance: nothing is asked. */
    fun onHoldCancel() {
        if (machineState.value is ChatStateMachine.Listening) cycleJob?.cancel()
    }

    /**
     * Listens to the motion sensor only while detection is enabled and the machine is idle;
     * the sensor is released for the whole duration of a cycle.
     */
    private fun observeShakes() {
        viewModelScope.launch {
            combine(shakeDetectionEnabled, machineState) { enabled, machine ->
                enabled && machine is ChatStateMachine.Idle
            }
                .distinctUntilChanged()
                .collectLatest { armed ->
                    if (armed) {
                        observeShakeUseCase().collect { event ->
                            if (event == ShakeEvent.Started) startCycle()
                        }
                    }
                }
        }
    }

    private fun startCycle() {
        if (cycleJob?.isActive == true) return
        // No answer can be asked for before the choices it depends on are known.
        val settings = settings.value ?: return

        cycleJob = viewModelScope.launch {
            try {
                _effects.trySend(ChatEffect.ShakeDetected)
                answer(settings, question = null)
            } finally {
                machineState.value = ChatStateMachine.Idle
            }
        }
    }

    /**
     * Listens for as long as the screen is held, then answers what was heard. A hold in which
     * nothing was understood asks nothing: the ball goes back to rest.
     */
    private fun listenAndAnswer(settings: UserSettings) {
        val release = CompletableDeferred<Unit>()
        released = release

        cycleJob = viewModelScope.launch {
            try {
                machineState.value = ChatStateMachine.Listening()
                _effects.trySend(ChatEffect.ListeningStarted)

                val transcript = MutableStateFlow("")
                val heard = async { hear(settings.language, release, transcript) }
                // The question ends when the user lets go, or when the recognizer has heard the
                // end of it; the last words can take a moment to be recognized after that.
                select {
                    release.onAwait { }
                    heard.onAwait { }
                }
                _effects.trySend(ChatEffect.ListeningStopped)
                val question = withTimeoutOrNull(FINAL_TRANSCRIPT_TIMEOUT_MS) { heard.await() }
                    ?: transcript.value.also { heard.cancel() }

                if (question.isNotBlank()) answer(settings, question)
            } finally {
                machineState.value = ChatStateMachine.Idle
            }
        }
    }

    /**
     * Listens until the recognizer is done and returns the question it understood, empty when
     * it understood nothing. Publishes the words, to [transcript] and to the screen, as they
     * arrive.
     */
    private suspend fun hear(
        language: AppLanguage,
        release: CompletableDeferred<Unit>,
        transcript: MutableStateFlow<String>
    ): String {
        try {
            speechListener.listen(language, release).collect { event ->
                if (event is SpeechEvent.Transcript) {
                    transcript.value = event.text.trim()
                    if (machineState.value is ChatStateMachine.Listening) {
                        machineState.value = ChatStateMachine.Listening(transcript.value)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // A recognizer that breaks is a question nobody heard, not an error to show.
        }
        return transcript.value
    }

    /** Thinks, reveals the answer and leaves it on screen for a while. */
    private suspend fun answer(settings: UserSettings, question: String?) {
        machineState.value = ChatStateMachine.Running

        val answer = fetchAnswer(settings, question)

        machineState.value = ChatStateMachine.Completed(answer)
        _effects.trySend(ChatEffect.AnswerRevealed)

        delay(queryConfig.completionHoldTimeMs + queryConfig.cooldownMs)
    }

    /**
     * Requests an answer and waits at least [QueryConfig.minThinkingMs], so the thinking
     * animation never flashes by, and at most [QueryConfig.maxThinkingMs] for the request.
     */
    private suspend fun fetchAnswer(settings: UserSettings, question: String?): AnswerText = coroutineScope {
        val request = async { requestAnswer(settings, question) }
        delay(queryConfig.minThinkingMs)

        when (val result = request.await()) {
            is Magic8BallResult.Success ->
                result.message.takeIf { it.isNotBlank() }?.let(AnswerText::Plain)
            is Magic8BallResult.Failure, null -> null
        } ?: AnswerText.Fallback
    }

    /**
     * Returns null when the request times out or fails unexpectedly.
     *
     * Privacy: a question the watch heard is passed on so the AI service can answer it; the
     * data layer sends it only when AI answers are in use, capped in length, and the watch
     * stores it nowhere. A shake carries no question, and then only [SHAKE_TRIGGER] is sent.
     */
    private suspend fun requestAnswer(settings: UserSettings, question: String?): Magic8BallResult? = try {
        withTimeoutOrNull(queryConfig.maxThinkingMs) {
            sendMessageUseCase(
                trigger = SHAKE_TRIGGER,
                languageCode = settings.language.code,
                personality = settings.personality,
                useAi = settings.aiAnswersEnabled,
                question = question
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private enum class MicrophoneAccess { NotAsked, Granted, Denied }

    private companion object {
        const val SHAKE_TRIGGER = "shake"

        /** How long the last words may take to be recognized once the user has let go. */
        const val FINAL_TRANSCRIPT_TIMEOUT_MS = 1_500L
    }
}
