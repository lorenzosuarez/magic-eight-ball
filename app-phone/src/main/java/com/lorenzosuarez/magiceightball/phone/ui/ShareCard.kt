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
                FlatBall(
                    personality = personality,
                    modifier = Modifier
                        .offset(x = SHARE_BALL_LEFT, y = SHARE_BALL_TOP)
                        .requiredSize(SHARE_BALL_SIZE)
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
                    }
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
                            modifier = Modifier.padding(top = 12.dp, end = SHARE_REMARK_INSET),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SHARE_INK,
                            maxLines = 3,
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
    Box(modifier = modifier.rotate(SHARE_BALL_TILT)) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            if (worn != null) {
                // Drawn on a ball of radius WORN_RADIUS whose centre is at WORN_CENTRE.
                val unit = radius / WORN_RADIUS
                withTransform({
                    translate(center.x - WORN_CENTRE_X * unit, center.y - WORN_CENTRE_Y * unit)
                    scale(unit, unit, pivot = Offset.Zero)
                    // Small on the crown of the ball, unless it hangs down its sides.
                    if (personality != Magic8BallPersonality.DOG) {
                        scale(WORN_SIZE, WORN_SIZE, pivot = Offset(WORN_CENTRE_X, WORN_CENTRE_Y - WORN_RADIUS))
                    }
                }) {
                    drawPath(worn, SHARE_INK)
                    drawPath(worn, SHARE_INK, style = Stroke(width = WORN_SOFTEN, join = StrokeJoin.Round))
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
 * What the ball wears for a style: one plain shape in the ball's own ink, as an SVG path on a
 * ball of radius [WORN_RADIUS] centred at ([WORN_CENTRE_X], [WORN_CENTRE_Y]). The classic
 * style wears nothing.
 */
private fun silhouette(personality: Magic8BallPersonality): String? = when (personality) {
    Magic8BallPersonality.NEUTRAL -> null
    Magic8BallPersonality.SARCASTIC -> "M33 23 L56 17 L57 22 L34 28Z"
    Magic8BallPersonality.MYSTICAL -> "M56 4 A11 11 0 1 0 61 24 A9 9 0 1 1 56 4Z"
    Magic8BallPersonality.GRANDMA -> "M41 25 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0Z"
    Magic8BallPersonality.PIRATE -> "M28 35 Q50 6 72 35 Q50 26 28 35Z"
    Magic8BallPersonality.PHILOSOPHER ->
        "M46 22 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0Z M55 13 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0Z M62 6 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0Z"
    Magic8BallPersonality.COACH -> "M31 34 Q31 13 50 13 Q69 13 69 34Z M14 29 h24 v5 h-24Z"
    Magic8BallPersonality.LOVESTRUCK -> "M50 24 C39 16 42 6 50 12 C58 6 61 16 50 24Z"
    Magic8BallPersonality.DRAMA -> "M35 36 L32 14 L42 24 L50 10 L58 24 L68 14 L65 36Z"
    Magic8BallPersonality.ROBOT -> "M48.5 12 h3 v20 h-3Z M45.5 11 a4.5 4.5 0 1 0 9 0 a4.5 4.5 0 1 0 -9 0Z"
    Magic8BallPersonality.DETECTIVE -> "M23 27 h54 v5 h-54Z M35 29 Q35 10 50 10 Q65 10 65 29Z"
    Magic8BallPersonality.CONSPIRACY -> "M34 36 L50 4 L66 36Z"
    Magic8BallPersonality.CAT -> "M31 40 L30 20 L45 31Z M55 31 L70 20 L69 40Z"
    Magic8BallPersonality.ZOOMER -> "M55 0 L41 17 L50 17 L45 30 L61 12 L52 12Z"
    Magic8BallPersonality.VILLAIN -> "M33 36 Q24 24 31 12 Q33 24 42 30Z M67 36 Q76 24 69 12 Q67 24 58 30Z"
    Magic8BallPersonality.POET -> "M50 31 Q38 15 55 2 Q65 17 50 31Z"
    Magic8BallPersonality.ASTRO -> "M50 2 L52.5 11.5 L62 14 L52.5 16.5 L50 26 L47.5 16.5 L38 14 L47.5 11.5Z"
    Magic8BallPersonality.THERAPIST ->
        "M48.8 16 h2.5 v16 h-2.5Z M50 22 Q40 20 38 10 Q49 11 50 22Z M51 20 Q61 18 63 8 Q52 9 51 20Z"
    Magic8BallPersonality.DOG -> "M27 40 Q11 42 13 66 Q24 68 29 52Z M73 40 Q89 42 87 66 Q76 68 71 52Z"
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
