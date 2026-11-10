package com.example.magiceightball.feature.chat

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Process-scoped flag indicating whether the startup toast was shown, so it appears only once
 * per app process.
 */
private val startupToastShown = AtomicBoolean(false)

/**
 * Idle screen displayed when the chat is not active: the title exactly in the centre and a
 * helper line a fixed distance below it.
 *
 * Both are positioned in a single layout pass, so they are in place from the very first frame.
 *
 * @param titleRes Resource id for the title text.
 * @param modifier Modifier applied to the root layout.
 */
@Composable
fun ChatScreenIdle(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (startupToastShown.compareAndSet(false, true)) {
            Toast.makeText(context, R.string.shake_to_start, Toast.LENGTH_SHORT).show()
        }
    }

    val displayMedium = MaterialTheme.typography.displayMedium
    val titleStyle = remember(displayMedium) {
        displayMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }

    Layout(
        content = {
            Text(
                text = stringResource(titleRes),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = titleStyle
            )
            Text(
                text = stringResource(R.string.uses_gemini_api),
                color = HelperColor,
                style = MaterialTheme.typography.bodyExtraSmall
            )
        },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) { (titleMeasurable, helperMeasurable), constraints ->
        val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val title = titleMeasurable.measure(contentConstraints)
        val helper = helperMeasurable.measure(contentConstraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            val titleY = (constraints.maxHeight - title.height) / 2
            title.place(
                x = (constraints.maxWidth - title.width) / 2,
                y = titleY
            )
            helper.place(
                x = (constraints.maxWidth - helper.width) / 2,
                y = titleY + title.height + HelperSpacing.roundToPx()
            )
        }
    }
}

private val HelperSpacing = 16.dp
private val HelperColor = Color.LightGray.copy(alpha = 0.5f)

@Preview(name = "ChatScreenIdle - Wear", showBackground = true, widthDp = 192, heightDp = 192)
@Composable
private fun ChatScreenIdlePreview() {
    MagicEightBallTheme {
        ChatScreenIdle(
            titleRes = R.string.title_main,
            modifier = Modifier
                .size(192.dp)
                .background(Color.Black, shape = CircleShape)
        )
    }
}
