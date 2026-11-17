package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
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
 * The ball's page with a finger on the ball, which no picture of the page at rest shows: the
 * ball has given way and its halo has come out, in the colour of whoever answers.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = "w432dp-h768dp-400dpi")
class BallPressTest {

    private val compose = createComposeRule()

    /**
     * See [HistoryPageTest]: the rule's empty activity is declared to Robolectric here. Drawn by
     * the hardware, as the app's own is: otherwise what the ball's layer draws past its own
     * bounds, the light around it, is cut off square in the picture.
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
                            theme = android.R.style.Theme_Material_NoActionBar
                            flags = ActivityInfo.FLAG_HARDWARE_ACCELERATED
                        }
                    )
                }
            }
        )
        .around(compose)

    @Test
    @Config(qualifiers = "+night")
    fun `a finger on the ball brings its halo out on the dark page`() = press("phone_133_ball_pressed_dark")

    @Test
    fun `a finger on the ball brings its halo out on the light page, where it is all the light there is`() =
        press("phone_134_ball_pressed_light")

    private fun press(name: String) {
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    EightBallContent(
                        state = EightBallUiState(
                            settings = UserSettings(language = AppLanguage.SPANISH, personality = Magic8BallPersonality.DRAMA)
                        ),
                        now = 0L,
                        voiceLevel = { 0f },
                        // An animation that never ends would never come to rest for its picture.
                        still = true,
                        onAsk = {},
                        onHoldStart = {},
                        onHoldEnd = {},
                        onHoldCancel = {},
                        onToggleVoiceQuestion = {},
                        onDismiss = {},
                        onDeleteHistoryEntry = {},
                        onRestoreHistoryEntry = {},
                        onClearHistory = {}
                    )
                }
            }
        }
        // Down and not up again: the picture is of the ball as it is under the finger.
        compose.onNodeWithContentDescription("Magic AI Ball").performTouchInput { down(center) }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
