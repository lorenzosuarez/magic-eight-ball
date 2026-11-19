package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.UserSettings
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

/** The page that says what the app is built with, and on what terms. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
class LicensesTest {

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
    fun `the libraries and the font are named with their licence whole, and a licence reads as sentences`() {
        val state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH))
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(state.settings.language) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        SettingsContent(
                            state = state,
                            onLanguageSelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {},
                            nav = OptionsNav(OptionsSection.LICENSES)
                        )
                    }
                }
            }
        }

        compose.onNodeWithText("AndroidX, Jetpack Compose, Kotlin, Dagger Hilt, Firebase").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Bibliotecas con las que está hecha la app").assertExists()
        compose.onNodeWithText("END OF TERMS AND CONDITIONS", substring = true).assertExists()
        // Not broken where the file broke it: "for" ended a line there, and "any" began the next.
        compose.onNodeWithText("this software for any purpose with or without fee", substring = true).assertExists()
        // The font's licence whole, from the font's own copyright line to the licence's last words,
        // and its title on its own, without the lines of dashes that frame it in the file.
        compose.onNodeWithText("Copyright 2025 The Google Sans Project Authors (github.com/googlefonts/googlesans)", substring = true).assertExists()
        compose.onNodeWithText("\n\nSIL OPEN FONT LICENSE Version 1.1 - 26 February 2007\n\nPREAMBLE", substring = true).assertExists()
        compose.onNodeWithText("OTHER DEALINGS IN THE FONT SOFTWARE.", substring = true).assertExists()
        // A rule that stands alone between two parts is kept: Lucide's, before Feather's notice.
        compose.onNodeWithText("\n\n---\n\nThe following Lucide icons", substring = true).assertExists()
    }
}
