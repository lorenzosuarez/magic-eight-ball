package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Setting up a party as the user does it: a name, the keyboard's key, the next name.
 *
 * What the dialog holds is shown here on its own: a text field inside a dialog's window never
 * comes to rest on the JVM, so the dialog itself is tried on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
class PartyDialogTest {

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

    private fun show(draft: PartyDraft) = compose.setContent {
        MagicEightBallPhoneTheme(dark = true) { PartyFields(draft, focusOnOpen = false) }
    }

    private val field get() = compose.onNode(hasSetTextAction())

    @Test
    fun `names go in one at a time with the keyboard's key, and saving needs two`() {
        val draft = PartyDraft(emptyList())
        show(draft)
        compose.onNodeWithText("Add at least two players").assertIsDisplayed()
        compose.onNodeWithText("Add").assertIsNotEnabled()
        assertFalse(draft.canSave)

        field.performTextInput("ana")
        compose.onNodeWithText("Add").assertIsEnabled()
        field.performImeAction()
        compose.onNodeWithText("Ana").assertIsDisplayed()
        assertFalse(draft.canSave)

        field.performTextInput("Beto")
        field.performImeAction()
        compose.onNodeWithText("2 of 8 players").assertIsDisplayed()

        assertTrue(draft.canSave)
        assertEquals(listOf("Ana", "Beto"), draft.toSave())
    }

    @Test
    fun `several names typed at once are taken apart, and none is kept twice`() {
        val draft = PartyDraft(emptyList())
        show(draft)

        field.performTextInput("Ana, beto,ana,")

        compose.onNodeWithText("Ana").assertIsDisplayed()
        compose.onNodeWithText("Beto").assertIsDisplayed()
        compose.onNodeWithText("2 of 8 players").assertIsDisplayed()
        assertEquals("", draft.typed)
    }

    @Test
    fun `a touch takes a name off, and a name still in the field is saved with the rest`() {
        val draft = PartyDraft(listOf("Ana", "Beto", "Caro"))
        show(draft)

        compose.onNodeWithText("Beto").performClick()
        compose.onNodeWithText("Beto").assertDoesNotExist()

        field.performTextInput("Dani")

        assertEquals(listOf("Ana", "Caro", "Dani"), draft.toSave())
    }

    @Test
    fun `a party that is full takes no more names`() {
        val draft = PartyDraft(List(8) { "P$it" })
        show(draft)

        // A field that is off takes no text at all.
        compose.onNode(hasSetTextAction()).assertDoesNotExist()
        compose.onNodeWithText("8 of 8 players").assertIsDisplayed()

        draft.type("Extra")
        assertEquals(8, draft.toSave().size)
        assertFalse(draft.canAdd)
    }

    @Test
    fun `a draft comes back as it was left`() {
        val draft = PartyDraft(listOf("Ana", "Beto")).apply { type("Ca") }

        val saved = with(PartyDraft.Saver) { androidx.compose.runtime.saveable.SaverScope { true }.save(draft) }
        val restored = PartyDraft.Saver.restore(saved!!)!!

        assertEquals(listOf("Ana", "Beto"), restored.players)
        assertEquals("Ca", restored.typed)
    }
}
