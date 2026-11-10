package com.example.magiceightball.core.common

/**
 * The colours the ball is drawn with, on the watch and on the phone alike, as ARGB values: a
 * near-black blue behind everything, cool greys for the ball and for a die that is still
 * thinking, and one accent, kept for the answer.
 *
 * Plain numbers rather than a UI toolkit's colour type, so that this module stays pure Kotlin
 * and each app wraps them in its own.
 */
object BrandPalette {
    /** The background, from the top of the screen to the bottom: never one flat colour. */
    const val BACKDROP_TOP = 0xFF0E1B27
    const val BACKDROP_MIDDLE = 0xFF040C16
    const val BACKDROP_BOTTOM = 0xFF01060C

    const val SURFACE = 0xFF030B14
    const val SURFACE_RAISED = 0xFF1A2632
    const val ON_SURFACE = 0xFFF2F5F8
    const val ON_SURFACE_QUIET = 0xFF9AA7B4

    /** The ball itself, lit from above. */
    const val BALL_TOP = 0xFF5C6773
    const val BALL_BOTTOM = 0xFF3F4853

    /** The window in the ball, from its centre to its edge. */
    const val WINDOW_CENTER = 0xFF131C28
    const val WINDOW_EDGE = 0xFF080D14

    /**
     * The window's two borders, both in the ball's own greys: a fine edge a little lighter than
     * the ball, where its surface turns in, and a darker one inside it, in the window's shadow.
     */
    const val WINDOW_RIM = 0xFF74808C
    const val WINDOW_RIM_INNER = 0xFF262E38

    /** The die while the ball thinks: no colour yet, only the ball's grey, a little lighter. */
    const val DIE_THINKING_TOP = 0xFF737E8A
    const val DIE_THINKING_BOTTOM = 0xFF5F6975

    /** The accent: the die once it carries an answer, and whatever is selected. */
    const val ACCENT = 0xFF7C96FF
    const val ACCENT_DEEP = 0xFF4558D6
    const val ON_ACCENT = 0xFFFFFFFF
}
