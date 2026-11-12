package com.example.magiceightball.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.gateway.AnswerReports
import com.example.magiceightball.core.domain.gateway.SpeechEvent
import com.example.magiceightball.core.domain.gateway.SpeechListener
import com.example.magiceightball.core.domain.model.asSpokenQuestion
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
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
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    aiAnswersAvailability: AiAnswersAvailability,
    private val speechListener: SpeechListener,
    private val answerReports: AnswerReports,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val shakeDetectionEnabled = MutableStateFlow(false)
    private val speechAvailable = MutableStateFlow(speechListener.isAvailable())
    private val microphone = MutableStateFlow(MicrophoneAccess.NotAsked)
    private val aiAllowanceSpent = MutableStateFlow(false)

    /** A watch that was refused the microphone listens no more than one without a recognizer. */
    private val canListen = combine(speechAvailable, microphone) { speech, access ->
        speech && access != MicrophoneAccess.Denied
    }

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
        aiAllowanceSpent,
        canListen
    ) { machine, saved, aiAvailable, aiSpent, listens ->
        saved?.let {
            ChatState(
                machineState = machine,
                settings = it,
                aiAnswersAvailable = aiAvailable,
                aiAllowanceSpent = aiSpent,
                canListen = listens
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _voiceLevel = MutableStateFlow(0f)

    /**
     * How loud the voice is while listening, from 0 to 1. Kept out of [state] because it changes
     * many times a second and only ever drives a drawing.
     */
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects: Flow<ChatEffect> = _effects.receiveAsFlow()

    private var cycleJob: Job? = null

    /** Completed when the user lets go of the screen while the app is listening. */
    private var released: CompletableDeferred<Unit>? = null
    private var awaitingPermission = false

    /** The choices the answer on screen was given for: what a report of it says. */
    private var answeredFor: UserSettings? = null

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
     * Turning it off also abandons a question in progress, so nothing runs or changes unseen.
     * An answer already given keeps its time: it is what the user comes back to read.
     */
    fun setShakeDetectionEnabled(enabled: Boolean) {
        shakeDetectionEnabled.value = enabled
        if (enabled) {
            speechAvailable.value = speechListener.isAvailable()
        } else if (isBusy()) {
            cycleJob?.cancel()
        }
    }

    /** Reports what the system says about the microphone permission, outside of a request. */
    fun onMicrophonePermissionChecked(granted: Boolean) {
        // A request whose answer never came must not leave every later hold waiting for it.
        awaitingPermission = false
        microphone.value = when {
            granted -> MicrophoneAccess.Granted
            // Taken away in the system settings: it can be asked for again.
            microphone.value == MicrophoneAccess.Granted -> MicrophoneAccess.NotAsked
            else -> microphone.value
        }
    }

    /**
     * Reports the user's answer to the permission request. No is taken at its word: the app
     * stops offering to listen and does not ask again, and a press asks as a tap does.
     */
    fun onMicrophonePermissionResult(granted: Boolean) {
        awaitingPermission = false
        microphone.value = if (granted) MicrophoneAccess.Granted else MicrophoneAccess.Denied
    }

    /** A short press on the ball asks, as a shake does. */
    fun onTap() = startCycle()

    /**
     * The finger has stayed on the screen: listen to the question, where the app may. Without
     * the microphone the first hold asks for it instead, and the next one listens.
     */
    fun onHoldStart() {
        val state = state.value ?: return
        if (!state.canAskByVoice || awaitingPermission || isBusy()) return
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
        val listening = machineState.value as? ChatStateMachine.Listening ?: return
        // A question that has already been heard to its end is not undone by a later touch.
        if (!listening.finished) cycleJob?.cancel()
    }

    /**
     * The user says the answer on screen should not have been given. Only what the AI service
     * wrote can be reported: the app's own answers were read before they were shipped.
     */
    fun reportAnswer() {
        val shown = machineState.value as? ChatStateMachine.Completed ?: return
        val said = shown.answer as? AnswerText.Plain ?: return
        val settings = answeredFor ?: return
        if (!said.fromAi || shown.reported) return
        answerReports.report(said.value, remark = null, settings.personality, settings.language.code)
        machineState.value = shown.copy(reported = true)
    }

    /**
     * Listens to the motion sensor only while detection is enabled and the ball is at rest or
     * showing an answer; the sensor is released while a question is in progress. After an
     * answer it stays released for [QueryConfig.cooldownMs], so the tail of the shake that
     * asked does not ask again.
     */
    private fun observeShakes() {
        viewModelScope.launch {
            combine(shakeDetectionEnabled, machineState) { enabled, machine ->
                when {
                    !enabled -> null
                    machine is ChatStateMachine.Idle -> 0L
                    machine is ChatStateMachine.Completed -> queryConfig.cooldownMs
                    else -> null
                }
            }
                .distinctUntilChanged()
                .collectLatest { armIn ->
                    if (armIn != null) {
                        delay(armIn)
                        observeShakeUseCase().collect { event ->
                            if (event == ShakeEvent.Started) startCycle()
                        }
                    }
                }
        }
    }

    /** A question is in progress. An answer on screen is not one: it gives way to the next. */
    private fun isBusy(): Boolean =
        cycleJob?.isActive == true && machineState.value !is ChatStateMachine.Completed

    /**
     * Runs [cycle] as the one thing the ball is doing, unless a question is in progress, and
     * puts the ball back at rest when it is over or abandoned.
     */
    private fun launchCycle(cycle: suspend CoroutineScope.() -> Unit) {
        if (isBusy()) return
        val shown = cycleJob

        cycleJob = viewModelScope.launch {
            // The answer on screen has had its time: its cycle ends before this one shows anything.
            shown?.cancelAndJoin()
            try {
                cycle()
            } finally {
                machineState.value = ChatStateMachine.Idle
            }
        }
    }

    private fun startCycle() {
        // No answer can be asked for before the choices it depends on are known.
        val settings = settings.value ?: return

        launchCycle {
            _effects.trySend(ChatEffect.ShakeDetected)
            answer(settings, question = null)
        }
    }

    /**
     * Listens for as long as the screen is held, then answers what was heard. A hold in which
     * nothing was understood asks nothing: the ball goes back to rest.
     */
    private fun listenAndAnswer(settings: UserSettings) {
        val release = CompletableDeferred<Unit>()
        released = release

        launchCycle {
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
            // Whatever was said to the ball is asked of it: see [asSpokenQuestion].
            val question = (
                withTimeoutOrNull(FINAL_TRANSCRIPT_TIMEOUT_MS) { heard.await() }
                    ?: transcript.value.also { heard.cancel() }
                ).asSpokenQuestion(settings.language.code)

            if (question.isNotBlank()) {
                // Whether or not the words showed up as they were said, the question is
                // shown whole before the ball starts on it.
                machineState.value = ChatStateMachine.Listening(question, finished = true)
                delay(HEARD_QUESTION_MS)
                answer(settings, question)
            } else {
                _effects.trySend(ChatEffect.NothingHeard)
            }
        }
    }

    /**
     * Listens until the recognizer is done and returns the question it understood, empty when
     * it understood nothing. Publishes the words, to [transcript] and to the screen, and the
     * voice level as they arrive.
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
                        val listening = machineState.value as? ChatStateMachine.Listening
                        if (listening != null && !listening.finished) {
                            machineState.value = ChatStateMachine.Listening(transcript.value)
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

    /**
     * Thinks, reveals the answer and leaves it on screen for [ANSWER_SHOWN_MS], unless the next
     * shake, tap or question comes first.
     */
    private suspend fun answer(settings: UserSettings, question: String?) {
        machineState.value = ChatStateMachine.Running

        val answer = fetchAnswer(settings, question)

        answeredFor = settings
        machineState.value = ChatStateMachine.Completed(answer)
        _effects.trySend(ChatEffect.AnswerRevealed)

        delay(ANSWER_SHOWN_MS)
    }

    /**
     * Requests an answer and waits at least [QueryConfig.minThinkingMs], so the thinking
     * animation never flashes by, and at most [QueryConfig.maxThinkingMs] for the request.
     */
    private suspend fun fetchAnswer(settings: UserSettings, question: String?): AnswerText = coroutineScope {
        val request = async { requestAnswer(settings, question) }
        delay(queryConfig.minThinkingMs)

        when (val result = request.await()) {
            is Magic8BallResult.Success -> {
                aiAllowanceSpent.value = result.aiAllowanceSpent
                result.message.takeIf { it.isNotBlank() }?.let { AnswerText.Plain(it, result.fromAi) }
            }
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

        /** How long the question that was understood stays on screen before the ball thinks. */
        const val HEARD_QUESTION_MS = 1_200L

        /**
         * How long an answer stays on the watch: time for the wrist to come back from the shake
         * and for the die to stop rocking, and then to read it.
         */
        const val ANSWER_SHOWN_MS = 8_000L
    }
}
