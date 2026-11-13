package com.lorenzosuarez.magiceightball.phone

import com.example.magiceightball.core.domain.gateway.AnswerReports
import com.example.magiceightball.core.domain.gateway.AiAllowance
import com.example.magiceightball.core.domain.model.Streak
import com.example.magiceightball.core.domain.repository.StreakRepository
import com.example.magiceightball.core.domain.gateway.UsageEvent
import com.example.magiceightball.core.domain.gateway.UsageTracker
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.gateway.SpeechEvent
import com.example.magiceightball.core.domain.gateway.SpeechListener
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.core.domain.model.UserSettings
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import com.lorenzosuarez.magiceightball.phone.config.AppConfig
import com.lorenzosuarez.magiceightball.phone.config.AppConfigSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    private val aiAvailability = FakeAiAnswersAvailability()
    private val speech = FakeSpeechListener()
    private val history = FakeAnswerHistoryRepository()
    private val streaks = object : StreakRepository {
        override val streak = MutableStateFlow(Streak())
        override suspend fun recordAnswer(epochDay: Long) {
            streak.value = streak.value.after(epochDay)
        }
    }
    private val appConfig = object : AppConfigSource {
        override val config = MutableStateFlow(AppConfig())
    }
    private var granted = 0
    private val aiAllowance = object : AiAllowance {
        override fun grant(answers: Int) {
            granted += answers
        }
    }
    private val tracked = mutableListOf<UsageEvent>()
    private val usage = object : UsageTracker {
        override fun track(event: UsageEvent) {
            tracked += event
        }
    }
    private val clock = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC)

    // Start-up and settings

    @Test
    fun `an answer from the history is put back in the ball as it was given, and not kept twice`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val kept = HistoryEntry(
            id = 7,
            question = "Will it rain?",
            answer = "Nay, matey.\nThe parrot bet against you.",
            askedAtMillis = 1L,
            fromAi = true,
            style = Magic8BallPersonality.PIRATE
        )

        viewModel.showAgain(kept)
        advanceUntilIdle()

        val shown = viewModel.state.value?.phase as BallPhase.Answered
        assertEquals("Will it rain?", shown.question)
        assertEquals(AnswerText.Plain("Nay, matey.", fromAi = true, remark = "The parrot bet against you."), shown.answer)
        assertEquals(Magic8BallPersonality.PIRATE, shown.style)
        assertEquals(emptyList<HistoryEntry>(), history.entries.value)
        assertEquals(0, llmGateway.requests.size)
        assertEquals(emptyList<String>(), tracked.map { it.name })

        // One the ball could not answer is shown as the app's own answer, signed by nobody.
        viewModel.dismiss()
        viewModel.showAgain(kept.copy(answer = null, style = null))
        val unanswered = viewModel.state.value?.phase as BallPhase.Answered
        assertEquals(AnswerText.Fallback, unanswered.answer)
        assertEquals(null, unanswered.style)
    }

    @Test
    fun `an answer the AI wrote can be reported once, as it was written and without the question`() = runTest {
        val sent = mutableListOf<String>()
        val viewModel = createViewModel(
            reports = object : AnswerReports {
                override fun report(answer: String, remark: String?, style: Magic8BallPersonality?, languageCode: String) {
                    sent += "$languageCode/$style: $answer | $remark"
                }
            }
        )
        advanceUntilIdle()
        val kept = HistoryEntry(
            id = 7,
            question = "Will my secret come out?",
            answer = "Nay, matey.\nThe parrot talks.",
            askedAtMillis = 1L,
            fromAi = true,
            style = Magic8BallPersonality.PIRATE
        )

        viewModel.showAgain(kept)
        viewModel.reportAnswer()
        viewModel.reportAnswer()

        assertEquals(listOf("en/PIRATE: Nay, matey. | The parrot talks."), sent)
        assertTrue((viewModel.state.value?.phase as BallPhase.Answered).reported)

        // The app's own answers were read before they were shipped: there is nothing to report.
        viewModel.dismiss()
        viewModel.showAgain(kept.copy(fromAi = false))
        viewModel.reportAnswer()
        assertEquals(1, sent.size)
    }

    @Test
    fun `an answer is rated good or bad, counted once each way and without what it said`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.showAgain(
            HistoryEntry(
                id = 7,
                question = "Will my secret come out?",
                answer = "Nay, matey.",
                askedAtMillis = 1L,
                fromAi = true,
                style = Magic8BallPersonality.PIRATE
            )
        )
        tracked.clear()

        viewModel.rateAnswer(true)
        viewModel.rateAnswer(true)
        viewModel.rateAnswer(false)

        assertEquals(
            listOf(
                UsageEvent.AnswerRated(liked = true, style = Magic8BallPersonality.PIRATE, fromAi = true),
                UsageEvent.AnswerRated(liked = false, style = Magic8BallPersonality.PIRATE, fromAi = true)
            ),
            tracked
        )
        assertEquals(false, (viewModel.state.value?.phase as BallPhase.Answered).liked)
        assertEquals(mapOf("liked" to "no", "style" to "pirate", "source" to "ai"), tracked.last().params)
    }

    @Test
    fun `the day's verdict opens on the ball, is kept, and gives way to a question in progress`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showDailyVerdict("That ship has sunk, matey.")
        advanceUntilIdle()

        val shown = viewModel.state.value?.phase as BallPhase.Answered
        assertEquals(AnswerText.Plain("That ship has sunk, matey."), shown.answer)
        assertEquals("That ship has sunk, matey.", history.entries.value.first().answer)
        assertEquals(listOf("open_daily_verdict"), tracked.map { it.name })
        assertEquals(0, llmGateway.requests.size)

        viewModel.showDailyVerdict("   ")
        assertEquals(shown, viewModel.state.value?.phase)
    }

    @Test
    fun `the second line is asked for only while it is switched on from afar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.ask()
        advanceUntilIdle()
        assertEquals(true, llmGateway.requests.last().wantsRemark)
        viewModel.dismiss()
        advanceUntilIdle()

        appConfig.config.value = AppConfig(aiRemarkEnabled = false)
        viewModel.ask()
        advanceUntilIdle()

        assertEquals(false, llmGateway.requests.last().wantsRemark)
        assertEquals(false, viewModel.state.value?.config?.aiRemarkEnabled)
    }

    @Test
    fun `an ad watched through adds to the day's AI answers and is counted`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAiAnswersEarned(5)
        advanceUntilIdle()

        assertEquals(5, granted)
        assertEquals(false, viewModel.state.value?.aiAllowanceSpent)
        assertEquals(listOf("earn_ai_answers"), tracked.map { it.name })
    }

    @Test
    fun `an answer counts towards the run of days, and the run is shown from the state`() = runTest {
        val today = NOW / 86_400_000L
        streaks.streak.value = Streak(days = 2, best = 2, lastDay = today - 1)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value?.streakDays)

        viewModel.ask()
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value?.streakDays)
    }

    @Test
    fun `what is done is counted, and never what was asked or answered`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.ask()
        advanceUntilIdle()
        viewModel.onAnswerShared()
        viewModel.setPersonality(Magic8BallPersonality.CAT)
        viewModel.setSpokenAnswersEnabled(true)
        viewModel.setPartyNames(listOf("Ana", "Beto"))
        advanceUntilIdle()

        assertEquals(
            listOf("ask", "share_answer", "choose_style", "change_setting", "set_party"),
            tracked.map { it.name }
        )
        assertEquals("tap", tracked.first().params["how"])
        assertEquals(mapOf("players" to "2"), tracked.last().params)
        // Nothing the user wrote or the ball said: every value is one of a few fixed words.
        assertTrue(tracked.flatMap { it.params.values }.none { "Ana" in it || "Beto" in it || " " in it })
    }

    @Test
    fun `initial state is idle with the saved choices`() = runTest {
        val saved = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.MYSTICAL,
            aiAnswersEnabled = false,
            voiceQuestionsEnabled = false
        )
        userSettings.saved.value = saved

        val viewModel = createViewModel()

        assertEquals(
            EightBallUiState(
                phase = BallPhase.Idle,
                settings = saved,
                aiAnswersAvailable = false,
                voice = VoiceStatus.Off,
                notice = null
            ),
            viewModel.state.value
        )
    }

    @Test
    fun `there is no state and no answer until the saved choices are read`() = runTest {
        userSettings.saved.value = null
        val viewModel = createViewModel(resumed = true)
        assertNull(viewModel.state.value)

        viewModel.ask()
        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        shake()
        assertNull(viewModel.state.value)
        assertEquals(0, llmGateway.requests.size)
        assertEquals(0, speech.sessions.size)

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
        viewModel.setVoiceQuestionsEnabled(false)
        runCurrent()

        val expected = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.MYSTICAL,
            aiAnswersEnabled = false,
            voiceQuestionsEnabled = false
        )
        assertEquals(expected, userSettings.saved.value)
        assertEquals(expected, viewModel.state.value?.settings)
    }

    @Test
    fun `at a party the ball names one of the players and the turn passes on`() = runTest {
        val viewModel = createViewModel()
        viewModel.setPartyNames(listOf("Ana", "Beto", "Caro"))
        runCurrent()
        assertEquals("Ana", viewModel.state.value?.turnName)

        viewModel.askWho("Who of you?")
        advanceUntilIdle()
        val answered = viewModel.state.value?.phase as BallPhase.Answered
        assertEquals("Who of you?", answered.question)
        assertTrue((answered.answer as AnswerText.Plain).value in listOf("Ana", "Beto", "Caro"))
        assertEquals(0, llmGateway.requests.size)

        viewModel.dismiss()
        advanceUntilIdle()
        assertEquals("Beto", viewModel.state.value?.turnName)
    }

    @Test
    fun `alone there are no turns and nobody to name`() = runTest {
        val viewModel = createViewModel()
        viewModel.setPartyNames(listOf("Ana"))
        runCurrent()
        assertNull(viewModel.state.value?.turnName)

        viewModel.askWho("Who of you?")
        advanceUntilIdle()
        assertEquals(BallPhase.Idle, viewModel.state.value?.phase)
    }

    @Test
    fun `saved choices are restored by a new view model`() = runTest {
        createViewModel().setVoiceQuestionsEnabled(false)
        runCurrent()

        assertEquals(VoiceStatus.Off, createViewModel().state.value?.voice)
    }

    @Test
    fun `ai answers are used only when the user allows them and they are available`() = runTest {
        val viewModel = createViewModel()
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
    fun `the request carries the chosen language, personality and ai choice`() = runTest {
        userSettings.saved.value = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.MYSTICAL,
            aiAnswersEnabled = false
        )
        val viewModel = createViewModel()

        viewModel.ask()

        assertEquals(
            Magic8BallRequest(
                userTrigger = EightBallViewModel.ASK_TRIGGER,
                languageCode = "es",
                personality = Magic8BallPersonality.MYSTICAL,
                useAi = false,
                question = null,
                wantsRemark = true
            ),
            llmGateway.requests.single()
        )
    }

    // Tap

    @Test
    fun `a tap thinks for the minimum time and then reveals the answer`() = runTest {
        val viewModel = createViewModel()

        viewModel.ask()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)

        advanceTimeBy(MIN_THINKING_MS - 1)
        assertEquals(BallPhase.Thinking(""), viewModel.phase)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(
            BallPhase.Answered(
                id = 0,
                question = "",
                answer = AnswerText.Plain(FakeLlmGateway.ANSWER),
                style = Magic8BallPersonality.SARCASTIC
            ),
            viewModel.phase
        )
    }

    @Test
    fun `a tap never listens and sends only the fixed trigger`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.ask()

        assertEquals(0, speech.sessions.size)
        val request = llmGateway.requests.single()
        assertEquals(EightBallViewModel.ASK_TRIGGER, request.userTrigger)
        assertNull(request.question)
    }

    @Test
    fun `asking again is ignored while thinking, and answers again right after`() = runTest {
        val viewModel = createViewModel()

        viewModel.ask()
        viewModel.ask()
        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        assertEquals(1, llmGateway.requests.size)

        finishThinking()
        viewModel.ask()
        finishThinking()

        assertEquals(2, llmGateway.requests.size)
        assertEquals(1L, (viewModel.phase as BallPhase.Answered).id)
    }

    @Test
    fun `the answer stays on the ball until the next question`() = runTest {
        val viewModel = createViewModel()

        viewModel.ask()
        finishThinking()
        advanceTimeBy(60_000)

        assertTrue(viewModel.phase is BallPhase.Answered)
    }

    // Shake

    @Test
    fun `a shake produces an answer just like a tap`() = runTest {
        val viewModel = createViewModel(resumed = true, microphoneGranted = true)

        shake()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)

        finishThinking()
        assertEquals(AnswerText.Plain(FakeLlmGateway.ANSWER), (viewModel.phase as BallPhase.Answered).answer)
        assertNull(llmGateway.requests.single().question)
        assertEquals(0, speech.sessions.size)
    }

    @Test
    fun `the sensor is used only while resumed and at rest`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)
        assertEquals(0, motionGateway.subscribers)

        viewModel.setResumed(true)
        assertEquals(1, motionGateway.subscribers)

        viewModel.onHoldStart()
        assertEquals(0, motionGateway.subscribers)

        viewModel.onHoldEnd()
        speech.sessions.single().finish("Will it rain?")
        assertEquals(0, motionGateway.subscribers)

        finishThinking()
        // The tail of a shake must not ask again: the sensor comes back after the cooldown.
        advanceTimeBy(COOLDOWN_MS - 1)
        assertEquals(0, motionGateway.subscribers)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, motionGateway.subscribers)

        viewModel.setResumed(false)
        assertEquals(0, motionGateway.subscribers)
    }

    @Test
    fun `stop events do not ask the ball`() = runTest {
        val viewModel = createViewModel(resumed = true)

        motionGateway.events.emit(ShakeEvent.Stopped)
        runCurrent()

        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `leaving the screen while thinking still delivers the answer`() = runTest {
        val viewModel = createViewModel(resumed = true)

        viewModel.ask()
        viewModel.setResumed(false)
        finishThinking()

        assertTrue(viewModel.phase is BallPhase.Answered)
    }

    // Hold to ask by voice

    @Test
    fun `hold listens, release thinks, and the answer is to the question heard`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
        val session = speech.sessions.single()
        assertEquals(AppLanguage.ENGLISH, session.language)

        session.hear("Will it")
        assertEquals(BallPhase.Listening("Will it"), viewModel.phase)
        session.hear("Will it rain")
        assertEquals(BallPhase.Listening("Will it rain"), viewModel.phase)

        viewModel.onHoldEnd()
        assertTrue(session.stopRequested)
        assertEquals(BallPhase.Thinking("Will it rain"), viewModel.phase)
        assertEquals(0, llmGateway.requests.size)

        session.finish("Will it rain tomorrow?")
        assertEquals(BallPhase.Thinking("Will it rain tomorrow?"), viewModel.phase)
        assertEquals("Will it rain tomorrow?", llmGateway.requests.single().question)
        assertEquals(EightBallViewModel.ASK_TRIGGER, llmGateway.requests.single().userTrigger)

        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
        assertEquals(
            BallPhase.Answered(
                id = 0,
                question = "Will it rain tomorrow?",
                answer = AnswerText.Plain(FakeLlmGateway.ANSWER),
                style = Magic8BallPersonality.SARCASTIC
            ),
            viewModel.phase
        )
        assertFalse(session.active)
    }

    @Test
    fun `the recognizer is asked for the language chosen in the app`() = runTest {
        userSettings.saved.value = UserSettings(language = AppLanguage.SPANISH)
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()

        assertEquals(AppLanguage.SPANISH, speech.sessions.single().language)
    }

    @Test
    fun `the voice level follows the recognizer while listening and falls back to zero`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().events.trySend(SpeechEvent.Level(0.6f))
        assertEquals(0.6f, viewModel.voiceLevel.value)

        viewModel.onHoldEnd()
        speech.sessions.single().finish("Will it rain?")
        assertEquals(0f, viewModel.voiceLevel.value)
    }

    @Test
    fun `when the last words never arrive the words heard so far are the question`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        val session = speech.sessions.single()
        session.hear("Should I go")
        viewModel.onHoldEnd()

        advanceTimeBy(EightBallViewModel.FINAL_TRANSCRIPT_TIMEOUT_MS - 1)
        assertEquals(0, llmGateway.requests.size)
        advanceTimeBy(1)
        runCurrent()

        // The words heard, written as the question they were.
        assertEquals("Should I go?", llmGateway.requests.single().question)
        assertFalse(session.active)
    }

    @Test
    fun `a hold in which nothing was said asks without a question`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        speech.sessions.single().finish(null)

        assertEquals(BallPhase.Thinking(""), viewModel.phase)
        assertNull(llmGateway.requests.single().question)
        finishThinking()
        assertEquals("", (viewModel.phase as BallPhase.Answered).question)
    }

    @Test
    fun `a question the recognizer hears to its end is answered while the finger is still down`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().finish("Will it rain?")
        assertEquals(BallPhase.Thinking("Will it rain?"), viewModel.phase)

        finishThinking()
        assertTrue(viewModel.phase is BallPhase.Answered)

        // Lifting the finger afterwards belongs to that question: it asks nothing more.
        viewModel.onHoldEnd()
        assertTrue(viewModel.phase is BallPhase.Answered)
        assertEquals(1, llmGateway.requests.size)
    }

    @Test
    fun `a recognizer that gives up empty-handed keeps waiting for the release`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().finish(null)
        assertEquals(BallPhase.Listening(""), viewModel.phase)
        assertEquals(0, llmGateway.requests.size)

        viewModel.onHoldEnd()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)
        assertNull(llmGateway.requests.single().question)
    }

    @Test
    fun `a recognizer that gives up is started again while the finger is still down`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().finish(null)
        advanceTimeBy(EightBallViewModel.LISTEN_RETRY_MS)
        runCurrent()
        assertEquals(2, speech.sessions.size)
        assertTrue(speech.sessions.last().active)

        speech.sessions.last().finish("Will it rain?")
        assertEquals(BallPhase.Thinking("Will it rain?"), viewModel.phase)
    }

    @Test
    fun `a recognizer that fails is a question nobody heard, never an error`() = runTest {
        speech.failure = IllegalStateException("recognizer died")
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
        viewModel.onHoldEnd()
        finishThinking()

        val phase = viewModel.phase as BallPhase.Answered
        assertEquals(AnswerText.Plain(FakeLlmGateway.ANSWER), phase.answer)
        assertNull(llmGateway.requests.single().question)
        assertNull(viewModel.state.value?.notice)
    }

    @Test
    fun `leaving the screen while listening releases the microphone and asks nothing`() = runTest {
        val viewModel = createViewModel(resumed = true, microphoneGranted = true)

        viewModel.onHoldStart()
        val session = speech.sessions.single()
        session.hear("Will it")
        viewModel.setResumed(false)
        runCurrent()

        assertFalse(session.active)
        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0f, viewModel.voiceLevel.value)

        viewModel.onHoldCancel()
        advanceTimeBy(MAX_THINKING_MS)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `an interrupted hold puts the previous answer back`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)
        viewModel.ask()
        finishThinking()
        val answered = viewModel.phase

        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
        viewModel.onHoldCancel()
        runCurrent()

        assertEquals(answered, viewModel.phase)
        assertFalse(speech.sessions.single().active)
        assertEquals(1, llmGateway.requests.size)
    }

    // Microphone off, denied or impossible

    @Test
    fun `with voice questions off a hold is a long tap`() = runTest {
        userSettings.saved.value = UserSettings(voiceQuestionsEnabled = false)
        val viewModel = createViewModel(microphoneGranted = true)
        assertEquals(VoiceStatus.Off, viewModel.state.value?.voice)

        viewModel.onHoldStart()
        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0, speech.sessions.size)

        viewModel.onHoldEnd()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)
        assertNull(llmGateway.requests.single().question)
        assertNull(viewModel.state.value?.notice)
    }

    @Test
    fun `the first hold asks for the microphone permission instead of asking the ball`() = runTest {
        val viewModel = createViewModel()
        val effects = viewModel.collectEffects(this)
        assertEquals(VoiceStatus.Ready, viewModel.state.value?.voice)

        viewModel.onHoldStart()
        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        runCurrent()

        assertEquals(listOf<EightBallEffect>(EightBallEffect.RequestMicrophonePermission), effects)
        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0, speech.sessions.size)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `once the permission is granted the next hold listens`() = runTest {
        val viewModel = createViewModel()

        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = true)
        assertEquals(VoiceStatus.Ready, viewModel.state.value?.voice)
        assertNull(viewModel.state.value?.notice)
        assertEquals(0, speech.sessions.size)

        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
    }

    @Test
    fun `after a refusal a hold asks for the permission again and never answers unasked`() = runTest {
        val viewModel = createViewModel()
        val effects = viewModel.collectEffects(this)

        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = false)
        assertEquals(VoiceStatus.PermissionDenied, viewModel.state.value?.voice)
        assertEquals(Notice.MicrophoneDenied, viewModel.state.value?.notice)

        advanceTimeBy(EightBallViewModel.NOTICE_DURATION_MS)
        runCurrent()
        assertNull(viewModel.state.value?.notice)

        // A hold is still someone wanting to speak: asked again, and no answer to nothing.
        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        runCurrent()
        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0, speech.sessions.size)
        assertEquals(0, llmGateway.requests.size)
        assertEquals(2, effects.count { it == EightBallEffect.RequestMicrophonePermission })

        // Refused again, by the user or by a system that no longer asks: explained again.
        viewModel.onMicrophonePermissionResult(granted = false)
        assertEquals(Notice.MicrophoneDenied, viewModel.state.value?.notice)

        // A tap keeps asking the ball, and granting it at last makes the next hold listen.
        viewModel.ask()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)
        finishThinking()
        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = true)
        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
    }

    @Test
    fun `a permission request that never got its answer does not block the next hold`() = runTest {
        val viewModel = createViewModel()
        val effects = viewModel.collectEffects(this)

        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionChecked(granted = false)
        viewModel.onHoldStart()
        runCurrent()

        assertEquals(2, effects.count { it == EightBallEffect.RequestMicrophonePermission })
    }

    @Test
    fun `asking clears the notice`() = runTest {
        val viewModel = createViewModel()
        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = false)

        viewModel.ask()

        assertNull(viewModel.state.value?.notice)
    }

    @Test
    fun `a permission granted in the system settings is picked up on return`() = runTest {
        val viewModel = createViewModel()
        viewModel.onHoldStart()
        viewModel.onMicrophonePermissionResult(granted = false)

        viewModel.onMicrophonePermissionChecked(granted = false)
        assertEquals(VoiceStatus.PermissionDenied, viewModel.state.value?.voice)

        viewModel.onMicrophonePermissionChecked(granted = true)
        assertEquals(VoiceStatus.Ready, viewModel.state.value?.voice)

        viewModel.onHoldStart()
        assertEquals(BallPhase.Listening(""), viewModel.phase)
    }

    @Test
    fun `a permission taken away in the system settings is asked for again`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)
        val effects = viewModel.collectEffects(this)

        viewModel.onMicrophonePermissionChecked(granted = false)
        viewModel.onHoldStart()
        runCurrent()

        assertEquals(listOf<EightBallEffect>(EightBallEffect.RequestMicrophonePermission), effects)
        assertEquals(0, speech.sessions.size)
    }

    @Test
    fun `without a recognizer the app is tap and shake only and says so once`() = runTest {
        speech.available = false
        val viewModel = createViewModel(resumed = true, microphoneGranted = true)
        val effects = viewModel.collectEffects(this)
        assertEquals(VoiceStatus.Unsupported, viewModel.state.value?.voice)

        viewModel.onHoldStart()
        assertEquals(Notice.VoiceUnsupported, viewModel.state.value?.notice)
        assertEquals(0, speech.sessions.size)

        viewModel.onHoldEnd()
        assertEquals(BallPhase.Thinking(""), viewModel.phase)
        assertNull(viewModel.state.value?.notice)
        finishThinking()

        viewModel.onHoldStart()
        assertNull(viewModel.state.value?.notice)
        assertFalse(EightBallEffect.RequestMicrophonePermission in effects)

        shake(afterCooldown = true)
        assertEquals(BallPhase.Thinking(""), viewModel.phase)
    }

    @Test
    fun `a recognizer installed later is noticed when the screen comes back`() = runTest {
        speech.available = false
        val viewModel = createViewModel(resumed = true)

        speech.available = true
        viewModel.setResumed(false)
        viewModel.setResumed(true)

        assertEquals(VoiceStatus.Ready, viewModel.state.value?.voice)
    }

    // The hands-free equivalent of holding

    @Test
    fun `toggling listens and toggling again ends the question`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.toggleVoiceQuestion()
        assertEquals(BallPhase.Listening(handsFree = true), viewModel.phase)
        val session = speech.sessions.single()
        session.hear("Is it time")

        viewModel.toggleVoiceQuestion()
        assertTrue(session.stopRequested)
        session.finish("Is it time?")

        assertEquals("Is it time?", llmGateway.requests.single().question)
    }

    @Test
    fun `hands-free listening that hears nothing just stops`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.toggleVoiceQuestion()
        speech.sessions.single().finish(null)

        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(0, llmGateway.requests.size)
    }

    @Test
    fun `hands-free listening answers by itself once the question has been heard`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.toggleVoiceQuestion()
        speech.sessions.single().finish("Is it time?")
        finishThinking()

        assertEquals("Is it time?", (viewModel.phase as BallPhase.Answered).question)
    }

    // Privacy: what is passed on

    @Test
    fun `a question is passed on with the user's ai choice, whatever it is`() = runTest {
        userSettings.saved.value = UserSettings(aiAnswersEnabled = false)
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().finish("Will it rain?")

        // The data layer never sends a question while useAi is false.
        val request = llmGateway.requests.single()
        assertEquals(false, request.useAi)
        assertEquals("Will it rain?", request.question)
    }

    @Test
    fun `a transcript of only spaces is no question`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)

        viewModel.onHoldStart()
        speech.sessions.single().hear("   ")
        viewModel.onHoldEnd()
        speech.sessions.single().finish("  ")

        assertNull(llmGateway.requests.single().question)
    }

    // Failures are never shown

    @Test
    fun `a failed answer becomes the fallback answer, not an error`() = runTest {
        llmGateway.answer = { Magic8BallResult.Failure(LlmError.Network) }
        val viewModel = createViewModel()

        viewModel.ask()
        finishThinking()

        assertEquals(AnswerText.Fallback, (viewModel.phase as BallPhase.Answered).answer)
        assertNull(viewModel.state.value?.notice)
    }

    @Test
    fun `a blank answer becomes the fallback answer`() = runTest {
        llmGateway.answer = { Magic8BallResult.Success("  ") }
        val viewModel = createViewModel()

        viewModel.ask()
        finishThinking()

        assertEquals(AnswerText.Fallback, (viewModel.phase as BallPhase.Answered).answer)
    }

    @Test
    fun `an unexpected error becomes the fallback answer and the ball can be asked again`() = runTest {
        llmGateway.answer = { error("boom") }
        val viewModel = createViewModel()

        viewModel.ask()
        finishThinking()
        assertEquals(AnswerText.Fallback, (viewModel.phase as BallPhase.Answered).answer)

        llmGateway.answer = { Magic8BallResult.Success(FakeLlmGateway.ANSWER) }
        viewModel.ask()
        finishThinking()
        assertEquals(AnswerText.Plain(FakeLlmGateway.ANSWER), (viewModel.phase as BallPhase.Answered).answer)
    }

    @Test
    fun `a slow answer is replaced by the fallback at the maximum thinking time`() = runTest {
        llmGateway.answer = { awaitCancellation() }
        val viewModel = createViewModel()

        viewModel.ask()
        advanceTimeBy(MAX_THINKING_MS - 1)
        assertEquals(BallPhase.Thinking(""), viewModel.phase)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(AnswerText.Fallback, (viewModel.phase as BallPhase.Answered).answer)
    }

    // Effects

    @Test
    fun `a tap announces the ask and then the answer`() = runTest {
        val viewModel = createViewModel()
        val effects = viewModel.collectEffects(this)

        viewModel.ask()
        runCurrent()
        assertEquals(listOf<EightBallEffect>(EightBallEffect.AskStarted), effects)

        finishThinking()
        assertEquals(listOf(EightBallEffect.AskStarted, EightBallEffect.AnswerRevealed), effects)
    }

    @Test
    fun `a spoken question announces listening, the release and then the answer`() = runTest {
        val viewModel = createViewModel(microphoneGranted = true)
        val effects = viewModel.collectEffects(this)

        viewModel.onHoldStart()
        viewModel.onHoldEnd()
        speech.sessions.single().finish("Will it rain?")
        finishThinking()

        assertEquals(
            listOf(
                EightBallEffect.ListeningStarted,
                EightBallEffect.ListeningStopped,
                EightBallEffect.AnswerRevealed
            ),
            effects
        )
    }

    // Closing, asking again and the history

    @Test
    fun `closing an answer goes back to rest`() = runTest {
        val viewModel = createViewModel()
        viewModel.ask()
        finishThinking()

        viewModel.dismiss()
        runCurrent()

        assertEquals(BallPhase.Idle, viewModel.phase)
    }

    @Test
    fun `closing while thinking drops the question and nothing is answered or saved`() = runTest {
        val viewModel = createViewModel()
        viewModel.ask()
        runCurrent()
        assertTrue(viewModel.phase is BallPhase.Thinking)

        viewModel.dismiss()
        finishThinking()

        assertEquals(BallPhase.Idle, viewModel.phase)
        assertEquals(emptyList<Any>(), viewModel.state.value?.history)
    }

    @Test
    fun `closing while listening releases the microphone and goes back to rest`() = runTest {
        val viewModel = createViewModel(resumed = true, microphoneGranted = true)
        viewModel.ask()
        finishThinking()
        viewModel.onHoldStart()
        runCurrent()
        assertTrue(viewModel.phase is BallPhase.Listening)

        viewModel.dismiss()
        runCurrent()

        assertEquals(BallPhase.Idle, viewModel.phase)
        assertFalse(speech.sessions.single().active)
    }

    @Test
    fun `every answer is saved with its question and who wrote it, newest first`() = runTest {
        val viewModel = createViewModel(resumed = true, microphoneGranted = true)
        viewModel.ask()
        finishThinking()

        llmGateway.answer = { Magic8BallResult.Success("Without a doubt", fromAi = true) }
        viewModel.onHoldStart()
        runCurrent()
        speech.sessions.single().finish("Will it rain?")
        viewModel.onHoldEnd()
        finishThinking()

        val saved = viewModel.state.value?.history.orEmpty()
        assertEquals(listOf("Will it rain?", ""), saved.map { it.question })
        assertEquals(listOf("Without a doubt", FakeLlmGateway.ANSWER), saved.map { it.answer })
        assertEquals(listOf(true, false), saved.map { it.fromAi })
    }

    @Test
    fun `an answer the app had to make up is saved without a text`() = runTest {
        llmGateway.answer = { Magic8BallResult.Failure(LlmError.Network) }
        val viewModel = createViewModel()
        viewModel.ask()
        finishThinking()

        assertEquals(listOf<String?>(null), viewModel.state.value?.history?.map { it.answer })
    }

    @Test
    fun `an entry can be deleted and put back, and all of them cleared`() = runTest {
        val viewModel = createViewModel()
        repeat(3) {
            viewModel.ask()
            finishThinking()
        }
        val saved = viewModel.state.value!!.history
        assertEquals(listOf(NOW, NOW, NOW), saved.map { it.askedAtMillis })

        viewModel.deleteHistoryEntry(saved[1].id)
        runCurrent()
        assertEquals(listOf(saved[0], saved[2]), viewModel.state.value?.history)

        viewModel.restoreHistoryEntry(saved[1])
        runCurrent()
        assertEquals(saved, viewModel.state.value?.history)

        viewModel.clearHistory()
        runCurrent()
        assertEquals(emptyList<Any>(), viewModel.state.value?.history)
    }

    private val EightBallViewModel.phase: BallPhase? get() = state.value?.phase

    private fun EightBallViewModel.collectEffects(scope: TestScope): List<EightBallEffect> {
        val collected = mutableListOf<EightBallEffect>()
        scope.backgroundScope.launch { effects.collect(collected::add) }
        return collected
    }

    /** Delivers a shake and lets the resulting state reach the subscribers. */
    private suspend fun TestScope.shake(afterCooldown: Boolean = false) {
        if (afterCooldown) advanceTimeBy(COOLDOWN_MS)
        runCurrent()
        motionGateway.events.emit(ShakeEvent.Started)
        runCurrent()
    }

    /** Lets the answer arrive. */
    private fun TestScope.finishThinking() {
        advanceTimeBy(MIN_THINKING_MS)
        runCurrent()
    }

    private fun createViewModel(
        resumed: Boolean = false,
        microphoneGranted: Boolean = false,
        reports: AnswerReports = AnswerReports.None
    ): EightBallViewModel {
        val viewModel = EightBallViewModel(
            sendMessageUseCase = SendMessageUseCase(llmGateway),
            observeShakeUseCase = ObserveShakeUseCase(motionGateway),
            userSettingsRepository = userSettings,
            aiAnswersAvailability = aiAvailability,
            speechListener = speech,
            history = history,
            clock = clock,
            queryConfig = queryConfig,
            usage = usage,
            streaks = streaks,
            aiAllowance = aiAllowance,
            appConfig = appConfig,
            reports = reports
        )
        viewModel.onMicrophonePermissionChecked(microphoneGranted)
        viewModel.setResumed(resumed)
        return viewModel
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

    /** Stands for the history file: newest first, as the real one keeps it. */
    private class FakeAnswerHistoryRepository : AnswerHistoryRepository {
        override val entries = MutableStateFlow<List<HistoryEntry>>(emptyList())

        override suspend fun add(
            question: String,
            answer: String?,
            askedAtMillis: Long,
            fromAi: Boolean,
            style: Magic8BallPersonality?
        ) {
            entries.update { listOf(HistoryEntry(it.size + 1L, question, answer, askedAtMillis, fromAi, style)) + it }
        }

        override suspend fun remove(id: Long) {
            entries.update { old -> old.filterNot { it.id == id } }
        }

        override suspend fun restore(entry: HistoryEntry) {
            entries.update { old -> (old + entry).sortedByDescending { it.id } }
        }

        override suspend fun clear() {
            entries.value = emptyList()
        }
    }

    private class FakeAiAnswersAvailability : AiAnswersAvailability {
        override val isAvailable = MutableStateFlow(false)
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

    /** A recognizer driven by the test: one [Session] per time the app listens. */
    private class FakeSpeechListener : SpeechListener {
        var available = true
        var failure: Exception? = null
        val sessions = mutableListOf<Session>()

        override fun isAvailable(): Boolean = available

        override fun listen(language: AppLanguage, stop: Deferred<Unit>): Flow<SpeechEvent> = flow {
            val session = Session(language, stop)
            sessions += session
            failure?.let { throw it }
            try {
                for (event in session.events) emit(event)
            } finally {
                session.active = false
            }
        }

        class Session(val language: AppLanguage, private val stop: Deferred<Unit>) {
            val events = Channel<SpeechEvent>(Channel.UNLIMITED)
            var active = true

            val stopRequested: Boolean get() = stop.isCompleted

            fun hear(words: String) {
                events.trySend(SpeechEvent.Transcript(words, isFinal = false))
            }

            /** The recognizer is done: with [question] as what it understood, or with nothing. */
            fun finish(question: String?) {
                question?.let { events.trySend(SpeechEvent.Transcript(it, isFinal = true)) }
                events.close()
            }
        }
    }

    private companion object {
        const val MIN_THINKING_MS = 1_000L
        const val MAX_THINKING_MS = 5_000L
        const val COOLDOWN_MS = 500L
        const val NOW = 1_800_000_000_000L
    }
}
