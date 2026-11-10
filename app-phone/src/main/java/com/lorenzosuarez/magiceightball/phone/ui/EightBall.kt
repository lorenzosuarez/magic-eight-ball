package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
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
import kotlin.math.ceil
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

    /** Its window towards the user, the die turning in it. */
    Thinking,

    /** Larger, the die at rest in the window with the answer on it. */
    Answered
}

/**
 * The ball, drawn flat: a disc with its "8", which beats slowly at rest and has rings around it
 * while it listens. To answer, the "8" gives way to the window, where the die turns while the
 * ball thinks; then the ball grows, the die slows to a stop and [answer] appears on it.
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
    val windowUp = answered || mode == BallMode.Thinking
    val face = remember { Animatable(if (windowUp) 1f else 0f) }
    val dieSize = remember { Animatable(if (answered) 1f else DIE_THINKING_SIZE) }
    val dieTurn = remember { Animatable(0f) }
    val text = remember { Animatable(if (answered) 1f else 0f) }
    val turning = remember { TurnMemory() }
    // The die keeps its text while it fades, after the answer is gone from the state.
    val shownAnswer = remember { LastAnswer() }.also { if (answer != null) it.text = answer }.text

    LaunchedEffect(mode, reducedMotion) {
        val carriedVelocity = if (turning.turning) DIE_DEGREES_PER_SECOND else 0f
        turning.turning = false
        when (mode) {
            BallMode.Thinking -> {
                launch { text.animateTo(0f, tween(TEXT_OUT_MS)) }
                launch { face.animateTo(1f, FaceSpring) }
                launch { dieSize.animateTo(DIE_THINKING_SIZE, tween(DIE_RESIZE_MS)) }
                if (!reducedMotion) {
                    turning.turning = true
                    // Half a turn to get up to speed, then on at that speed.
                    dieTurn.animateTo(dieTurn.value + 180f, tween(DIE_TURN_MS, easing = SpeedUp))
                    while (true) {
                        dieTurn.animateTo(dieTurn.value + 360f, tween(DIE_TURN_MS, easing = LinearEasing))
                    }
                }
            }
            BallMode.Answered -> {
                launch { face.animateTo(1f, FaceSpring) }
                launch { dieSize.animateTo(1f, GrowSpring) }
                launch {
                    delay(TEXT_IN_DELAY_MS)
                    text.animateTo(1f, tween(TEXT_IN_MS))
                }
                // The die looks the same every third of a turn: it stops on the next one that
                // leaves it room to slow down, without ever turning back.
                val stop = ceil((dieTurn.value + DIE_BRAKING_DEGREES) / DIE_SYMMETRY) * DIE_SYMMETRY
                dieTurn.animateTo(stop, BrakeSpring, initialVelocity = carriedVelocity)
                dieTurn.snapTo(0f)
            }
            BallMode.Idle, BallMode.Listening -> {
                launch { text.animateTo(0f, tween(TEXT_OUT_MS)) }
                face.animateTo(0f, tween(FACE_BACK_MS))
                dieTurn.snapTo(0f)
                dieSize.snapTo(DIE_THINKING_SIZE)
            }
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
        animationSpec = GrowSpring,
        label = "grown"
    )

    // One clock for the heartbeat, the rings and the die's shimmer: it runs only while one of
    // them is on screen.
    val pulse = remember { Animatable(0f) }
    val period = when (mode) {
        BallMode.Idle -> HEARTBEAT_MS
        BallMode.Listening -> RING_PULSE_MS
        BallMode.Thinking -> SHIMMER_MS
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
                    with(ball) {
                        draw(
                            face = face.value,
                            dieSize = dieSize.value,
                            dieTurnDegrees = dieTurn.value,
                            // While it turns, the die's light comes and goes, barely.
                            dieAlpha = 1f - SHIMMER_DEPTH * beat(pulse.value) * thinking.value,
                            textAlpha = text.value
                        )
                    }
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

/** Whether the last animation left the die turning, so the next one starts at that speed. */
private class TurnMemory {
    var turning = false
}

/** What surrounds the ball: a faint halo at rest and rings while listening. */
private class BallRings(private val center: Offset, private val radius: Float) {

    fun DrawScope.draw(resting: Float, listening: Float, level: Float, pulse: Float) {
        // At rest: one halo that follows the heartbeat.
        if (resting > 0f) {
            drawCircle(
                color = RingColor,
                radius = radius * (1f + HALO_REACH * (HALO_REST + (1f - HALO_REST) * beat(pulse))),
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
    private val dieEdge = Stroke(width = dieRadius * DIE_EDGE_WIDTH, join = StrokeJoin.Round)

    private val answerLayout: FittedText? = answer?.takeIf { it.isNotBlank() }?.let {
        fitInDie(it, textMeasurer, density)
    }

    /**
     * @param face 0 for the "8", 1 for the window; in between one gives way to the other.
     * @param dieSize the die's size relative to the one it has at rest with its answer.
     * @param dieTurnDegrees how far the die has turned around its centre.
     * @param textAlpha how clearly the answer shows; it is never turned with the die.
     */
    fun DrawScope.draw(face: Float, dieSize: Float, dieTurnDegrees: Float, dieAlpha: Float, textAlpha: Float) {
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
                clipPath(liquidOutline) {
                    withTransform({
                        rotate(dieTurnDegrees, pivot = center)
                        scale(dieSize, pivot = center)
                    }) {
                        drawPath(diePath, dieBrush, alpha = face * dieAlpha)
                        drawPath(diePath, dieBrush, alpha = face * dieAlpha, style = dieEdge)
                    }
                    if (textAlpha > 0f) {
                        answerLayout?.let { drawText(it.layout, topLeft = it.topLeft, alpha = textAlpha * face) }
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

/** No overshoot anywhere: things arrive and stay. */
private val GrowSpring = spring<Float>(dampingRatio = 1f, stiffness = 90f)
private val BrakeSpring = spring<Float>(dampingRatio = 1f, stiffness = 55f)

/** Accelerates evenly from rest, ending at the speed of a linear turn of twice the angle. */
private val SpeedUp = Easing { fraction -> fraction * fraction }

private const val FACE_BACK_MS = 220
private const val FACE_SHRINK = 0.3f
private const val STAGE_FADE_MS = 260

/** The die while the ball thinks: small enough to be seen whole as it turns. */
private const val DIE_THINKING_SIZE = 0.58f
private const val DIE_RESIZE_MS = 200
private const val DIE_TURN_MS = 1500
private const val DIE_DEGREES_PER_SECOND = 360f * 1000f / DIE_TURN_MS

/** A triangle looks the same every third of a turn. */
private const val DIE_SYMMETRY = 120f

/** The least the die still turns once the answer is here, so it never stops dead. */
private const val DIE_BRAKING_DEGREES = 70f
private const val SHIMMER_MS = 1300
private const val SHIMMER_DEPTH = 0.3f
/** The answer waits for the die to be all but still. */
private const val TEXT_IN_DELAY_MS = 750L
private const val TEXT_IN_MS = 320
private const val TEXT_OUT_MS = 100

private const val PRESS_SHRINK = 0.05f
private const val VOICE_SWELL = 0.04f

/** The heartbeat at rest: slow and small enough to be felt more than seen. */
private const val HEARTBEAT_MS = 2400
private const val HEARTBEAT_SWELL = 0.03f
private const val HALO_REACH = 0.1f
private const val HALO_REST = 0.6f
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

/** Radii of the two faces, relative to the ball's. */
private const val EIGHT_RADIUS = 0.46f
private const val WINDOW_RADIUS = 0.86f

/** The die's circumradius relative to the window's: above 1, its corners are out of sight. */
private const val DIE_RADIUS = 1.18f

/** A wide outline in the die's own colour rounds its corners. */
private const val DIE_EDGE_WIDTH = 0.05f

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
