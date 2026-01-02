package com.example.magiceightball.feature.chat

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
 * Holds a process-scoped flag indicating whether the startup toast was shown.
 * This prevents showing the Toast multiple times during the same app process.
 */
private object StartupToastShown {
    val shown = AtomicBoolean(false)
}

/**
 * Idle screen displayed when the chat is not active.
 *
 * @param titleRes Resource id for the title text.
 * @param modifier Modifier applied to the root Box.
 */
@Composable
fun ChatScreenIdle(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (StartupToastShown.shown.compareAndSet(false, true)) {
            Toast.makeText(context, context.getString(R.string.shake_to_start), Toast.LENGTH_LONG)
                .show()
        }
    }

    val titleHeightPx = remember { mutableIntStateOf(0) }
    val helperHeightPx = remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val spacingDp = 16.dp
    val defaultOffset = 72.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = stringResource(titleRes),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    titleHeightPx.intValue = coordinates.size.height
                }
            )
        }

        val offsetDp = if (titleHeightPx.intValue > 0 && helperHeightPx.intValue > 0) {
            with(density) {
                ((titleHeightPx.intValue / 2f) + (helperHeightPx.intValue / 2f)).toDp()
            } + spacingDp
        } else {
            defaultOffset
        }

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .onGloballyPositioned { coordinates -> helperHeightPx.intValue = coordinates.size.height }
                .offset(y = offsetDp),
            text = stringResource(R.string.uses_gemini_api),
            color = Color.LightGray.copy(
                alpha = 0.5f
            ),
            style = MaterialTheme.typography.bodyExtraSmall,
        )
    }
}

@Preview(name = "ChatScreenIdle - Wear", showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun ChatScreenIdlePreview() {
    MagicEightBallTheme {
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(Color.Black, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                ChatScreenIdle(titleRes = R.string.title_main)
            }
        }
    }
}
