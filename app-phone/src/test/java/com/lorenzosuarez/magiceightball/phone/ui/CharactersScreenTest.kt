package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.example.magiceightball.core.domain.repository.NoCommunityCharacters
import com.lorenzosuarez.magiceightball.phone.characters.CharactersQuery
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

/** The screen of characters, as the user meets it: a touch, and what comes of it. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class CharactersScreenTest {

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

    private val chosen = mutableListOf<CharacterListing>()
    private var closes = 0
    private var creates = 0
    private val edited = mutableListOf<CustomCharacter>()
    private val nemo = CustomCharacter(id = "1", name = "Capitán Nemo", description = "Un capitán gruñón.")

    private fun show(
        query: CharactersQuery = CharactersQuery(),
        community: CommunityCharacters = NoCommunityCharacters,
        chosenNow: String? = CharacterListing.Original(Magic8BallPersonality.SARCASTIC).key
    ) = compose.setContent {
        MagicEightBallPhoneTheme(dark = true) {
            CharactersShown(
                query = query,
                community = community,
                chosen = chosenNow,
                onChoose = { chosen += it },
                onClose = { closes++ },
                onCreate = { creates++ },
                onEdit = { edited += it }
            )
        }
    }

    private fun scrollTo(text: String) = compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))

    @Test
    fun `the way out, and the way to make a character, are at the top and say what they do`() {
        show()
        compose.onNodeWithText("Characters").assertIsDisplayed()

        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNodeWithContentDescription("Create a character").performClick()

        assertEquals(1, closes)
        assertEquals(1, creates)
    }

    @Test
    fun `a card shows that it was touched at once, and tells who was chosen`() {
        show()
        scrollTo("Classic")
        compose.onNodeWithText("Classic").performClick()

        // Nothing here saves anything: what is chosen stays as it was, and the touch shows all the same.
        compose.onNodeWithText("Classic").assertIsSelected()
        scrollTo("Sarcastic")
        compose.onNodeWithText("Sarcastic").assertIsNotSelected()
        assertEquals(listOf<CharacterListing>(CharacterListing.Original(Magic8BallPersonality.NEUTRAL)), chosen)
    }

    @Test
    fun `the one that answers is the one marked, however far down it is`() {
        show(chosenNow = CharacterListing.Original(Magic8BallPersonality.DOG).key)
        scrollTo("Dog")
        compose.onNodeWithText("Dog").assertIsSelected()
    }

    @Test
    fun `one of the user's own is chosen by its card and corrected by its pencil`() {
        show(query = CharactersQuery(yours = listOf(nemo)), chosenNow = CharacterListing.Yours(nemo).key)
        compose.onNodeWithText("Capitán Nemo").assertIsSelected()
        // In their own words, which is what tells one of theirs from another.
        compose.onNodeWithText("Un capitán gruñón.").assertIsDisplayed()

        compose.onNodeWithContentDescription("Edit Capitán Nemo").performClick()
        assertEquals(listOf(nemo), edited)
        assertTrue(chosen.isEmpty())
    }

    @Test
    fun `a shelf shows its own kind, and with nobody else's there is no shelf for them`() {
        show(query = CharactersQuery(yours = listOf(nemo)))
        assertEquals(0, compose.onAllNodesWithText("Community").fetchSemanticsNodes().size)

        compose.onNodeWithText("Originals").performClick()
        compose.onNodeWithText("Originals").assertIsSelected()
        assertEquals(0, compose.onAllNodesWithText("Capitán Nemo").fetchSemanticsNodes().size)

        compose.onNodeWithText("Yours").performClick()
        compose.onNodeWithText("Capitán Nemo").assertIsDisplayed()
        assertEquals(0, compose.onAllNodesWithText("Sarcastic").fetchSemanticsNodes().size)
    }

    @Test
    fun `an empty shelf of one's own says so, and leads to making one`() {
        show(query = CharactersQuery(shelf = CharacterShelf.YOURS))
        compose.onNodeWithText("You haven't made any yet").assertIsDisplayed()

        compose.onNodeWithText("Create a character").performClick()
        assertEquals(1, creates)
    }

    @Test
    fun `thousands of other people's are fetched only as far as they are read`() {
        val community = FakeCommunity(5_000)
        show(query = CharactersQuery(shelf = CharacterShelf.COMMUNITY), community = community)
        compose.onNodeWithText("Personaje 1").assertIsDisplayed()
        compose.onAllNodesWithText("by autora0")[0].assertIsDisplayed()
        val atFirst = community.asked.sumOf { it.second }
        assertTrue("fetched $atFirst at first", atFirst < 200)

        scrollTo("Personaje 150")
        compose.onNodeWithText("Personaje 150").assertIsDisplayed()
        val fetched = community.asked.sumOf { it.second }
        assertTrue("fetched $fetched of 5000", fetched in 150..400)
    }

    @Test
    fun `other people's that cannot be fetched are said, and asked for again with a touch`() {
        val community = FakeCommunity(50, failing = true)
        show(query = CharactersQuery(shelf = CharacterShelf.COMMUNITY), community = community)
        compose.onNodeWithText("Couldn't load them. Check your connection").assertIsDisplayed()

        community.failing = false
        compose.onNodeWithText("Try again").performClick()
        compose.onNodeWithText("Personaje 1").assertIsDisplayed()
    }
}
