package com.example.magiceightball.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.common.Result
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
// import com.example.magiceightball.feature.chat.ChatState // If it's in a separate file

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val _machineState = MutableStateFlow<ChatStateMachine>(ChatStateMachine.Idle)
    private val _languageCode = MutableStateFlow("en") // Default to English

    val state: StateFlow<ChatState> = combine(
        _machineState,
        _languageCode
    ) { machine, lang ->
        val title = if (lang == "es") R.string.title_main_es else R.string.title_main
        
        ChatState(
            machineState = machine,
            titleRes = title,
            languageCode = lang,
            shakeStatusRes = if (machine is ChatStateMachine.Running) R.string.title_shaking else null // Simplified
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

    fun setLanguage(code: String) {
        _languageCode.value = code
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

        // Transition to Running
        _machineState.update { ChatStateMachine.Running }

        viewModelScope.launch {
            // Trigger UseCase with language
            val result = sendMessageUseCase(
                trigger = "shake",
                languageCode = _languageCode.value
            )
            
            val answerText = when (result) {
                is Magic8BallResult.Success -> result.message
                is Magic8BallResult.Failure -> "Ask again"
            }

            // Transition to Completed
            _machineState.update { ChatStateMachine.Completed(answerText) }

            delay(queryConfig.completionHoldTimeMs)
            
            delay(queryConfig.cooldownMs)

            // Back to Idle
            _machineState.update { ChatStateMachine.Idle }
            startShakeDetection()
        }
    }

    fun handleIntent(intent: ChatIntent) {
        // Reserved for future manual interactions
    }
}
