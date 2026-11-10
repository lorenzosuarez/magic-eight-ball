package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** What the ball is showing. */
enum class BallMode {
    /** At rest, its "8" towards the user, beating slowly. */
    Idle,

    /** As [Idle], with rings around it that grow with the user's voice. */
    Listening,

    /** As [Idle], with ripples leaving it. */
    Thinking,

    /** Larger, its window towards the user, the die and its answer in it. */
    Answered
}

/**
 * The ball, drawn flat: a disc with its "8", which beats slowly at rest. Rings surround it while
 * it listens, ripples leave it while it thinks, and for the answer it grows and its "8" gives
 * way to the window, where the die surfaces with [answer] written on it.
 *
 * Every animated value, [voiceLevel] included, is read in the draw or layer phase only, so an
 * animation frame never recomposes or re-measures anything, and all brushes, paths and text
 * layouts are built once per size and answer.
 *
 * @param answer the text on the die while [BallMode.Answered]; ignored otherwise.
 * @param pressed whether a finger is on the ball: it gives way a little.
 * @param voiceLevel how loud the voice is, from 0 to 1, while [BallMode.Listening].
 * @param reducedMotion true to keep the ball still, for users who turned animations off.
 */
@Composable
fun EightBall(
    mode: BallMode,
    answer: String?,
    pressed: Boolean,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val answered = mode == BallMode.Answered
    val face = remember { Animatable(if (answered) 1f else 0f) }
    val die = remember { Animatable(if (answered) 1f else 0f) }
    // The die keeps its text while it sinks back, after the answer is gone from the state.
    val shownAnswer = remember { LastAnswer() }.also { if (answer != null) it.text = answer }.text

    LaunchedEffect(answered) {
        if (answered) {
            launch { face.animateTo(1f, FaceSpring) }
            delay(DIE_REVEAL_DELAY_MS)
            die.animateTo(1f, DieSpring)
        } else {
            launch { die.animateTo(0f, tween(DIE_HIDE_MS)) }
            face.animateTo(0f, tween(FACE_BACK_MS))
        }
    }

    val press = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    val resting = animateFloatAsState(
        targetValue = if (mode == BallMode.Idle) 1f else 0f,
        animationSpec = tween(STAGE_FADE_MS),
        label = "resting"
    )
    val listening = animateFloatAsState(
        targetValue = if (mode == BallMode.Listening) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "listening"
    )
    val thinking = animateFloatAsState(
        targetValue = if (mode == BallMode.Thinking) 1f else 0f,
        animationSpec = tween(STAGE_FADE_MS),
        label = "thinking"
    )
    val grown = animateFloatAsState(
        targetValue = if (answered) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "grown"
    )

    // One clock for the heartbeat, the rings and the ripples: it runs only while something moves.
    val pulse = remember { Animatable(0f) }
    val period = when (mode) {
        BallMode.Idle -> HEARTBEAT_MS
        BallMode.Listening -> RING_PULSE_MS
        BallMode.Thinking -> RIPPLE_MS
        BallMode.Answered -> 0
    }
    LaunchedEffect(period, reducedMotion) {
        while (period > 0 && !reducedMotion) {
            pulse.snapTo(0f)
            pulse.animateTo(1f, tween(period, easing = LinearEasing))
        }
        pulse.snapTo(0f)
    }

    // Without animations the ball does not move with the voice; its still rings say it listens.
    val voiceMotion: () -> Float = if (reducedMotion) NoVoiceMotion else voiceLevel
    val textMeasurer = rememberTextMeasurer()

    Spacer(
        modifier = modifier
            .aspectRatio(1f)
            // Behind the ball and outside its layer: the rings do not shrink and swell with it.
            .drawWithCache {
                val rings = BallRings(size.center, size.minDimension / 2f)
                onDrawBehind {
                    with(rings) {
                        draw(
                            resting = resting.value,
                            listening = listening.value,
                            thinking = thinking.value,
                            level = voiceMotion(),
                            pulse = pulse.value
                        )
                    }
                }
            }
            .graphicsLayer {
                val scale = 1f - PRESS_SHRINK * press.value +
                    HEARTBEAT_SWELL * beat(pulse.value) * resting.value +
                    VOICE_SWELL * listening.value * voiceMotion() +
                    ANSWER_GROWTH * grown.value
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                val ball = BallDrawing(
                    density = this,
                    center = size.center,
                    radius = size.minDimension / 2f,
                    answer = shownAnswer,
                    textMeasurer = textMeasurer
                )
                onDrawBehind {
                    with(ball) { draw(face = face.value, dieReveal = die.value) }
                }
            }
    )
}

private val NoVoiceMotion: () -> Float = { 0f }

/** A heartbeat over one turn of the clock: up and back down, smoothly, from 0 to 1 to 0. */
private fun beat(pulse: Float): Float = 0.5f - 0.5f * cos(TWO_PI * pulse)

private class LastAnswer {
    var text: String? = null
}

/** What surrounds the ball: a faint halo at rest, rings while listening, ripples while thinking. */
private class BallRings(private val center: Offset, private val radius: Float) {
    private val rippleStroke = Stroke(width = radius * RIPPLE_WIDTH)

    fun DrawScope.draw(resting: Float, listening: Float, thinking: Float, level: Float, pulse: Float) {
        // At rest: one halo that follows the heartbeat.
        if (resting > 0f) {
            drawCircle(
                color = RingColor,
                radius = radius * (1f + HALO_REACH * (0.6f + 0.4f * beat(pulse))),
                center = center,
                alpha = resting * HALO_ALPHA
            )
        }
        // Listening: discs one inside the other, breathing slowly and pushed out by the voice.
        if (listening > 0f) {
            for (ring in RING_COUNT downTo 1) {
                val breath = 0.5f + 0.5f * sin(TWO_PI * (pulse - ring * RING_BREATH_LAG))
                val reach = RING_STEP * ring * (RING_REST + (1f - RING_REST) * breath) +
                    RING_VOICE_REACH * level * ring / RING_COUNT
                drawCircle(
                    color = RingColor,
                    radius = radius * (1f + reach * listening),
                    center = center,
                    alpha = listening * RING_ALPHA
                )
            }
        }
        // Thinking: ripples leaving the ball one after the other.
        if (thinking > 0f) {
            repeat(RIPPLE_COUNT) { ripple ->
                val progress = (pulse + ripple.toFloat() / RIPPLE_COUNT) % 1f
                drawCircle(
                    color = RingColor,
                    radius = radius * (1.02f + RIPPLE_REACH * progress),
                    center = center,
                    alpha = thinking * RIPPLE_ALPHA * (1f - progress),
                    style = rippleStroke
                )
            }
        }
    }
}

/** Everything the ball is made of, built once per size and answer; [draw] then only places it. */
private class BallDrawing(
    density: Density,
    private val center: Offset,
    private val radius: Float,
    answer: String?,
    textMeasurer: TextMeasurer
) {
    private val discBrush = Brush.verticalGradient(
        0f to DiscTop,
        1f to DiscBottom,
        startY = center.y - radius,
        endY = center.y + radius
    )

    // The "8" side.
    private val eightRadius = radius * EIGHT_RADIUS
    private val eight: TextLayoutResult = textMeasurer.measure(
        text = "8",
        style = TextStyle(
            color = DiscBottom,
            fontSize = with(density) { (eightRadius * 1.5f).toSp() },
            fontWeight = FontWeight.Black,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )
    private val eightTopLeft = Offset(
        x = center.x - eight.size.width / 2f,
        y = center.y - eight.size.height / 2f
    )

    // The window side.
    private val liquidRadius = radius * WINDOW_RADIUS
    private val liquidOutline = Path().apply { addOval(Rect(center, liquidRadius)) }

    // The die: a triangle pointing down, close enough to the window that its three corners are
    // out of sight. That leaves its face, where the answer is written, as wide as the window allows.
    private val dieRadius = liquidRadius * DIE_RADIUS
    private val dieTop = center.y - dieRadius / 2f
    private val dieHalfWidth = dieRadius * SIN_60
    private val diePath = Path().apply {
        moveTo(center.x - dieHalfWidth, dieTop)
        lineTo(center.x + dieHalfWidth, dieTop)
        lineTo(center.x, center.y + dieRadius)
        close()
    }
    private val dieBrush = Brush.verticalGradient(
        0f to DieLit,
        1f to DieDeep,
        startY = dieTop,
        endY = center.y + dieRadius
    )
    private val dieEdge = Stroke(width = dieRadius * 0.05f, join = StrokeJoin.Round)

    private val answerLayout: FittedText? = answer?.takeIf { it.isNotBlank() }?.let {
        fitInDie(it, textMeasurer, density)
    }

    /** @param face 0 for the "8", 1 for the window; in between one gives way to the other. */
    fun DrawScope.draw(face: Float, dieReveal: Float) {
        drawCircle(discBrush, radius, center)

        if (face < 1f) {
            scale(1f - FACE_SHRINK * face, pivot = center) {
                drawCircle(Color.White, eightRadius, center, alpha = 1f - face)
                drawText(eight, topLeft = eightTopLeft, alpha = 1f - face)
            }
        }
        if (face > 0f) {
            scale(1f - FACE_SHRINK * (1f - face), pivot = center) {
                drawCircle(Liquid, liquidRadius, center, alpha = face)
                if (dieReveal > 0f) {
                    val alpha = (dieReveal * face).coerceIn(0f, 1f)
                    // The die comes up from the dark: it grows, rises and clears as it surfaces.
                    clipPath(liquidOutline) {
                        withTransform({
                            translate(top = (1f - dieReveal) * dieRadius * DIE_RISE)
                            scale(DIE_START_SCALE + (1f - DIE_START_SCALE) * dieReveal, pivot = center)
                        }) {
                            drawPath(diePath, dieBrush, alpha = alpha)
                            drawPath(diePath, dieBrush, alpha = alpha, style = dieEdge)
                            answerLayout?.let { drawText(it.layout, topLeft = it.topLeft, alpha = alpha) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Lays [text] out as large as the die allows. A line fits when it is no wider than the part
     * of the die seen through the window, from the line's upper edge to its lower one, so the
     * search tries, from the largest size down, a few wrapping widths and a few distances from
     * the die's top edge, and keeps the first layout whose every line fits. Short answers end up large and near the middle; long ones use the whole
     * width at the top. Runs once per answer and size, never while animating.
     */
    private fun fitInDie(text: String, textMeasurer: TextMeasurer, density: Density): FittedText {
        val dieHeight = dieRadius * 1.5f
        val sidePadding = dieHalfWidth * DIE_TEXT_SIDE_PADDING

        /** Half the width left for a line between these two depths under the die's top edge. */
        fun halfWidthBetween(top: Float, bottom: Float): Float {
            val die = dieHalfWidth * (1f - bottom / dieHeight)
            val fromCenter = maxOf(abs(dieTop + top - center.y), abs(dieTop + bottom - center.y))
            val window = sqrt((liquidRadius * liquidRadius - fromCenter * fromCenter).coerceAtLeast(0f))
            return minOf(die, window) - sidePadding
        }

        fun layout(fontSizePx: Float, maxWidth: Float): TextLayoutResult = textMeasurer.measure(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontSize = with(density) { fontSizePx.toSp() },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 1.14.em,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            constraints = Constraints(maxWidth = maxWidth.roundToInt().coerceAtLeast(1))
        )

        fun TextLayoutResult.fitsFrom(topDepth: Float): Boolean =
            (0 until lineCount).all { line ->
                val lineWidth = getLineRight(line) - getLineLeft(line)
                val breaksAWord = line < lineCount - 1 && !text[getLineEnd(line) - 1].isWhitespace()
                !breaksAWord &&
                    lineWidth / 2f <= halfWidthBetween(topDepth + getLineTop(line), topDepth + getLineBottom(line))
            }

        fun place(layout: TextLayoutResult, topDepth: Float) = FittedText(
            layout = layout,
            topLeft = Offset(center.x - layout.size.width / 2f, dieTop + topDepth)
        )

        val largest = dieRadius * DIE_TEXT_MAX_SIZE
        val smallest = dieRadius * DIE_TEXT_MIN_SIZE
        for (step in 0..DIE_TEXT_SIZE_STEPS) {
            val fontSize = largest + (smallest - largest) * step / DIE_TEXT_SIZE_STEPS
            for (topFraction in DIE_TEXT_TOPS) {
                val topDepth = dieHeight * topFraction
                val widest = halfWidthBetween(topDepth, topDepth + fontSize) * 2f
                for (widthFraction in DIE_TEXT_WIDTHS) {
                    val candidate = layout(fontSize, widest * widthFraction)
                    if (candidate.fitsFrom(topDepth)) return place(candidate, topDepth)
                }
            }
        }
        // Longer than a die can hold: as small as it gets, from the very top.
        val topDepth = dieHeight * DIE_TEXT_TOPS.last()
        val widest = halfWidthBetween(topDepth, topDepth + smallest) * 2f
        return place(layout(smallest, widest * DIE_TEXT_WIDTHS.last()), topDepth)
    }

    private class FittedText(val layout: TextLayoutResult, val topLeft: Offset)
}

private val FaceSpring = spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
private val DieSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 140f)

private const val FACE_BACK_MS = 220
private const val FACE_SHRINK = 0.3f
private const val DIE_REVEAL_DELAY_MS = 160L
private const val DIE_HIDE_MS = 140
private const val DIE_START_SCALE = 0.5f
private const val DIE_RISE = 0.22f
private const val STAGE_FADE_MS = 260

private const val PRESS_SHRINK = 0.05f
private const val VOICE_SWELL = 0.04f

/** The heartbeat at rest: slow and small enough to be felt more than seen. */
private const val HEARTBEAT_MS = 2400
private const val HEARTBEAT_SWELL = 0.03f
private const val HALO_REACH = 0.1f
private const val HALO_ALPHA = 0.05f

/** How much larger the ball is while it shows an answer, so the answer can be read. */
private const val ANSWER_GROWTH = 0.3f

/** Rings while listening: how far each one stands from the next, relative to the ball's radius. */
private const val RING_COUNT = 3
private const val RING_STEP = 0.16f
private const val RING_REST = 0.6f
private const val RING_BREATH_LAG = 0.16f
private const val RING_VOICE_REACH = 0.4f
private const val RING_ALPHA = 0.07f
private const val RING_PULSE_MS = 2100

/** Ripples while thinking. */
private const val RIPPLE_COUNT = 3
private const val RIPPLE_REACH = 0.7f
private const val RIPPLE_ALPHA = 0.3f
private const val RIPPLE_WIDTH = 0.012f
private const val RIPPLE_MS = 1800

/** Radii of the two faces, relative to the ball's. */
private const val EIGHT_RADIUS = 0.46f
private const val WINDOW_RADIUS = 0.86f

/** The die's circumradius relative to the window's: above 1, its corners are out of sight. */
private const val DIE_RADIUS = 1.18f

private const val DIE_TEXT_MAX_SIZE = 0.3f
private const val DIE_TEXT_MIN_SIZE = 0.1f
private const val DIE_TEXT_SIZE_STEPS = 12
private const val DIE_TEXT_SIDE_PADDING = 0.09f
private val DIE_TEXT_TOPS = floatArrayOf(0.26f, 0.2f, 0.14f, 0.09f)
private val DIE_TEXT_WIDTHS = floatArrayOf(1f, 0.85f, 0.7f, 0.55f)

private const val SIN_60 = 0.8660254f
private const val TWO_PI = (2.0 * Math.PI).toFloat()

private val RingColor = Color.White
private val DiscTop = Color(0xFF5C6773)
private val DiscBottom = Color(0xFF3F4853)
private val Liquid = Color(0xFF070C24)
private val DieLit = Color(0xFF6C8AFF)
private val DieDeep = Color(0xFF2C41CC)
