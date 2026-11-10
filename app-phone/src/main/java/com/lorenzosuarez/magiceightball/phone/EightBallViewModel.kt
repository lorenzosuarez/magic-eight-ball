package com.lorenzosuarez.magiceightball.phone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class EightBallViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeShakeUseCase: ObserveShakeUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    private val queryConfig: QueryConfig
) : ViewModel() {

    private val _state = MutableStateFlow(EightBallUiState())
    val state: StateFlow<EightBallUiState> = _state.asStateFlow()

    /** The text field's content. Snapshot state, so typing is never a frame behind the keyboard. */
    var question by mutableStateOf("")
        private set

    private val _effects = Channel<EightBallEffect>(Channel.BUFFERED)
    val effects: Flow<EightBallEffect> = _effects.receiveAsFlow()

    private val shakeDetectionEnabled = MutableStateFlow(false)
    private var cycleJob: Job? = null
    private var nextEntryId = 0L

    init {
        observePersonality()
        observeShakes()
    }

    fun onQuestionChange(value: String) {
        question = value.take(MAX_QUESTION_LENGTH)
    }

    fun setPersonality(personality: Magic8BallPersonality) {
        viewModelScope.launch { userSettingsRepository.setPersonality(personality) }
    }

    /** Turns shake detection on while the screen is the one the user is looking at. */
    fun setShakeDetectionEnabled(enabled: Boolean) {
        shakeDetectionEnabled.value = enabled
    }

    /**
     * Asks the ball, whether by a tap, the keyboard action or a shake. Ignored while an answer is
     * on its way and for a short moment after it is revealed, so one gesture is one answer.
     */
    fun ask() {
        if (cycleJob?.isActive == true) return
        val asked = question.trim()
        question = ""

        cycleJob = viewModelScope.launch {
            _state.update { it.copy(phase = BallPhase.Thinking) }
            _effects.trySend(EightBallEffect.AskStarted)

            val entry = HistoryEntry(
                id = nextEntryId++,
                question = asked,
                answer = fetchAnswer(_state.value.personality)
            )
            _state.update {
                it.copy(
                    phase = BallPhase.Answered(entry),
                    history = (listOf(entry) + it.history).take(EightBallUiState.MAX_ENTRIES)
                )
            }
            _effects.trySend(EightBallEffect.AnswerRevealed)

            delay(queryConfig.cooldownMs)
        }
    }

    private fun observePersonality() {
        viewModelScope.launch {
            userSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(personality = settings.personality) }
            }
        }
    }

    /**
     * Listens to the motion sensor only while detection is enabled and the ball is not thinking;
     * the sensor is released while an answer is on its way.
     */
    private fun observeShakes() {
        viewModelScope.launch {
            combine(shakeDetectionEnabled, _state) { enabled, state ->
                enabled && state.phase !is BallPhase.Thinking
            }
                .distinctUntilChanged()
                .collectLatest { armed ->
                    if (armed) {
                        observeShakeUseCase().collect { event ->
                            if (event == ShakeEvent.Started) ask()
                        }
                    }
                }
        }
    }

    /**
     * Requests an answer and waits at least [QueryConfig.minThinkingMs], so the thinking
     * animation never flashes by, and at most [QueryConfig.maxThinkingMs] for the request.
     * Whatever goes wrong ends in [AnswerText.Fallback]: the user never sees an error.
     */
    private suspend fun fetchAnswer(personality: Magic8BallPersonality): AnswerText = coroutineScope {
        val request = async { requestAnswer(personality) }
        delay(queryConfig.minThinkingMs)

        when (val result = request.await()) {
            is Magic8BallResult.Success ->
                result.message.takeIf { it.isNotBlank() }?.let(AnswerText::Plain)
            is Magic8BallResult.Failure, null -> null
        } ?: AnswerText.Fallback
    }

    /**
     * Builds the request for an answer. Returns null when it times out or fails unexpectedly.
     *
     * Privacy: the question the user typed is NOT part of the request and never leaves the
     * device; it is only shown on screen and in the in-memory history. The request carries the
     * same fixed [ASK_TRIGGER] as the watch app, so Gemini (when it is switched on) receives
     * nothing the user wrote.
     */
    private suspend fun requestAnswer(personality: Magic8BallPersonality): Magic8BallResult? = try {
        withTimeoutOrNull(queryConfig.maxThinkingMs) {
            sendMessageUseCase(
                trigger = ASK_TRIGGER,
                languageCode = AppLanguage.ENGLISH.code,
                personality = personality,
                useAi = true
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    companion object {
        const val MAX_QUESTION_LENGTH = 120

        /** The fixed trigger the watch app sends as well. */
        const val ASK_TRIGGER = "shake"
    }
}
