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
                // A few sentences need the room: the ball steps back into the corner for them.
                val reading = remark != null && remark.length > SHARE_LONG_REMARK
                FlatBall(
                    personality = personality,
                    modifier = Modifier
                        .offset(x = SHARE_BALL_LEFT, y = SHARE_BALL_TOP)
                        .requiredSize(SHARE_BALL_SIZE)
                        .graphicsLayer {
                            if (reading) {
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
                            modifier = Modifier.padding(top = 12.dp, end = if (reading) SHARE_READING_INSET else SHARE_REMARK_INSET),
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
    val worn = remember(personality) { silhouette(personality)?.let { PathParser().parsePathString(it).toPath() } }
    val cut = remember(personality) { detail(personality)?.let { PathParser().parsePathString(it).toPath() } }
    Box(modifier = modifier.rotate(SHARE_BALL_TILT)) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            if (worn != null) {
                // Drawn on a ball of radius WORN_RADIUS whose centre is at WORN_CENTRE.
                val unit = radius / WORN_RADIUS
                withTransform({
                    translate(center.x - WORN_CENTRE_X * unit, center.y - WORN_CENTRE_Y * unit)
                    scale(unit, unit, pivot = Offset.Zero)
                    // Small, on the crown of the ball.
                    scale(WORN_SIZE, WORN_SIZE, pivot = Offset(WORN_CENTRE_X, WORN_CENTRE_Y - WORN_RADIUS))
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
 * What the ball wears for a style: one thing on its head, in the ball's own ink, as an SVG path
 * on a ball of radius [WORN_RADIUS] centred at ([WORN_CENTRE_X], [WORN_CENTRE_Y]). The classic
 * style wears nothing.
 *
 * They are drawn to one set of rules, so that the nineteen read as one family: each sits on
 * the crown and touches it (only a halo floats), each is built of circles, bars and plain
 * curves, each is about as wide and as heavy as the next, and each stands on the ball's axis.
 * Overlapping parts of one shape run the same way round, or they would cut holes in each other.
 */
private fun silhouette(personality: Magic8BallPersonality): String? = when (personality) {
    Magic8BallPersonality.NEUTRAL -> null
    Magic8BallPersonality.SARCASTIC ->
        "M67.0 15.0 L66.4 13.3 L64.7 11.8 L62.0 10.4 L58.5 9.4 L54.4 8.7 L50.0 8.5 L45.6 8.7 L41.5 9.4 L38.0 10.4 L35.3 11.8 L33.6 13.3 L33.0 15.0 L33.6 16.7 L35.3 18.2 L38.0 19.6 L41.5 20.6 L45.6 21.3 L50.0 21.5 L54.4 21.3 L58.5 20.6 L62.0 19.6 L64.7 18.3 L66.4 16.7Z" +
        " M61.0 15.0 L60.6 15.7 L59.5 16.3 L57.8 16.8 L55.5 17.3 L52.8 17.5 L50.0 17.6 L47.2 17.5 L44.5 17.3 L42.2 16.8 L40.5 16.3 L39.4 15.7 L39.0 15.0 L39.4 14.3 L40.5 13.7 L42.2 13.2 L44.5 12.7 L47.2 12.5 L50.0 12.4 L52.8 12.5 L55.5 12.7 L57.8 13.2 L59.5 13.7 L60.6 14.3Z"
    Magic8BallPersonality.MYSTICAL ->
        "M46.0 2.0 L49.6 13.4 L61.0 17.0 L49.6 20.6 L46.0 32.0 L42.4 20.6 L31.0 17.0 L42.4 13.4Z" +
        " M67.0 1.0 L68.7 6.3 L74.0 8.0 L68.7 9.7 L67.0 15.0 L65.3 9.7 L60.0 8.0 L65.3 6.3Z"
    Magic8BallPersonality.GRANDMA ->
        "M35 36 Q33 15 50 15 Q67 15 65 36 Q50 31 35 36Z" +
        " M43.5 10 a6.5 6.5 0 1 1 13 0 a6.5 6.5 0 1 1 -13 0Z"
    Magic8BallPersonality.PIRATE ->
        "M19 25 Q24 30 34 27 Q40 6 50 6 Q60 6 66 27 Q76 30 81 25 Q80 37 50 33 Q20 37 19 25Z"
    Magic8BallPersonality.PHILOSOPHER ->
        "M34.5 14 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
        " M44.5 10 a8.5 8.5 0 1 0 17 0 a8.5 8.5 0 1 0 -17 0Z" +
        " M52.5 17 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
        " M40.5 19 a7.5 7.5 0 1 0 15 0 a7.5 7.5 0 1 0 -15 0Z" +
        " M42.6 31 a3.4 3.4 0 1 0 6.8 0 a3.4 3.4 0 1 0 -6.8 0Z"
    Magic8BallPersonality.COACH ->
        "M37 35 Q36 15 52 15 Q68 15 67 35 Q52 31 37 35Z M14 30 Q26 25 40 27 v6.5 Q27 32 16 35.5Z"
    Magic8BallPersonality.LOVESTRUCK ->
        "M35 15 a7.6 7.6 0 1 0 15.2 0 a7.6 7.6 0 1 0 -15.2 0Z" +
        " M49.8 15 a7.6 7.6 0 1 0 15.2 0 a7.6 7.6 0 1 0 -15.2 0Z M36.2 19 L50 33 L63.8 19 L50 11Z"
    Magic8BallPersonality.DRAMA ->
        "M36 34 L34 15 L43 23 L50 10 L57 23 L66 15 L64 34 Q50 30 36 34Z" +
        " M30.8 13.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z" +
        " M46.8 8.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z" +
        " M62.8 13.5 a3.2 3.2 0 1 1 6.4 0 a3.2 3.2 0 1 1 -6.4 0Z"
    Magic8BallPersonality.ROBOT ->
        "M48.5 12 v16 h3 v-16Z M44.5 10 a5.5 5.5 0 1 0 11 0 a5.5 5.5 0 1 0 -11 0Z" +
        " M41 35 Q50 32 59 35 Q59 25 50 25 Q41 25 41 35Z"
    Magic8BallPersonality.DETECTIVE ->
        "M22 35 Q50 26 78 35 v4.5 Q50 30.5 22 39.5Z M35 31 Q33 9 44 9 Q50 12.5 56 9 Q67 9 65 31Z"
    Magic8BallPersonality.CONSPIRACY ->
        "M34 37 L50 4 L66 37 Q50 31 34 37Z"
    Magic8BallPersonality.CAT ->
        "M34 36 L35 11 L48 29Z M66 36 L52 29 L65 11Z"
    Magic8BallPersonality.ZOOMER ->
        "M7.6 47.9 A49 49 0 0 1 92.4 47.9 L88.5 50.2 A44.5 44.5 0 0 0 11.5 50.2Z" +
        " M16.0 54.0 L15.8 56.6 L15.1 59.0 L14.1 61.1 L12.8 62.7 L11.2 63.7 L9.5 64.0 L7.8 63.7 L6.3 62.7 L4.9 61.1 L3.9 59.0 L3.2 56.6 L3.0 54.0 L3.2 51.4 L3.9 49.0 L4.9 46.9 L6.2 45.3 L7.8 44.3 L9.5 44.0 L11.2 44.3 L12.8 45.3 L14.1 46.9 L15.1 49.0 L15.8 51.4Z" +
        " M97.0 54.0 L96.8 56.6 L96.1 59.0 L95.1 61.1 L93.8 62.7 L92.2 63.7 L90.5 64.0 L88.8 63.7 L87.2 62.7 L85.9 61.1 L84.9 59.0 L84.2 56.6 L84.0 54.0 L84.2 51.4 L84.9 49.0 L85.9 46.9 L87.2 45.3 L88.8 44.3 L90.5 44.0 L92.2 44.3 L93.8 45.3 L95.1 46.9 L96.1 49.0 L96.8 51.4Z"
    Magic8BallPersonality.VILLAIN ->
        "M35 36 Q27 23 35 8 Q38 21 47 29Z M65 36 L53 29 Q62 21 65 8 Q73 23 65 36Z"
    Magic8BallPersonality.POET ->
        "M27 31 Q24 15 46 14 Q73 12 73 25 Q73 34 50 33.5 Q34 35 27 31Z M48.6 8 h2.8 v8 h-2.8Z"
    Magic8BallPersonality.ASTRO ->
        "M39.5 19 a10.5 10.5 0 1 0 21 0 a10.5 10.5 0 1 0 -21 0Z" +
        " M70.4 14.4 L69.3 13.2 L67.0 12.4 L63.5 12.1 L59.0 12.3 L54.0 12.9 L48.6 14.1 L43.4 15.6 L38.6 17.3 L34.6 19.3 L31.7 21.2 L30.0 23.0 L29.6 24.6 L30.7 25.8 L33.0 26.6 L36.5 26.9 L41.0 26.7 L46.0 26.1 L51.4 24.9 L56.6 23.4 L61.4 21.7 L65.4 19.7 L68.3 17.8 L70.0 16.0Z" +
        " M65.5 15.6 L65.1 16.3 L63.7 17.3 L61.4 18.3 L58.2 19.5 L54.6 20.7 L50.6 21.7 L46.5 22.7 L42.7 23.4 L39.4 23.8 L36.8 24.0 L35.1 23.8 L34.5 23.4 L34.9 22.7 L36.3 21.7 L38.6 20.7 L41.8 19.5 L45.4 18.3 L49.4 17.3 L53.5 16.3 L57.3 15.6 L60.6 15.2 L63.2 15.0 L64.9 15.2Z"
    Magic8BallPersonality.THERAPIST ->
        "M30 22 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0Z M52 22 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0Z" +
        " M46 19.5 v3.2 h8 v-3.2Z"
    Magic8BallPersonality.DOG ->
        "M34.5 20.7 L36.4 28.5 L65.5 21.3 L63.6 13.5Z" +
        " M28.9811 20.3595 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
        " M31.11 28.8981 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
        " M58.09 13.1019 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z" +
        " M60.2189 21.6405 a5.4 5.4 0 1 0 10.8 0 a5.4 5.4 0 1 0 -10.8 0Z"
}

/**
 * What is cut out of [silhouette] in white, drawn in the same place and at the same size: at
 * most one cut each, as thick as the next, and only where the shape does not say what it is
 * without it. A hat's band, a pair of lenses, the eye in a pyramid.
 */
private fun detail(personality: Magic8BallPersonality): String? = when (personality) {
    Magic8BallPersonality.GRANDMA ->
        "M35.6 27.5 Q50 25.1 64.4 27.5 v3.4 Q50 28.5 35.6 30.9Z"
    Magic8BallPersonality.PIRATE ->
        "M46.4 13.4 L44.4 15.4 L53.6 24.6 L55.6 22.6Z M44.4 22.6 L46.4 24.6 L55.6 15.4 L53.6 13.4Z"
    Magic8BallPersonality.ROBOT ->
        "M47.8 10 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0Z"
    Magic8BallPersonality.DETECTIVE ->
        "M35.2 25 Q50 23 64.8 25 v3.2 Q50 26.2 35.2 28.2Z"
    Magic8BallPersonality.CONSPIRACY ->
        "M42 25 Q50 16.5 58 25 Q50 33.5 42 25Z M47 25 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0Z"
    Magic8BallPersonality.THERAPIST ->
        "M33.2 22 a5.8 5.8 0 1 0 11.6 0 a5.8 5.8 0 1 0 -11.6 0Z" +
        " M55.2 22 a5.8 5.8 0 1 0 11.6 0 a5.8 5.8 0 1 0 -11.6 0Z"
    else -> null
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
private const val SHARE_MARK_ALPHA = 0.55f

private val SHARE_REMARK_INSET = 76.dp
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

private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-8-ball.png"
