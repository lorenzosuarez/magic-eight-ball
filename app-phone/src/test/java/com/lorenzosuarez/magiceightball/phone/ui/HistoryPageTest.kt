package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
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

/** The history page as the user works it: scrolled through, swiped and tapped. */
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
    fun `a swipe to either side deletes that entry`() {
        show()

        // Further than the text is wide: most of the way across the screen.
        compose.onNodeWithText("Answer 1").performTouchInput { swipeRight(endX = centerX + SWIPE_PX) }
        compose.onNodeWithText("Answer 3").performTouchInput { swipeLeft(endX = centerX - SWIPE_PX) }
        compose.waitForIdle()

        assertEquals(listOf(entries[1], entries[3]), deleted)
    }

    @Test
    fun `an entry swiped away and put back stays, and is not deleted again`() {
        var shown by mutableStateOf(entries.take(5))
        compose.setContent {
            MagicEightBallPhoneTheme {
                HistoryContent(
                    entries = shown,
                    now = 0L,
                    onDelete = { entry ->
                        deleted += entry
                        shown = shown - entry
                    },
                    onClearAll = {}
                )
            }
        }

        compose.onNodeWithText("Answer 1").performTouchInput { swipeRight(endX = centerX + SWIPE_PX) }
        compose.waitForIdle()
        assertEquals(listOf(entries[1]), deleted)

        // "Undo": the same entry, with the same id, is in the list again.
        shown = entries.take(5)
        compose.waitForIdle()

        compose.onNodeWithText("Answer 1").assertIsDisplayed()
        assertEquals(listOf(entries[1]), deleted)
    }

    @Test
    fun `the delete button deletes its own entry`() {
        show()

        compose.onAllNodesWithContentDescription("Delete").onFirst().performClick()

        assertEquals(listOf(entries[0]), deleted)
    }

    private companion object {
        const val ENTRIES = 50
        const val SWIPE_PX = 600f
    }
}
