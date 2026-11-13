package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/**
 * The small turning ring that stands where a mark or an arrow will be, while what it leads to
 * is on its way. Silent by itself: whatever holds it says what is being waited for. Drawn here
 * and not taken from the kit, whose ring shrinks to a dot between turns: this one is always most
 * of a circle, so that it reads as a ring at whatever moment it is looked at.
 */
@Composable
internal fun WaitingRing(color: Color, modifier: Modifier = Modifier) {
    val turn by rememberInfiniteTransition(label = "waiting ring").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(RING_TURN_MS, easing = LinearEasing)),
        label = "waiting ring turn"
    )
    Canvas(modifier = modifier.size(RING_SIZE).clearAndSetSemantics {}) {
        val stroke = RING_STROKE.toPx()
        drawArc(
            color = color,
            startAngle = turn - 90f,
            sweepAngle = RING_SWEEP,
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/**
 * Before a button's words while what the button started is on its way: the ring, in the
 * button's own ink. With nothing on its way there is nothing, and the words stand alone.
 */
@Composable
internal fun BusyRing(busy: Boolean) {
    if (busy) {
        WaitingRing(color = LocalContentColor.current)
        Spacer(Modifier.width(RING_GAP))
    }
}

/**
 * A bar standing for a line of words that is not known yet, with a band of light crossing it:
 * the bars, the light and its pace are those of the die while the ball thinks, so that waiting
 * looks the same wherever the app does it.
 */
@Composable
internal fun Modifier.waitingBar(): Modifier {
    val bar = MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_ALPHA)
    val passing by rememberInfiniteTransition(label = "waiting").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PASS_MS, easing = LinearEasing)),
        label = "waiting light"
    )
    return drawBehind {
        val round = CornerRadius(size.height / 2)
        drawRoundRect(color = bar, cornerRadius = round)
        // From wholly off one end to wholly off the other, and only over the bar itself.
        val band = size.width * BAND_WIDTH
        val from = -band + (size.width + band) * passing
        clipPath(Path().apply { addRoundRect(RoundRect(size.toRect(), round)) }) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = LIGHT_ALPHA), Color.Transparent),
                    startX = from,
                    endX = from + band
                )
            )
        }
    }
}

private val RING_SIZE = 20.dp
private val RING_STROKE = 2.dp
private const val RING_SWEEP = 270f
private const val RING_TURN_MS = 900

private val RING_GAP = 8.dp

/** As on the thinking die: how strong a bar is, how wide and strong the light, and how long it takes to cross. */
private const val BAR_ALPHA = 0.14f
private const val LIGHT_ALPHA = 0.5f
private const val BAND_WIDTH = 0.9f
private const val PASS_MS = 1_400
