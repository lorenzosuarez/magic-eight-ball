package com.example.magiceightball.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val _machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    private val _personality = MutableStateFlow(Magic8BallPersonality.SARCASTIC)

    val state: StateFlow<ChatState> = combine(
        _machineState,
        _language,
        _personality
    ) { machine, lang, personality ->
        val title = if (lang == AppLanguage.SPANISH) R.string.title_main_es else R.string.title_main
        
        ChatState(
            machineState = machine,
            titleRes = title,
            language = lang,
            personality = personality,
            shakeStatusRes = if (machine is ChatStateMachine.Running) R.string.title_shaking else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatState()
    )

    private val _effects = Channel<ChatEffect>()
    val effects = _effects.receiveAsFlow()

    private var shakeCollectionJob: Job? = null

    init {
        startShakeDetection()
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
    }

    fun setPersonality(personality: Magic8BallPersonality) {
        _personality.value = personality
    }

    private fun startShakeDetection() {
        if (shakeCollectionJob?.isActive == true) return

        shakeCollectionJob = viewModelScope.launch {
            observeShakeUseCase()
                .collect { event ->
                    if (event == ShakeEvent.Started) {
                        onShakeStarted()
                    }
                }
        }
    }

    private fun stopShakeDetection() {
        shakeCollectionJob?.cancel()
        shakeCollectionJob = null
    }

    /**
     * Orchestrates the shake-to-query state machine.
     * Transitions: Idle -> Running -> Completed -> Idle.
     * Gates sensor detection during active processing.
     */
    private fun onShakeStarted() {
        stopShakeDetection()

        _machineState.update { ChatStateMachine.Running }

        viewModelScope.launch {
            val result = sendMessageUseCase(
                trigger = "shake",
                languageCode = _language.value.code,
                personality = _personality.value
            )
            
            val answerText = when (result) {
                is Magic8BallResult.Success -> result.message
                is Magic8BallResult.Failure -> "Ask again"
            }

            _machineState.update { ChatStateMachine.Completed(answerText) }

            delay(queryConfig.completionHoldTimeMs)
            
            delay(queryConfig.cooldownMs)

            _machineState.update { ChatStateMachine.Idle }
            startShakeDetection()
        }
    }

    fun handleIntent(intent: ChatIntent) {
    }
}
