package com.lorenzosuarez.magiceightball.phone.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.lorenzosuarez.magiceightball.phone.R
import java.io.File

/**
 * An answer as a picture to send to someone, composed as a poster and not as a picture of the
 * screen: one flat colour, which is the colour of the style that answered, and black ink. What
 * was asked is a speech bubble, so it reads as said by the person; what the ball said is the
 * headline, as large as it fits, signed by the style underneath like the author of a quote.
 * The ball leans towards the words from the corner, dressed as the style.
 *
 * Always the same size and colours, whatever the phone and its settings, so it looks the same
 * wherever it ends up. It is laid out but drawn only into [layer], never on the screen:
 * [GraphicsLayer.toImageBitmap] then gives the picture.
 *
 * @param personality the style that answered: it gives the colour and what the ball wears.
 * @param styleLabel the name of that style, which signs the answer.
 * @param remark what the character added under its answer.
 * @param visible true to draw it on the screen as well, for previews and screenshot tests.
 */
@Composable
internal fun ShareCard(
    question: String,
    answer: String,
    layer: GraphicsLayer,
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    remark: String? = null,
    styleLabel: String? = null,
    personality: Magic8BallPersonality = Magic8BallPersonality.NEUTRAL,
    look: CustomCharacter? = null
) {
    // A character of the user's own has the colour and wears the accessory it borrowed.
    val colour = look?.colour ?: personality
    val emblem = look?.emblem ?: personality
    CompositionLocalProvider(LocalDensity provides Density(SHARE_DENSITY, fontScale = 1f)) {
        MagicEightBallPhoneTheme(dark = false) {
            Box(
                modifier = modifier
                    .requiredSize(SHARE_WIDTH, SHARE_HEIGHT)
                    .clearAndSetSemantics {}
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        if (visible) drawLayer(layer)
                    }
                    .clipToBounds()
                    .background(shareColor(colour))
            ) {
                // Anything written under the answer needs the room: the ball steps back into the corner for it.
                val reading = remark != null && remark.length > SHARE_LONG_REMARK
                FlatBall(
                    personality = emblem,
                    modifier = Modifier
                        .offset(x = SHARE_BALL_LEFT, y = SHARE_BALL_TOP)
                        .requiredSize(SHARE_BALL_SIZE)
                        .graphicsLayer {
                            if (remark != null) {
                                scaleX = SHARE_BALL_BACK
                                scaleY = SHARE_BALL_BACK
                                translationX = SHARE_BALL_BACK_SHIFT.toPx()
                                // Less far down than across: the white disc stays whole on the card.
                                translationY = SHARE_BALL_BACK_DROP.toPx()
                            }
                        }
                )
                Column(modifier = Modifier.fillMaxSize().padding(SHARE_MARGIN)) {
                    if (question.isNotBlank()) {
                        Text(
                            text = question,
                            modifier = Modifier
                                .drawBehind { drawBubble() }
                                .padding(start = 16.dp, end = 16.dp, top = 11.dp, bottom = 11.dp + SHARE_BUBBLE_TAIL),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SHARE_INK,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    // As large as it fits: a short answer shouts, a long one still leaves room.
                    // And never a word cut in two: the longest one sets the size as well.
                    val longest = answer.split(' ').maxOf { it.length }
                    val size = when {
                        longest > SHARE_WORD_AT_38 -> 30
                        longest > SHARE_WORD_AT_46 || answer.length > SHARE_MEDIUM_ANSWER -> 38
                        longest > SHARE_WORD_AT_58 || answer.length > SHARE_SHORT_ANSWER -> 46
                        else -> 58
                    }.let { if (remark != null) minOf(it, SHARE_READING_ANSWER) else it }
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = size.sp,
                            lineHeight = (size * SHARE_ANSWER_LEADING).sp,
                            letterSpacing = (-size * SHARE_ANSWER_TRACKING).sp,
                            lineBreak = LineBreak.Heading
                        ),
                        fontWeight = FontWeight.Black,
                        color = SHARE_INK,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (remark != null) {
                        Text(
                            text = remark,
                            // Kept clear of the ball, which comes in from the right lower down.
                            modifier = Modifier.padding(top = 12.dp, end = SHARE_READING_INSET),
                            // A few sentences are set smaller, so that all of them are on the card.
                            style = if (reading) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SHARE_INK,
                            maxLines = if (reading) 9 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Signed like a quote, in another hand: the one change of voice on the card.
                    if (styleLabel != null) {
                        val signature = "\u2014 $styleLabel"
                        val hand = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        )
                        val measurer = rememberTextMeasurer()
                        BoxWithConstraints(modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) {
                            // A name the user gave may be wider than any of the app's: it is
                            // written smaller, a step at a time, before it is cut short.
                            val room = constraints.maxWidth
                            val size = remember(signature, room) {
                                SIGNATURE_SIZES.firstOrNull {
                                    measurer.measure(signature, hand.copy(fontSize = it), softWrap = false).size.width <= room
                                } ?: SIGNATURE_SIZES.last()
                            }
                            Text(
                                text = signature,
                                style = hand.copy(fontSize = size),
                                color = SHARE_INK,
                                // One line and never a second: too wide even then, it is cut short, not lost.
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // A maker's mark, not a line to read.
                    Text(
                        text = stringResource(R.string.brand_name),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SHARE_INK.copy(alpha = SHARE_MARK_ALPHA)
                    )
                }
            }
        }
    }
}

/** A white bubble around what was asked, its tail at the left, away from the ball, so it is not the ball that seems to say it. */
private fun DrawScope.drawBubble() {
    val tail = SHARE_BUBBLE_TAIL.toPx()
    val body = size.height - tail
    val corner = minOf(SHARE_BUBBLE_CORNER.toPx(), body / 2)
    drawRoundRect(Color.White, size = Size(size.width, body), cornerRadius = CornerRadius(corner))
    drawPath(
        Path().apply {
            moveTo(corner + tail * 1.4f, body - 1f)
            lineTo(corner - tail * 0.5f, body + tail)
            lineTo(corner, body - tail)
            close()
        },
        Color.White
    )
}

/**
 * The ball as the card draws it, wearing what [emblem] wears, in a square of [size]: for showing
 * a look where one is picked. It is the card's own drawing at the card's own measures, brought
 * to the size asked for, with room over and beside the ball for what it wears.
 */
@Composable
internal fun CharacterBall(emblem: Magic8BallPersonality, size: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    Box(modifier = modifier.requiredSize(size).clipToBounds().clearAndSetSemantics {}) {
        CompositionLocalProvider(
            LocalDensity provides Density(density * (size / SHARE_BALL_SIZE) * BALL_IN_SQUARE, fontScale = 1f)
        ) {
            FlatBall(
                personality = emblem,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = SHARE_BALL_SIZE * BALL_DROP)
                    .requiredSize(SHARE_BALL_SIZE)
            )
        }
    }
}

/** How much of its square the ball takes, and how far under the middle it sits, to leave the crown its room. */
private const val BALL_IN_SQUARE = 0.6f
private const val BALL_DROP = 0.16f

/**
 * The ball with nothing but its two colours. It has one axis, tipped towards the words: what
 * it wears sits on top of that axis, the white disc is turned along it and the number stands
 * on it, so the three agree on where the ball is looking.
 */
@Composable
private fun FlatBall(personality: Magic8BallPersonality, modifier: Modifier = Modifier) {
    val wears = remember(personality) { worn(personality) }
    val worn = remember(wears) { wears?.shape?.let { PathParser().parsePathString(it).toPath() } }
    val cut = remember(wears) { wears?.cut?.let { PathParser().parsePathString(it).toPath() } }
    val beside = remember(wears) { wears?.beside?.let { PathParser().parsePathString(it).toPath() } }
    val besideCut = remember(wears) { wears?.besideCut?.let { PathParser().parsePathString(it).toPath() } }
    Box(modifier = modifier.rotate(SHARE_BALL_TILT)) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            if (beside != null) {
                // Behind the ball, at the ball's own size.
                val unit = radius / WORN_RADIUS
                withTransform({
                    translate(center.x - WORN_CENTRE_X * unit, center.y - WORN_CENTRE_Y * unit)
                    scale(unit, unit, pivot = Offset.Zero)
                }) {
                    drawPath(beside, SHARE_INK)
                    drawPath(beside, SHARE_INK, style = Stroke(width = BESIDE_SOFTEN, join = StrokeJoin.Round))
                    if (besideCut != null) drawPath(besideCut, Color.White)
                }
            }
            if (wears != null && worn != null) {
                // Drawn on a ball of radius WORN_RADIUS whose centre is at WORN_CENTRE.
                val unit = radius / WORN_RADIUS
                withTransform({
                    translate(center.x - WORN_CENTRE_X * unit, center.y - WORN_CENTRE_Y * unit)
                    scale(unit, unit, pivot = Offset.Zero)
                    // Small, on the crown of the ball, and a little crooked where it sits.
                    val crown = Offset(WORN_CENTRE_X, WORN_CENTRE_Y - WORN_RADIUS)
                    scale(WORN_SIZE * wears.size, WORN_SIZE * wears.size, pivot = crown)
                    rotate(wears.tilt, pivot = crown + Offset(0f, WORN_SEAT))
                }) {
                    drawPath(worn, SHARE_INK)
                    drawPath(worn, SHARE_INK, style = Stroke(width = WORN_SOFTEN, join = StrokeJoin.Round))
                    // What is cut out of it in white: the one detail that says what the shape is.
                    if (cut != null) drawPath(cut, Color.White)
                }
            }
            drawCircle(SHARE_INK, radius)
            val disc = Size(radius * SHARE_DISC_WIDE * 2, radius * SHARE_DISC_TALL * 2)
            drawOval(
                Color.White,
                topLeft = Offset(
                    center.x + radius * SHARE_DISC_X - disc.width / 2,
                    center.y + radius * SHARE_DISC_Y - disc.height / 2
                ),
                size = disc
            )
        }
        Text(
            text = "8",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = SHARE_BALL_SIZE / 2 * SHARE_DISC_X, y = SHARE_BALL_SIZE / 2 * SHARE_DISC_Y)
                .graphicsLayer { scaleX = SHARE_DISC_WIDE / SHARE_DISC_TALL },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp, lineHeight = 52.sp),
            fontWeight = FontWeight.Black,
            color = SHARE_INK
        )
    }
}

/**
 * What the ball wears for a style, in the ball's own ink: SVG paths on a ball of radius
 * [WORN_RADIUS] centred at ([WORN_CENTRE_X], [WORN_CENTRE_Y]).
 *
 * @property shape the thing itself. Overlapping parts of it run the same way round, or they
 * would cut holes in each other.
 * @property cut what is cut out of it in white: at most one detail, as thick as the next one's,
 * and only where the shape does not say what it is without it.
 * @property size how large it is beside the others: a small hat on a large ball is the joke.
 * @property tilt how crooked it sits, in degrees: put on in a hurry, never straight.
 * @property beside what the character has with it, beside the ball and partly behind it, at
 * the ball's own size: a tail, the glass a detective looks through. Kept on the side the words
 * are on, clear of them and of the card's edge.
 * @property besideCut what is cut out of [beside] in white.
 */
private class Worn(
    val shape: String,
    val cut: String? = null,
    val size: Float = 1f,
    val tilt: Float = 0f,
    val beside: String? = null,
    val besideCut: String? = null
)

/**
 * What the app's characters wear, drawn to one set of rules so that they read as one family. There are two
 * kinds: something worn, which sits on the crown and touches it, and a small charm that floats
 * just over it (a pair of quotes, a moon, a star, a heart, a thought). Two of them have
 * something beside them as well, where that is what says who they are. Each is built of
 * circles, bars and plain curves, stands on the ball's axis and is about as heavy as the next.
 * The classic style wears nothing.
 */
private fun worn(personality: Magic8BallPersonality): Worn? = when (personality) {
    Magic8BallPersonality.NEUTRAL -> null
    // One of the user's own wears what it borrowed from another: asked for by that other's name.
    Magic8BallPersonality.CUSTOM -> null
    Magic8BallPersonality.SARCASTIC -> Worn(
        shape = "M35 11 a6 6 0 1 1 12 0 q0 9 -9 14 q3 -5 2 -8.4 a6 6 0 0 1 -5 -5.6Z" +
            " M53 11 a6 6 0 1 1 12 0 q0 9 -9 14 q3 -5 2 -8.4 a6 6 0 0 1 -5 -5.6Z"
    )
    Magic8BallPersonality.MYSTICAL -> Worn(
        shape = "M56 4 A11 11 0 1 0 61 24 A9 9 0 1 1 56 4Z" +
            " M72 6 l1.6 4.4 l4.4 1.6 l-4.4 1.6 l-1.6 4.4 l-1.6 -4.4 l-4.4 -1.6 l4.4 -1.6Z"
    )
    Magic8BallPersonality.GRANDMA -> Worn(
        shape = "M39.50 21.50 a10.5 10.5 0 1 0 21 0 a10.5 10.5 0 1 0 -21 0Z" +
            " M48.6 22.2 L50.8 23.9 L63.2 8.2 L61.0 6.5Z M54.1 25.7 L56.3 27.4 L68.7 11.7 L66.5 10.0Z" +
            " M59.30 7.30 a2.7 2.7 0 1 0 5.4 0 a2.7 2.7 0 1 0 -5.4 0Z" +
            " M64.80 10.80 a2.7 2.7 0 1 0 5.4 0 a2.7 2.7 0 1 0 -5.4 0Z",
        cut = "M43 19 Q50 15.5 57 19 v1.9 Q50 17.4 43 20.9Z" +
            " M42 24.5 Q50 20.7 58 24.5 v1.9 Q50 22.6 42 26.4Z",
        size = 1.1f
    )
    Magic8BallPersonality.PIRATE -> Worn(
        shape = "M18 30 Q22 34 31 33 Q50 28 69 33 Q78 34 82 30 Q80 24 71 24 Q66 7 50 7 Q34 7 29 24 Q20 24 18 30Z",
        cut = "M47.7 12.3 L45.5 13.7 L52.3 24.7 L54.5 23.3Z" +
            " M51.54 24.01 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0Z" +
            " M44.66 12.99 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0Z" +
            " M43.4 18.6 L43.9 21.1 L56.6 18.4 L56.1 15.9Z" +
            " M54.46 17.15 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0Z" +
            " M41.74 19.85 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0Z",
        size = 0.8f,
        tilt = -5.0f
    )
    Magic8BallPersonality.PHILOSOPHER -> Worn(
        shape = "M63.0 13.0 L62.7 11.6 L61.9 10.2 L60.6 8.9 L58.9 7.9 L56.8 7.1 L54.4 6.7 L52.0 6.5 L49.6 6.7 L47.2 7.1 L45.1 7.9 L43.4 8.9 L42.1 10.2 L41.3 11.6 L41.0 13.0 L41.3 14.4 L42.1 15.8 L43.4 17.1 L45.1 18.1 L47.2 18.9 L49.6 19.3 L52.0 19.5 L54.4 19.3 L56.8 18.9 L58.9 18.1 L60.6 17.1 L61.9 15.8 L62.7 14.4Z" +
            " M58.10 13.00 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M53.77 18.32 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M44.04 19.63 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M36.24 15.95 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M36.24 10.05 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M44.04 6.37 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M53.77 7.68 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M41.00 26.00 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0Z" +
            " M45.60 32.00 a1.9 1.9 0 1 0 3.8 0 a1.9 1.9 0 1 0 -3.8 0Z",
        cut = "M44.80 13.00 a1.8 1.8 0 1 0 3.6 0 a1.8 1.8 0 1 0 -3.6 0Z" +
            " M50.20 13.00 a1.8 1.8 0 1 0 3.6 0 a1.8 1.8 0 1 0 -3.6 0Z" +
            " M55.60 13.00 a1.8 1.8 0 1 0 3.6 0 a1.8 1.8 0 1 0 -3.6 0Z",
        size = 1.05f,
        tilt = 20.0f
    )
    Magic8BallPersonality.COACH -> Worn(
        shape = "M37 33 Q37 14 52 14 Q67 14 67 33 Q52 30 37 33Z" +
            " M38 33 Q28 34.5 20 34.8 Q15.8 34.6 16.6 31.6 Q17.6 29.2 21.5 29.3 Q30 29.5 39 28.5Z",
        cut = "M40 26.5 Q53 24.3 66 26.5 v2.8 Q53 27.1 40 29.3Z",
        tilt = 10.0f
    )
    Magic8BallPersonality.LOVESTRUCK -> Worn(
        shape = "M50 26 C37 16.5 40.5 4.5 50 11.5 C59.5 4.5 63 16.5 50 26Z"
    )
    Magic8BallPersonality.DRAMA -> Worn(
        shape = "M36 34 L34 15 L43 23 L50 10 L57 23 L66 15 L64 34 Q50 30 36 34Z" +
            " M30.8 13.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z" +
            " M46.8 8.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z" +
            " M62.8 13.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z",
        size = 0.9f,
        tilt = 8f
    )
    Magic8BallPersonality.ROBOT -> Worn(
        shape = "M48.5 12 v16 h3 v-16Z M44.5 10 a5.5 5.5 0 1 0 11 0 a5.5 5.5 0 1 0 -11 0Z" +
            " M41 35 Q50 32 59 35 Q59 25 50 25 Q41 25 41 35Z",
        cut = "M47.8 10 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0Z",
        size = 1.2f
    )
    Magic8BallPersonality.DETECTIVE -> Worn(
        shape = "M23 33.5 Q50 27 77 33.5 Q78.5 36.3 75 36.8 Q50 31.3 25 36.8 Q21.5 36.3 23 33.5Z" +
            " M36 31.5 Q34.5 14 43 12.5 Q50 15 57 12.5 Q65.5 14 64 31.5Z",
        cut = "M36 24.5 Q50 22.5 64 24.5 v3.4 Q50 25.9 36 27.9Z",
        size = 0.85f,
        tilt = 3.0f,
        beside = "M11.90 39.24 a9.5 9.5 0 1 0 19 0 a9.5 9.5 0 1 0 -19 0Z" +
            " M15.20 39.24 a6.2 6.2 0 1 1 12.4 0 a6.2 6.2 0 1 1 -12.4 0Z" +
            " M16.4 46.7 L13.4 43.5 L5.6 50.5 L8.6 53.8Z"
    )
    Magic8BallPersonality.CONSPIRACY -> Worn(
        shape = "M34 37 L50 4 L66 37 Q50 31 34 37Z",
        cut = "M42 25 Q50 16.5 58 25 Q50 33.5 42 25Z M47 25 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0Z",
        size = 1.0f
    )
    Magic8BallPersonality.CAT -> Worn(
        shape = "M29 38 Q28 22 33 12 Q41 20 47 31Z M71 38 L53 31 Q59 20 67 12 Q72 22 71 38Z",
        cut = "M33.5 31.5 Q33 24 35 19 Q39 24 42 30.5Z M66.5 31.5 L58 30.5 Q61 24 65 19 Q67 24 66.5 31.5Z",
        size = 1.12f,
        beside = "M30.2 55.1 L28.6 55.1 L27.3 54.9 L26.0 54.7 L24.9 54.4 L23.9 54.0 L23.0 53.6 L22.1 53.1 L21.4 52.6 L20.8 52.0 L20.2 51.4 L19.8 50.8 L19.4 50.2 L19.1 49.5 L18.8 48.7 L18.7 48.0 L18.6 47.2 L18.6 46.4 L18.7 45.5 L18.9 44.7 L19.2 43.8 L19.6 42.9 L20.0 42.0 L20.6 41.0 L21.3 40.1 L21.5 39.9 L21.8 39.6 L22.0 39.4 L22.3 39.3 L22.5 39.1 L22.7 39.0 L22.9 39.0 L23.1 38.9 L23.3 38.9 L23.4 38.9 L23.5 38.9 L23.5 38.9 L23.6 38.9 L23.6 38.9 L23.6 38.9 L23.6 38.9 L23.6 38.8 L23.6 38.9 L23.6 38.9 L23.6 39.0 L23.6 39.1 L23.6 39.3 L23.5 39.6 L23.3 40.0 L23.1 40.8 L23.1 41.5 L23.5 42.2 L24.1 42.7 L24.9 42.9 L25.6 42.9 L26.3 42.5 L26.8 41.9 L27.1 41.3 L27.4 40.7 L27.6 40.0 L27.7 39.3 L27.7 38.6 L27.7 38.0 L27.5 37.3 L27.2 36.7 L26.8 36.1 L26.3 35.6 L25.8 35.2 L25.2 34.9 L24.6 34.7 L24.0 34.6 L23.4 34.5 L22.7 34.5 L22.1 34.6 L21.4 34.7 L20.8 34.9 L20.1 35.2 L19.5 35.6 L18.9 36.0 L18.3 36.5 L17.7 37.1 L16.8 38.3 L15.9 39.5 L15.3 40.8 L14.7 42.0 L14.2 43.3 L13.9 44.7 L13.7 46.0 L13.7 47.3 L13.8 48.6 L14.0 49.9 L14.3 51.1 L14.8 52.4 L15.5 53.5 L16.2 54.6 L17.1 55.6 L18.1 56.6 L19.2 57.4 L20.5 58.2 L21.8 58.9 L23.3 59.4 L24.8 59.9 L26.5 60.2 L28.2 60.4 L29.9 60.5Z"
    )
    Magic8BallPersonality.ZOOMER -> Worn(
        shape = "M6.6 50 A47.5 47.5 0 0 1 93.4 50 L90.3 51 A44.3 44.3 0 0 0 9.7 51Z" +
            " M15.8 54.0 L15.6 56.1 L15.2 58.1 L14.5 59.9 L13.5 61.4 L12.4 62.6 L11.1 63.3 L9.8 63.5 L8.5 63.3 L7.2 62.6 L6.1 61.4 L5.1 59.9 L4.4 58.1 L4.0 56.1 L3.8 54.0 L4.0 51.9 L4.4 49.9 L5.1 48.1 L6.1 46.6 L7.2 45.4 L8.5 44.7 L9.8 44.5 L11.1 44.7 L12.4 45.4 L13.5 46.6 L14.5 48.1 L15.2 49.9 L15.6 51.9Z" +
            " M96.2 54.0 L96.0 56.1 L95.6 58.1 L94.9 59.9 L93.9 61.4 L92.8 62.6 L91.5 63.3 L90.2 63.5 L88.9 63.3 L87.6 62.6 L86.5 61.4 L85.5 59.9 L84.8 58.1 L84.4 56.1 L84.2 54.0 L84.4 51.9 L84.8 49.9 L85.5 48.1 L86.5 46.6 L87.6 45.4 L88.9 44.7 L90.2 44.5 L91.5 44.7 L92.8 45.4 L93.9 46.6 L94.9 48.1 L95.6 49.9 L96.0 51.9Z"
    )
    Magic8BallPersonality.VILLAIN -> Worn(
        shape = "M37 39 Q23 30 29 9 Q33 24 45 32Z M63 39 L55 32 Q67 24 71 9 Q77 30 63 39Z",
        size = 0.95f
    )
    Magic8BallPersonality.POET -> Worn(
        shape = "M27 31 Q24 15 46 14 Q73 12 73 25 Q73 34 50 33.5 Q34 35 27 31Z M48.6 8 h2.8 v8 h-2.8Z",
        size = 0.82f
    )
    Magic8BallPersonality.ASTRO -> Worn(
        shape = "M50 2 L52.5 11.5 L62 14 L52.5 16.5 L50 26 L47.5 16.5 L38 14 L47.5 11.5Z"
    )
    Magic8BallPersonality.THERAPIST -> Worn(
        shape = "M32.40 21.20 a8.6 8.6 0 1 0 17.2 0 a8.6 8.6 0 1 0 -17.2 0Z" +
            " M35.40 21.20 a5.6 5.6 0 1 1 11.2 0 a5.6 5.6 0 1 1 -11.2 0Z" +
            " M50.40 21.20 a8.6 8.6 0 1 0 17.2 0 a8.6 8.6 0 1 0 -17.2 0Z" +
            " M53.40 21.20 a5.6 5.6 0 1 1 11.2 0 a5.6 5.6 0 1 1 -11.2 0Z" +
            " M47.4 19.2 L47.4 22.6 Q50 20.4 52.6 22.6 L52.6 19.2 Q50 17 47.4 19.2Z" +
            " M33.4 19.0 L34.7 16.7 L29.0 13.4 L27.7 15.7Z M65.3 16.7 L66.6 19.0 L72.3 15.7 L71.0 13.4Z",
        size = 1.15f,
        tilt = 9.0f
    )
    Magic8BallPersonality.DOG -> Worn(
        shape = "M34.5 20.7 L36.4 28.5 L65.5 21.3 L63.6 13.5Z" +
            " M28.9811 20.3595 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M31.11 28.8981 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M58.09 13.1019 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
            " M60.2189 21.6405 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z",
        size = 0.9f
    )
}

/**
 * The colour of a style's card. Each is light enough for black ink, and no two neighbours in
 * the list of styles are close, so a chat full of shared answers tells them apart at a glance.
 */
internal fun shareColor(personality: Magic8BallPersonality): Color = Color(
    when (personality) {
        Magic8BallPersonality.SARCASTIC -> 0xFFFFDD33
        Magic8BallPersonality.MYSTICAL -> 0xFFC9B6FF
        Magic8BallPersonality.NEUTRAL -> 0xFFF2EEE3
        Magic8BallPersonality.GRANDMA -> 0xFFFFC2D1
        Magic8BallPersonality.PIRATE -> 0xFF5FD6C9
        Magic8BallPersonality.PHILOSOPHER -> 0xFFE3D5B8
        Magic8BallPersonality.COACH -> 0xFFFF8A3D
        Magic8BallPersonality.LOVESTRUCK -> 0xFFFF7A9C
        Magic8BallPersonality.DRAMA -> 0xFFFF5A4F
        Magic8BallPersonality.ROBOT -> 0xFFB8C4D0
        Magic8BallPersonality.DETECTIVE -> 0xFFD6C68A
        Magic8BallPersonality.CONSPIRACY -> 0xFF9BE564
        Magic8BallPersonality.CAT -> 0xFFFFB86B
        Magic8BallPersonality.ZOOMER -> 0xFFD4FF4F
        Magic8BallPersonality.VILLAIN -> 0xFFB58CFF
        Magic8BallPersonality.POET -> 0xFFBFD9FF
        Magic8BallPersonality.ASTRO -> 0xFF8FA8FF
        Magic8BallPersonality.THERAPIST -> 0xFFBFE8D2
        Magic8BallPersonality.DOG -> 0xFFF0B878
        // One of the user's own has the colour it borrowed: this is the card with none chosen.
        Magic8BallPersonality.CUSTOM -> 0xFFF2EEE3
    }
)

/** Saves [picture] where other apps may read it and hands it, with [text], to whichever app the user picks. */
internal fun Context.shareAnswer(picture: ImageBitmap, text: String) {
    val file = File(cacheDir, SHARE_DIRECTORY).apply { mkdirs() }.resolve(SHARE_FILE)
    try {
        file.outputStream().use { picture.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    } catch (_: java.io.IOException) {
        // No room to save it: there is nothing to share.
        return
    }
    val uri = FileProvider.getUriForFile(this, "$packageName.share", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_TEXT, text)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    // What lets the chooser show the picture, and read it.
    send.clipData = ClipData.newRawUri(null, uri)
    try {
        startActivity(Intent.createChooser(send, null))
    } catch (_: ActivityNotFoundException) {
        // No app to share with: nothing to do.
    }
}

/** 1080 by 1350 pixels: the shape a feed shows whole and a chat does not crop. */
private const val SHARE_DENSITY = 3f
private val SHARE_WIDTH = 360.dp
private val SHARE_HEIGHT = 450.dp
private val SHARE_MARGIN = 28.dp
private val SHARE_INK = Color(0xFF12110D)

/** The sizes the signature is tried at, largest first. */
private val SIGNATURE_SIZES = listOf(24.sp, 22.sp, 20.sp, 18.sp)

/** The card's ink, for whatever shows a card's look elsewhere: the same in both themes. */
internal val ShareInk = SHARE_INK

/** Up to these many letters an answer is set at the largest and the middle size. */
private const val SHARE_SHORT_ANSWER = 16
private const val SHARE_MEDIUM_ANSWER = 28

/** The longest word, in letters, that fits the card's width at each size of the answer. */
private const val SHARE_WORD_AT_58 = 9
private const val SHARE_WORD_AT_46 = 12
private const val SHARE_WORD_AT_38 = 15
private const val SHARE_ANSWER_LEADING = 0.98f
private const val SHARE_ANSWER_TRACKING = 0.02f

/** How faint the name of the app is: there to be found, not to be read first. */
private const val SHARE_MARK_ALPHA = 0.8f

/** From this many characters a remark is a few sentences, not a line. */
private const val SHARE_LONG_REMARK = 120
/** How far the ball steps back for them: its size, and how far towards the corner. */
private const val SHARE_BALL_BACK = 0.7f
private val SHARE_BALL_BACK_SHIFT = 30.dp
private val SHARE_BALL_BACK_DROP = 16.dp
/** With them, the answer gives up some of its size and the text some of its margin. */
private const val SHARE_READING_ANSWER = 38
private val SHARE_READING_INSET = 48.dp
private val SHARE_BUBBLE_CORNER = 20.dp
private val SHARE_BUBBLE_TAIL = 9.dp

/** The ball: where it sits, cut by the corner, and how far it leans towards the words. */
private val SHARE_BALL_SIZE = 210.dp
private val SHARE_BALL_LEFT = 190.dp
private val SHARE_BALL_TOP = 300.dp
private const val SHARE_BALL_TILT = -18f

/** The white disc, in parts of the ball's radius: off centre and narrowed, as on a sphere. */
private const val SHARE_DISC_X = -0.23f
private const val SHARE_DISC_Y = -0.18f
private const val SHARE_DISC_WIDE = 0.37f
private const val SHARE_DISC_TALL = 0.42f

/** The ball the silhouettes are drawn on. */
private const val WORN_RADIUS = 28f
private const val WORN_CENTRE_X = 50f
private const val WORN_CENTRE_Y = 58f
private const val WORN_SOFTEN = 1.5f
private const val WORN_SIZE = 0.66f
private const val BESIDE_SOFTEN = 1f
/** How far under the crown the point is that a crooked thing turns about. */
private const val WORN_SEAT = 2f

private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-8-ball.png"
