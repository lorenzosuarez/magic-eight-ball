package com.example.magiceightball.feature.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.components.BallColors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Process-scoped flag indicating whether the startup toast was shown, so it appears only once
 * per app process.
 */
private val startupToastShown = AtomicBoolean(false)

/**
 * Idle screen: the ball's "8" exactly in the centre and a short hint a fixed distance below it.
 *
 * Both are positioned in a single layout pass, so they are in place from the very first frame.
 *
 * @param usesAiAnswers Whether a shake asks the AI service, which the hint then says.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
fun ChatScreenIdle(usesAiAnswers: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current

    LaunchedEffect(Unit) {
        if (startupToastShown.compareAndSet(false, true)) {
            Toast.makeText(context, Strings.ShakeToStart.resId(language), Toast.LENGTH_SHORT).show()
        }
    }

    Layout(
        content = {
            EightBadge(name = localizedString(Strings.Title))
            Text(
                text = localizedString(if (usesAiAnswers) Strings.ShakeHintAi else Strings.ShakeHint),
                color = BallColors.OnSurface.copy(alpha = HELPER_ALPHA),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyExtraSmall
            )
        },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) { (badgeMeasurable, helperMeasurable), constraints ->
        val badgeSize = (min(constraints.maxWidth, constraints.maxHeight) * BADGE_SCREEN_FRACTION).roundToInt()
        val badge = badgeMeasurable.measure(Constraints.fixed(badgeSize, badgeSize))
        val helper = helperMeasurable.measure(constraints.copy(minWidth = 0, minHeight = 0))

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
 * The white disc with the "8", as on the phone's ball. The digit is laid out once per size and
 * scales with the disc; [name] is what it is called for accessibility.
 */
@Composable
private fun EightBadge(name: String, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
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
                onDrawBehind {
                    drawCircle(Color.White)
                    drawText(eight, topLeft = topLeft)
                }
            }
    )
}

private const val EIGHT = "8"

/** Share of the screen taken by the disc, and of the disc taken by its digit. */
private const val BADGE_SCREEN_FRACTION = 0.44f
private const val EIGHT_SIZE_FRACTION = 0.72f
private const val HELPER_ALPHA = 0.75f

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
