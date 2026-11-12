package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.gateway.AnswerReports
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.gateway.SpeechEvent
import com.example.magiceightball.core.domain.gateway.SpeechListener
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

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
    private val aiAvailability = FakeAiAnswersAvailability()
    private val speech = FakeSpeechListener()
    private val reports = FakeAnswerReports()

    @Test
    fun `initial state is idle with the saved choices`() = runTest {
        val saved = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.MYSTICAL,
            aiAnswersEnabled = false
        )
        userSettings.saved.value = saved

        val viewModel = createViewModel()

        assertEquals(
            ChatState(ChatStateMachine.Idle, saved, aiAnswersAvailable = false, canListen = true),
            viewModel.state.value
        )
    }

    @Test
    fun `there is no state and no cycle until the saved choices are read`() = runTest {
        userSettings.saved.value = null
        val viewModel = createViewModel(detectionEnabled = true)
        assertNull(viewModel.state.value)

        shake()
        assertNull(viewModel.state.value)
        assertEquals(0, llmGateway.requests.size)

        userSettings.saved.value = UserSettings(language = AppLanguage.SPANISH)
        runCurrent()
        assertEquals(AppLanguage.SPANISH, viewModel.state.value?.settings?.language)
    }

    @Test
    fun `choices are saved and reach the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.setLanguage(AppLanguage.SPANISH)
        viewModel.setPersonality(Magic8BallPersonality.MYSTICAL)
        viewModel.setAiAnswersEnabled(false)
        runCurrent()

        val expected = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.MYSTICAL,
            aiAnswersEnabled = false
        )
        assertEquals(expected, userSettings.saved.value)
        assertEquals(expected, viewModel.state.value?.settings)
    }

    @Test
    fun `saved choices are restored by a new view model`() = runTest {
        createViewModel().setLanguage(AppLanguage.SPANISH)
        runCurrent()

        assertEquals(AppLanguage.SPANISH, createViewModel().state.value?.settings?.language)
    }

    @Test
    fun `ai answers are used only when the user allows them and they are available`() = runTest {
        val viewModel = createViewModel()
        assertEquals(false, viewModel.state.value?.aiAnswersAvailable)
        assertEquals(false, viewModel.state.value?.usesAiAnswers)

        aiAvailability.isAvailable.value = true
        runCurrent()
        assertEquals(true, viewModel.state.value?.aiAnswersAvailable)
        assertEquals(true, viewModel.state.value?.usesAiAnswers)

        viewModel.setAiAnswersEnabled(false)
        runCurrent()
        assertEquals(true, viewModel.state.value?.aiAnswersAvailable)
        assertEquals(false, viewModel.state.value?.usesAiAnswers)
    }

    @Test
    fun `the classic style has its own answers, so nothing says the ai will answer`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(microphoneGranted = true)
        assertEquals(true, viewModel.state.value?.usesAiAnswers)
        assertEquals(true, viewModel.state.value?.canAskByVoice)

        viewModel.setPersonality(Magic8BallPersonality.NEUTRAL)
        runCurrent()
        assertEquals(false, viewModel.state.value?.usesAiAnswers)
        assertEquals(false, viewModel.state.value?.canAskByVoice)

        // A hold is then a press like any other: nothing listens.
        viewModel.onHoldStart()
        runCurrent()
        assertEquals(0, speech.sessions.size)
    }

    @Test
    fun `once the day's ai answers are used up nothing says the ai will answer, until it does again`() = runTest {
        aiAvailability.isAvailable.value = true
        llmGateway.answer = { Magic8BallResult.Success(FakeLlmGateway.ANSWER, aiAllowanceSpent = true) }
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)
        assertEquals(true, viewModel.state.value?.usesAiAnswers)

        shake()
        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
        runCurrent()
        assertEquals(true, viewModel.state.value?.aiAllowanceSpent)
        assertEquals(false, viewModel.state.value?.usesAiAnswers)
        assertEquals(false, viewModel.state.value?.canAskByVoice)

        llmGateway.answer = { Magic8BallResult.Success(FakeLlmGateway.ANSWER, fromAi = true) }
        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(true, viewModel.state.value?.usesAiAnswers)
        assertEquals(true, viewModel.state.value?.canAskByVoice)
    }

    @Test
    fun `request carries the user's ai choice`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        assertEquals(true, llmGateway.requests.single().useAi)

        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
        runCurrent()
        viewModel.setAiAnswersEnabled(false)
        runCurrent()

        shake()
        assertEquals(listOf(true, false), llmGateway.requests.map { it.useAi })
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
    fun `answer stays long enough to be read and then the machine returns to idle`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS - 1)
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
            advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
            runCurrent()
            assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        }
        assertEquals(2, llmGateway.requests.size)
    }

    @Test
    fun `a tap asks as a shake does`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ChatEffect>()
        backgroundScope.launch { viewModel.effects.collect(effects::add) }

        viewModel.onTap()
        runCurrent()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)
        assertEquals(listOf<ChatEffect>(ChatEffect.ShakeDetected), effects)

        // One question at a time: a tap while the ball thinks asks nothing more.
        viewModel.onTap()
        runCurrent()
        assertEquals(1, llmGateway.requests.size)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )
    }

    @Test
    fun `the next tap ends the answer and asks again`() = runTest {
        val viewModel = createViewModel()

        viewModel.onTap()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )

        llmGateway.answer = { Magic8BallResult.Success("Ask the cat") }
        viewModel.onTap()
        runCurrent()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)
        assertEquals(2, llmGateway.requests.size)

        // The second answer has its whole time, whenever the first one would have left.
        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS - 1)
        assertEquals(ChatStateMachine.Completed(AnswerText.Plain("Ask the cat")), viewModel.machineState)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
    }

    @Test
    fun `the next shake ends the answer too, but not the tail of the shake that asked`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(COOLDOWN_MS - 1)
        assertEquals(0, motionGateway.subscribers)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, motionGateway.subscribers)
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )

        shake()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)
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
    fun `a slow answer is replaced by the fallback at the maximum thinking time`() =
        runTest {
            llmGateway.answer = { awaitCancellation() }
            val viewModel = createViewModel(detectionEnabled = true)

            shake()
            advanceTimeBy(MAX_THINKING_MS - 1)
            assertEquals(ChatStateMachine.Running, viewModel.machineState)

            advanceTimeBy(1)
            runCurrent()
            assertEquals(
                ChatStateMachine.Completed(AnswerText.Fallback),
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
            ChatStateMachine.Completed(AnswerText.Fallback),
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
            ChatStateMachine.Completed(AnswerText.Fallback),
            viewModel.machineState
        )

        advanceTimeBy(ANSWER_SHOWN_MS)
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

        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
    }

    @Test
    fun `an answer already given is still there when the screen comes back within its time`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)
        val answered = ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER))

        shake()
        advanceTimeBy(MIN_THINKING_MS + COOLDOWN_MS)
        runCurrent()

        // The screen is left, or the watch dims: no shake counts, and the answer waits.
        viewModel.setShakeDetectionEnabled(false)
        runCurrent()
        assertEquals(answered, viewModel.machineState)
        assertEquals(0, motionGateway.subscribers)

        viewModel.setShakeDetectionEnabled(true)
        runCurrent()
        assertEquals(answered, viewModel.machineState)

        // Its time is counted from when it was given, not from when the screen came back.
        advanceTimeBy(ANSWER_SHOWN_MS - COOLDOWN_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
    }

    @Test
    fun `an answer the ai wrote can be reported once, with its style and language and without the question`() =
        runTest {
            aiAvailability.isAvailable.value = true
            userSettings.saved.value = UserSettings(AppLanguage.SPANISH, Magic8BallPersonality.PIRATE)
            llmGateway.answer = { Magic8BallResult.Success("Ni lo sueñes, grumete.", fromAi = true) }
            val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

            viewModel.onHoldStart()
            runCurrent()
            viewModel.onHoldEnd()
            speech.sessions.single().finish("¿Saldrá mi secreto a la luz?")
            advanceTimeBy(HEARD_QUESTION_MS + MIN_THINKING_MS)
            runCurrent()
            val answer = AnswerText.Plain("Ni lo sueñes, grumete.", fromAi = true)
            assertEquals(ChatStateMachine.Completed(answer), viewModel.machineState)

            // The choices may change while the answer is on screen: the report is about the answer.
            viewModel.setPersonality(Magic8BallPersonality.CAT)
            runCurrent()
            viewModel.reportAnswer()
            viewModel.reportAnswer()

            assertEquals(listOf("es/PIRATE: Ni lo sueñes, grumete. | null"), reports.sent)
            assertEquals(ChatStateMachine.Completed(answer, reported = true), viewModel.machineState)

            // Said once, it stays said for as long as the answer does, and no longer.
            advanceTimeBy(ANSWER_SHOWN_MS)
            runCurrent()
            assertEquals(ChatStateMachine.Idle, viewModel.machineState)
            viewModel.reportAnswer()
            assertEquals(1, reports.sent.size)
        }

    @Test
    fun `the app's own answers cannot be reported`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)

        // Nothing is on screen yet.
        viewModel.reportAnswer()

        // The bundled ones were read before they were shipped.
        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        viewModel.reportAnswer()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )

        // Nor is there anything to say about the answer the app gives when it has none.
        llmGateway.answer = { Magic8BallResult.Failure(LlmError.Network) }
        viewModel.onTap()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        viewModel.reportAnswer()
        assertEquals(ChatStateMachine.Completed(AnswerText.Fallback), viewModel.machineState)

        assertEquals(emptyList<String>(), reports.sent)
    }

    @Test
    fun `request carries the selected language and personality`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true)
        viewModel.setLanguage(AppLanguage.SPANISH)
        viewModel.setPersonality(Magic8BallPersonality.MYSTICAL)
        runCurrent()

        shake()

        val request = llmGateway.requests.single()
        assertEquals("es", request.languageCode)
        assertEquals(Magic8BallPersonality.MYSTICAL, request.personality)
    }

    @Test
    fun `the watch only ever sends the fixed trigger and never a question`() = runTest {
        userSettings.saved.value = UserSettings(voiceQuestionsEnabled = true)
        createViewModel(detectionEnabled = true)

        shake()

        assertEquals(
            Magic8BallRequest(
                userTrigger = "shake",
                languageCode = AppLanguage.ENGLISH.code,
                personality = Magic8BallPersonality.SARCASTIC,
                useAi = true,
                question = null
            ),
            llmGateway.requests.single()
        )
        assertNull(llmGateway.requests.single().questionForAi)
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

    // Questions asked out loud

    @Test
    fun `holding listens only when the ai service is there to answer`() = runTest {
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)
        assertEquals(false, viewModel.state.value?.canAskByVoice)

        viewModel.onHoldStart()
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(0, speech.sessions.size)

        aiAvailability.isAvailable.value = true
        runCurrent()
        assertEquals(true, viewModel.state.value?.canAskByVoice)

        userSettings.saved.update { it?.copy(aiAnswersEnabled = false) }
        runCurrent()
        assertEquals(false, viewModel.state.value?.canAskByVoice)
    }

    @Test
    fun `a watch that cannot listen does not offer to`() = runTest {
        speech.available = false
        aiAvailability.isAvailable.value = true

        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

        assertEquals(false, viewModel.state.value?.canAskByVoice)
    }

    @Test
    fun `the words appear as they are heard and the question is answered on release`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

        viewModel.onHoldStart()
        runCurrent()
        assertEquals(ChatStateMachine.Listening(), viewModel.machineState)

        val session = speech.sessions.single()
        session.hear("Will it")
        session.level(0.6f)
        runCurrent()
        assertEquals(ChatStateMachine.Listening("Will it"), viewModel.machineState)
        assertEquals(0.6f, viewModel.voiceLevel.value)

        viewModel.onHoldEnd()
        session.finish("Will it rain?")
        runCurrent()
        // The whole question stays on screen for a moment before the ball thinks about it.
        assertEquals(ChatStateMachine.Listening("Will it rain?", finished = true), viewModel.machineState)

        // A touch that comes and goes meanwhile does not undo a question already heard.
        viewModel.onHoldCancel()
        runCurrent()
        assertEquals(ChatStateMachine.Listening("Will it rain?", finished = true), viewModel.machineState)

        advanceTimeBy(HEARD_QUESTION_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)), viewModel.machineState)
        assertEquals("Will it rain?", llmGateway.requests.single().question)
        assertEquals(false, session.active)
        assertEquals(0f, viewModel.voiceLevel.value)
    }

    @Test
    fun `a hold in which nothing is understood asks nothing`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

        viewModel.onHoldStart()
        runCurrent()
        viewModel.onHoldEnd()
        speech.sessions.single().finish(null)
        runCurrent()

        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `the first hold asks for the microphone, and once it is granted the next one listens`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true)
        val effects = mutableListOf<ChatEffect>()
        backgroundScope.launch { viewModel.effects.collect(effects::add) }

        viewModel.onHoldStart()
        runCurrent()
        assertEquals(listOf<ChatEffect>(ChatEffect.RequestMicrophonePermission), effects)
        assertEquals(0, speech.sessions.size)

        // A hold while the system's dialog is up asks nothing more.
        viewModel.onHoldStart()
        runCurrent()
        assertEquals(1, effects.size)

        viewModel.onMicrophonePermissionResult(granted = true)
        viewModel.onHoldStart()
        runCurrent()
        assertEquals(ChatStateMachine.Listening(), viewModel.machineState)
    }

    @Test
    fun `a refused microphone is never asked for again, and the shake and the tap still answer`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true)
        val effects = mutableListOf<ChatEffect>()
        backgroundScope.launch { viewModel.effects.collect(effects::add) }

        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = false)
        runCurrent()
        assertEquals(false, viewModel.state.value?.canAskByVoice)
        // The ai still answers a shake, and the screen may still say so.
        assertEquals(true, viewModel.state.value?.usesAiAnswers)

        // Coming back to the app with the permission still refused changes nothing.
        viewModel.onMicrophonePermissionChecked(granted = false)
        viewModel.onHoldStart()
        runCurrent()
        assertEquals(false, viewModel.state.value?.canAskByVoice)
        assertEquals(1, effects.count { it == ChatEffect.RequestMicrophonePermission })
        assertEquals(0, speech.sessions.size)

        viewModel.onTap()
        runCurrent()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)
        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
        runCurrent()

        shake()
        assertEquals(ChatStateMachine.Running, viewModel.machineState)
        advanceTimeBy(MIN_THINKING_MS + ANSWER_SHOWN_MS)
        runCurrent()

        // Granted later in the system settings, the microphone is offered again.
        viewModel.onMicrophonePermissionChecked(granted = true)
        runCurrent()
        assertEquals(true, viewModel.state.value?.canAskByVoice)
        viewModel.onHoldStart()
        runCurrent()
        assertEquals(ChatStateMachine.Listening(), viewModel.machineState)
    }

    @Test
    fun `a question asked out loud ends the answer on screen as a tap does`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

        shake()
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            ChatStateMachine.Completed(AnswerText.Plain(FakeLlmGateway.ANSWER)),
            viewModel.machineState
        )

        viewModel.onHoldStart()
        runCurrent()
        assertEquals(ChatStateMachine.Listening(), viewModel.machineState)

        // What is left of the first answer's time does not cut the question short.
        advanceTimeBy(ANSWER_SHOWN_MS)
        runCurrent()
        assertEquals(ChatStateMachine.Listening(), viewModel.machineState)
    }

    @Test
    fun `a hold that is taken away, or a screen that is left, stops listening and asks nothing`() = runTest {
        aiAvailability.isAvailable.value = true
        val viewModel = createViewModel(detectionEnabled = true, microphoneGranted = true)

        viewModel.onHoldStart()
        runCurrent()
        viewModel.onHoldCancel()
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(false, speech.sessions.single().active)

        viewModel.onHoldStart()
        runCurrent()
        viewModel.setShakeDetectionEnabled(false)
        runCurrent()
        assertEquals(ChatStateMachine.Idle, viewModel.machineState)
        assertEquals(false, speech.sessions.last().active)
        assertEquals(0, llmGateway.requests.size)
    }

    /** Delivers a shake and lets the resulting state reach the subscribers. */
    private suspend fun TestScope.shake() {
        motionGateway.events.emit(ShakeEvent.Started)
        runCurrent()
    }

    private val ChatViewModel.machineState: ChatStateMachine?
        get() = state.value?.machineState

    private fun createViewModel(
        detectionEnabled: Boolean = false,
        microphoneGranted: Boolean = false
    ): ChatViewModel {
        val viewModel = ChatViewModel(
            sendMessageUseCase = SendMessageUseCase(llmGateway),
            observeShakeUseCase = ObserveShakeUseCase(motionGateway),
            userSettingsRepository = userSettings,
            aiAnswersAvailability = aiAvailability,
            speechListener = speech,
            answerReports = reports,
            queryConfig = queryConfig
        )
        viewModel.onMicrophonePermissionChecked(microphoneGranted)
        viewModel.setShakeDetectionEnabled(detectionEnabled)
        return viewModel
    }

    /** A recognizer driven by the test: one [Session] per time the app listens. */
    private class FakeSpeechListener : SpeechListener {
        var available = true
        val sessions = mutableListOf<Session>()

        override fun isAvailable(): Boolean = available

        override fun listen(language: AppLanguage, stop: Deferred<Unit>): Flow<SpeechEvent> = flow {
            val session = Session(language)
            sessions += session
            try {
                for (event in session.events) emit(event)
            } finally {
                session.active = false
            }
        }

        class Session(val language: AppLanguage) {
            val events = Channel<SpeechEvent>(Channel.UNLIMITED)
            var active = true

            fun hear(words: String) {
                events.trySend(SpeechEvent.Transcript(words, isFinal = false))
            }

            fun level(value: Float) {
                events.trySend(SpeechEvent.Level(value))
            }

            /** The recognizer is done: with [question] as what it understood, or with nothing. */
            fun finish(question: String?) {
                question?.let { events.trySend(SpeechEvent.Transcript(it, isFinal = true)) }
                events.close()
            }
        }
    }

    private class FakeMotionGateway : MotionGateway {
        val events = MutableSharedFlow<ShakeEvent>()
        override val shakeEvents = events

        val subscribers: Int get() = events.subscriptionCount.value
    }

    /** Stands for the settings file: what is saved here outlives a ViewModel. */
    private class FakeUserSettingsRepository : UserSettingsRepository {
        val saved = MutableStateFlow<UserSettings?>(UserSettings())
        override val settings = saved.filterNotNull()

        override suspend fun setLanguage(language: AppLanguage) {
            saved.update { it?.copy(language = language) }
        }

        override suspend fun setPersonality(personality: Magic8BallPersonality) {
            saved.update { it?.copy(personality = personality) }
        }

        override suspend fun setAiAnswersEnabled(enabled: Boolean) {
            saved.update { it?.copy(aiAnswersEnabled = enabled) }
        }

        override suspend fun setVoiceQuestionsEnabled(enabled: Boolean) {
            saved.update { it?.copy(voiceQuestionsEnabled = enabled) }
        }

        override suspend fun setSpokenAnswersEnabled(enabled: Boolean) {
            saved.update { it?.copy(spokenAnswersEnabled = enabled) }
        }


        override suspend fun setDailyVerdictEnabled(enabled: Boolean) {
            saved.update { it?.copy(dailyVerdictEnabled = enabled) }
        }

        override suspend fun setPartyNames(names: List<String>) {
            saved.update { it?.copy(partyNames = names) }
        }
    }

    private class FakeAiAnswersAvailability : AiAnswersAvailability {
        override val isAvailable = MutableStateFlow(false)
    }

    private class FakeAnswerReports : AnswerReports {
        val sent = mutableListOf<String>()

        override fun report(answer: String, remark: String?, style: Magic8BallPersonality?, languageCode: String) {
            sent += "$languageCode/$style: $answer | $remark"
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
        const val COOLDOWN_MS = 1_000L
        const val ANSWER_SHOWN_MS = 8_000L
        const val HEARD_QUESTION_MS = 1_200L
    }
}
