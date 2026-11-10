package com.lorenzosuarez.magiceightball.phone

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.model.UserSettings
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EightBallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val queryConfig = QueryConfig(
        minThinkingMs = MIN_THINKING_MS,
        maxThinkingMs = MAX_THINKING_MS,
        cooldownMs = COOLDOWN_MS
    )

    private val motionGateway = FakeMotionGateway()
    private val llmGateway = FakeLlmGateway()
    private val userSettings = FakeUserSettingsRepository()

    @Test
    fun `initial state is idle with the saved personality and no history`() = runTest {
        userSettings.saved.value = UserSettings(personality = Magic8BallPersonality.MYSTICAL)

        val viewModel = createViewModel()

        assertEquals(
            EightBallUiState(BallPhase.Idle, Magic8BallPersonality.MYSTICAL, emptyList()),
            viewModel.state.value
        )
    }

    @Test
    fun `a tap thinks for the minimum time and then reveals the answer`() = runTest {
        val viewModel = createViewModel()

        viewModel.ask()
        assertEquals(BallPhase.Thinking, viewModel.state.value.phase)

        advanceTimeBy(MIN_THINKING_MS - 1)
        assertEquals(BallPhase.Thinking, viewModel.state.value.phase)

        advanceTimeBy(1)
        runCurrent()
        val expected = HistoryEntry(id = 0, question = "", answer = AnswerText.Plain(FakeLlmGateway.ANSWER))
        assertEquals(BallPhase.Answered(expected), viewModel.state.value.phase)
        assertEquals(listOf(expected), viewModel.state.value.history)
    }

    @Test
    fun `a tap answers even while shake detection is off`() = runTest {
        val viewModel = createViewModel(detectionEnabled = false)

        viewModel.ask()
        finishCycle()

        assertEquals(1, viewModel.state.value.history.size)
    }

    @Test
    fun `a shake produces an answer just like a tap`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        assertEquals(BallPhase.Thinking, viewModel.state.value.phase)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            AnswerText.Plain(FakeLlmGateway.ANSWER),
            viewModel.state.value.history.single().answer
        )
    }

    @Test
    fun `the sensor is used only while detection is enabled and the ball is not thinking`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, motionGateway.subscribers)

        viewModel.setShakeDetectionEnabled(true)
        assertEquals(1, motionGateway.subscribers)

        shake()
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(1, motionGateway.subscribers)

        viewModel.setShakeDetectionEnabled(false)
        assertEquals(0, motionGateway.subscribers)
    }

    @Test
    fun `leaving the screen while thinking still delivers the answer`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        viewModel.ask()
        viewModel.setShakeDetectionEnabled(false)
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()

        assertEquals(1, viewModel.state.value.history.size)
    }

    @Test
    fun `stop events do not ask the ball`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        motionGateway.events.emit(ShakeEvent.Stopped)
        runCurrent()

        assertEquals(BallPhase.Idle, viewModel.state.value.phase)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `asking again is ignored while thinking and during the cooldown`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        viewModel.ask()
        viewModel.ask()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()

        viewModel.ask()
        shake()
        advanceTimeBy(COOLDOWN_MS - 1)
        assertEquals(1, llmGateway.requests.size)

        advanceTimeBy(1)
        runCurrent()
        viewModel.ask()
        assertEquals(2, llmGateway.requests.size)
    }

    @Test
    fun `history keeps the last five answers, newest first`() = runTest {
        val viewModel = createViewModel()

        repeat(7) { index ->
            llmGateway.answer = { Magic8BallResult.Success("Answer $index") }
            viewModel.onQuestionChange("Question $index")
            viewModel.ask()
            finishCycle()
        }

        val history = viewModel.state.value.history
        assertEquals(EightBallUiState.MAX_ENTRIES, history.size)
        assertEquals(listOf(6L, 5L, 4L, 3L, 2L), history.map { it.id })
        assertEquals("Question 6", history.first().question)
        assertEquals(AnswerText.Plain("Answer 6"), history.first().answer)
        assertEquals(BallPhase.Answered(history.first()), viewModel.state.value.phase)
    }

    @Test
    fun `the typed question is trimmed into the history and the field is cleared`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQuestionChange("  Will it rain?  ")
        viewModel.ask()

        assertEquals("", viewModel.question)
        finishCycle()
        assertEquals("Will it rain?", viewModel.state.value.history.single().question)
    }

    @Test
    fun `the question is capped at its maximum length`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQuestionChange("a".repeat(EightBallViewModel.MAX_QUESTION_LENGTH + 10))

        assertEquals(EightBallViewModel.MAX_QUESTION_LENGTH, viewModel.question.length)
    }

    @Test
    fun `the typed question never leaves the device`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQuestionChange("Something private")
        viewModel.ask()

        assertEquals(
            Magic8BallRequest(
                userTrigger = EightBallViewModel.ASK_TRIGGER,
                languageCode = AppLanguage.ENGLISH.code,
                personality = Magic8BallPersonality.SARCASTIC,
                useAi = true
            ),
            llmGateway.requests.single()
        )
    }

    @Test
    fun `the chosen personality is saved and used for the next answer`() = runTest {
        val viewModel = createViewModel()

        viewModel.setPersonality(Magic8BallPersonality.MYSTICAL)
        runCurrent()
        assertEquals(Magic8BallPersonality.MYSTICAL, userSettings.saved.value.personality)
        assertEquals(Magic8BallPersonality.MYSTICAL, viewModel.state.value.personality)

        viewModel.ask()
        assertEquals(Magic8BallPersonality.MYSTICAL, llmGateway.requests.single().personality)
    }

    @Test
    fun `a failed answer becomes the fallback answer, not an error`() = runTest {
        llmGateway.answer = { Magic8BallResult.Failure(LlmError.Network) }
        val viewModel = createViewModel()

        viewModel.ask()
        finishCycle()

        assertEquals(AnswerText.Fallback, viewModel.state.value.history.single().answer)
    }

    @Test
    fun `a blank answer becomes the fallback answer`() = runTest {
        llmGateway.answer = { Magic8BallResult.Success("  ") }
        val viewModel = createViewModel()

        viewModel.ask()
        finishCycle()

        assertEquals(AnswerText.Fallback, viewModel.state.value.history.single().answer)
    }

    @Test
    fun `an unexpected error becomes the fallback answer and the ball can be asked again`() = runTest {
        llmGateway.answer = { error("boom") }
        val viewModel = createViewModel()

        viewModel.ask()
        finishCycle()
        assertEquals(AnswerText.Fallback, viewModel.state.value.history.single().answer)

        llmGateway.answer = { Magic8BallResult.Success(FakeLlmGateway.ANSWER) }
        viewModel.ask()
        finishCycle()
        assertEquals(AnswerText.Plain(FakeLlmGateway.ANSWER), viewModel.state.value.history.first().answer)
    }

    @Test
    fun `a slow answer is replaced by the fallback at the maximum thinking time`() = runTest {
        llmGateway.answer = { awaitCancellation() }
        val viewModel = createViewModel()

        viewModel.ask()
        advanceTimeBy(MAX_THINKING_MS - 1)
        assertEquals(BallPhase.Thinking, viewModel.state.value.phase)

        advanceTimeBy(1)
        runCurrent()
        val phase = viewModel.state.value.phase
        assertTrue(phase is BallPhase.Answered && phase.entry.answer == AnswerText.Fallback)
    }

    @Test
    fun `effects announce the start and then the answer`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<EightBallEffect>()
        backgroundScope.launch { viewModel.effects.collect(effects::add) }

        viewModel.ask()
        runCurrent()
        assertEquals(listOf<EightBallEffect>(EightBallEffect.AskStarted), effects)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(listOf(EightBallEffect.AskStarted, EightBallEffect.AnswerRevealed), effects)
    }

    /** Delivers a shake and lets the resulting state reach the subscribers. */
    private suspend fun TestScope.shake() {
        motionGateway.events.emit(ShakeEvent.Started)
        runCurrent()
    }

    /** Lets the answer arrive and the cooldown pass, so the ball can be asked again. */
    private fun TestScope.finishCycle() {
        advanceTimeBy(MIN_THINKING_MS + COOLDOWN_MS)
        runCurrent()
    }

    private fun createViewModel(detectionEnabled: Boolean = false): EightBallViewModel {
        val viewModel = EightBallViewModel(
            sendMessageUseCase = SendMessageUseCase(llmGateway),
            observeShakeUseCase = ObserveShakeUseCase(motionGateway),
            userSettingsRepository = userSettings,
            queryConfig = queryConfig
        )
        viewModel.setShakeDetectionEnabled(detectionEnabled)
        return viewModel
    }

    private class FakeMotionGateway : MotionGateway {
        val events = MutableSharedFlow<ShakeEvent>()
        override val shakeEvents = events

        val subscribers: Int get() = events.subscriptionCount.value
    }

    /** Stands for the settings file: what is saved here outlives a ViewModel. */
    private class FakeUserSettingsRepository : UserSettingsRepository {
        val saved = MutableStateFlow(UserSettings())
        override val settings = saved

        override suspend fun setLanguage(language: AppLanguage) {
            saved.update { it.copy(language = language) }
        }

        override suspend fun setPersonality(personality: Magic8BallPersonality) {
            saved.update { it.copy(personality = personality) }
        }

        override suspend fun setAiAnswersEnabled(enabled: Boolean) {
            saved.update { it.copy(aiAnswersEnabled = enabled) }
        }
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
        const val COOLDOWN_MS = 500L
    }
}
