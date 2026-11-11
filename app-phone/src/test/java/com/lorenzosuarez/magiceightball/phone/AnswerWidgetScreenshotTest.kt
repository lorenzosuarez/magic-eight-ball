package com.lorenzosuarez.magiceightball.phone

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The widget's layout in its two states, side by side, at the size of two cells by two. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class AnswerWidgetScreenshotTest {

    @Test
    fun widget_at_rest_and_with_an_answer() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val side = (CELLS_DP * activity.resources.displayMetrics.density).toInt()
        val row = LinearLayout(activity).apply { setBackgroundColor(0xFF3A5A40.toInt()) }
        listOf<String?>(null, "Mis fuentes se ríen.").forEach { answer ->
            val widget = activity.layoutInflater.inflate(R.layout.widget_answer, row, false)
            if (answer != null) {
                widget.findViewById<ImageView>(R.id.widget_face).setImageResource(R.drawable.widget_die)
                widget.findViewById<TextView>(R.id.widget_text).apply {
                    visibility = View.VISIBLE
                    text = answer
                }
            }
            row.addView(widget, LinearLayout.LayoutParams(side, side))
        }
        activity.setContentView(row, FrameLayout.LayoutParams(side * 2, side))
        row.captureRoboImage("build/outputs/roborazzi/widget_1_rest_and_answer.png")
    }

    private companion object {
        const val CELLS_DP = 150
    }
}
