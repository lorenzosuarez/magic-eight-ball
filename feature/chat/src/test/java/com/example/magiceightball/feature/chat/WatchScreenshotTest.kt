package com.example.magiceightball.feature.chat

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
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

    /**
     * The compose rule starts an empty activity, which only exists here: it is declared to
     * Robolectric before the rule runs, rather than in a manifest that would ship with the app.
     */
    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(
            object : ExternalResource() {
                override fun before() {
                    val context = ApplicationProvider.getApplicationContext<Application>()
                    shadowOf(context.packageManager).addOrUpdateActivity(
                        ActivityInfo().apply {
                            packageName = context.packageName
                            name = ComponentActivity::class.java.name
                            // No title bar: the content is the whole screen, as on the watch.
                            theme = android.R.style.Theme_Material_NoActionBar
                        }
                    )
                }
            }
        )
        .around(compose)

    @Test
    fun idle() = capture("watch_1_idle") { ChatScreenContent(ChatState(aiAnswersAvailable = true)) }

    @Test
    fun idle_can_ask_by_voice_spanish() = capture("watch_7_idle_voice_spanish", AppLanguage.SPANISH) {
        ChatScreenContent(ChatState(aiAnswersAvailable = true, canAskByVoice = true))
    }

    @Test
    fun listening() = capture("watch_8_listening") {
        ChatScreenContent(
            ChatState(
                machineState = ChatStateMachine.Listening("Should I order pizza tonight"),
                aiAnswersAvailable = true,
                canAskByVoice = true
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
                canAskByVoice = true
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
    fun answer_style_spanish() = capture("watch_4_answer_style_spanish", AppLanguage.SPANISH) {
        PersonalitySelectionScreen(selectedPersonality = Magic8BallPersonality.NEUTRAL, onPersonalitySelected = {})
    }

    @Test
    fun ai_answers_unavailable() = capture("watch_5_ai_answers_unavailable") {
        AiAnswersSelectionScreen(isEnabled = true, isAvailable = false, onEnabledChange = {})
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
