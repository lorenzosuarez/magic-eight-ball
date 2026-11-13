package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.UserSettings
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.ProfileOutcome
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import com.lorenzosuarez.magiceightball.phone.config.AppConfig
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Being asked who one is, as the user meets it: what is typed, what is said back, and the two ways out. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h1500dp-xhdpi")
class ProfileSheetTest {

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

    private val saved = mutableListOf<Pair<String, LocalDate>>()

    private fun showFields(draft: ProfileDraft, firstTime: Boolean = true) = compose.setContent {
        MagicEightBallPhoneTheme {
            ProvideAppLanguage(AppLanguage.SPANISH) {
                ProfileContent(
                    draft = draft,
                    firstTime = firstTime,
                    today = TODAY,
                    busy = false,
                    failed = false,
                    onSave = { name, date -> saved += name to date },
                    onSecondary = {},
                    onOpenPrivacyPolicy = {}
                )
            }
        }
    }

    private fun count(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().size

    @Test
    fun `agreeing waits for a name and a whole date, and saves them as typed`() {
        showFields(ProfileDraft())
        compose.onNodeWithText("Aceptar y continuar").assertIsNotEnabled()

        compose.onNodeWithText("Nombre").performTextInput("Lorena")
        compose.onNodeWithText("Día").performTextInput("12")
        compose.onNodeWithText("Mes").performTextInput("3")
        compose.onNodeWithText("Aceptar y continuar").assertIsNotEnabled()
        compose.onNodeWithText("Año").performTextInput("1990")

        compose.onNodeWithText("Aceptar y continuar").assertIsEnabled().performClick()
        assertEquals(listOf("Lorena" to LocalDate.of(1990, 3, 12)), saved)
    }

    @Test
    fun `a date that does not exist is said, only once it is whole, and cannot be agreed to`() {
        showFields(ProfileDraft("Lorena"))
        compose.onNodeWithText("Día").performTextInput("31")
        compose.onNodeWithText("Mes").performTextInput("2")
        assertEquals(0, count("Esa fecha no existe"))

        compose.onNodeWithText("Año").performTextInput("1990")
        compose.onNodeWithText("Esa fecha no existe").assertIsDisplayed()
        compose.onNodeWithText("Aceptar y continuar").assertIsNotEnabled()
    }

    @Test
    fun `what is said to be sent is on the screen before the button that agrees to it`() {
        showFields(ProfileDraft())
        compose.onNodeWithText("Qué se envía a la IA de Google").assertIsDisplayed()
        compose.onNodeWithText("Sin perfil no se guarda ninguna cuenta").assertIsDisplayed()
        compose.onNodeWithText("Ahora no").assertIsEnabled()
    }

    @Test
    fun `the fields take digits for the date and no more than a name's length`() {
        val draft = ProfileDraft()
        draft.typeDay("1a2b3")
        draft.typeMonth(" 07x")
        draft.typeYear("19905")
        draft.typeName("A".repeat(60))

        assertEquals("12", draft.day)
        assertEquals("07", draft.month)
        assertEquals("1990", draft.year)
        assertEquals(20, draft.name.length)
        assertEquals(LocalDate.of(1990, 7, 12), draft.date(TODAY))
        assertNull(ProfileDraft("Ana").date(TODAY))
    }

    @Test
    fun `whoever has just signed in is asked who they are, and not now takes the account back`() {
        var state by mutableStateOf(signedIn(ProfileState.Missing))
        var declined = 0
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    AccountPrompts(
                        state = state,
                        onSaveProfile = { _, _ -> ProfileOutcome.FAILED },
                        onDeclineProfile = {
                            declined++
                            state = state.copy(account = AccountState.SignedOut, profile = ProfileState.Unknown)
                        },
                        onOpenPrivacyPolicy = {},
                        onAgeNoticeSeen = {}
                    )
                }
            }
        }
        compose.onNodeWithText("¿Quién pregunta?").assertIsDisplayed()
        // Google's name for the account, down to its first word, is where the name starts.
        compose.onNodeWithText("Lorena").assertIsDisplayed()

        compose.onNodeWithText("Ahora no").performClick()
        compose.waitForIdle()

        assertEquals(1, declined)
        assertEquals(0, count("¿Quién pregunta?"))
    }

    @Test
    fun `an account with a profile is asked nothing, and one too young is told there is none for them`() {
        var state by mutableStateOf(signedIn(ProfileState.Unknown))
        var seen = 0
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    AccountPrompts(
                        state = state,
                        onSaveProfile = { _, _ -> ProfileOutcome.FAILED },
                        onDeclineProfile = {},
                        onOpenPrivacyPolicy = {},
                        onAgeNoticeSeen = {
                            seen++
                            state = state.copy(ageNotice = false)
                        }
                    )
                }
            }
        }
        assertEquals(0, count("¿Quién pregunta?"))

        state = state.copy(account = AccountState.SignedOut, underAge = true, ageNotice = true)
        compose.onNodeWithText("No puedes tener cuenta").assertIsDisplayed()
        compose.onNodeWithText("Entendido").performClick()
        compose.waitForIdle()

        assertEquals(1, seen)
        assertEquals(0, count("No puedes tener cuenta"))
    }

    private fun signedIn(profile: ProfileState) = EightBallUiState(
        settings = UserSettings(language = AppLanguage.SPANISH),
        config = AppConfig(accountEnabled = true),
        account = AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com"),
        profile = profile
    )

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 11, 13)
    }
}
