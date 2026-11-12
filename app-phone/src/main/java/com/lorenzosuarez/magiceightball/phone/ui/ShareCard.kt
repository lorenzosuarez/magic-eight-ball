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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.material3.MaterialTheme
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
 * An answer as a picture to send to someone, composed as a card and not as a picture of the
 * screen: what the ball said is the headline, large enough to read in a chat's small preview,
 * with what was asked over it and who said it under it. The ball itself is the mark in the
 * corner, cut by the edge, so the card is recognised without the ball taking the words' place.
 *
 * Always dark and always the same size, whatever the phone and its settings, so it looks the
 * same wherever it ends up. It is laid out but drawn only into [layer], never on the screen:
 * [GraphicsLayer.toImageBitmap] then gives the picture.
 *
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
    styleLabel: String? = null
) {
    CompositionLocalProvider(LocalDensity provides Density(SHARE_DENSITY, fontScale = 1f)) {
        MagicEightBallPhoneTheme(dark = true) {
            val glow = MaterialTheme.colorScheme.primary
            Box(
                modifier = modifier
                    .requiredSize(SHARE_WIDTH, SHARE_HEIGHT)
                    .clearAndSetSemantics {}
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        if (visible) drawLayer(layer)
                    }
                    .clipToBounds()
                    .background(Brush.verticalGradient(listOf(SHARE_TOP, SHARE_BOTTOM)))
                    .drawBehind {
                        // Light from the corner the ball sits in, as if it gave it off.
                        val corner = Offset(size.width, size.height)
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(glow.copy(alpha = SHARE_GLOW_ALPHA), Color.Transparent),
                                center = corner,
                                radius = size.width * SHARE_GLOW_REACH
                            )
                        )
                    }
            ) {
                // The mark: the ball at rest, most of it past the corner.
                EightBall(
                    mode = BallMode.Idle,
                    answer = "",
                    pressed = false,
                    voiceLevel = { 0f },
                    still = true,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = SHARE_BALL_SIZE * SHARE_BALL_OUT, y = SHARE_BALL_SIZE * SHARE_BALL_DOWN)
                        .requiredSize(SHARE_BALL_SIZE)
                )
                Column(modifier = Modifier.fillMaxSize().padding(SHARE_MARGIN)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notification),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.brand_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    if (question.isNotBlank()) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    // A short answer shouts; a long one still has to leave room for the rest.
                    val large = answer.length <= SHARE_LARGE_ANSWER
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = if (large) SHARE_ANSWER_SIZE else SHARE_LONG_ANSWER_SIZE,
                            lineHeight = if (large) SHARE_ANSWER_LINE else SHARE_LONG_ANSWER_LINE,
                            letterSpacing = (-0.5).sp,
                            lineBreak = LineBreak.Heading
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (remark != null) {
                        Text(
                            text = remark,
                            modifier = Modifier.padding(top = 14.dp, end = SHARE_REMARK_INSET),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // The one touch of the app's colour: it sets off who is speaking.
                    Box(
                        modifier = Modifier
                            .padding(top = 22.dp, bottom = 12.dp)
                            .size(width = 28.dp, height = 3.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    // Who spoke, and on the same line whether the AI wrote it.
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (styleLabel != null) {
                            Text(
                                text = styleLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (fromAi) {
                            Text(
                                text = (if (styleLabel != null) "·  " else "") + stringResource(R.string.answer_from_ai),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.share_card_invite),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
private val SHARE_MARGIN = 32.dp
private val SHARE_ANSWER_SIZE = 40.sp
private val SHARE_ANSWER_LINE = 44.sp
private val SHARE_LONG_ANSWER_SIZE = 34.sp
private val SHARE_LONG_ANSWER_LINE = 38.sp

/** Up to this many letters an answer is set at the larger size. */
private const val SHARE_LARGE_ANSWER = 18

/** Kept clear at the right of the remark: lower down, that is where the ball comes in. */
private val SHARE_REMARK_INSET = 16.dp

/** The ball, and how much of its width and height lies past the corner. */
private val SHARE_BALL_SIZE = 220.dp
private const val SHARE_BALL_OUT = 0.36f
private const val SHARE_BALL_DOWN = 0.46f

private val SHARE_TOP = Color(0xFF05070D)
private val SHARE_BOTTOM = Color(0xFF0A1130)
private const val SHARE_GLOW_ALPHA = 0.34f
private const val SHARE_GLOW_REACH = 0.95f

private const val SHARE_DIRECTORY = "share"
private const val SHARE_FILE = "magic-8-ball.png"
