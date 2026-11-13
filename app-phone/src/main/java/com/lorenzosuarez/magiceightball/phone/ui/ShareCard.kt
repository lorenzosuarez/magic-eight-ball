package com.lorenzosuarez.magiceightball.phone.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
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
    personality: Magic8BallPersonality = Magic8BallPersonality.NEUTRAL
) {
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
                    .background(shareColor(personality))
            ) {
                // Anything written under the answer needs the room: the ball steps back into the corner for it.
                val reading = remark != null && remark.length > SHARE_LONG_REMARK
                FlatBall(
                    personality = personality,
                    modifier = Modifier
                        .offset(x = SHARE_BALL_LEFT, y = SHARE_BALL_TOP)
                        .requiredSize(SHARE_BALL_SIZE)
                        .graphicsLayer {
                            if (remark != null) {
                                scaleX = SHARE_BALL_BACK
                                scaleY = SHARE_BALL_BACK
                                translationX = SHARE_BALL_BACK_SHIFT.toPx()
                                translationY = SHARE_BALL_BACK_SHIFT.toPx()
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
                    }.let { if (reading) minOf(it, SHARE_READING_ANSWER) else it }
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
                        Text(
                            text = "\u2014 $styleLabel",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 24.sp
                            ),
                            color = SHARE_INK,
                            maxLines = 1
                        )
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
 * The ball with nothing but its two colours. It has one axis, tipped towards the words: what
 * it wears sits on top of that axis, the white disc is turned along it and the number stands
 * on it, so the three agree on where the ball is looking.
 */
@Composable
private fun FlatBall(personality: Magic8BallPersonality, modifier: Modifier = Modifier) {
    val wears = remember(personality) { worn(personality) }
    val worn = remember(wears) { wears?.shape?.let { PathParser().parsePathString(it).toPath() } }
    val cut = remember(wears) { wears?.cut?.let { PathParser().parsePathString(it).toPath() } }
    Box(modifier = modifier.rotate(SHARE_BALL_TILT)) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
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
 */
private class Worn(val shape: String, val cut: String? = null, val size: Float = 1f, val tilt: Float = 0f)

/**
 * The nineteen, drawn to one set of rules so that they read as one family. There are two
 * kinds: something worn, which sits on the crown and touches it, and a small charm that floats
 * just over it (a pair of quotes, a moon, a star, a heart, a thought). Each is built of
 * circles, bars and plain curves, stands on the ball's axis and is about as heavy as the next.
 * The classic style wears nothing.
 */
private fun worn(personality: Magic8BallPersonality): Worn? = when (personality) {
    Magic8BallPersonality.NEUTRAL -> null
    Magic8BallPersonality.SARCASTIC -> Worn(
        shape = "M35 11 a6 6 0 1 1 12 0 q0 9 -9 14 q3 -5 2 -8.4 a6 6 0 0 1 -5 -5.6Z" +
            " M53 11 a6 6 0 1 1 12 0 q0 9 -9 14 q3 -5 2 -8.4 a6 6 0 0 1 -5 -5.6Z"
    )
    Magic8BallPersonality.MYSTICAL -> Worn(
        shape = "M56 4 A11 11 0 1 0 61 24 A9 9 0 1 1 56 4Z" +
            " M72 6 l1.6 4.4 l4.4 1.6 l-4.4 1.6 l-1.6 4.4 l-1.6 -4.4 l-4.4 -1.6 l4.4 -1.6Z"
    )
    Magic8BallPersonality.GRANDMA -> Worn(
        shape = "M35 36 Q33 15 50 15 Q67 15 65 36 Q50 31 35 36Z" +
            " M43.5 10 a6.5 6.5 0 1 1 13 0 a6.5 6.5 0 1 1 -13 0Z",
        cut = "M35.6 27.5 Q50 25.1 64.4 27.5 v3.4 Q50 28.5 35.6 30.9Z",
        tilt = 6f
    )
    Magic8BallPersonality.PIRATE -> Worn(
        shape = "M19 25 Q24 30 34 27 Q40 6 50 6 Q60 6 66 27 Q76 30 81 25 Q80 37 50 33 Q20 37 19 25Z",
        cut = "M46.4 13.4 L44.4 15.4 L53.6 24.6 L55.6 22.6Z M44.4 22.6 L46.4 24.6 L55.6 15.4 L53.6 13.4Z",
        size = 0.8f,
        tilt = -5f
    )
    Magic8BallPersonality.PHILOSOPHER -> Worn(
        shape = "M34.5 14 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
            " M44.5 10 a8.5 8.5 0 1 0 17 0 a8.5 8.5 0 1 0 -17 0Z" +
            " M52.5 17 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
            " M40.5 19 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
            " M42.6 31 a3.4 3.4 0 1 0 6.8 0 a3.4 3.4 0 1 0 -6.8 0Z"
    )
    Magic8BallPersonality.COACH -> Worn(
        shape = "M38 35 Q37 17 52 17 Q67 17 66 35 Q52 31.5 38 35Z M39 35 L18 35.5 Q15 33.5 18 31 L39 29Z",
        size = 0.9f,
        tilt = -6f
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
        cut = "M47.8 10 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0Z"
    )
    Magic8BallPersonality.DETECTIVE -> Worn(
        shape = "M22 35 Q50 26 78 35 v4.5 Q50 30.5 22 39.5Z M35 31 Q33 9 44 9 Q50 12.5 56 9 Q67 9 65 31Z",
        cut = "M35.2 25 Q50 23 64.8 25 v3.2 Q50 26.2 35.2 28.2Z",
        size = 0.72f,
        tilt = 7f
    )
    Magic8BallPersonality.CONSPIRACY -> Worn(
        shape = "M34 37 L50 4 L66 37 Q50 31 34 37Z",
        cut = "M42 25 Q50 16.5 58 25 Q50 33.5 42 25Z M47 25 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0Z",
        size = 0.85f
    )
    Magic8BallPersonality.CAT -> Worn(
        shape = "M34 36 L35 11 L48 29Z M66 36 L52 29 L65 11Z"
    )
    Magic8BallPersonality.ZOOMER -> Worn(
        shape = "M7.6 47.9 A49 49 0 0 1 92.4 47.9 L88.5 50.2 A44.5 44.5 0 0 0 11.5 50.2Z" +
            " M16.0 54.0 L15.8 56.6 L15.1 59.0 L14.1 61.1 L12.8 62.7 L11.2 63.7 L9.5 64.0 L7.8 63.7 L6.3 62.7 L4.9 61.1 L3.9 59.0 L3.2 56.6 L3.0 54.0 L3.2 51.4 L3.9 49.0 L4.9 46.9 L6.2 45.3 L7.8 44.3 L9.5 44.0 L11.2 44.3 L12.8 45.3 L14.1 46.9 L15.1 49.0 L15.8 51.4Z" +
            " M97.0 54.0 L96.8 56.6 L96.1 59.0 L95.1 61.1 L93.8 62.7 L92.2 63.7 L90.5 64.0 L88.8 63.7 L87.2 62.7 L85.9 61.1 L84.9 59.0 L84.2 56.6 L84.0 54.0 L84.2 51.4 L84.9 49.0 L85.9 46.9 L87.2 45.3 L88.8 44.3 L90.5 44.0 L92.2 44.3 L93.8 45.3 L95.1 46.9 L96.1 49.0 L96.8 51.4Z"
    )
    Magic8BallPersonality.VILLAIN -> Worn(
        shape = "M35 36 Q27 23 35 8 Q38 21 47 29Z M65 36 L53 29 Q62 21 65 8 Q73 23 65 36Z",
        size = 0.9f
    )
    Magic8BallPersonality.POET -> Worn(
        shape = "M27 31 Q24 15 46 14 Q73 12 73 25 Q73 34 50 33.5 Q34 35 27 31Z M48.6 8 h2.8 v8 h-2.8Z",
        size = 0.82f
    )
    Magic8BallPersonality.ASTRO -> Worn(
        shape = "M50 2 L52.5 11.5 L62 14 L52.5 16.5 L50 26 L47.5 16.5 L38 14 L47.5 11.5Z"
    )
    Magic8BallPersonality.THERAPIST -> Worn(
        shape = "M32.6 22.5 a8.4 8.4 0 1 0 16.8 0 a8.4 8.4 0 1 0 -16.8 0Z" +
            " M50.6 22.5 a8.4 8.4 0 1 0 16.8 0 a8.4 8.4 0 1 0 -16.8 0Z" +
            " M47.5 21 L47.5 23.8 Q50 21.8 52.5 23.8 L52.5 21 Q50 19 47.5 21Z",
        cut = "M35.2 22.5 a5.8 5.8 0 1 0 11.6 0 a5.8 5.8 0 1 0 -11.6 0Z" +
            " M53.2 22.5 a5.8 5.8 0 1 0 11.6 0 a5.8 5.8 0 1 0 -11.6 0Z",
        tilt = 9f
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
/** How far under the crown the point is that a crooked thing turns about. */
private const val WORN_SEAT = 2f

private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-8-ball.png"
