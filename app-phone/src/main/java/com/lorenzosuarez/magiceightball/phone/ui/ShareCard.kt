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
 * the ball said is the headline, as large as it fits, and the style signs it underneath like
 * the author of a quote. The ball is a flat mark cut by the corner.
 *
 * Always the same size and colours, whatever the phone and its settings, so it looks the same
 * wherever it ends up. It is laid out but drawn only into [layer], never on the screen:
 * [GraphicsLayer.toImageBitmap] then gives the picture.
 *
 * @param paper the colour of the card: see [shareColor].
 * @param styleLabel the name of the style that answered: the character is half the joke.
 * @param fromAi whether the AI service wrote this answer: only then does the picture say so.
 * @param remark what the character added under its answer.
 * @param visible true to draw it on the screen as well, for previews and screenshot tests.
 */
@Composable
internal fun ShareCard(
    question: String,
    answer: String,
    layer: GraphicsLayer,
    modifier: Modifier = Modifier,
    fromAi: Boolean = false,
    visible: Boolean = false,
    remark: String? = null,
    styleLabel: String? = null,
    paper: Color = shareColor(Magic8BallPersonality.NEUTRAL)
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
                    .background(paper)
            ) {
                FlatBall(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = SHARE_BALL_SIZE * SHARE_BALL_OUT, y = SHARE_BALL_SIZE * SHARE_BALL_OUT)
                        .requiredSize(SHARE_BALL_SIZE)
                )
                Column(modifier = Modifier.fillMaxSize().padding(SHARE_MARGIN)) {
                    if (question.isNotBlank()) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SHARE_INK,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    // As large as it fits: a short answer shouts, a long one still leaves room.
                    val size = when {
                        answer.length <= SHARE_SHORT_ANSWER -> 58
                        answer.length <= SHARE_MEDIUM_ANSWER -> 46
                        else -> 38
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
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (remark != null) {
                        Text(
                            text = remark,
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = SHARE_INK,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Signed like a quote, in another hand: the one change of voice on the card.
                    if (styleLabel != null) {
                        Text(
                            text = "\u2014 $styleLabel",
                            modifier = Modifier.padding(top = 14.dp),
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
                    Text(
                        text = stringResource(R.string.brand_name),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SHARE_INK
                    )
                    Text(
                        text = stringResource(if (fromAi) R.string.answer_from_ai else R.string.share_card_invite),
                        style = MaterialTheme.typography.labelMedium,
                        color = SHARE_INK
                    )
                }
            }
        }
    }
}

/** The ball with nothing but its two colours, tipped as if it had just been put down. */
@Composable
private fun FlatBall(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(SHARE_INK, CircleShape), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .offset(x = -SHARE_BALL_SIZE * SHARE_DISC_SHIFT, y = -SHARE_BALL_SIZE * SHARE_DISC_SHIFT)
                .requiredSize(SHARE_BALL_SIZE * SHARE_DISC)
                .rotate(SHARE_DISC_TILT)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "8",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp, lineHeight = 54.sp),
                fontWeight = FontWeight.Black,
                color = SHARE_INK
            )
        }
    }
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
private const val SHARE_ANSWER_LEADING = 0.98f
private const val SHARE_ANSWER_TRACKING = 0.02f

/** The ball, how much of it lies past the corner, and the white disc with its number. */
private val SHARE_BALL_SIZE = 190.dp
private const val SHARE_BALL_OUT = 0.26f
private const val SHARE_DISC = 0.46f
private const val SHARE_DISC_SHIFT = 0.07f
private const val SHARE_DISC_TILT = -14f

private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-8-ball.png"
