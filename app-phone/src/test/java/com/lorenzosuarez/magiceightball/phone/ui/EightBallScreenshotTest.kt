package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.Notice
import com.lorenzosuarez.magiceightball.phone.VoiceStatus
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

/**
 * Renders the real screen on the JVM (no device or emulator) with fixed state, at Google Play's
 * screenshot sizes. `./gradlew :app-phone:recordRoborazziDebug` writes the PNGs to
 * `app-phone/build/outputs/roborazzi/`; a plain unit-test run only checks that the screen renders.
 *
 * A plain [Application] stands in for the Hilt one, so nothing is injected and nothing touches the network.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = PHONE)
class EightBallScreenshotTest {

    @Test
    fun phone_idle_light() = capture("phone_1_ask_light", EightBallUiState(history = HISTORY))

    @Test
    fun phone_thinking_light() = capture(
        name = "phone_18_thinking_light",
        state = EightBallUiState(phase = BallPhase.Thinking("Should I order pizza tonight?"))
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_idle_spanish_no_history_dark() = capture(
        name = "phone_19_ask_spanish_no_history_dark",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH))
    )

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_idle_history_spanish_large_font() = capture(
        name = "phone_20_small_phone_history_spanish_large_font",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH), history = HISTORY),
        fontScale = 2f,
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_history() = capture(
        name = "phone_21_history",
        state = EightBallUiState(history = HISTORY),
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_history_empty_spanish() = capture(
        name = "phone_22_history_empty_spanish",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH)),
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_listening_light() = capture(
        name = "phone_2_listening_light",
        state = EightBallUiState(phase = BallPhase.Listening("Should I order pizza")),
        voiceLevel = 0.7f
    )

    @Test
    fun phone_answer_light() = capture("phone_3_answer_light", ANSWERED)

    @Test
    @Config(qualifiers = "+night")
    fun phone_idle_dark() = capture("phone_4_ask_dark", EightBallUiState())

    @Test
    @Config(qualifiers = "+night")
    fun phone_listening_dark() = capture(
        name = "phone_5_listening_dark",
        state = EightBallUiState(phase = BallPhase.Listening("Will I finish my book")),
        voiceLevel = 0.5f
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_answer_dark() = capture(
        name = "phone_6_answer_dark",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "Will I finish my book this month?",
                answer = AnswerText.Plain("A definitive maybe.")
            )
        )
    )

    @Test
    fun phone_settings_light() = captureWithSettings("phone_7_settings_light", ANSWERED)

    @Test
    @Config(qualifiers = "+night")
    fun phone_settings_dark() = captureWithSettings(
        name = "phone_8_settings_dark",
        state = EightBallUiState(
            settings = UserSettings(
                language = AppLanguage.SPANISH,
                personality = Magic8BallPersonality.MYSTICAL
            ),
            aiAnswersAvailable = true,
            voice = VoiceStatus.PermissionDenied
        )
    )

    @Test
    fun phone_spanish_tap_only() = capture(
        name = "phone_9_spanish_tap_only",
        state = EightBallUiState(
            phase = BallPhase.Answered(id = 0, question = "", answer = AnswerText.Plain("Ni en tus sueños.")),
            settings = UserSettings(language = AppLanguage.SPANISH, voiceQuestionsEnabled = false),
            voice = VoiceStatus.Off
        )
    )

    @Test
    fun phone_long_answer_large_font() = capture(
        name = "phone_10_long_answer_large_font",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "Is this really going to be the year that everything finally changes for me?",
                answer = AnswerText.Plain("Absolutely not, according to my sources.")
            )
        ),
        fontScale = 2f
    )

    @Test
    fun phone_longest_bundled_answer() = capture(
        name = "phone_11_longest_bundled_answer",
        state = answered("Odds are good. Suspiciously.")
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_longest_bundled_word_spanish() = capture(
        name = "phone_12_longest_bundled_word_spanish",
        state = answered("Sí, sorprendentemente.", AppLanguage.SPANISH)
    )

    @Test
    fun phone_six_word_ai_answer_spanish() = capture(
        name = "phone_13_six_word_ai_answer_spanish",
        state = answered("Definitivamente no, según mis fuentes confiables.", AppLanguage.SPANISH)
    )

    @Test
    fun phone_microphone_denied_notice() = capture(
        name = "phone_14_microphone_denied_notice",
        state = EightBallUiState(voice = VoiceStatus.PermissionDenied, notice = Notice.MicrophoneDenied)
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_voice_unsupported_notice_spanish() = capture(
        name = "phone_15_voice_unsupported_notice_spanish",
        state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            voice = VoiceStatus.Unsupported,
            notice = Notice.VoiceUnsupported
        )
    )

    /** The sheet's content on its own: under a font scale the sheet's window is not rendered on the JVM. */
    @Test
    fun phone_settings_spanish_large_font() {
        val state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH, aiAnswersEnabled = false),
            aiAnswersAvailable = true
        )
        captureRoboImage(filePath = path("phone_16_settings_spanish_large_font")) {
            WithFontScale(2f) {
                MagicEightBallPhoneTheme {
                    ProvideAppLanguage(state.settings.language) {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            SettingsContent(
                                state = state,
                                onLanguageSelected = {},
                                onPersonalitySelected = {},
                                onAiAnswersChange = {},
                                onVoiceQuestionsChange = {},
                                onAllowMicrophone = {},
                                onShareApp = {},
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_answer_large_font() = capture(
        name = "phone_17_small_phone_large_font",
        state = answered("Odds are good. Suspiciously."),
        fontScale = 2f
    )

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_answer_light() = capture("tablet_1_answer_light", ANSWERED)

    @Test
    @Config(qualifiers = TABLET_LANDSCAPE)
    fun tablet_landscape_idle_dark() = capture("tablet_2_ask_landscape_dark", EightBallUiState())

    private fun capture(
        name: String,
        state: EightBallUiState,
        voiceLevel: Float = 0f,
        fontScale: Float = 1f,
        initialPage: Int = BALL_PAGE
    ) {
        captureRoboImage(filePath = path(name)) {
            WithFontScale(fontScale) { Screen(state, voiceLevel, initialPage) }
        }
    }

    /** The screen with the real settings sheet open over it: both windows in one picture. */
    @OptIn(ExperimentalRoborazziApi::class)
    private fun captureWithSettings(name: String, state: EightBallUiState) {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        activity.setContent {
            Screen(state, voiceLevel = 0f, overlay = {
                SettingsSheet(
                    state = state,
                    onLanguageSelected = {},
                    onPersonalitySelected = {},
                    onAiAnswersChange = {},
                    onVoiceQuestionsChange = {},
                    onAllowMicrophone = {},
                    onShareApp = {},
                    onDismiss = {}
                )
            })
        }
        // Lets the sheet finish sliding in.
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
        captureScreenRoboImage(filePath = path(name))
    }

    @Composable
    private fun Screen(
        state: EightBallUiState,
        voiceLevel: Float,
        initialPage: Int = BALL_PAGE,
        overlay: @Composable () -> Unit = {}
    ) {
        MagicEightBallPhoneTheme {
            ProvideAppLanguage(state.settings.language) {
                EightBallContent(
                    state = state,
                    now = NOW,
                    voiceLevel = { voiceLevel },
                    // An animation that never ends would never come to rest for its picture.
                    reducedMotion = true,
                    onAsk = {},
                    onHoldStart = {},
                    onHoldEnd = {},
                    onHoldCancel = {},
                    onToggleVoiceQuestion = {},
                    onDismiss = {},
                    onDeleteHistoryEntry = {},
                    onRestoreHistoryEntry = {},
                    onClearHistory = {},
                    onOpenSettings = {},
                    initialPage = initialPage
                )
                overlay()
            }
        }
    }

    @Composable
    private fun WithFontScale(fontScale: Float, content: @Composable () -> Unit) {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale),
            content = content
        )
    }

    private fun answered(answer: String, language: AppLanguage = AppLanguage.ENGLISH) = EightBallUiState(
        phase = BallPhase.Answered(id = 0, question = "", answer = AnswerText.Plain(answer)),
        settings = UserSettings(language = language)
    )

    private fun path(name: String) = "build/outputs/roborazzi/$name.png"
}

/** 1080 x 1920 px. */
private const val PHONE = "w432dp-h768dp-400dpi"

/** 720 x 1280 px, a small phone. */
private const val SMALL_PHONE = "w360dp-h640dp-xhdpi"

/** 1200 x 1920 px, a 7-inch tablet. */
private const val TABLET = "w600dp-h960dp-xhdpi"

/** 1920 x 1200 px, the same tablet on its side. */
private const val TABLET_LANDSCAPE = "w960dp-h600dp-land-night-xhdpi"

private const val NOW = 1_800_000_000_000L
private const val MINUTE = 60_000L

private val HISTORY = listOf(
    HistoryEntry(3, "Should I order pizza tonight?", "Yes. Screenshot this.", NOW - 20_000L, fromAi = true),
    HistoryEntry(2, "", "Don't count on it.", NOW - 12 * MINUTE, fromAi = false),
    HistoryEntry(1, "Will I finish my book this month?", null, NOW - 26 * 60 * MINUTE, fromAi = false)
)

private val ANSWERED = EightBallUiState(
    phase = BallPhase.Answered(
        id = 0,
        question = "Should I order pizza tonight?",
        answer = AnswerText.Plain("Yes. Screenshot this.")
    ),
    aiAnswersAvailable = true
)
