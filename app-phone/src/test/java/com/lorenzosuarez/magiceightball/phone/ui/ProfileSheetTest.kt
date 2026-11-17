package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
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
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

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

    /** The window the fields are shown in, which is what the keyboard on the screen types through. */
    private lateinit var window: View

    /** The fields on the sheet's own ground, as the app shows them: some of what follows is looked at in a picture. */
    private fun showFields(draft: ProfileDraft, firstTime: Boolean = true, dark: Boolean = false) = compose.setContent {
        window = LocalView.current
        MagicEightBallPhoneTheme(dark = dark) {
            ProvideAppLanguage(AppLanguage.SPANISH) {
                Surface(color = MaterialTheme.colorScheme.sheetGround) {
                    ProfileContent(
                        draft = draft,
                        firstTime = firstTime,
                        today = TODAY,
                        busy = false,
                        trouble = null,
                        onSave = { name, date -> saved += name to date },
                        onSecondary = {},
                        onOpenPrivacyPolicy = {},
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
        }
    }

    private fun count(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().size

    /** Where the cursor is in a field, or what of it is selected. */
    private fun selected(from: Int, to: Int = from) =
        SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(from, to))

    private fun picture(name: String) = compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")

    /**
     * The keyboard on the screen, as the field that has the focus meets it: one connection, given
     * when the field takes the focus and typed through until it loses it.
     */
    private fun keyboard(): InputConnection =
        compose.runOnUiThread { checkNotNull(window.onCreateInputConnection(EditorInfo())) }

    private fun InputConnection.does(what: InputConnection.() -> Unit) {
        compose.runOnUiThread { what() }
        compose.waitForIdle()
    }

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
    fun `a date that cannot be one is said, only once it is whole, and cannot be agreed to`() {
        showFields(ProfileDraft("Lorena"))
        compose.onNodeWithText("Día").performTextInput("31")
        compose.onNodeWithText("Mes").performTextInput("2")
        assertEquals(0, count("Revisa la fecha"))

        compose.onNodeWithText("Año").performTextInput("1990")
        compose.onNodeWithText("Revisa la fecha").assertIsDisplayed()
        compose.onNodeWithText("Aceptar y continuar").assertIsNotEnabled()
    }

    @Test
    fun `a number of the date typed whole hands the cursor to the next, and the last keeps it`() {
        val draft = ProfileDraft("Lorena")
        showFields(draft)
        val (day, month, year) = listOf("Día", "Mes", "Año").map { compose.onNodeWithText(it) }

        day.performTextInput("15")
        month.assertIsFocused()
        // A month that none of two digits begins with is whole at its first.
        month.performTextInput("3")
        year.assertIsFocused()
        year.performTextInput("1990")
        year.assertIsFocused()
        assertEquals(LocalDate.of(1990, 3, 15), draft.date(TODAY))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `deleting in an empty number goes back to the one before, to go on deleting there`() {
        val draft = ProfileDraft("Lorena")
        showFields(draft)
        val (day, month) = listOf("Día", "Mes").map { compose.onNodeWithText(it) }
        day.performTextInput("15")

        month.performKeyInput { pressKey(Key.Backspace) }
        day.assertIsFocused()
        // With the cursor at its end, and nothing in it selected: the next touch of the key takes one digit.
        day.assert(selected(2))
        day.performKeyInput { pressKey(Key.Backspace) }
        assertEquals("1", draft.day)
        day.assertIsFocused()

        // The first has none before it: the key does nothing there, and nothing is thrown out.
        day.performKeyInput { pressKey(Key.Backspace) }
        day.performKeyInput { pressKey(Key.Backspace) }
        day.assertIsFocused()
        assertEquals("Lorena", draft.name)
    }

    @Test
    fun `the keyboard on the screen goes back too, whichever way it says that it deletes`() {
        val draft = ProfileDraft("Lorena")
        showFields(draft)
        val (day, month, year) = listOf("Día", "Mes", "Año").map { compose.onNodeWithText(it) }
        day.performTextInput("15")
        month.assertIsFocused()

        // Some send the key itself when there is nothing before the cursor.
        keyboard().does {
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }
        day.assertIsFocused().assert(selected(2))
        // Where there is something they ask for it to be deleted, a digit at a time, and what is typed after goes on.
        val inTheDay = keyboard()
        inTheDay.does { deleteSurroundingText(1, 0) }
        assertEquals("1", draft.day)
        inTheDay.does { commitText("6", 1) }
        month.assertIsFocused()
        keyboard().does { commitText("3", 1) }
        year.assertIsFocused()

        // Others ask the same of an empty field, which the field never hears of: it goes back all the same,
        // and takes nothing from the number it goes back into.
        keyboard().does { deleteSurroundingText(1, 0) }
        month.assertIsFocused().assert(selected(1))
        assertEquals(listOf("16", "3", ""), listOf(draft.day, draft.month, draft.year))

        // Asked twice before the keyboard has seen the cursor go, as when its key is held down: the
        // second is nobody's. The month is come back into once, and by a touch is selected whole as ever.
        year.performClick()
        keyboard().does {
            deleteSurroundingText(1, 0)
            deleteSurroundingText(1, 0)
        }
        month.assertIsFocused()
        assertEquals("3", draft.month)
        day.performClick()
        month.performClick()
        month.assert(selected(0, 1))
    }

    @Test
    fun `the keyboard on the screen types a whole number through the connection it was first given`() {
        val draft = ProfileDraft("Lorena")
        showFields(draft)
        compose.onNodeWithText("Año").performClick()

        // From the instant of the touch: a digit that comes before the field has selected what it held
        // is not typed over by the next. And a field that started again with the keyboard at every key
        // would not hear the second.
        val inTheYear = keyboard()
        "1990".forEach { digit -> inTheYear.does { commitText(digit.toString(), 1) } }
        assertEquals("1990", draft.year)
    }

    @Test
    fun `a number come back into is selected whole, and what is typed takes its place`() {
        val draft = ProfileDraft("Lorena", LocalDate.of(1990, 3, 15))
        showFields(draft, firstTime = false)
        val (day, month) = listOf("Día", "Mes").map { compose.onNodeWithText(it) }

        // By a touch, which puts its own cursor down first: it is selected all the same.
        day.performClick()
        day.assert(selected(0, 2))
        day.performTextInput("7")
        assertEquals("7", draft.day)
        // And it goes on as it did the first time, to a month that is selected in its turn.
        month.assertIsFocused()
        month.assert(selected(0, 1))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(qualifiers = "w432dp-h768dp-400dpi")
    fun `a day corrected hands the cursor to a month that is selected whole`() =
        corrected(dark = false, "phone_145_profile_month_selected_spanish")

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(qualifiers = "w432dp-h768dp-night-400dpi")
    fun `a day corrected hands the cursor to a month that is selected whole, in the dark too`() =
        corrected(dark = true, "phone_146_profile_month_selected_dark")

    private fun corrected(dark: Boolean, picture: String) {
        val draft = ProfileDraft("Lorena", LocalDate.of(1990, 3, 15))
        showFields(draft, firstTime = false, dark = dark)

        compose.onNodeWithText("Día").performTextInput("16")
        compose.onNodeWithText("Mes").assertIsFocused().assert(selected(0, 1))
        assertEquals(LocalDate.of(1990, 3, 16), draft.date(TODAY))
        picture(picture)
    }

    @Test
    fun `what is said to be sent is on the screen before the button that agrees to it`() {
        showFields(ProfileDraft())
        compose.onNodeWithText("Qué se envía a Gemini, de Google").assertIsDisplayed()
        // The other way out says what it is: going on with no account.
        compose.onNodeWithText("Continuar sin cuenta").assertIsEnabled()
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
    fun `whoever has just signed in is asked who they are, and going on without an account takes it back`() {
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
                            AccountOutcome.DONE
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

        compose.onNodeWithText("Continuar sin cuenta").performClick()
        compose.waitForIdle()

        assertEquals(1, declined)
        assertEquals(0, count("¿Quién pregunta?"))
    }

    @Test
    fun `on the sheet itself, the keyboard types the date in one go and takes a slip back the same way`() {
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    AccountPrompts(
                        state = signedIn(ProfileState.Missing),
                        onSaveProfile = { name, date ->
                            saved += name to date
                            ProfileOutcome.FAILED
                        },
                        onDeclineProfile = { AccountOutcome.DONE },
                        onOpenPrivacyPolicy = {},
                        onAgeNoticeSeen = {}
                    )
                }
            }
        }
        val (day, month, year) = listOf("Día", "Mes", "Año").map { compose.onNodeWithText(it) }
        day.performClick()
        compose.waitForIdle()
        // The sheet is a window of its own, and it is into that one that the keyboard types.
        window = checkNotNull(checkNotNull(ShadowDialog.getLatestDialog().window).decorView.composed())

        val inTheDay = keyboard()
        "15".forEach { digit -> inTheDay.does { commitText(digit.toString(), 1) } }
        month.assertIsFocused()
        // The day was a slip: taken back from the month, which has nothing in it yet, and typed again.
        keyboard().does { deleteSurroundingText(1, 0) }
        day.assertIsFocused()
        val backInTheDay = keyboard()
        backInTheDay.does { deleteSurroundingText(1, 0) }
        backInTheDay.does { commitText("6", 1) }
        month.assertIsFocused()
        val inTheMonth = keyboard()
        "12".forEach { digit -> inTheMonth.does { commitText(digit.toString(), 1) } }
        year.assertIsFocused()
        val inTheYear = keyboard()
        "1990".forEach { digit -> inTheYear.does { commitText(digit.toString(), 1) } }

        compose.onNodeWithText("Aceptar y continuar").performClick()
        compose.waitForIdle()
        assertEquals(listOf("Lorena" to LocalDate.of(1990, 12, 16)), saved)
    }

    /** Where a window is drawn by Compose, which is what the keyboard is connected to. */
    private fun View.composed(): View? = when (this) {
        is ViewRootForTest -> this
        is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).composed() }
        else -> null
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
                        onDeclineProfile = { AccountOutcome.DONE },
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
