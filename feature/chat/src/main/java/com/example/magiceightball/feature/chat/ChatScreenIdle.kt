package com.example.magiceightball.feature.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.components.BallColors
import com.example.magiceightball.feature.chat.components.DesignedMotion
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The ball at rest, and while it listens: its "8" exactly in the centre and one line a fixed
 * distance below it. The line says how to ask (by holding where the app may listen, by shaking
 * otherwise) and, while the app listens, what it has understood so far.
 *
 * Both are positioned in a single layout pass, so they are in place from the very first frame.
 *
 * @param usesAiAnswers whether a shake asks the AI service, which the hint then says.
 * @param canAskByVoice whether holding the screen listens to a question.
 * @param listening the listening in progress, if any.
 * @param voiceLevel how loud the voice is while listening, from 0 to 1; read while drawing only.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
fun ChatScreenIdle(
    usesAiAnswers: Boolean,
    modifier: Modifier = Modifier,
    canAskByVoice: Boolean = false,
    listening: ChatStateMachine.Listening? = null,
    voiceLevel: () -> Float = { 0f }
) {
    val heard = listening?.transcript.orEmpty()
    val line = when {
        // The latest words are the proof that the app is hearing: when there are too many to
        // fit, it is the first ones that give way.
        heard.length > TRANSCRIPT_MAX_CHARS -> ELLIPSIS + heard.takeLast(TRANSCRIPT_MAX_CHARS).trimStart()
        heard.isNotEmpty() -> heard
        listening != null -> localizedString(Strings.Listening)
        canAskByVoice -> localizedString(Strings.HoldHint)
        usesAiAnswers -> localizedString(Strings.ShakeHintAi)
        else -> localizedString(Strings.ShakeHint)
    }

    Layout(
        content = {
            EightBadge(
                name = localizedString(Strings.Title),
                listening = listening != null,
                voiceLevel = voiceLevel
            )
            Text(
                text = line,
                modifier = Modifier.semantics { if (listening != null) liveRegion = LiveRegionMode.Polite },
                // What the user said is theirs: it is written larger and brighter than a hint.
                color = BallColors.OnSurface.copy(alpha = if (heard.isNotEmpty()) 1f else HELPER_ALPHA),
                textAlign = TextAlign.Center,
                maxLines = HELPER_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                style = if (heard.isNotEmpty()) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyExtraSmall
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) { (badgeMeasurable, helperMeasurable), constraints ->
        val badgeSize = (min(constraints.maxWidth, constraints.maxHeight) * BADGE_SCREEN_FRACTION).roundToInt()
        val badge = badgeMeasurable.measure(Constraints.fixed(badgeSize, badgeSize))
        // Low on a round screen there is less width than across its middle.
        val helper = helperMeasurable.measure(
            Constraints(maxWidth = (constraints.maxWidth * HELPER_WIDTH_FRACTION).roundToInt())
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            val badgeY = (constraints.maxHeight - badge.height) / 2
            badge.place(
                x = (constraints.maxWidth - badge.width) / 2,
                y = badgeY
            )
            helper.place(
                x = (constraints.maxWidth - helper.width) / 2,
                y = badgeY + badge.height + HelperSpacing.roundToPx()
            )
        }
    }
}

/**
 * The white disc with the "8", as on the phone's ball. While the app listens, rings spread
 * around it: they breathe slowly on their own and are pushed out by the voice, and the disc
 * swells a little with it, so that speaking visibly does something. The digit is laid out once
 * per size and scales with the disc; [name] is what it is called for accessibility.
 */
@Composable
private fun EightBadge(
    name: String,
    listening: Boolean,
    voiceLevel: () -> Float,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val rings = remember { Animatable(0f) }
    LaunchedEffect(listening) {
        withContext(DesignedMotion) { rings.animateTo(if (listening) 1f else 0f, tween(RINGS_FADE_MILLIS)) }
    }
    // The rings' own slow breath, on the display's frames, for as long as the app listens.
    val breath = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(listening) {
        if (!listening) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsedMillis = (now - start) / NANOS_PER_MILLI
                breath.floatValue = (elapsedMillis % RING_BREATH_MILLIS).toFloat() / RING_BREATH_MILLIS
            }
        }
    }

    Spacer(
        modifier
            .semantics { contentDescription = name }
            .drawWithCache {
                val eight = textMeasurer.measure(
                    text = EIGHT,
                    style = TextStyle(
                        color = BallColors.BallBottom,
                        fontSize = (size.minDimension * EIGHT_SIZE_FRACTION).toSp(),
                        fontWeight = FontWeight.Black,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                val topLeft = Offset(
                    x = (size.width - eight.size.width) / 2f,
                    y = (size.height - eight.size.height) / 2f
                )
                val radius = size.minDimension / 2f
                onDrawBehind {
                    val shown = rings.value
                    if (shown > 0f) {
                        val level = voiceLevel()
                        for (ring in RING_COUNT downTo 1) {
                            val swell = 0.5f + 0.5f * sin(TWO_PI * (breath.floatValue - ring * RING_BREATH_LAG))
                            val reach = RING_STEP * ring * (RING_REST + (1f - RING_REST) * swell) +
                                RING_VOICE_REACH * level * ring / RING_COUNT
                            drawCircle(Color.White, radius * (1f + reach * shown), alpha = RING_ALPHA * shown)
                        }
                    }
                    scale(1f + BADGE_VOICE_SWELL * voiceLevel() * shown) {
                        drawCircle(Color.White)
                        drawText(eight, topLeft = topLeft)
                    }
                }
            }
    )
}

private const val EIGHT = "8"

/** Share of the screen taken by the disc, and of the disc taken by its digit. */
private const val BADGE_SCREEN_FRACTION = 0.44f
private const val EIGHT_SIZE_FRACTION = 0.72f
private const val HELPER_ALPHA = 0.75f
private const val HELPER_MAX_LINES = 2
private const val HELPER_WIDTH_FRACTION = 0.8f

/** No more of what was heard than fits under the "8": beyond this, its latest part. */
private const val TRANSCRIPT_MAX_CHARS = 42
private const val ELLIPSIS = "…"

/**
 * The rings around the "8" while the app listens, as on the phone: how far each one stands
 * from the next relative to the disc's radius, how much of that it keeps between breaths, and
 * how far the voice pushes the outermost one.
 */
private const val RING_COUNT = 3
private const val RING_STEP = 0.17f
private const val RING_REST = 0.55f
private const val RING_BREATH_LAG = 0.16f
private const val RING_BREATH_MILLIS = 1900L
private const val RING_VOICE_REACH = 0.42f
private const val RING_ALPHA = 0.1f
private const val RINGS_FADE_MILLIS = 220
private const val BADGE_VOICE_SWELL = 0.06f
private const val NANOS_PER_MILLI = 1_000_000L
private const val TWO_PI = (2.0 * Math.PI).toFloat()

private val HelperSpacing = 10.dp

@Preview(name = "ChatScreenIdle - Wear", showBackground = true, widthDp = 192, heightDp = 192)
@Composable
private fun ChatScreenIdlePreview() {
    MagicEightBallTheme {
        ChatScreenIdle(
            usesAiAnswers = true,
            modifier = Modifier
                .size(192.dp)
                .background(BallColors.Ball, shape = CircleShape)
        )
    }
}
