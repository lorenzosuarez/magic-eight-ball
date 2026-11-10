package com.example.magiceightball.feature.chat.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.magiceightball.core.common.BrandPalette

/** The [BrandPalette] the watch shares with the phone, as the colours and brushes drawn here. */
internal object BallColors {
    val OnSurface = Color(BrandPalette.ON_SURFACE)
    val OnSurfaceQuiet = Color(BrandPalette.ON_SURFACE_QUIET)
    val Accent = Color(BrandPalette.ACCENT)
    val OnAccent = Color(BrandPalette.ON_ACCENT)

    /** Behind the settings: never one flat colour. */
    val Backdrop = Brush.verticalGradient(
        listOf(
            Color(BrandPalette.BACKDROP_TOP),
            Color(BrandPalette.BACKDROP_MIDDLE),
            Color(BrandPalette.BACKDROP_BOTTOM)
        )
    )

    /** The ball, which on the watch is the whole screen, lit from above. */
    val BallBottom = Color(BrandPalette.BALL_CLOSE_BOTTOM)
    val Ball = Brush.verticalGradient(listOf(Color(BrandPalette.BALL_CLOSE_TOP), BallBottom))

    /** The window in the ball, from its centre to its edge. */
    val WindowCenter = Color(BrandPalette.WINDOW_CENTER)
    val WindowEdge = Color(BrandPalette.WINDOW_EDGE)

    /** The window's two borders, in the ball's own greys: a lighter edge and a darker one inside it. */
    // A little lighter than the ball it is cut into, which here is the deeper one.
    val WindowRim = Color(BrandPalette.BALL_TOP)
    val BezelTop = Color(0xFF05070B)
    val BezelBottom = Color(0xFF2B323D)

    /** The answer as seen through the liquid: not quite white, not quite solid. */
    val AnswerInk = Color(0xFFDDE6FF).copy(alpha = 0.86f)

    /** The die while the ball thinks, and the die with its answer. */
    val DieThinking = Brush.verticalGradient(
        listOf(Color(BrandPalette.DIE_THINKING_TOP), Color(BrandPalette.DIE_THINKING_BOTTOM))
    )
    val DieAnswered = Brush.verticalGradient(listOf(Accent, Color(BrandPalette.ACCENT_DEEP)))
}
