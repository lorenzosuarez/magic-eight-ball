package com.example.magiceightball.feature.chat

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the watch's screens on the JVM (no device or emulator), on a clock the test drives
 * because the thinking die never stops moving. `./gradlew :feature:chat:recordRoborazziDebug`
 * writes the PNGs to `feature/chat/build/outputs/roborazzi/`; a plain unit-test run only checks
 * that the screens render.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w192dp-h192dp-round-xhdpi")
class WatchScreenshotTest {

    private val compose = createComposeRule()

    @get:Rule
    val rules: RuleChain = onWatch(compose)

    @Test
    fun idle() = capture("watch_1_idle") { ChatScreenContent(ChatState(aiAnswersAvailable = true)) }

    /** No AI to promise: the classic style has its own answers. */
    @Test
    fun idle_classic_portuguese() = capture("watch_14_idle_classic_portuguese", AppLanguage.PORTUGUESE) {
        ChatScreenContent(
            ChatState(
                settings = UserSettings(personality = Magic8BallPersonality.NEUTRAL),
                aiAnswersAvailable = true,
                canListen = true
            )
        )
    }

    @Test
    fun idle_can_ask_by_voice_spanish() = capture("watch_7_idle_voice_spanish", AppLanguage.SPANISH) {
        ChatScreenContent(ChatState(aiAnswersAvailable = true, canListen = true))
    }

    @Test
    fun ambient() = capture("watch_15_ambient") { AmbientBall() }

    @Test
    fun listening() = capture("watch_8_listening") {
        ChatScreenContent(
            ChatState(
                machineState = ChatStateMachine.Listening("Should I order pizza tonight"),
                aiAnswersAvailable = true,
                canListen = true
            ),
            voiceLevel = { 0.7f }
        )
    }

    @Test
    fun listening_long_question_spanish() = capture("watch_9_listening_long_spanish", AppLanguage.SPANISH) {
        ChatScreenContent(
            ChatState(
                machineState = ChatStateMachine.Listening(
                    "me gustaría saber si este año por fin voy a poder terminar de escribir mi libro"
                ),
                aiAnswersAvailable = true,
                canListen = true
            ),
            voiceLevel = { 0.2f }
        )
    }

    @Test
    fun thinking() = capture("watch_2_thinking") {
        ChatScreenContent(ChatState(machineState = ChatStateMachine.Running))
    }

    @Test
    fun answer() = capture("watch_3_answer", settleMs = ANSWER_SETTLE_MS) {
        ChatScreenContent(ChatState(machineState = ChatStateMachine.Completed(AnswerText.Plain("Without a doubt."))))
    }

    @Test
    fun longest_answer_spanish() = capture("watch_6_longest_answer_spanish", AppLanguage.SPANISH, ANSWER_SETTLE_MS) {
        ChatScreenContent(
            ChatState(machineState = ChatStateMachine.Completed(AnswerText.Plain("Concéntrate y pregunta.")))
        )
    }

    @Test
    fun long_word_answer_spanish() = capture("watch_10_long_word_spanish", AppLanguage.SPANISH, ANSWER_SETTLE_MS) {
        ChatScreenContent(
            ChatState(machineState = ChatStateMachine.Completed(AnswerText.Plain("Sí, sorprendentemente.")))
        )
    }

    @Test
    fun longest_bundled_answer_spanish() =
        capture("watch_12_longest_bundled_spanish", AppLanguage.SPANISH, ANSWER_SETTLE_MS) {
            ChatScreenContent(
                ChatState(machineState = ChatStateMachine.Completed(AnswerText.Plain("Técnicamente sí. Qué pena.")))
            )
        }

    @Test
    fun longest_ai_answer() = capture("watch_11_longest_ai_answer", settleMs = ANSWER_SETTLE_MS) {
        ChatScreenContent(
            ChatState(
                machineState = ChatStateMachine.Completed(
                    AnswerText.Plain(
                        "Absolutely, unquestionably, overwhelmingly, extraordinarily, undoubtedly yes.",
                        fromAi = true
                    )
                )
            )
        )
    }

    /** Six words as the AI service may write them: longer than any bundled answer, and still whole. */
    @Test
    fun long_ai_answer_spanish() = capture("watch_16_long_ai_answer_spanish", AppLanguage.SPANISH, ANSWER_SETTLE_MS) {
        ChatScreenContent(
            ChatState(
                machineState = ChatStateMachine.Completed(
                    AnswerText.Plain("Las estrellas dicen que definitivamente sí.", fromAi = true)
                )
            )
        )
    }

    @Test
    fun reported_ai_answer_portuguese() =
        capture("watch_17_reported_ai_answer_portuguese", AppLanguage.PORTUGUESE, ANSWER_SETTLE_MS) {
            ChatScreenContent(
                ChatState(
                    machineState = ChatStateMachine.Completed(
                        AnswerText.Plain("Nem em sonho, marujo.", fromAi = true),
                        reported = true
                    )
                )
            )
        }

    @Test
    fun answer_style_spanish() = capture("watch_4_answer_style_spanish", AppLanguage.SPANISH) {
        PersonalitySelectionScreen(selectedPersonality = Magic8BallPersonality.NEUTRAL, onPersonalitySelected = {})
    }

    /** The whole list, opened on the style in use: every one of them can be reached. */
    @Test
    fun answer_style_list_spanish() = capture("watch_13_answer_style_list_spanish", AppLanguage.SPANISH) {
        PersonalityList(selectedPersonality = Magic8BallPersonality.DOG, onPersonalitySelected = {})
    }

    @Test
    fun ai_answers_unavailable() = capture("watch_5_ai_answers_unavailable") {
        AiAnswersSelectionScreen(isEnabled = true, isAvailable = false, onEnabledChange = {})
    }

    @Test
    fun ai_answers_classic_style_spanish() = capture("watch_18_ai_answers_classic_spanish", AppLanguage.SPANISH) {
        AiAnswersSelectionScreen(isEnabled = true, isAvailable = true, onEnabledChange = {}, isClassicStyle = true)
    }

    @Test
    fun ai_answers_spent_portuguese() = capture("watch_19_ai_answers_spent_portuguese", AppLanguage.PORTUGUESE) {
        AiAnswersSelectionScreen(isEnabled = true, isAvailable = true, onEnabledChange = {}, isSpentForToday = true)
    }

    private fun capture(
        name: String,
        language: AppLanguage = AppLanguage.ENGLISH,
        settleMs: Long = SETTLE_MS,
        content: @Composable () -> Unit
    ) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MagicEightBallTheme {
                CompositionLocalProvider(LocalAppLanguage provides language, content = content)
            }
        }
        compose.mainClock.advanceTimeBy(settleMs)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    private companion object {
        /** Long enough for every entrance to finish; the thinking die is caught mid-motion. */
        const val SETTLE_MS = 2_000L

        /** Long enough for the die to have stopped rocking. */
        const val ANSWER_SETTLE_MS = 8_000L
    }
}
