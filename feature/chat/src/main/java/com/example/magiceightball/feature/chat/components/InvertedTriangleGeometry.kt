package com.example.magiceightball.feature.chat.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlin.math.max
import kotlin.math.min

/**
 * Geometry of the rounded, downward-pointing triangle of the prediction die.
 *
 * It is the single source of truth for both the outline that is drawn and the room that is
 * available to the content inside it, so the two can never drift apart.
 */
internal class InvertedTriangleGeometry(val size: Size) {

    val cornerRadius: Float = min(size.width, size.height) * CORNER_RADIUS_FRACTION

    /** Width of the triangle at its widest, just below the rounded top corners. */
    val maxWidth: Float = size.width - cornerRadius

    fun toPath(): Path {
        val width = size.width
        val height = size.height
        val radius = cornerRadius
        val halfRadius = radius / 2f
        val centerX = width / 2f

        return Path().apply {
            moveTo(radius, 0f)
            lineTo(width - radius, 0f)
            quadraticTo(width, 0f, width - halfRadius, radius)
            lineTo(centerX + halfRadius, height - radius)
            quadraticTo(centerX, height, centerX - halfRadius, height - radius)
            lineTo(halfRadius, radius)
            quadraticTo(0f, 0f, radius, 0f)
            close()
        }
    }

    /**
     * Highest y at which the triangle is at least [width] wide. Only the rounded top corners
     * make the triangle narrower towards the top.
     */
    fun highestYWithWidth(width: Float): Float = max(0f, width - (size.width - 2f * cornerRadius))

    /**
     * Lowest y at which the triangle is still at least [width] wide, or negative infinity if it
     * never is. Curved parts are approximated by their chords, which errs on the narrow side.
     */
    fun lowestYWithWidth(width: Float): Float {
        val radius = cornerRadius
        return when {
            width > maxWidth -> Float.NEGATIVE_INFINITY
            width >= radius -> {
                val sideSlope = (size.height - 2f * radius) / (size.width - 2f * radius)
                radius + (maxWidth - width) * sideSlope
            }
            else -> size.height - radius + (radius - max(0f, width)) / 2f
        }
    }

    /**
     * Width of the triangle at height [y], with the same approximations as above, which err on
     * the narrow side.
     */
    fun widthAt(y: Float): Float {
        val radius = cornerRadius
        return when {
            y < 0f || y > size.height -> 0f
            y < radius -> min(maxWidth, size.width - 2f * radius + y)
            y <= size.height - radius -> {
                val sideSlope = (size.height - 2f * radius) / (size.width - 2f * radius)
                maxWidth - (y - radius) / sideSlope
            }
            else -> max(0f, radius - 2f * (y - (size.height - radius)))
        }
    }

    private companion object {
        const val CORNER_RADIUS_FRACTION = 0.2f
    }
}
