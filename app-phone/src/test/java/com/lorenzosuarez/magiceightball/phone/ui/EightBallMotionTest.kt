package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
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
 * Plays the ball's way to an answer on a clock the test drives, frame by frame: the "8" gives
 * way to the window, the die turns, and with the answer it slows down, stops and shows its
 * text. `./gradlew :app-phone:recordRoborazziDebug` writes the frames to
 * `app-phone/build/outputs/roborazzi/motion/`; a plain run checks that the motion comes to rest.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = "w240dp-h240dp-xhdpi")
class EightBallMotionTest {

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
                    shadowOf(context.packageManager).addActivityIfNotPresent(
                        ComponentName(context, ComponentActivity::class.java)
                    )
                }
            }
        )
        .around(compose)

    @Test
    fun the_die_turns_while_thinking_and_comes_to_rest_with_the_answer() {
        var mode by mutableStateOf(BallMode.Idle)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MagicEightBallPhoneTheme {
                Box(Modifier.background(Backdrop).padding(48.dp)) {
                    EightBall(
                        mode = mode,
                        answer = "Without a doubt.",
                        pressed = false,
                        voiceLevel = { 0f },
                        reducedMotion = false,
                        modifier = Modifier.size(144.dp)
                    )
                }
            }
        }

        var frame = 0
        fun play(frames: Int) = repeat(frames) {
            compose.mainClock.advanceTimeBy(FRAME_MS)
            compose.onRoot().captureRoboImage("build/outputs/roborazzi/motion/frame_%02d.png".format(frame++))
        }

        mode = BallMode.Thinking
        play(THINKING_FRAMES)
        mode = BallMode.Answered
        play(ANSWER_FRAMES)

        // The die turns for as long as the ball thinks, but the answer must leave nothing moving.
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
    }

    private companion object {
        const val FRAME_MS = 200L
        const val THINKING_FRAMES = 12
        const val ANSWER_FRAMES = 12
    }
}
