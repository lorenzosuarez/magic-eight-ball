package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.config.AppConfig
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

/** The account on the page of options, as the user meets it: a touch, and what comes of it. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
class AccountSectionTest {

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

    private fun show(
        account: AccountState,
        fontScale: Float = 1f,
        onSignIn: suspend () -> AccountOutcome = { AccountOutcome.DONE },
        onSignOut: () -> Unit = {},
        onDeleteAccount: suspend () -> AccountOutcome = { AccountOutcome.DONE }
    ) = compose.setContent {
        val state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            aiAnswersAvailable = true,
            config = AppConfig(accountEnabled = true),
            account = account
        )
        CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
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
                            onSignIn = onSignIn,
                            onSignOut = onSignOut,
                            onDeleteAccount = onDeleteAccount
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a phone with no Google account is told to add one, on the smallest screen in the largest type`() {
        show(AccountState.SignedOut, fontScale = 1.5f, onSignIn = { AccountOutcome.NO_GOOGLE_ACCOUNT })
        compose.onNodeWithText("Continuar con Google").performClick()
        compose.onNodeWithText(NO_GOOGLE).assertIsDisplayed()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/phone_53_small_phone_account_no_google_large_font.png")
    }

    @Test
    fun `a sign-in that fails says so, and one the user backs out of says nothing`() {
        var outcome = AccountOutcome.FAILED
        show(AccountState.SignedOut, onSignIn = { outcome })
        compose.onNodeWithText("Continuar con Google").performClick()
        compose.onNodeWithText(SIGN_IN_FAILED).assertIsDisplayed()

        outcome = AccountOutcome.CANCELLED
        compose.onNodeWithText("Continuar con Google").performClick()
        assertEquals(0, compose.onAllNodesWithText(SIGN_IN_FAILED).fetchSemanticsNodes().size)
    }

    @Test
    fun `signing out is one touch, and deleting the account asks first`() {
        var signedOut = 0
        var deleted = 0
        show(
            AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com"),
            onSignOut = { signedOut++ },
            onDeleteAccount = { deleted++; AccountOutcome.FAILED }
        )
        compose.onNodeWithText("Cerrar sesión").performClick()
        assertEquals(1, signedOut)

        compose.onNodeWithText("Eliminar cuenta").performScrollTo().performClick()
        compose.onNodeWithText("¿Eliminar tu cuenta?").assertIsDisplayed()
        assertEquals(0, deleted)

        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(0, deleted)

        compose.onNodeWithText("Eliminar cuenta").performClick()
        compose.onAllNodesWithText("Eliminar cuenta")[1].performClick()
        assertEquals(1, deleted)
        compose.onNodeWithText("No se pudo eliminar la cuenta. Inténtalo de nuevo.").assertIsDisplayed()
    }

    private companion object {
        const val NO_GOOGLE =
            "No hay ninguna cuenta de Google en este teléfono. Añade una en Ajustes e inténtalo de nuevo."
        const val SIGN_IN_FAILED = "No se pudo entrar. Revisa tu conexión e inténtalo de nuevo."
    }
}
