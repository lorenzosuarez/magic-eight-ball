package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.magiceightball.core.domain.model.look
import com.example.magiceightball.core.domain.model.CharacterLook
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
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
 * [SharePicture.take] then gives the picture.
 *
 * @param personality the style that answered: it gives the colour and what the ball wears.
 * @param styleLabel the name of that style, which signs the answer.
 * @param remark what the character added under its answer.
 * @param visible true to draw it on the screen as well, for previews and screenshot tests.
 * @param asked read while drawing, so that whatever changes it has the card drawn afresh: see
 * [SharePicture].
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
    look: CustomCharacter? = null,
    asked: () -> Int = { 0 }
) {
    // A character of the user's own has the colour and wears the accessory it borrowed.
    val colour = look?.colour ?: personality
    val emblem = look?.emblem ?: personality.look
    CompositionLocalProvider(LocalDensity provides Density(SHARE_DENSITY, fontScale = 1f)) {
        MagicEightBallPhoneTheme(dark = false) {
            Box(
                modifier = modifier
                    .requiredSize(SHARE_WIDTH, SHARE_HEIGHT)
                    .clearAndSetSemantics {}
                    .drawWithContent {
                        // Read here and for nothing else: a picture being asked for is what has
                        // this drawn, and so recorded, again.
                        asked()
                        layer.record { this@drawWithContent.drawContent() }
                        if (visible) drawLayer(layer)
                    }
                    .clipToBounds()
                    .background(shareColor(colour))
            ) {
                // Anything written under the answer needs the room: the ball steps back into the corner for it.
                val reading = remark != null && remark.length > SHARE_LONG_REMARK
                // And for a name written across what the ball wears it draws in towards the corner,
                // by as much as leaves the name clear. Where the name ends is known once the words
                // are placed, and the ball is placed after them: it is read in the same frame,
                // with nothing composed again for it.
                var signed by remember { mutableStateOf(Offset.Unspecified) }
                val figure = remember(emblem) { figureFromCorner(emblem) }
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
                        // How wide the name is written, for whoever asks where it ends.
                        var written by remember { mutableIntStateOf(0) }
                        val density = LocalDensity.current
                        BoxWithConstraints(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .onPlaced {
                                    // Measured from the card's own edges, with some air: the words are set in from them.
                                    signed = with(density) {
                                        val margin = SHARE_MARGIN.toPx()
                                        val air = SHARE_SIGNED_CLEAR.toPx()
                                        Offset(
                                            (margin + written + air).toDp().value,
                                            (margin + it.positionInParent().y + it.size.height + air).toDp().value
                                        )
                                    }
                                }
                        ) {
                            // A name the user gave may be wider than any of the app's: it is
                            // written smaller, a step at a time, before it is cut short.
                            val room = constraints.maxWidth
                            val size = remember(signature, room) {
                                val widths = SIGNATURE_SIZES.map {
                                    it to measurer.measure(signature, hand.copy(fontSize = it), softWrap = false).size.width
                                }
                                val fits = widths.firstOrNull { it.second <= room } ?: widths.last()
                                written = minOf(fits.second, room)
                                fits.first
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
                FlatBall(
                    look = emblem,
                    rim = shareColor(colour),
                    modifier = Modifier
                        // Under the words, though it is placed after them: by then it is known where the name ends.
                        .zIndex(-1f)
                        .offset(x = SHARE_BALL_LEFT, y = SHARE_BALL_TOP)
                        .requiredSize(SHARE_BALL_SIZE)
                        .graphicsLayer {
                            if (remark != null) {
                                // A little down in size and further into the corner, clear of the words.
                                scaleX = SHARE_FIGURE * SHARE_BALL_BACK
                                scaleY = SHARE_FIGURE * SHARE_BALL_BACK
                                translationX = SHARE_FIGURE_IN.toPx() + SHARE_BALL_BACK_SHIFT.toPx()
                                // Less far down than across: the white disc stays whole on the card.
                                translationY = SHARE_FIGURE_IN.toPx() + SHARE_BALL_BACK_DROP.toPx()
                            } else {
                                // About the card's corner: what is in sight of the ball stays the
                                // same part of it at any size, its white disc whole.
                                val drawn = if (styleLabel == null || signed.isUnspecified) 1f else drawnInFor(figure, signed)
                                val middle = SHARE_BALL_SIZE / 2
                                scaleX = SHARE_FIGURE * drawn
                                scaleY = SHARE_FIGURE * drawn
                                translationX = (SHARE_WIDTH - SHARE_BALL_LEFT - middle).toPx() * (1 - drawn) + SHARE_FIGURE_IN.toPx() * drawn
                                translationY = (SHARE_HEIGHT - SHARE_BALL_TOP - middle).toPx() * (1 - drawn) + SHARE_FIGURE_IN.toPx() * drawn
                            }
                        }
                )
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

/** The figure, with nothing but its two colours, drawn about the middle of the room it is given. */
@Composable
private fun FlatBall(look: CharacterLook, rim: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawFigure(look = look, ink = SHARE_INK, centre = center, radius = size.minDimension / 2, rim = rim) }
}

/**
 * Where the figure of [look] is on the card at its full size: each point of its edge as how far
 * left of the card's lower right corner it is, then how far above it, in dp. What is past the
 * card's edges is left out: nothing is written there.
 */
private fun figureFromCorner(look: CharacterLook): FloatArray {
    val outline = figureOutline(look)
    val radius = SHARE_BALL_SIZE.value / 2 * SHARE_FIGURE
    val left = SHARE_WIDTH.value - SHARE_BALL_LEFT.value - SHARE_BALL_SIZE.value / 2 - SHARE_FIGURE_IN.value
    val up = SHARE_HEIGHT.value - SHARE_BALL_TOP.value - SHARE_BALL_SIZE.value / 2 - SHARE_FIGURE_IN.value
    val points = ArrayList<Float>(outline.size)
    for (at in outline.indices step 2) {
        val x = left - outline[at] * radius
        val y = up - outline[at + 1] * radius
        if (x > 0f && y > 0f) {
            points += x
            points += y
        }
    }
    return points.toFloatArray()
}

/**
 * How far the figure is drawn in towards the card's corner so that none of it is under what is
 * written as far as [signed], which is where the name ends and where its foot is, in dp: each
 * point of [figure] has to be past the name's end or below its foot.
 */
private fun drawnInFor(figure: FloatArray, signed: Offset): Float {
    val beside = SHARE_WIDTH.value - signed.x
    val under = SHARE_HEIGHT.value - signed.y
    var drawn = 1f
    for (at in figure.indices step 2) drawn = minOf(drawn, maxOf(beside / figure[at], under / figure[at + 1]))
    return drawn.coerceAtLeast(SHARE_BALL_DRAWN_IN)
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
        Magic8BallPersonality.GAMER -> 0xFF4DD9FF
        // One of the user's own has the colour it borrowed: this is the card with none chosen.
        Magic8BallPersonality.CUSTOM -> 0xFFF2EEE3
    }
)

/**
 * The picture an answer is shared as, taken when it is asked for and not kept from before.
 *
 * The card is drawn into [layer] and never on the screen, and what a layer holds that the
 * screen does not show can be thrown away under it: when memory runs short, and by the taking
 * of a picture itself, which lets go of a layer nothing else is showing. The second picture of
 * the same answer was then of nothing, see-through from edge to edge, which wherever it is sent
 * shows as a black rectangle. So the card is drawn again for every picture, the picture is
 * looked at before it is handed on, and one that came out empty is taken again. If it still
 * does, there is none: nothing is better shared than that.
 */
@Stable
internal class SharePicture(val layer: GraphicsLayer) {

    /** How many times a picture was asked for. The card reads it while drawing: see [ShareCard]. */
    var asked by mutableIntStateOf(0)
        private set

    /** The picture, as pixels of its own that no longer depend on the layer; null when it could not be drawn. */
    suspend fun take(): Bitmap? {
        repeat(SHARE_TRIES) { attempt ->
            asked++
            // Two frames: the one that hears it was asked for, and the one after that has drawn it.
            withFrameNanos {}
            withFrameNanos {}
            val picture = try {
                layer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(SHARE_TAG, "The picture could not be taken: ${e.javaClass.simpleName}")
                null
            }
            if (picture != null && !picture.isBlank()) return picture
            Log.w(SHARE_TAG, "The picture came out empty, try ${attempt + 1} of $SHARE_TRIES")
        }
        return null
    }
}

/** A [SharePicture] with a layer of its own, for as long as the screen that shares is there. */
@Composable
internal fun rememberSharePicture(): SharePicture {
    val layer = rememberGraphicsLayer()
    return remember(layer) { SharePicture(layer) }
}

/**
 * Whether nothing was drawn. The card covers the whole picture in a colour that is never black,
 * so a see-through point anywhere says so, and so does black at every point looked at.
 */
internal fun Bitmap.isBlank(): Boolean {
    if (width < 3 || height < 3) return true
    val points = listOf(1, width / 2, width - 2).flatMap { x -> listOf(1, height / 2, height - 2).map { y -> getPixel(x, y) } }
    return points.any { it ushr 24 != 0xFF } || points.all { it == BLACK }
}

/**
 * Where the next picture is saved in [directory]. Each has a name of its own, by [now], so that
 * an app still reading the last one is not handed another under it, and so that nothing shows
 * a picture it kept for a name it has seen before. All but the latest few are deleted.
 */
internal fun shareFile(directory: File, now: Long): File {
    directory.mkdirs()
    directory.listFiles().orEmpty()
        .filter { it.isFile }
        .sortedByDescending { it.lastModified() }
        .drop(SHARE_KEPT)
        .forEach { it.delete() }
    return directory.resolve("$SHARE_FILE-$now.png")
}

/**
 * Saves [picture] where other apps may read it and hands it, with [text], to whichever app the
 * user picks. False when it could not be saved or there is no app to hand it to.
 */
internal fun Context.shareAnswer(picture: Bitmap, text: String): Boolean {
    val file = shareFile(File(cacheDir, SHARE_DIRECTORY), System.currentTimeMillis())
    try {
        val saved = file.outputStream().use { picture.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (!saved) return false
    } catch (_: java.io.IOException) {
        // No room to save it: there is nothing to share.
        return false
    }
    val uri = FileProvider.getUriForFile(this, "$packageName.share", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_TEXT, text)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    // What lets the chooser show the picture, and read it.
    send.clipData = ClipData.newRawUri(null, uri)
    return try {
        startActivity(Intent.createChooser(send, null))
        true
    } catch (_: ActivityNotFoundException) {
        false
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
private val SHARE_BALL_BACK_DROP = 11.dp
/**
 * How much air a name has between it and the figure, and how far in the ball is drawn at the
 * most for it: a name signed lower than any answer leaves it is rather written over a hat than
 * beside a dot.
 */
private val SHARE_SIGNED_CLEAR = 6.dp
private const val SHARE_BALL_DRAWN_IN = 0.6f
/** With them, the answer gives up some of its size and the text some of its margin. */
private const val SHARE_READING_ANSWER = 38
private val SHARE_READING_INSET = 48.dp
private val SHARE_BUBBLE_CORNER = 20.dp
private val SHARE_BUBBLE_TAIL = 9.dp

/** The ball: where it sits, cut by the corner, and how far it leans towards the words. */
private val SHARE_BALL_SIZE = 210.dp
private val SHARE_BALL_LEFT = 190.dp
private val SHARE_BALL_TOP = 300.dp

/** How large the ball and what it wears are drawn in the room the ball has, and how far into the corner they are moved for it. */
private const val SHARE_FIGURE = 0.92f
private val SHARE_FIGURE_IN = 8.dp


private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-ai-ball"

/** The pictures left where they were saved: the one being shared and the two before it. */
private const val SHARE_KEPT = 2

/** How many times a picture is taken before giving up on it. */
private const val SHARE_TRIES = 3
private const val SHARE_TAG = "ShareCard"
private const val BLACK = 0xFF000000.toInt()
