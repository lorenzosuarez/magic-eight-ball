package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Every answer the app can show has to sit inside the die, whole and readable, whatever the
 * size of the ball: the bundled answers at a comfortable size, and the longest the AI is
 * allowed to give at least without being cut off.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class)
class AnswerFitTest {

    private val density = Density(density = 2.625f, fontScale = 1f)
    private val textMeasurer = TextMeasurer(
        createFontFamilyResolver(ApplicationProvider.getApplicationContext<Application>()),
        density,
        LayoutDirection.Ltr
    )

    @Test
    fun `every bundled answer fits at a readable size on every ball`() {
        val answers = bundledAnswers()
        assertTrue("expected the bundled answers, found ${answers.size}", answers.size > 200)

        val failures = buildList {
            for (diameter in BALL_DIAMETERS_PX) for (answer in answers) {
                val fit = fitAnswerInBall(answer, diameter, density, textMeasurer)
                if (fit == null || !fit.fits || fit.fontSize < DIE_TEXT_READABLE_SIZE - ROUNDING) {
                    add("$diameter px: \"$answer\" fits=${fit?.fits} size=${fit?.fontSize}")
                }
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun `the longest answers the AI may give are never cut off`() {
        val failures = buildList {
            for (diameter in BALL_DIAMETERS_PX) for (answer in LONGEST_AI_ANSWERS) {
                val fit = fitAnswerInBall(answer, diameter, density, textMeasurer)
                if (fit == null || !fit.fits) add("$diameter px: \"$answer\" size=${fit?.fontSize}")
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    /** Every string in the answers file, whatever language or style it is filed under. */
    private fun bundledAnswers(): List<String> {
        val strings = mutableListOf<String>()
        fun collect(node: Any?) {
            when (node) {
                is String -> strings += node
                is JSONArray -> for (i in 0 until node.length()) collect(node.get(i))
                is JSONObject -> node.keys().forEach { collect(node.get(it)) }
            }
        }
        collect(JSONObject(File(ANSWERS_FILE).readText()))
        return strings
    }

    private companion object {
        const val ROUNDING = 0.001f
        const val ANSWERS_FILE = "../core/data/src/main/assets/magic_8_ball_responses.json"

        /** The resting ball on a small phone, on a large one and on a tablet. */
        val BALL_DIAMETERS_PX = floatArrayOf(420f, 760f, 1100f)

        /** Six words, the most the app accepts, as long as words get. */
        val LONGEST_AI_ANSWERS = listOf(
            "Sorprendentemente, probablemente sí, aunque dependerá completamente.",
            "Absolutely, unquestionably, overwhelmingly, extraordinarily, undoubtedly yes.",
            "Desafortunadamente, las circunstancias actuales indican incertidumbre.",
            "Extraordinariamente."
        )
    }
}
