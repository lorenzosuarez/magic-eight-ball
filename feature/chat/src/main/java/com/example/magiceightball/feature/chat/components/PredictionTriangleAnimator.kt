package com.example.magiceightball.feature.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch

/**
 * Motion of the die inside the ball, the same as on the phone.
 *
 * While [isThinking] the die hangs deep in the liquid, small, and sways from side to side.
 * When the answer is in, it floats up until it fills the window; coming up tips it over, and
 * the liquid rocks it upright, each swing smaller than the one before.
 *
 * All values are read inside [graphicsLayer], so the motion only updates layer properties:
 * no recomposition, no layout and no redrawing of [content].
 */
@Composable
fun PredictionTriangleAnimator(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val sway = remember { Animatable(0f) }
    val surfacing = remember { Animatable(if (isThinking) 0f else 1f) }

    LaunchedEffect(isThinking) {
        launch { surfacing.animateTo(if (isThinking) 0f else 1f, SurfacingSpec) }
        if (isThinking) {
            sway.animateTo(1f, tween(SWAY_MILLIS / 2, easing = EaseOutSine))
            sway.animateTo(
                targetValue = -1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(SWAY_MILLIS, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            sway.animateTo(0f, SettleSpec, initialVelocity = RISE_TIP_SWAYS_PER_SECOND)
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            val surfaced = surfacing.value
            val scale = lerp(SUBMERGED_SCALE, 1f, surfaced)
            scaleX = scale
            scaleY = scale
            translationY = (1f - surfaced) * SUBMERGED_DEPTH.toPx()
            rotationZ = sway.value * MAX_SWAY_DEGREES
        },
        propagateMinConstraints = true,
        content = content
    )
}

private const val SWAY_MILLIS = 1300
private const val MAX_SWAY_DEGREES = 7f
private const val SUBMERGED_SCALE = 0.5f
private val SUBMERGED_DEPTH = 12.dp

/** The push to one side that coming up gives the die, in sways per second. */
private const val RISE_TIP_SWAYS_PER_SECOND = -21f

/** The die comes up without overshoot: it arrives and stays. */
private val SurfacingSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 55f)

/** The die rocking upright: it passes the vertical a few times, each time by less, and stops. */
private val SettleSpec = spring<Float>(dampingRatio = 0.28f, stiffness = 34f)
