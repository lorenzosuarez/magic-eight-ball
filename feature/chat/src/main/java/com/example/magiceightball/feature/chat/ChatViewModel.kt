package com.example.magiceightball.feature.chat

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
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
    private val queryConfig: QueryConfig,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val shakeDetectionEnabled = MutableStateFlow(false)
    private val language = savedStateHandle.getStateFlow(KEY_LANGUAGE, AppLanguage.ENGLISH)
    private val personality =
        savedStateHandle.getStateFlow(KEY_PERSONALITY, Magic8BallPersonality.SARCASTIC)

    val state: StateFlow<ChatState> = combine(machineState, language, personality, ::chatState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SUBSCRIPTION_TIMEOUT_MS),
            initialValue = chatState(machineState.value, language.value, personality.value)
        )

    private val _effects = Channel<ChatEffect>(Channel.BUFFERED)
    val effects: Flow<ChatEffect> = _effects.receiveAsFlow()

    private var cycleJob: Job? = null

    init {
        observeShakes()
    }

    fun setLanguage(language: AppLanguage) {
        savedStateHandle[KEY_LANGUAGE] = language
    }

    fun setPersonality(personality: Magic8BallPersonality) {
        savedStateHandle[KEY_PERSONALITY] = personality
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

        cycleJob = viewModelScope.launch {
            try {
                machineState.value = ChatStateMachine.Running
                _effects.trySend(ChatEffect.ShakeDetected)

                val answer = fetchAnswer()

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
    private suspend fun fetchAnswer(): AnswerText = coroutineScope {
        val language = language.value
        val personality = personality.value

        val request = async { requestAnswer(language, personality) }
        delay(queryConfig.minThinkingMs)

        when (val result = request.await()) {
            is Magic8BallResult.Success ->
                result.message.takeIf { it.isNotBlank() }?.let(AnswerText::Plain)
            is Magic8BallResult.Failure, null -> null
        } ?: AnswerText.Resource(fallbackAnswerRes(language))
    }

    /** Returns null when the request times out or fails unexpectedly. */
    private suspend fun requestAnswer(
        language: AppLanguage,
        personality: Magic8BallPersonality
    ): Magic8BallResult? = try {
        withTimeoutOrNull(queryConfig.maxThinkingMs) {
            sendMessageUseCase(
                trigger = SHAKE_TRIGGER,
                languageCode = language.code,
                personality = personality
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_PERSONALITY = "personality"
        const val SHAKE_TRIGGER = "shake"
        const val STATE_SUBSCRIPTION_TIMEOUT_MS = 5_000L

        fun chatState(
            machine: ChatStateMachine,
            language: AppLanguage,
            personality: Magic8BallPersonality
        ) = ChatState(
            machineState = machine,
            titleRes = when (language) {
                AppLanguage.ENGLISH -> R.string.title_main
                AppLanguage.SPANISH -> R.string.title_main_es
            },
            language = language,
            personality = personality
        )

        @StringRes
        fun fallbackAnswerRes(language: AppLanguage): Int = when (language) {
            AppLanguage.ENGLISH -> R.string.answer_fallback
            AppLanguage.SPANISH -> R.string.answer_fallback_es
        }
    }
}
