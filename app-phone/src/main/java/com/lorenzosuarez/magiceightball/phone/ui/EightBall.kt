package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.R

/**
 * The ball. It shows its "8" until it is asked, rattles while [BallPhase.Thinking] and lets the
 * blue die surface in its window once [BallPhase.Answered].
 *
 * Every animated value is read in the draw or layer phase only, so an animation frame never
 * recomposes or re-measures anything. The colours are the ball's own in both themes.
 */
@Composable
fun EightBall(
    phase: BallPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thinking = phase is BallPhase.Thinking

    val rattle = remember { Animatable(0f) }
    LaunchedEffect(thinking) {
        if (thinking) {
            rattle.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = RATTLE_PERIOD_MS
                        0f at 0 using LinearEasing
                        1f at RATTLE_PERIOD_MS / 4 using LinearEasing
                        -1f at RATTLE_PERIOD_MS * 3 / 4 using LinearEasing
                        0f at RATTLE_PERIOD_MS
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rattle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val eightVisibility by animateFloatAsState(
        targetValue = if (phase is BallPhase.Idle) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "eight"
    )
    val dieReveal by animateFloatAsState(
        targetValue = if (phase is BallPhase.Answered) 1f else 0f,
        animationSpec = if (phase is BallPhase.Answered) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(durationMillis = 150)
        },
        label = "die"
    )

    val textMeasurer = rememberTextMeasurer()
    val description = stringResource(R.string.ball_content_description)
    val thinkingDescription = stringResource(R.string.ball_state_thinking)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                translationX = rattle.value * RATTLE_DISTANCE.toPx()
                rotationZ = rattle.value * RATTLE_DEGREES
            }
            .clip(CircleShape)
            .clickable(
                enabled = !thinking,
                onClickLabel = stringResource(R.string.ball_action_ask),
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = description
                if (thinking) stateDescription = thinkingDescription
            }
            .drawWithCache {
                val center = size.center
                val radius = size.minDimension / 2f
                val windowRadius = radius * WINDOW_RADIUS_FRACTION

                val bodyBrush = Brush.radialGradient(
                    colors = listOf(BallHighlight, BallBody, BallShade),
                    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.4f),
                    radius = radius * 1.6f
                )
                val liquidBrush = Brush.radialGradient(
                    colors = listOf(LiquidLight, LiquidDark),
                    center = center,
                    radius = windowRadius
                )
                val glossBrush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(center.x - radius * 0.45f, center.y - radius * 0.5f),
                    radius = radius * 0.5f
                )

                // The die: an equilateral triangle pointing up, centred in the window.
                val dieRadius = windowRadius * 0.82f
                val halfBase = dieRadius * SIN_60
                val die = Path().apply {
                    moveTo(center.x, center.y - dieRadius)
                    lineTo(center.x + halfBase, center.y + dieRadius / 2f)
                    lineTo(center.x - halfBase, center.y + dieRadius / 2f)
                    close()
                }
                val dieBrush = Brush.verticalGradient(
                    colors = listOf(DieLight, DieDark),
                    startY = center.y - dieRadius,
                    endY = center.y + dieRadius / 2f
                )

                val eight = textMeasurer.measure(
                    text = "8",
                    style = TextStyle(
                        color = BallShade,
                        fontSize = (windowRadius * 1.3f).toSp(),
                        fontWeight = FontWeight.Black
                    )
                )
                val eightTopLeft = Offset(
                    x = center.x - eight.size.width / 2f,
                    y = center.y - eight.size.height / 2f
                )

                onDrawBehind {
                    drawCircle(bodyBrush, radius)
                    drawCircle(liquidBrush, windowRadius)

                    val reveal = dieReveal
                    if (reveal > 0f) {
                        scale(DIE_START_SCALE + (1f - DIE_START_SCALE) * reveal) {
                            drawPath(die, dieBrush, alpha = reveal.coerceIn(0f, 1f))
                        }
                    }

                    val eightAlpha = eightVisibility
                    if (eightAlpha > 0f) {
                        drawCircle(EightDisc, windowRadius, alpha = eightAlpha)
                        drawText(eight, topLeft = eightTopLeft, alpha = eightAlpha)
                    }

                    drawCircle(glossBrush, radius)
                }
            }
    )
}

private const val RATTLE_PERIOD_MS = 280
private const val RATTLE_DEGREES = 5f
private val RATTLE_DISTANCE = 10.dp

private const val WINDOW_RADIUS_FRACTION = 0.5f
private const val DIE_START_SCALE = 0.55f
private const val SIN_60 = 0.8660254f

private val BallHighlight = Color(0xFF4A4F57)
private val BallBody = Color(0xFF17181C)
private val BallShade = Color(0xFF050506)
private val LiquidLight = Color(0xFF1B2470)
private val LiquidDark = Color(0xFF05071F)
private val DieLight = Color(0xFF5C7CFF)
private val DieDark = Color(0xFF2A3FC4)
private val EightDisc = Color(0xFFF4F5F7)
