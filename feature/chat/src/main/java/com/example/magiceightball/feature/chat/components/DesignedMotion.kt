package com.example.magiceightball.feature.chat.components

import androidx.compose.ui.MotionDurationScale

/**
 * Motion at the speed it was designed for, whatever the system's animation scale is.
 *
 * The die is the app: how it thinks and answers is told by how it moves, and with the system's
 * animations turned off it would only jump from one picture to the next. This is a product
 * decision, and it covers the die alone, as it does the ball on the phone.
 */
internal object DesignedMotion : MotionDurationScale {
    override val scaleFactor: Float get() = 1f
}
