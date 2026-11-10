package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.HistoryEntry
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the real screen on the JVM (no device or emulator) with fixed state, at Google Play's
 * screenshot sizes. `./gradlew :app-phone:recordRoborazziDebug` writes the PNGs to
 * `app-phone/build/outputs/roborazzi/`; a plain unit-test run only checks that the screen renders.
 *
 * SDK 30 keeps the app's own palette: from Android 12 on the theme follows the wallpaper. A plain
 * [Application] stands in for the Hilt one, so nothing is injected and nothing touches the network.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = PHONE)
class EightBallScreenshotTest {

    @Test
    fun phone_idle_light() = capture("phone_1_ask_light", EightBallUiState())

    @Test
    fun phone_answered_light() = capture("phone_2_answer_light", answeredState(historySize = 2))

    @Test
    @Config(qualifiers = "+night")
    fun phone_answered_dark() = capture(
        name = "phone_3_answer_dark",
        state = answeredState(historySize = 2),
        question = "Is it going to rain tomorrow?"
    )

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_history_light() = capture("tablet_1_history_light", answeredState(historySize = 3))

    private fun capture(name: String, state: EightBallUiState, question: String = "") {
        captureRoboImage(filePath = "build/outputs/roborazzi/$name.png") {
            MagicEightBallPhoneTheme {
                EightBallContent(
                    state = state,
                    question = question,
                    onQuestionChange = {},
                    onAsk = {},
                    onPersonalitySelected = {}
                )
            }
        }
    }

    /** The newest [historySize] answers, the first of them still showing on the ball. */
    private fun answeredState(historySize: Int): EightBallUiState {
        val history = HISTORY.take(historySize)
        return EightBallUiState(phase = BallPhase.Answered(history.first()), history = history)
    }
}

/** 1080 x 1920 px. */
private const val PHONE = "w432dp-h768dp-400dpi"

/** 1200 x 1920 px, a 7-inch tablet. */
private const val TABLET = "w600dp-h960dp-xhdpi"

private val HISTORY = listOf(
    "Should I order pizza tonight?" to "Yes. Screenshot this.",
    "Will I finish my book this month?" to "A definitive maybe.",
    "" to "Reply hazy. Shake harder."
).mapIndexed { index, (question, answer) ->
    HistoryEntry(id = (3 - index).toLong(), question = question, answer = AnswerText.Plain(answer))
}
