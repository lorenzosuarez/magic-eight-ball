package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Lets a row that scrolls sideways fade out at an edge it runs past, so that what is cut off
 * there plainly goes on, and leaves an edge where nothing is hidden as it is. The fade grows
 * with the first of the scrolling and goes as the end comes into view: it never appears at once.
 *
 * Put it before the scrolling itself, so that it is the row's window that fades and not what
 * moves under it:
 * ```
 * Modifier.fadingEdges(scroll).horizontalScroll(scroll)
 * ```
 *
 * It reads where the row is while drawing, so scrolling composes nothing again.
 */
internal fun Modifier.fadingEdges(scroll: ScrollState, length: Dp = EDGE_FADE): Modifier = this
    // Drawn apart, so that fading takes from the row alone and not from what lies behind it.
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val reach = length.toPx().coerceAtMost(size.width / 2)
        if (reach <= 0f) return@drawWithContent
        // Until it has been measured a row does not know how far it goes: no edge is faded on a guess.
        val ahead = if (scroll.maxValue == Int.MAX_VALUE) 0 else scroll.maxValue - scroll.value
        val before = (scroll.value / reach).coerceIn(0f, 1f)
        val after = (ahead / reach).coerceIn(0f, 1f)
        // A row starts where reading does.
        val (left, right) = if (layoutDirection == LayoutDirection.Rtl) after to before else before to after
        if (left > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black.copy(alpha = 1f - left), Color.Black),
                    startX = 0f,
                    endX = reach
                ),
                size = Size(reach, size.height),
                blendMode = BlendMode.DstIn
            )
        }
        if (right > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Black.copy(alpha = 1f - right)),
                    startX = size.width - reach,
                    endX = size.width
                ),
                topLeft = Offset(size.width - reach, 0f),
                size = Size(reach, size.height),
                blendMode = BlendMode.DstIn
            )
        }
    }

/** A row that scrolls sideways and fades at whichever edge it runs past: the two always go together. */
internal fun Modifier.fadingRow(scroll: ScrollState): Modifier = fadingEdges(scroll).horizontalScroll(scroll)

/** How far in from an edge a row fades: enough to read as a fade, not so much that a card is lost in it. */
internal val EDGE_FADE = 32.dp
