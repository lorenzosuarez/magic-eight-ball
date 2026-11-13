package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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

/**
 * The account on the page of options, as the user meets it: a touch, and what comes of it.
 *
 * No picture is taken of the question asked before deleting: on the JVM a dialog over a sheet is
 * drawn behind it, full width and in the device's type size, none of which is so on a device.
 */
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

    /** Who is signed in, which a deletion or a signing out changes under the page. */
    private var account by mutableStateOf<AccountState>(AccountState.SignedOut)

    /** Whether they hold what is sold. */
    private var premium = false
    private val snackbars = SnackbarHostState()

    private fun show(
        fontScale: Float = 1f,
        onSignIn: suspend () -> AccountOutcome = { AccountOutcome.DONE },
        onSignOut: () -> Unit = {},
        onDeleteAccount: suspend () -> AccountOutcome = { AccountOutcome.DONE }
    ) = compose.setContent {
        val state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            aiAnswersAvailable = true,
            config = AppConfig(accountEnabled = true),
            account = account,
            premium = premium
        )
        CompositionLocalProvider(
            LocalDensity provides Density(LocalDensity.current.density, fontScale),
            LocalSnackbars provides snackbars
        ) {
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

    private fun count(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().size

    @Test
    fun `a device with no Google account is told so, and shown the way to add one`() {
        show(fontScale = 1.5f, onSignIn = { AccountOutcome.NO_GOOGLE_ACCOUNT })
        compose.onNodeWithText("Continuar con Google").performClick()
        compose.onNodeWithText("No hay ninguna cuenta de Google en este dispositivo").assertIsDisplayed()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/phone_53_small_phone_account_no_google_large_font.png")

        compose.onNodeWithText("Añadir cuenta de Google").performClick()
        val opened = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertEquals(Settings.ACTION_ADD_ACCOUNT, opened.action)
    }

    @Test
    fun `a sign-in that fails says so, and one the user backs out of says nothing`() {
        var outcome = AccountOutcome.FAILED
        show(onSignIn = { outcome })
        compose.onNodeWithText("Continuar con Google").performClick()
        compose.onNodeWithText(SIGN_IN_FAILED).assertIsDisplayed()

        outcome = AccountOutcome.CANCELLED
        compose.onNodeWithText("Continuar con Google").performClick()
        assertEquals(0, count(SIGN_IN_FAILED))
    }

    @Test
    fun `the account opens from its row, and signing out there is one touch`() {
        account = LORENA
        var signedOut = 0
        show(onSignOut = { signedOut++ })
        assertEquals(0, count("Cerrar sesión"))

        compose.onNodeWithText("Lorena Suárez").performClick()
        compose.onNodeWithText("Cerrar sesión").performClick()
        compose.waitForIdle()

        assertEquals(1, signedOut)
        assertEquals(0, count("Cerrar sesión"))
    }

    @Test
    fun `whoever paid is told so on their account, and nobody else is told anything`() {
        account = LORENA
        premium = true
        show()
        assertEquals(0, count("Tienes Premium"))
        compose.onNodeWithText("Lorena Suárez").performClick()
        compose.onNodeWithText("Tienes Premium").assertIsDisplayed()
    }

    @Test
    fun `an account that holds nothing says nothing of it`() {
        account = LORENA
        show()
        compose.onNodeWithText("Lorena Suárez").performClick()
        compose.onNodeWithText("Cerrar sesión").assertIsDisplayed()
        assertEquals(0, count("Tienes Premium"))
    }

    @Test
    fun `deleting the account asks first, and a deletion that fails is said where it was tried`() {
        account = LORENA
        var deleted = 0
        show(fontScale = 1.5f, onDeleteAccount = { deleted++; AccountOutcome.FAILED })
        compose.onNodeWithText("Lorena Suárez").performClick()

        compose.onNodeWithText("Eliminar cuenta").performClick()
        compose.onNodeWithText("¿Eliminar tu cuenta?").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(0, deleted)

        compose.onNodeWithText("Eliminar cuenta").performClick()
        // The row under the dialog, and the dialog's own button: the button is the later one.
        compose.onAllNodesWithText("Eliminar cuenta")[1].performClick()
        compose.waitForIdle()

        assertEquals(1, deleted)
        compose.onNodeWithText("No se pudo eliminar la cuenta. Inténtalo de nuevo").assertIsDisplayed()
        assertEquals(null, snackbars.currentSnackbarData)
    }

    @Test
    fun `an account that is deleted closes its sheet, and the page says it is done`() {
        account = LORENA
        show(
            onDeleteAccount = {
                account = AccountState.SignedOut
                AccountOutcome.DONE
            }
        )
        compose.onNodeWithText("Lorena Suárez").performClick()
        compose.onNodeWithText("Eliminar cuenta").performClick()
        compose.onNodeWithText("¿Eliminar tu cuenta?").assertIsDisplayed()
        compose.onAllNodesWithText("Eliminar cuenta")[1].performClick()
        compose.waitForIdle()

        assertEquals(0, count("Cerrar sesión"))
        compose.onNodeWithText("Continuar con Google").assertIsDisplayed()
        assertEquals("Cuenta eliminada.", snackbars.currentSnackbarData?.visuals?.message)
    }

    private companion object {
        val LORENA = AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com")
        const val SIGN_IN_FAILED = "No se pudo entrar. Revisa tu conexión e inténtalo de nuevo"
    }
}
