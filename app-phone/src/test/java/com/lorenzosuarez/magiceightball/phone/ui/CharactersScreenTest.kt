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

    private val used = mutableListOf<CharacterListing>()
    private var closes = 0
    private var creates = 0
    private val edited = mutableListOf<CharacterListing.Yours>()
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
                onUse = { used += it },
                onClose = { closes++ },
                onCreate = { creates++ },
                onEdit = { edited += it }
            )
        }
    }

    private fun scrollTo(text: String) = compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))

    private fun count(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().size

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
    fun `a touch opens a character to be looked at, and it answers only once that is asked for`() {
        show()
        scrollTo("Classic")
        compose.onNodeWithText("Classic").performClick()

        // Opened, with whose it is and something it would say; nothing has been chosen by brushing past it.
        compose.onNodeWithText("A Magic AI Ball original").assertIsDisplayed()
        assertTrue(used.isEmpty())

        compose.onNodeWithText("Use this character").performClick()
        compose.waitForIdle()
        assertEquals(listOf<CharacterListing>(CharacterListing.Original(Magic8BallPersonality.NEUTRAL)), used)
        assertEquals(0, count("Use this character"))
    }

    @Test
    fun `the one that answers is the one marked, and opened it has nothing to ask for`() {
        show(chosenNow = CharacterListing.Original(Magic8BallPersonality.DOG).key)
        scrollTo("Dog")
        compose.onNodeWithText("Dog").assertIsSelected()
        scrollTo("Cat")
        compose.onNodeWithText("Cat").assertIsNotSelected()

        compose.onNodeWithText("Dog").performClick()
        // Said as what is so: there is nothing to ask for, and no button that would do nothing.
        compose.onNodeWithText("Answering now").assertIsDisplayed()
        assertEquals(0, count("Use this character"))
    }

    @Test
    fun `one of the user's own says what they wrote of it, and is corrected from where it is opened`() {
        show(query = CharactersQuery(yours = listOf(nemo)), chosenNow = CharacterListing.Yours(nemo).key)
        compose.onNodeWithText("Capitán Nemo").assertIsSelected()
        compose.onNodeWithText("Un capitán gruñón.").assertIsDisplayed()

        compose.onNodeWithText("Capitán Nemo").performClick()
        compose.onNodeWithText("Made by you").assertIsDisplayed()
        compose.onNodeWithText("Edit").performClick()
        compose.waitForIdle()
        assertEquals(listOf(CharacterListing.Yours(nemo)), edited)
        assertTrue(used.isEmpty())
    }

    @Test
    fun `a shelf shows its own kind`() {
        show(query = CharactersQuery(yours = listOf(nemo)))

        compose.onNodeWithText("Originals").performClick()
        compose.onNodeWithText("Originals").assertIsSelected()
        assertEquals(0, count("Capitán Nemo"))

        compose.onNodeWithText("Yours").performClick()
        compose.onNodeWithText("Capitán Nemo").assertIsDisplayed()
        assertEquals(0, count("Sarcastic"))
    }

    @Test
    fun `before there are any, the shelf of other people's says they are coming, and not that something failed`() {
        show()
        compose.onNodeWithText("Community").performClick()

        compose.onNodeWithText("The community is coming soon").assertIsDisplayed()
        assertEquals(0, count("Try again"))
    }

    @Test
    fun `an empty shelf of one's own says so, and leads to making one`() {
        show(query = CharactersQuery(shelf = CharacterShelf.YOURS))
        compose.onNodeWithText("You haven't made any yet").assertIsDisplayed()

        compose.onNodeWithText("Create a character").performClick()
        assertEquals(1, creates)
    }

    @Test
    fun `thousands of other people's are fetched only as far as they are read, and one is opened with who made it`() {
        val community = FakeCommunity(5_000)
        show(query = CharactersQuery(shelf = CharacterShelf.COMMUNITY), community = community)
        compose.onNodeWithText("Personaje 1").assertIsDisplayed()
        val atFirst = community.asked.sumOf { it.second }
        assertTrue("fetched $atFirst at first", atFirst < 200)

        scrollTo("Personaje 150")
        compose.onNodeWithText("Personaje 150").assertIsDisplayed()
        val fetched = community.asked.sumOf { it.second }
        assertTrue("fetched $fetched of 5000", fetched in 150..400)

        compose.onNodeWithText("Personaje 150").performClick()
        compose.onNodeWithText("by autora5").assertIsDisplayed()
        compose.onNodeWithText("Use this character").performClick()
        compose.waitForIdle()
        assertEquals("c:149", used.single().key)
    }

    @Test
    fun `other people's that cannot be fetched are said, and asked for again with a touch`() {
        val community = FakeCommunity(50, failing = true)
        show(query = CharactersQuery(shelf = CharacterShelf.COMMUNITY), community = community)
        compose.onNodeWithText("Couldn't load the characters").assertIsDisplayed()

        community.failing = false
        compose.onNodeWithText("Try again").performClick()
        compose.onNodeWithText("Personaje 1").assertIsDisplayed()
    }
}
