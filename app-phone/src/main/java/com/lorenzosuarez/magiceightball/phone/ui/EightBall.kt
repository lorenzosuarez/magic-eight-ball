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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.luminance
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
    /** At rest, its "8" towards the user. */
    Idle,

    /** As [Idle], glowing and swelling with the user's voice. */
    Listening,

    /** Spinning. */
    Thinking,

    /** Its window towards the user, the die and its answer floating in it. */
    Answered
}

/**
 * The ball: a sphere with its "8" on one side and its window on the other. It turns around
 * while [BallMode.Thinking] and comes to rest on the window, where the die surfaces with
 * [answer] written on it.
 *
 * Every animated value, [voiceLevel] included, is read in the draw or layer phase only, so an
 * animation frame never recomposes or re-measures anything, and all brushes, paths and text
 * layouts are built once per size and answer. The ball keeps its own colours in both themes;
 * only the glow around it takes the theme's.
 *
 * @param answer the text on the die while [BallMode.Answered]; ignored otherwise.
 * @param pressed whether a finger is on the screen: the ball gives way a little.
 * @param voiceLevel how loud the voice is, from 0 to 1, while [BallMode.Listening].
 * @param reducedMotion true to change faces without the spin and not to pulse with the voice, for
 * users who turned animations off.
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
    val turn = remember { Animatable(if (answered) WINDOW_ANGLE else EIGHT_ANGLE) }
    val die = remember { Animatable(if (answered) 1f else 0f) }
    val spin = remember { SpinMemory() }
    // The die keeps its text while it sinks back, after the answer is gone from the state.
    val shownAnswer = remember { LastAnswer() }.also { if (answer != null) it.text = answer }.text

    LaunchedEffect(mode, reducedMotion) {
        val carriedVelocity = if (spin.spinning) SPIN_DEGREES_PER_SECOND else 0f
        spin.spinning = false
        when (mode) {
            BallMode.Thinking -> {
                launch { die.animateTo(0f, tween(DIE_HIDE_MS)) }
                if (reducedMotion) {
                    turn.snapTo(WINDOW_ANGLE)
                } else {
                    spin.spinning = true
                    // Half a turn to get up to speed, then whole turns at that speed.
                    turn.animateTo(turn.value + 180f, tween(SPIN_TURN_MS, easing = SpinUp))
                    while (true) {
                        turn.animateTo(turn.value + 360f, tween(SPIN_TURN_MS, easing = LinearEasing))
                    }
                }
            }
            BallMode.Answered -> {
                launch {
                    delay(DIE_REVEAL_DELAY_MS)
                    die.animateTo(1f, DieSpring)
                }
                turn.settleOn(WINDOW_ANGLE, carriedVelocity)
            }
            BallMode.Idle, BallMode.Listening -> {
                launch { die.animateTo(0f, tween(DIE_HIDE_MS)) }
                turn.settleOn(EIGHT_ANGLE, carriedVelocity)
            }
        }
    }

    val press = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    val listening = animateFloatAsState(
        targetValue = if (mode == BallMode.Listening) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "listening"
    )

    // Without animations the ball does not pulse with the voice; its glow still says it listens.
    val voiceMotion: () -> Float = if (reducedMotion) NoVoiceMotion else voiceLevel

    val accent = MaterialTheme.colorScheme.primary
    val onDarkBackground = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val textMeasurer = rememberTextMeasurer()

    Spacer(
        modifier = modifier
            .aspectRatio(1f)
            // Behind the ball and outside its layer: the glow and the shadow do not shrink
            // and swell with it.
            .drawWithCache {
                val surroundings = BallSurroundings(size.center, size.minDimension / 2f, accent, onDarkBackground)
                onDrawBehind {
                    with(surroundings) { draw(listening = listening.value, level = voiceMotion()) }
                }
            }
            .graphicsLayer {
                val scale = 1f - PRESS_SHRINK * press.value + VOICE_SWELL * listening.value * voiceMotion()
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                val ball = BallDrawing(
                    density = this,
                    center = size.center,
                    radius = size.minDimension / 2f,
                    answer = shownAnswer,
                    textMeasurer = textMeasurer,
                    accent = accent,
                    onDarkBackground = onDarkBackground
                )
                onDrawBehind {
                    with(ball) { draw(turnDegrees = turn.value, dieReveal = die.value) }
                }
            }
    )
}

private val NoVoiceMotion: () -> Float = { 0f }

/** Whether the last animation left the ball spinning, so the next one starts at that speed. */
private class SpinMemory {
    var spinning = false
}

private class LastAnswer {
    var text: String? = null
}

/**
 * Brings the ball to rest on the face at [faceAngle], turning on in the direction it spins and
 * arriving with a little overshoot. Afterwards the angle is brought back into one turn.
 */
private suspend fun Animatable<Float, *>.settleOn(faceAngle: Float, velocity: Float) {
    // The next time that face comes round, at least a short way ahead when the ball is moving.
    val lead = if (velocity > 0f) SETTLE_LEAD_DEGREES else 0f
    val target = ceil((value + lead - faceAngle) / 360f) * 360f + faceAngle
    animateTo(target, SettleSpring, initialVelocity = velocity)
    snapTo(faceAngle)
}

/** The soft light around the ball and the shadow under it. Built once per size and theme. */
private class BallSurroundings(
    private val center: Offset,
    private val radius: Float,
    accent: Color,
    private val onDarkBackground: Boolean
) {
    private val glowRadius = radius * GLOW_RADIUS
    private val glowBrush = Brush.radialGradient(
        0f to accent,
        0.5f to accent.copy(alpha = 0.55f),
        1f to accent.copy(alpha = 0f),
        center = center,
        radius = glowRadius
    )

    private val shadowCenter = Offset(center.x, center.y + radius * 1.08f)
    private val shadowRadius = radius * 0.9f
    private val shadowBrush = Brush.radialGradient(
        0f to Color.Black,
        0.55f to Color.Black.copy(alpha = 0.5f),
        1f to Color.Transparent,
        center = shadowCenter,
        radius = shadowRadius
    )

    fun DrawScope.draw(listening: Float, level: Float) {
        val restAlpha = if (onDarkBackground) GLOW_REST_ALPHA_DARK else GLOW_REST_ALPHA_LIGHT
        scale(scale = 1f + listening * (GLOW_LISTEN_GROWTH + GLOW_VOICE_GROWTH * level), pivot = center) {
            drawCircle(
                brush = glowBrush,
                radius = glowRadius,
                center = center,
                alpha = (restAlpha + listening * GLOW_LISTEN_ALPHA).coerceIn(0f, 1f)
            )
        }
        scale(scaleX = 1f, scaleY = SHADOW_FLATNESS, pivot = shadowCenter) {
            drawCircle(
                brush = shadowBrush,
                radius = shadowRadius,
                center = shadowCenter,
                alpha = if (onDarkBackground) SHADOW_ALPHA_DARK else SHADOW_ALPHA_LIGHT
            )
        }
    }
}

/**
 * Everything the ball is made of, built once per size and answer; [draw] then only places it.
 *
 * The two faces are caps of the sphere seen from the front: turning by an angle a moves a
 * cap's centre sideways by sin(a) and narrows it by cos(a), and a cap is hidden while it faces
 * away.
 */
private class BallDrawing(
    density: Density,
    private val center: Offset,
    private val radius: Float,
    answer: String?,
    textMeasurer: TextMeasurer,
    accent: Color,
    onDarkBackground: Boolean
) {
    private val outline = Path().apply { addOval(Rect(center, radius)) }

    private val bodyBrush = Brush.radialGradient(
        0f to BodyLit,
        0.42f to BodyMid,
        1f to BodyDeep,
        center = Offset(center.x - radius * 0.32f, center.y - radius * 0.38f),
        radius = radius * 1.5f
    )

    /** Light from the surroundings catching the edge; stronger where the ball needs to stand out. */
    private val rimBrush = Brush.radialGradient(
        0f to Color.Transparent,
        0.86f to Color.Transparent,
        1f to Color.White.copy(alpha = if (onDarkBackground) 0.2f else 0.09f),
        center = center,
        radius = radius
    )

    /** The glow's colour reflected on the underside. */
    private val bounceCenter = Offset(center.x + radius * 0.2f, center.y + radius * 0.95f)
    private val bounceRadius = radius * 0.8f
    private val bounceBrush = Brush.radialGradient(
        0f to accent.copy(alpha = if (onDarkBackground) 0.3f else 0.22f),
        1f to accent.copy(alpha = 0f),
        center = bounceCenter,
        radius = bounceRadius
    )

    /** The reflection of a soft light above and to the left: a blurred oval lying on the curve. */
    private val glossCenter = Offset(center.x - radius * 0.36f, center.y - radius * 0.5f)
    private val glossRadius = radius * 0.5f
    private val glossBrush = Brush.radialGradient(
        0f to Color.White.copy(alpha = 0.4f),
        0.5f to Color.White.copy(alpha = 0.12f),
        1f to Color.Transparent,
        center = glossCenter,
        radius = glossRadius
    )

    // The "8" side.
    private val eightRadius = radius * EIGHT_RADIUS
    private val eightTravel = radius * capTravel(EIGHT_RADIUS)
    private val eightDiscBrush = Brush.verticalGradient(
        0f to Color.White,
        1f to EightDiscShade,
        startY = center.y - eightRadius,
        endY = center.y + eightRadius
    )
    private val eight: TextLayoutResult = textMeasurer.measure(
        text = "8",
        style = TextStyle(
            color = BodyDeep,
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
    private val windowRadius = radius * WINDOW_RADIUS
    private val windowTravel = radius * capTravel(WINDOW_RADIUS)
    private val bezelBrush = Brush.linearGradient(
        0f to BezelLit,
        1f to BezelDeep,
        start = Offset(center.x - windowRadius, center.y - windowRadius),
        end = Offset(center.x + windowRadius, center.y + windowRadius)
    )
    private val liquidRadius = windowRadius * 0.94f
    private val liquidBrush = Brush.radialGradient(
        0f to LiquidLit,
        1f to LiquidDeep,
        center = center,
        radius = liquidRadius
    )
    private val depthBrush = Brush.radialGradient(
        0f to Color.Transparent,
        0.7f to Color.Transparent,
        1f to Color.Black.copy(alpha = 0.55f),
        center = center,
        radius = liquidRadius
    )

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
    private val dieEdge = Stroke(width = dieRadius * 0.07f, join = StrokeJoin.Round)
    private val dieHighlight = Stroke(width = dieRadius * 0.014f, join = StrokeJoin.Round)

    private val answerLayout: FittedText? = answer?.takeIf { it.isNotBlank() }?.let {
        fitInDie(it, textMeasurer, density)
    }

    fun DrawScope.draw(turnDegrees: Float, dieReveal: Float) {
        val angle = turnDegrees * DEGREES_TO_RADIANS
        val sine = sin(angle)
        val cosine = cos(angle)

        clipPath(outline) {
            drawCircle(bodyBrush, radius, center)
            drawCircle(bounceBrush, bounceRadius, bounceCenter)

            if (cosine > FACE_EDGE) {
                withTransform({
                    translate(left = sine * eightTravel)
                    scale(scaleX = cosine, scaleY = 1f, pivot = center)
                }) {
                    drawCircle(eightDiscBrush, eightRadius, center)
                    drawText(eight, topLeft = eightTopLeft)
                }
            }
            if (cosine < -FACE_EDGE) {
                withTransform({
                    translate(left = -sine * windowTravel)
                    scale(scaleX = -cosine, scaleY = 1f, pivot = center)
                }) {
                    drawWindow(dieReveal)
                }
            }

            drawCircle(rimBrush, radius, center)
            withTransform({
                rotate(degrees = GLOSS_TILT_DEGREES, pivot = glossCenter)
                scale(scaleX = 1.3f, scaleY = 0.62f, pivot = glossCenter)
            }) {
                drawCircle(glossBrush, glossRadius, glossCenter)
            }
        }
    }

    private fun DrawScope.drawWindow(dieReveal: Float) {
        drawCircle(bezelBrush, windowRadius, center)
        drawCircle(liquidBrush, liquidRadius, center)

        if (dieReveal > 0f) {
            val alpha = dieReveal.coerceIn(0f, 1f)
            // The die comes up from the dark: it grows, rises and clears as it surfaces.
            clipPath(liquidOutline) {
                withTransform({
                    translate(top = (1f - dieReveal) * dieRadius * DIE_RISE)
                    scale(DIE_START_SCALE + (1f - DIE_START_SCALE) * dieReveal, pivot = center)
                }) {
                    drawPath(diePath, dieBrush, alpha = alpha)
                    drawPath(diePath, dieBrush, alpha = alpha, style = dieEdge)
                    drawPath(diePath, DieEdgeLight, alpha = alpha * 0.5f, style = dieHighlight)
                    answerLayout?.let { drawText(it.layout, topLeft = it.topLeft, alpha = alpha) }
                }
            }
        }
        drawCircle(depthBrush, liquidRadius, center)
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

    private companion object {
        /** How far a cap of this relative radius travels sideways: the cosine of its half angle. */
        fun capTravel(relativeRadius: Float): Float = sqrt(1f - relativeRadius * relativeRadius)
    }
}

/** Accelerates evenly from rest, ending at the speed of a linear turn of the same duration. */
private val SpinUp = Easing { fraction -> fraction * fraction }

private val SettleSpring = spring<Float>(dampingRatio = 0.58f, stiffness = 70f)
private val DieSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 140f)

private const val EIGHT_ANGLE = 0f
private const val WINDOW_ANGLE = 180f
private const val SPIN_TURN_MS = 640
private const val SPIN_DEGREES_PER_SECOND = 360f * 1000f / SPIN_TURN_MS
private const val SETTLE_LEAD_DEGREES = 100f
private const val DIE_REVEAL_DELAY_MS = 220L
private const val DIE_HIDE_MS = 140
private const val DIE_START_SCALE = 0.5f
private const val DIE_RISE = 0.22f

private const val PRESS_SHRINK = 0.045f
private const val VOICE_SWELL = 0.07f

private const val GLOW_RADIUS = 1.75f
private const val GLOW_REST_ALPHA_LIGHT = 0.16f
private const val GLOW_REST_ALPHA_DARK = 0.26f
private const val GLOW_LISTEN_ALPHA = 0.3f
private const val GLOW_LISTEN_GROWTH = 0.1f
private const val GLOW_VOICE_GROWTH = 0.22f
private const val SHADOW_FLATNESS = 0.14f
private const val SHADOW_ALPHA_LIGHT = 0.28f
private const val SHADOW_ALPHA_DARK = 0.6f

/** Radii of the two faces, relative to the ball's. */
private const val EIGHT_RADIUS = 0.43f
private const val WINDOW_RADIUS = 0.74f

/** A face this close to edge-on is not drawn. */
private const val FACE_EDGE = 0.02f
private const val GLOSS_TILT_DEGREES = -38f

/** The die's circumradius relative to the window's: above 1, its corners are out of sight. */
private const val DIE_RADIUS = 1.18f

private const val DIE_TEXT_MAX_SIZE = 0.3f
private const val DIE_TEXT_MIN_SIZE = 0.1f
private const val DIE_TEXT_SIZE_STEPS = 12
private const val DIE_TEXT_SIDE_PADDING = 0.09f
private val DIE_TEXT_TOPS = floatArrayOf(0.26f, 0.2f, 0.14f, 0.09f)
private val DIE_TEXT_WIDTHS = floatArrayOf(1f, 0.85f, 0.7f, 0.55f)

private const val SIN_60 = 0.8660254f
private const val DEGREES_TO_RADIANS = (Math.PI / 180.0).toFloat()

private val BodyLit = Color(0xFF50545E)
private val BodyMid = Color(0xFF1D1E23)
private val BodyDeep = Color(0xFF08080A)
private val EightDiscShade = Color(0xFFDCDFE6)
private val BezelLit = Color(0xFF3A3D47)
private val BezelDeep = Color(0xFF020203)
private val LiquidLit = Color(0xFF1C2678)
private val LiquidDeep = Color(0xFF05071E)
private val DieLit = Color(0xFF6C8AFF)
private val DieDeep = Color(0xFF2C41CC)
private val DieEdgeLight = Color(0xFFB9C6FF)
