package com.example.magiceightball.feature.chat

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Text
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What a finger on the ball means, on the JVM (no device or emulator): a tap, a hold, a swipe
 * to the pages under the ball, and a tap on the report under an answer.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w192dp-h192dp-round-xhdpi")
class ChatScreenGestureTest {

    private val compose = createComposeRule()

    @get:Rule
    val rules: RuleChain = onWatch(compose)

    /** What the screen made of each gesture, in order. */
    private val heard = mutableListOf<String>()

    @Test
    fun `where the app cannot listen a press asks, however long it lasts`() {
        compose.setContent { Ball(ChatState()) }

        compose.onRoot().performTouchInput { click() }
        compose.onRoot().performTouchInput { longClick() }

        assertEquals(listOf("tap", "tap"), heard)
    }

    @Test
    fun `where the app can listen a quick press asks and a long one is held for the question`() {
        compose.setContent { Ball(ChatState(aiAnswersAvailable = true, canListen = true)) }

        compose.onRoot().performTouchInput { click() }
        assertEquals(listOf("tap"), heard)

        compose.onRoot().performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(BEFORE_ANY_LONG_PRESS_MS)
        assertEquals(listOf("tap"), heard)

        compose.mainClock.advanceTimeBy(AFTER_ANY_LONG_PRESS_MS)
        assertEquals(listOf("tap", "hold"), heard)

        compose.onRoot().performTouchInput { up() }
        assertEquals(listOf("tap", "hold", "release"), heard)
    }

    @Test
    fun `a swipe to the pages under the ball is not a press`() {
        compose.setContent {
            MagicEightBallTheme {
                VerticalPager(rememberPagerState { 2 }) { page ->
                    if (page == 0) Ball(ChatState()) else Box(Modifier.fillMaxSize()) { Text(SETTINGS) }
                }
            }
        }

        compose.onRoot().performTouchInput { swipeUp() }

        compose.onNodeWithText(SETTINGS).assertIsDisplayed()
        assertEquals(emptyList<String>(), heard)

        // On the ball's own page, under the same pager, a tap is still a tap.
        compose.onRoot().performTouchInput { swipeDown() }
        compose.onRoot().performTouchInput { click() }
        assertEquals(listOf("tap"), heard)
    }

    @Test
    fun `the report under an answer the ai wrote has a finger's room and takes its own presses`() {
        val answer = AnswerText.Plain("Not a chance.", fromAi = true)
        compose.setContent {
            Ball(ChatState(ChatStateMachine.Completed(answer), aiAnswersAvailable = true, canListen = true))
        }

        compose.onNodeWithText("Report")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertEquals(listOf("report"), heard)

        // However long the finger stays on it, the ball does not take it for a question.
        compose.onNodeWithText("Report").performTouchInput { longClick() }
        assertEquals(listOf("report", "report"), heard)

        // Anywhere else the ball is still the ball: a tap asks again.
        compose.onRoot().performTouchInput { click(center) }
        assertEquals(listOf("report", "report", "tap"), heard)
    }

    @Test
    fun `once reported the answer says so, and a tap there does not ask the ball again`() {
        val answer = AnswerText.Plain("Not a chance.", fromAi = true)
        compose.setContent { Ball(ChatState(machineState = ChatStateMachine.Completed(answer, reported = true))) }

        compose.onNodeWithText("Report").assertDoesNotExist()
        compose.onNodeWithText("Reported").performClick()

        assertEquals(listOf("report"), heard)
    }

    @Test
    fun `the app's own answers show nothing to report`() {
        compose.setContent { Ball(ChatState(machineState = ChatStateMachine.Completed(AnswerText.Plain("Yes.")))) }
        compose.onNodeWithText("Report").assertDoesNotExist()

        compose.onRoot().performTouchInput { click(bottomCenter) }
        assertEquals(listOf("tap"), heard)
    }

    @Composable
    private fun Ball(state: ChatState) {
        MagicEightBallTheme {
            ChatScreenContent(
                state = state,
                onTap = { heard += "tap" },
                onHoldStart = { heard += "hold" },
                onHoldEnd = { heard += "release" },
                onHoldCancel = { heard += "cancel" },
                onReportAnswer = { heard += "report" }
            )
        }
    }

    private companion object {
        const val SETTINGS = "Settings"

        /** Shorter and longer than what any system takes for a long press. */
        const val BEFORE_ANY_LONG_PRESS_MS = 150L
        const val AFTER_ANY_LONG_PRESS_MS = 1_500L
    }
}
