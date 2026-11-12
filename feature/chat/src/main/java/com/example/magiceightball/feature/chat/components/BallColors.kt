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

    /**
     * Behind everything: black, as a watch's screens are. A screen that lights only what it
     * shows costs the battery nothing for the rest.
     */
    val Background = Color.Black

    /** The "8" on its white disc. */
    val Eight = Color(BrandPalette.BALL_CLOSE_BOTTOM)

    /** The window in the ball, from its centre to its edge. */
    val WindowCenter = Color(BrandPalette.WINDOW_CENTER)
    val WindowEdge = Color(BrandPalette.WINDOW_EDGE)

    /** The window's two borders, in the ball's own greys: a lighter edge and a darker one inside it. */
    // On black the lighter edge is all there is of the ball: it is what says where the window is.
    val WindowRim = Color(BrandPalette.BALL_TOP)
    val BezelTop = Color(0xFF05070B)
    val BezelBottom = Color(0xFF2B323D)

    /** The answer: white, on a die deep enough for it to be read at a glance. */
    val AnswerInk = Color.White

    /** The die while the ball thinks, and the die with its answer. */
    val DieThinking = Brush.verticalGradient(
        listOf(Color(BrandPalette.DIE_THINKING_TOP), Color(BrandPalette.DIE_THINKING_BOTTOM))
    )
    val DieAnswered = Brush.verticalGradient(listOf(Color(BrandPalette.DIE_TOP), Color(BrandPalette.DIE_BOTTOM)))
}
