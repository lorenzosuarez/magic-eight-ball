package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.captureRoboImage
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.Profile
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import com.lorenzosuarez.magiceightball.phone.config.AppConfig
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
 * A page's name: large at the top of what the page lists and, once that has been read past,
 * small in the bar over it, never both. Each page is read down, and looked at before and after.
 * `./gradlew :app-phone:recordRoborazziDebug` writes the pictures to
 * `app-phone/build/outputs/roborazzi/`; a plain run checks which of its two names a page has.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = PHONE_DARK)
class LargeTitleTest {

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
    fun `what was asked before is under the page's name, which is in the bar once it is read past`() {
        compose.setContent { Pages(SIGNED_IN.copy(history = ASKED), HISTORY_PAGE) }
        assertNamed("Recientes", large = true)
        picture("phone_150_history_named_spanish_dark")

        answers().performScrollToIndex(3)
        assertNamed("Recientes", large = false)
        picture("phone_151_history_read_down_spanish_dark")

        answers().performScrollToIndex(0)
        assertNamed("Recientes", large = true)
    }

    @Test
    fun `the options are under the page's name, which is in the bar once it is read past`() {
        compose.setContent { Pages(SIGNED_IN, OPTIONS_PAGE) }
        assertNamed("Opciones", large = true)
        picture("phone_152_options_named_spanish_dark")

        readDown()
        assertNamed("Opciones", large = false)
        picture("phone_153_options_read_down_spanish_dark")

        readUp()
        assertNamed("Opciones", large = true)
    }

    @Test
    fun `a subject of the options is under its own name, which is beside the way back once it is read past`() {
        compose.setContent { Pages(SIGNED_IN, OPTIONS_PAGE, OptionsSection.LICENSES) }
        assertNamed("Licencias de código abierto", large = true)
        picture("phone_154_subject_named_spanish_dark")

        readDown()
        assertNamed("Licencias de código abierto", large = false)
        picture("phone_155_subject_read_down_spanish_dark")
    }

    @Test
    fun `the characters are under the screen's name, and the shelves stay under the bar when it has left`() {
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) { CharactersShown(onCreate = {}) }
            }
        }
        assertNamed("Personajes", large = true)
        picture("phone_156_characters_named_spanish_dark")

        readDown()
        assertNamed("Personajes", large = false)
        compose.onNodeWithText("Comunidad").assert(isDisplayedUnderTheBar())
        picture("phone_157_characters_read_down_spanish_dark")

        readUp()
        assertNamed("Personajes", large = true)
    }

    /** A shelf that is short, or has nothing on it, could not hold the name away if the name were listed with it. */
    @Test
    fun `picking a shelf moves nothing under the hand, whether the name is there or has left`() {
        compose.setContent {
            MagicEightBallPhoneTheme {
                ProvideAppLanguage(AppLanguage.SPANISH) { CharactersShown(onCreate = {}) }
            }
        }
        val shelves = { compose.onNodeWithText("Comunidad").getUnclippedBoundsInRoot().top }
        val named = shelves()
        compose.onNodeWithText("Tuyos").performClick()
        assertEquals(named, shelves())
        compose.onNodeWithText("Todos").performClick()

        readDown()
        val away = shelves()
        assertTrue("the shelves went up with the name", away < named)
        listOf("Tuyos", "Comunidad", "Originales").forEach { shelf ->
            compose.onNodeWithText(shelf).performClick()
            assertEquals(shelf, away, shelves())
            assertNamed("Personajes", large = false)
        }
    }

    @Test
    fun `a subject opened from options read down has its own name alone, and the way back leads to the page as it was`() {
        compose.setContent { Pages(SIGNED_IN, OPTIONS_PAGE) }
        readDown()
        assertNamed("Opciones", large = false)

        compose.onNodeWithText("Licencias de código abierto").performClick()
        assertNamed("Licencias de código abierto", large = true)
        assertEquals(0, seen("Opciones").size)

        compose.onNodeWithContentDescription("Atrás").performClick()
        assertNamed("Opciones", large = false)
    }

    @Test
    fun `a page that comes back read down has its name in the bar`() {
        compose.setContent { Pages(SIGNED_IN.copy(history = ASKED), HISTORY_PAGE) }
        answers().performScrollToIndex(3)
        assertNamed("Recientes", large = false)

        // To the ball and back: the page was put away meanwhile, and is as it was left. Its name is
        // in the bar as the page is laid out again, before a frame has gone by: the bar does not
        // wait to be told so again, which would be the name fading in over a page already there.
        pages().performScrollToIndex(BALL_PAGE)
        assertEquals(0, seen("Recientes").size)
        compose.mainClock.autoAdvance = false
        pages().performScrollToIndex(HISTORY_PAGE)
        assertNamed("Recientes", large = false)
    }

    @Test
    fun `a page brought back by the system read down has its name in the bar`() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { Pages(SIGNED_IN, OPTIONS_PAGE) }
        readDown()

        restoration.emulateSavedInstanceStateRestore()
        assertNamed("Opciones", large = false)
    }

    @Test
    fun `with the last answer gone the name is back on the page, and no longer in the bar`() {
        var history by mutableStateOf(ASKED)
        compose.setContent { Pages(SIGNED_IN.copy(history = history), HISTORY_PAGE) }
        answers().performScrollToIndex(3)
        assertNamed("Recientes", large = false)

        history = emptyList()
        assertNamed("Recientes", large = true)
    }

    /** The three pages as the app wires them, with [page] in front and, of the options, [subject] open. */
    @Composable
    private fun Pages(state: EightBallUiState, page: Int, subject: OptionsSection? = null) {
        MagicEightBallPhoneTheme {
            ProvideAppLanguage(state.settings.language) {
                val nav = remember { OptionsNav(subject) }
                EightBallContent(
                    state = state,
                    now = NOW,
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
                    onClearHistory = {},
                    section = nav.section?.let { open -> SectionBar(stringResource(open.title)) { nav.section = null } },
                    options = {
                        SettingsContent(
                            state = state,
                            onLanguageSelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {},
                            nav = nav
                        )
                    },
                    initialPage = page
                )
            }
        }
    }

    /**
     * The pages side by side, and the answers listed on the one of them that is in front. These are
     * told where to go, not dragged there: dragged, a list gets its next rows ready on a frame that
     * never comes on the JVM (see [LocalListPrefetch]), and the test would never come to rest.
     */
    private fun pages() = compose.onAllNodes(hasScrollToIndexAction()).onFirst()

    private fun answers() = compose.onAllNodes(hasScrollToIndexAction()).onLast()

    /** Reads the page down as a thumb does: a drag up the middle of it, let go, and left to come to rest. */
    private fun readDown() {
        compose.onRoot().performTouchInput { swipeUp(startY = height * 0.8f, endY = height * 0.3f) }
        compose.waitForIdle()
    }

    /** And back to its top, with a drag to spare. */
    private fun readUp() {
        repeat(3) { compose.onRoot().performTouchInput { swipeDown(startY = height * 0.3f, endY = height * 0.9f) } }
        compose.waitForIdle()
    }

    /** Every place where [name] can be read on the screen now. */
    private fun seen(name: String) = compose.onAllNodesWithText(name).fetchSemanticsNodes().indices
        .map { compose.onAllNodesWithText(name)[it] }
        .filter { it.isDisplayed() }

    /** The page says [name] once, as its heading: under the bar when [large], and in the bar otherwise. */
    private fun assertNamed(name: String, large: Boolean) {
        compose.waitForIdle()
        val places = seen(name)
        assertEquals("places where \"$name\" is read", 1, places.size)
        places.single().assert(isHeading())
        assertEquals("\"$name\" is under the bar", large, places.single().getUnclippedBoundsInRoot().top >= TOP_BAR_HEIGHT)
    }

    private fun isDisplayedUnderTheBar() = SemanticsMatcher("is right under the bar") { node ->
        with(compose.density) { node.boundsInRoot.top.toDp() } in TOP_BAR_HEIGHT..TOP_BAR_HEIGHT + 16.dp
    }

    private fun picture(name: String) = compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
}

/** 1080 x 1920 px, as the app's other pictures are, in the dark. */
private const val PHONE_DARK = "w432dp-h768dp-night-400dpi"

private const val NOW = 1_800_000_000_000L
private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE

/** As the pages are where there are accounts and something is sold, with somebody signed in. */
private val SIGNED_IN = EightBallUiState(
    settings = UserSettings(language = AppLanguage.SPANISH),
    aiAnswersAvailable = true,
    config = AppConfig(accountEnabled = true, premiumEnabled = true),
    account = AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com"),
    profile = ProfileState.Present(Profile("Lorena", LocalDate.of(1990, 3, 12)))
)

/** More answers than a page holds, as people ask and as the characters answer. */
private val ASKED = listOf(
    HistoryEntry(9, "¿Me va a escribir esta noche?", "Ni con marea alta.", NOW - 20_000L, fromAi = true, style = Magic8BallPersonality.PIRATE),
    HistoryEntry(8, "¿Pido pizza?", "Tu estómago ya decidió.", NOW - 12 * MINUTE, fromAi = true, style = Magic8BallPersonality.DETECTIVE),
    HistoryEntry(7, "", "Es cierto.", NOW - 40 * MINUTE, fromAi = false),
    HistoryEntry(6, "¿Apruebo el examen del lunes?", "Sí. Y abrígate.", NOW - 3 * HOUR, fromAi = true, style = Magic8BallPersonality.GRANDMA),
    HistoryEntry(5, "¿Me compro las zapatillas?", "Mercurio retrógrado dice no.", NOW - 5 * HOUR, fromAi = true, style = Magic8BallPersonality.ASTRO),
    HistoryEntry(4, "", "No cuentes con ello.", NOW - 26 * HOUR, fromAi = false),
    HistoryEntry(3, "¿Hoy toca gimnasio?", "Ni en tus sueños.", NOW - 2 * 24 * HOUR, fromAi = true, style = Magic8BallPersonality.SARCASTIC),
    HistoryEntry(2, "¿Le pido un aumento a mi jefe?", "Pregunta de nuevo más tarde.", NOW - 3 * 24 * HOUR, fromAi = false),
    HistoryEntry(1, "¿Llueve mañana?", "Sin duda.", NOW - 4 * 24 * HOUR, fromAi = false)
)
