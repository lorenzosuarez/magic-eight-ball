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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import com.example.magiceightball.core.domain.model.QueryConfig

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _effects = Channel<ChatEffect>()
    val effects = _effects.receiveAsFlow()

    private var shakeCollectionJob: Job? = null

    init {
        startShakeDetection()
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

        _state.update { 
            it.copy(
                machineState = ChatStateMachine.Running,
                shakeStatusRes = R.string.status_shaking,
                titleRes = R.string.title_shaking
            )
        }

        viewModelScope.launch {
            val result = sendMessageUseCase("Give me a prediction")
            
            val answerText = when (result) {
                is com.example.magiceightball.core.domain.model.Magic8BallResult.Success -> result.message
                is com.example.magiceightball.core.domain.model.Magic8BallResult.Failure -> {
                     // Simple error mapping for UI
                     "Ask again" 
                }
            }

            _state.update {
                it.copy(
                    machineState = ChatStateMachine.Completed(answerText),
                    shakeStatusRes = null,
                    titleRes = R.string.title_main
                )
            }

            delay(queryConfig.completionHoldTimeMs)
            
            delay(queryConfig.cooldownMs)

            _state.update { 
                it.copy(machineState = ChatStateMachine.Idle) 
            }
            startShakeDetection()
        }
    }

    fun handleIntent(intent: ChatIntent) {
        // Reserved for future manual interactions
    }
}
