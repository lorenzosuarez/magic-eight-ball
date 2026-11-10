package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The history page as the user works it: scrolled through and tapped. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
class HistoryPageTest {

    private val compose = createComposeRule()

    /**
     * The compose rule starts an empty activity, which only exists here: it is declared to
     * Robolectric before the rule runs, rather than in a manifest that would ship with the app.
     */
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

    private val entries = List(ENTRIES) { index ->
        HistoryEntry(id = (ENTRIES - index).toLong(), question = "", answer = "Answer $index", askedAtMillis = 0L, fromAi = false)
    }
    private val deleted = mutableListOf<HistoryEntry>()

    private fun show() = compose.setContent {
        MagicEightBallPhoneTheme {
            HistoryContent(entries = entries, now = 0L, onDelete = deleted::add, onClearAll = {})
        }
    }

    /** Rows are built ahead of being laid out while the list moves: none of that may fail. */
    @Test
    fun `the whole list can be scrolled through, there and back`() {
        show()
        val list = compose.onNode(hasScrollToIndexAction())

        for (index in entries.indices) list.performScrollToIndex(index)
        for (index in entries.indices.reversed()) list.performScrollToIndex(index)

        compose.onNodeWithText("Answer 0").assertIsDisplayed()
    }

    @Test
    fun `the delete button deletes its own entry`() {
        show()

        compose.onAllNodesWithContentDescription("Delete").onFirst().performClick()

        assertEquals(listOf(entries[0]), deleted)
    }

    private companion object {
        const val ENTRIES = 50
    }
}
