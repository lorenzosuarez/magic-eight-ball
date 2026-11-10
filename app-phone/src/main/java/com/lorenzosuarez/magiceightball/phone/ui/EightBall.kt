package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
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
import com.example.magiceightball.core.common.BrandPalette
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

    /** Its window towards the user, the die deep in it, grey, swaying. */
    Thinking,

    /** Larger, the die risen to the window, upright, with the answer on it. */
    Answered
}

/**
 * The ball, drawn flat: a disc with its "8", which beats slowly at rest and has rings around it
 * while it listens. To answer, the "8" gives way to the window. While the ball thinks the die
 * hangs deep in the liquid, small and grey, swaying, with a light crossing it. With the answer
 * the ball grows and the die floats up: it comes close enough to fill the window, tips, rocks
 * back and forth ever less, and comes to rest upright, taking its colour and showing [answer].
 * The answer is written on the die and moves with it.
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
    val risen = remember { Animatable(if (answered) 1f else 0f) }
    val tilt = remember { Animatable(0f) }
    val text = remember { Animatable(if (answered) 1f else 0f) }
    val colour = remember { Animatable(if (answered) 1f else 0f) }
    // The die keeps its text while it fades, after the answer is gone from the state.
    val shownAnswer = remember { LastAnswer() }.also { if (answer != null) it.text = answer }.text

    LaunchedEffect(mode, reducedMotion) {
        when (mode) {
            BallMode.Thinking -> {
                launch { text.animateTo(0f, tween(TEXT_OUT_MS)) }
                launch { colour.animateTo(0f, tween(TEXT_OUT_MS)) }
                launch { face.animateTo(1f, FaceSpring) }
                launch { risen.animateTo(0f, tween(SINK_MS)) }
                if (reducedMotion) {
                    tilt.snapTo(0f)
                } else {
                    // Half a sway to one side, then from side to side for as long as it thinks.
                    tilt.animateTo(SWAY_DEGREES, tween(SWAY_MS / 2, easing = EaseOutSine))
                    tilt.animateTo(
                        targetValue = -SWAY_DEGREES,
                        animationSpec = infiniteRepeatable(
                            animation = tween(SWAY_MS, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }
            BallMode.Answered -> {
                launch { face.animateTo(1f, FaceSpring) }
                launch { risen.animateTo(1f, RiseSpring) }
                // Colour is the answer arriving: it washes over the die as it comes up.
                launch {
                    delay(COLOUR_IN_DELAY_MS)
                    colour.animateTo(1f, tween(COLOUR_IN_MS))
                }
                launch {
                    delay(TEXT_IN_DELAY_MS)
                    text.animateTo(1f, tween(TEXT_IN_MS))
                }
                if (reducedMotion) {
                    tilt.snapTo(0f)
                } else {
                    // Coming up tips the die over; the liquid then rocks it upright.
                    tilt.animateTo(0f, SettleSpring, initialVelocity = RISE_TIP_DEGREES_PER_SECOND)
                }
            }
            BallMode.Idle, BallMode.Listening -> {
                launch { text.animateTo(0f, tween(TEXT_OUT_MS)) }
                face.animateTo(0f, tween(FACE_BACK_MS))
                colour.snapTo(0f)
                tilt.snapTo(0f)
                risen.snapTo(0f)
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

    // One clock for the heartbeat and the rings: it runs only while one of them is on screen.
    val pulse = remember { Animatable(0f) }
    val period = when (mode) {
        BallMode.Idle -> HEARTBEAT_MS
        BallMode.Listening -> RING_PULSE_MS
        BallMode.Thinking, BallMode.Answered -> 0
    }
    LaunchedEffect(period, reducedMotion) {
        while (period > 0 && !reducedMotion) {
            pulse.snapTo(0f)
            pulse.animateTo(1f, tween(period, easing = LinearEasing))
        }
        pulse.snapTo(0f)
    }

    // The light that crosses the thinking die is the app saying it is working, so it is the one
    // motion that runs even for users who turned animations off: it follows the display's
    // frames directly, which that setting does not stop. Nothing moves; a highlight passes.
    val shimmer = remember { mutableFloatStateOf(0f) }
    val shimmers = mode == BallMode.Thinking
    LaunchedEffect(shimmers) {
        if (!shimmers) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsedMs = (now - start) / NANOS_PER_MILLI
                shimmer.floatValue = (elapsedMs % SHIMMER_MS).toFloat() / SHIMMER_MS
            }
        }
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
                            risen = risen.value,
                            tiltDegrees = tilt.value,
                            colour = colour.value,
                            shimmer = shimmer.floatValue,
                            shimmerAlpha = thinking.value,
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
    private val liquidBrush = Brush.radialGradient(
        0f to WindowCenter,
        1f to WindowEdge,
        center = center,
        radius = liquidRadius
    )
    private val liquidOutline = Path().apply { addOval(Rect(center, liquidRadius)) }

    /** The window's two borders, drawn over the die: a fine light edge and a darker one inside it. */
    private val rimStroke = Stroke(width = radius * RIM_WIDTH)
    private val rimRadius = liquidRadius - rimStroke.width / 2f
    private val innerRimStroke = Stroke(width = radius * INNER_RIM_WIDTH)
    private val innerRimRadius = liquidRadius - rimStroke.width - radius * RIM_GAP - innerRimStroke.width / 2f
    private val dieDepth = liquidRadius * DIE_DEPTH

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
    private val dieBottom = center.y + dieRadius

    /** The die with its answer, and the die that has none yet. */
    private val dieBrush = Brush.verticalGradient(0f to Accent, 1f to AccentDeep, startY = dieTop, endY = dieBottom)
    private val thinkingBrush = Brush.verticalGradient(
        0f to DieThinkingTop,
        1f to DieThinkingBottom,
        startY = dieTop,
        endY = dieBottom
    )
    private val dieEdge = Stroke(width = dieRadius * DIE_EDGE_WIDTH, join = StrokeJoin.Round)

    /** A band of light as tall as the die, which [draw] slides across it from left to right. */
    private val shimmerWidth = dieHalfWidth * SHIMMER_WIDTH
    private val shimmerStart = center.x - dieHalfWidth - shimmerWidth
    private val shimmerTravel = dieHalfWidth * 2f + shimmerWidth
    private val shimmerSize = Size(shimmerWidth, dieBottom - dieTop)
    private val shimmerBrush = Brush.horizontalGradient(
        0f to Color.White.copy(alpha = 0f),
        0.5f to Color.White.copy(alpha = SHIMMER_ALPHA),
        1f to Color.White.copy(alpha = 0f),
        startX = shimmerStart,
        endX = shimmerStart + shimmerWidth
    )

    private val answerLayout: FittedText? = answer?.takeIf { it.isNotBlank() }?.let {
        fitInDie(it, textMeasurer, density)
    }

    /**
     * @param face 0 for the "8", 1 for the window; in between one gives way to the other.
     * @param risen 0 for the die deep in the liquid, small and low; 1 for the die at the window,
     * close enough to fill it.
     * @param tiltDegrees how far the die leans to one side; the answer leans with it.
     * @param colour 0 for the grey die that is still thinking, 1 for the die with its answer.
     * @param shimmer how far across the die the band of light is, from 0 to 1.
     * @param shimmerAlpha how much of that light shows.
     * @param textAlpha how clearly the answer shows.
     */
    fun DrawScope.draw(
        face: Float,
        risen: Float,
        tiltDegrees: Float,
        colour: Float,
        shimmer: Float,
        shimmerAlpha: Float,
        textAlpha: Float
    ) {
        drawCircle(discBrush, radius, center)

        if (face < 1f) {
            scale(1f - FACE_SHRINK * face, pivot = center) {
                drawCircle(Color.White, eightRadius, center, alpha = 1f - face)
                drawText(eight, topLeft = eightTopLeft, alpha = 1f - face)
            }
        }
        if (face > 0f) {
            scale(1f - FACE_SHRINK * (1f - face), pivot = center) {
                drawCircle(liquidBrush, liquidRadius, center, alpha = face)
                clipPath(liquidOutline) {
                    withTransform({
                        translate(top = dieDepth * (1f - risen))
                        rotate(tiltDegrees, pivot = center)
                        scale(DIE_DEEP_SIZE + (1f - DIE_DEEP_SIZE) * risen, pivot = center)
                    }) {
                        if (colour < 1f) drawDie(thinkingBrush, alpha = face * (1f - colour))
                        if (colour > 0f) drawDie(dieBrush, alpha = face * colour)
                        if (shimmerAlpha > 0f) {
                            clipPath(diePath) {
                                translate(left = shimmerTravel * shimmer) {
                                    drawRect(
                                        brush = shimmerBrush,
                                        topLeft = Offset(shimmerStart, dieTop),
                                        size = shimmerSize,
                                        alpha = face * shimmerAlpha
                                    )
                                }
                            }
                        }
                        // Written on the die: it rises, leans and settles with it, and so
                        // never leaves it.
                        if (textAlpha > 0f) {
                            answerLayout?.let { drawText(it.layout, topLeft = it.topLeft, alpha = textAlpha * face) }
                        }
                    }
                }
                drawCircle(WindowRimInner, innerRimRadius, center, alpha = face, style = innerRimStroke)
                drawCircle(WindowRim, rimRadius, center, alpha = face, style = rimStroke)
            }
        }
    }

    /** The die, with an outline in its own colours that rounds its corners. */
    private fun DrawScope.drawDie(brush: Brush, alpha: Float) {
        drawPath(diePath, brush, alpha = alpha)
        drawPath(diePath, brush, alpha = alpha, style = dieEdge)
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

/** The ball grows and the die comes up without overshoot: they arrive and stay. */
private val GrowSpring = spring<Float>(dampingRatio = 1f, stiffness = 90f)
private val RiseSpring = spring<Float>(dampingRatio = 1f, stiffness = 55f)

/**
 * The die rocking upright in the liquid: it passes the vertical a few times, each time by
 * less, and stops.
 */
private val SettleSpring = spring<Float>(dampingRatio = 0.28f, stiffness = 34f)

private const val FACE_BACK_MS = 220
private const val FACE_SHRINK = 0.3f
private const val STAGE_FADE_MS = 260

/** The die deep in the liquid, while the ball thinks: its size and how far down it hangs. */
private const val DIE_DEEP_SIZE = 0.46f
private const val DIE_DEPTH = 0.14f
private const val SINK_MS = 200

/** How far the thinking die sways to each side, and how long one sway takes. */
private const val SWAY_DEGREES = 7f
private const val SWAY_MS = 1300

/** The push to one side that coming up gives the die. */
private const val RISE_TIP_DEGREES_PER_SECOND = -150f

/** The answer's colour, and then its text, as the die comes up. */
private const val COLOUR_IN_DELAY_MS = 200L
private const val COLOUR_IN_MS = 600
private const val TEXT_IN_DELAY_MS = 450L
private const val TEXT_IN_MS = 450
private const val TEXT_OUT_MS = 100

private const val SHIMMER_MS = 1400L
private const val NANOS_PER_MILLI = 1_000_000L

/** The band of light that crosses the thinking die: its width in half-dies, and its strength. */
private const val SHIMMER_WIDTH = 0.9f
private const val SHIMMER_ALPHA = 0.55f

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

/** The window's borders and the gap between them, relative to the ball's radius. */
private const val RIM_WIDTH = 0.012f
private const val RIM_GAP = 0.022f
private const val INNER_RIM_WIDTH = 0.02f

/** Radii of the two faces, relative to the ball's. */
private const val EIGHT_RADIUS = 0.46f
private const val WINDOW_RADIUS = 0.86f

/**
 * The die's circumradius relative to the window's. Well above 1: the risen die comes close
 * enough that its corners are out of sight and most of the window is the die, so there is
 * always die under the answer.
 */
private const val DIE_RADIUS = 1.36f

/** A wide outline in the die's own colour rounds its corners. */
private const val DIE_EDGE_WIDTH = 0.05f

private const val DIE_TEXT_MAX_SIZE = 0.3f
private const val DIE_TEXT_MIN_SIZE = 0.1f
private const val DIE_TEXT_SIZE_STEPS = 12
private const val DIE_TEXT_SIDE_PADDING = 0.15f
private val DIE_TEXT_TOPS = floatArrayOf(0.26f, 0.2f, 0.14f, 0.09f)
private val DIE_TEXT_WIDTHS = floatArrayOf(1f, 0.85f, 0.7f, 0.55f)

private const val SIN_60 = 0.8660254f
private const val TWO_PI = (2.0 * Math.PI).toFloat()

private val RingColor = Color.White
private val DiscTop = Color(BrandPalette.BALL_TOP)
private val DiscBottom = Color(BrandPalette.BALL_BOTTOM)
private val WindowCenter = Color(BrandPalette.WINDOW_CENTER)
private val WindowEdge = Color(BrandPalette.WINDOW_EDGE)
private val WindowRim = Color(BrandPalette.WINDOW_RIM)
private val WindowRimInner = Color(BrandPalette.WINDOW_RIM_INNER)
private val DieThinkingTop = Color(BrandPalette.DIE_THINKING_TOP)
private val DieThinkingBottom = Color(BrandPalette.DIE_THINKING_BOTTOM)
private val Accent = Color(BrandPalette.ACCENT)
private val AccentDeep = Color(BrandPalette.ACCENT_DEEP)
