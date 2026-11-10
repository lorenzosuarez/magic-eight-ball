package com.example.magiceightball.feature.chat

import androidx.lifecycle.SavedStateHandle
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val queryConfig = QueryConfig(
        minThinkingMs = MIN_THINKING_MS,
        maxThinkingMs = MAX_THINKING_MS,
        completionHoldTimeMs = HOLD_MS,
        cooldownMs = COOLDOWN_MS
    )

    private val motionGateway = FakeMotionGateway()
    private val llmGateway = FakeLlmGateway()

    @Test
    fun `initial state is idle with the english title`() = runTest {
        val viewModel = createViewModel()

        assertEquals(ChatState(), viewModel.state.value)
        assertEquals(R.string.title_main, viewModel.state.value.titleRes)
    }

    @Test
    fun `sensor is released until shake detection is enabled`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, motionGateway.subscribers)

        viewModel.setShakeDetectionEnabled(true)
        assertEquals(1, motionGateway.subscribers)

        viewModel.setShakeDetectionEnabled(false)
        assertEquals(0, motionGateway.subscribers)
    }

    @Test
    fun `an instant answer is still revealed only after the minimum thinking time`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)

        advanceTimeBy(MIN_THINKING_MS - 1)
        assertEquals(ChatStateMachine.Running, viewModel.machineState)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )
    }

    @Test
    fun `answer is held and then the machine returns to idle and listens again`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(MIN_THINKING_MS + HOLD_MS + COOLDOWN_MS - 1)
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )

        advanceTimeBy(1)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(1, motionGateway.subscribers)
    }

    @Test
    fun `a second cycle can run after the first one`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        repeat(2) {
            shake()
            assertEquals(ChatStateMachine.Running, viewModel.machineState)
            advanceTimeBy(MIN_THINKING_MS + HOLD_MS + COOLDOWN_MS)
            runCurrent()
            assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        }
        assertEquals(2, llmGateway.requests.size)
    }

    @Test
    fun `stop events do not start a cycle`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        motionGateway.events.emit(ShakeEvent.Stopped)
        runCurrent()

        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `a slow answer is replaced by the localized fallback at the maximum thinking time`() =
        runTest {
            llmGateway.answer = { awaitCancellation() }
            val viewModel = createViewModel(detectionEnabled = true)
            viewModel.setLanguage(AppLanguage.SPANISH)

            shake()
            advanceTimeBy(MAX_THINKING_MS - 1)
            assertEquals(ChatStateMachine.Running, viewModel.machineState)

            advanceTimeBy(1)
            runCurrent()
            assertEquals(
                ChatStateMachine.Completed(AnswerText.Resource(R.string.answer_fallback_es)),
                viewModel.machineState
            )
        }

    @Test
    fun `a failed answer shows the fallback`() = runTest {
        llmGateway.answer = { Magic8BallResult.Failure(LlmError.Network) }
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()

        assertEquals(
            ChatStateMachine.Completed(AnswerText.Resource(R.string.answer_fallback)),
            viewModel.machineState
        )
    }

    @Test
    fun `an unexpected error shows the fallback and the cycle still ends in idle`() = runTest {
        llmGateway.answer = { error("boom") }
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Resource(R.string.answer_fallback)),
            viewModel.machineState
        )

        advanceTimeBy(HOLD_MS + COOLDOWN_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
    }

    @Test
    fun `disabling detection abandons the running cycle`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        viewModel.setShakeDetectionEnabled(false)
        runCurrent()

        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(MIN_THINKING_MS + HOLD_MS + COOLDOWN_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
    }

    @Test
    fun `request carries the selected language and personality`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)
        viewModel.setLanguage(AppLanguage.SPANISH)
        viewModel.setPersonality(Magic8BallPersonality.MYSTICAL)

        shake()

        val request = llmGateway.requests.single()
        assertEquals("es", request.languageCode)
        assertEquals(Magic8BallPersonality.MYSTICAL, request.personality)
        runCurrent()
        assertEquals(R.string.title_main_es, viewModel.state.value.titleRes)
    }

    @Test
    fun `effects announce the shake and then the answer`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)
        val effects = mutableListOf<ChatEffect>()
        backgroundScope.launch { viewModel.effects.collect(effects::add) }

        shake()
        assertEquals(listOf<ChatEffect>(ChatEffect.ShakeDetected), effects)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(listOf(ChatEffect.ShakeDetected, ChatEffect.AnswerRevealed), effects)
    }

    /** Delivers a shake and lets the resulting state reach the subscribers. */
    private suspend fun TestScope.shake() {
        motionGateway.events.emit(ShakeEvent.Started)
        runCurrent()
    }

    private val ChatViewModel.machineState: ChatStateMachine
        get() = state.value.machineState

    /** Creates the ViewModel with an active subscriber, as the screen would be. */
    private fun TestScope.createViewModel(detectionEnabled: Boolean = false): ChatViewModel {
        val viewModel = ChatViewModel(
            sendMessageUseCase = SendMessageUseCase(llmGateway),
            observeShakeUseCase = ObserveShakeUseCase(motionGateway),
            queryConfig = queryConfig,
            savedStateHandle = SavedStateHandle()
        )
        backgroundScope.launch { viewModel.state.collect {} }
        viewModel.setShakeDetectionEnabled(detectionEnabled)
        return viewModel
    }

    private class FakeMotionGateway : MotionGateway {
        val events = MutableSharedFlow<ShakeEvent>()
        override val shakeEvents = events

        val subscribers: Int get() = events.subscriptionCount.value
    }

    private class FakeLlmGateway : LlmGateway {
        val requests = mutableListOf<Magic8BallRequest>()
        var answer: suspend () -> Magic8BallResult = { Magic8BallResult.Success(ANSWER) }

        override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
            requests += request
            return answer()
        }

        companion object {
            const val ANSWER = "It is certain"
        }
    }

    private companion object {
        const val MIN_THINKING_MS = 1_000L
        const val MAX_THINKING_MS = 5_000L
        const val HOLD_MS = 3_000L
        const val COOLDOWN_MS = 1_000L
    }
}
