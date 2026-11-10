package com.example.magiceightball.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
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
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    aiAnswersAvailability: AiAnswersAvailability,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val shakeDetectionEnabled = MutableStateFlow(false)

    /** The saved choices; null until they have been read, right after launch. */
    private val settings: StateFlow<UserSettings?> = userSettingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Null until the saved choices have been read, so the screen never shows a default that is
     * about to be replaced. Kept up to date without subscribers: the activity reads it to hold
     * the splash screen until then.
     */
    val state: StateFlow<ChatState?> =
        combine(machineState, settings, aiAnswersAvailability.isAvailable) { machine, saved, aiAvailable ->
            saved?.let { ChatState(machine, it, aiAvailable) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects: Flow<ChatEffect> = _effects.receiveAsFlow()

    private var cycleJob: Job? = null

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
        if (!enabled) cycleJob?.cancel()
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
                machineState.value = ChatStateMachine.Running
                _effects.trySend(ChatEffect.ShakeDetected)

                val answer = fetchAnswer(settings)

                machineState.value = ChatStateMachine.Completed(answer)
                _effects.trySend(ChatEffect.AnswerRevealed)

                delay(queryConfig.completionHoldTimeMs + queryConfig.cooldownMs)
            } finally {
                machineState.value = ChatStateMachine.Idle
            }
        }
    }

    /**
     * Requests an answer and waits at least [QueryConfig.minThinkingMs], so the thinking
     * animation never flashes by, and at most [QueryConfig.maxThinkingMs] for the request.
     */
    private suspend fun fetchAnswer(settings: UserSettings): AnswerText = coroutineScope {
        val request = async { requestAnswer(settings) }
        delay(queryConfig.minThinkingMs)

        when (val result = request.await()) {
            is Magic8BallResult.Success ->
                result.message.takeIf { it.isNotBlank() }?.let(AnswerText::Plain)
            is Magic8BallResult.Failure, null -> null
        } ?: AnswerText.Fallback
    }

    /** Returns null when the request times out or fails unexpectedly. */
    private suspend fun requestAnswer(settings: UserSettings): Magic8BallResult? = try {
        withTimeoutOrNull(queryConfig.maxThinkingMs) {
            sendMessageUseCase(
                trigger = SHAKE_TRIGGER,
                languageCode = settings.language.code,
                personality = settings.personality,
                useAi = settings.aiAnswersEnabled
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val SHAKE_TRIGGER = "shake"
    }
}
