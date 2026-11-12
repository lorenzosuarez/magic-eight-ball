package com.example.magiceightball

import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnswerTimelineTest {

    private val atRest = text("8")
    private val answered = text("It is certain")

    @Test
    fun `a tile nobody has touched shows the ball at rest, for as long as it is there`() {
        val entry = answerTimeline(atRest, answered = null, nowMillis = NOW).timelineEntries.single()

        assertEquals("8", entry.shownText)
        assertNull(entry.validity)
    }

    @Test
    fun `an answer is shown from the touch that asked for it, and gives way to the ball at rest a minute later`() {
        val entries = answerTimeline(atRest, answered, nowMillis = NOW).timelineEntries
        val alwaysThere = entries.single { it.validity == null }
        val forNow = entries.single { it.validity != null }

        assertEquals("8", alwaysThere.shownText)
        assertEquals("It is certain", forNow.shownText)
        assertEquals(NOW, forNow.validity?.startMillis)
        assertEquals(NOW + 60_000L, forNow.validity?.endMillis)
    }

    private val TimelineBuilders.TimelineEntry.shownText: String?
        get() = (layout?.root as? LayoutElementBuilders.Text)?.text?.value

    private fun text(value: String): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Text.Builder().setText(value).build()

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
