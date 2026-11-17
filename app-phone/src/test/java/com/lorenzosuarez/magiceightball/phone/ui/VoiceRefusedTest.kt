package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.CharacterLook
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import org.junit.Assert.assertEquals
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
 * A character of the user's own the AI service would not speak as: once the ball's own answer is
 * out, the bar at the bottom says so, by the character's name, with the way to correct it, and
 * says it one time. Its pictures are drawn with the screen's others, in [EightBallScreenshotTest].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = PHONE_LIGHT)
class VoiceRefusedTest {

    private val compose = createComposeRule()

    /** See [HistoryPageTest]: the rule's empty activity is declared to Robolectric here. */
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
                        }
                    )
                }
            }
        )
        .around(compose)

    @Test
    fun `the user is told by the character's name, once, and Edit opens that character`() {
        var told = 0
        var edited: String? = null
        compose.setContent {
            // As the view model does: taken up, it is no longer in the state.
            var refused by remember { mutableStateOf<CustomCharacter?>(GAUCHO) }
            Screen(
                REFUSED.copy(voiceRefused = refused),
                onVoiceRefusalTold = {
                    told++
                    refused = null
                },
                onEditCharacter = { edited = it }
            )
        }
        compose.waitForIdle()

        compose.onNodeWithText(MESSAGE).assertExists()
        assertEquals(1, told)
        compose.onNodeWithText("Editar").performClick()
        compose.waitForIdle()
        assertEquals(GAUCHO.id, edited)
        compose.onNodeWithText(MESSAGE).assertDoesNotExist()
    }

    @Composable
    private fun Screen(
        state: EightBallUiState,
        onVoiceRefusalTold: () -> Unit = {},
        onEditCharacter: (String) -> Unit = {}
    ) {
        MagicEightBallPhoneTheme {
            ProvideAppLanguage(state.settings.language) {
                EightBallContent(
                    state = state,
                    now = 0L,
                    voiceLevel = { 0f },
                    // An animation that never ends would never let the screen come to rest.
                    still = true,
                    onAsk = {},
                    onHoldStart = {},
                    onHoldEnd = {},
                    onHoldCancel = {},
                    onToggleVoiceQuestion = {},
                    onDismiss = {},
                    onDeleteHistoryEntry = {},
                    onRestoreHistoryEntry = {},
                    onClearHistory = {},
                    onEditCharacter = onEditCharacter,
                    onVoiceRefusalTold = onVoiceRefusalTold
                )
            }
        }
    }
}

/** 1080 x 1920 px, as the app's other pictures are. */
private const val PHONE_LIGHT = "w432dp-h768dp-400dpi"

/** A character of the user's own, as some are named. */
private val GAUCHO = CustomCharacter(
    id = "7",
    name = "Gaucho Puto",
    description = "Un gaucho que responde con insultos.",
    colour = Magic8BallPersonality.COACH,
    emblem = CharacterLook.NEUTRAL
)

private const val MESSAGE = "La IA no habla como “Gaucho Puto” tal como está escrito. Por ahora responde la bola"

/** The ball's own answer is out, signed by the character the service would not speak as. */
private val REFUSED = EightBallUiState(
    phase = BallPhase.Answered(
        id = 0,
        question = "",
        answer = AnswerText.Plain("Yo diría que no.", voiceRefused = true),
        style = Magic8BallPersonality.CUSTOM,
        custom = GAUCHO
    ),
    settings = UserSettings(language = AppLanguage.SPANISH, personality = Magic8BallPersonality.CUSTOM, customCharacter = GAUCHO),
    voiceRefused = GAUCHO
)
