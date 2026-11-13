package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.core.app.ApplicationProvider
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The sheet of styles as the user works it: opened, chosen from and closed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h640dp-xhdpi")
class StylesSheetTest {

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

    private var selected by mutableStateOf(Magic8BallPersonality.SARCASTIC)
    private var open by mutableStateOf(true)
    private var closes = 0

    private fun show() = compose.setContent {
        MagicEightBallPhoneTheme(dark = true) {
            if (open) {
                StylesSheet(
                    selected = selected,
                    onSelected = { selected = it },
                    onClose = {
                        closes++
                        open = false
                    }
                )
            }
        }
    }

    @Test
    fun `the close button puts the sheet away`() {
        show()
        compose.onNodeWithText("Characters").assertIsDisplayed()

        compose.onNodeWithContentDescription("Close").performClick()
        compose.waitForIdle()

        assertEquals(1, closes)
        compose.onNodeWithText("Characters").assertDoesNotExist()
    }

    @Test
    fun `choosing a style takes it and puts the sheet away`() {
        show()

        // Chosen as an assistive technology would: the card may lie outside the half-open sheet.
        compose.onNodeWithText("Cat").performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()

        assertEquals(Magic8BallPersonality.CAT, selected)
        assertEquals(1, closes)
        compose.onNodeWithText("Characters").assertDoesNotExist()
    }

    @Test
    fun `the chosen style is the one marked`() {
        selected = Magic8BallPersonality.POET
        show()

        compose.onNodeWithText("Poet").performScrollTo().assertIsSelected()
    }
}
