package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.common.BrandPalette

/**
 * The one thing to do on a sheet or in an empty place: a pill filled with the text's colour, as
 * everything chosen in the app is. There is one to a place, and it is the same everywhere.
 *
 * @param busy while what it started is on its way: a ring turns in it, and it takes no second touch.
 * @param sold whether it is what buys something: then, and only then, it is in the app's colour,
 * which the app keeps for what is sold.
 * @param short what it says where [text] would not fit on one line: a button is one line.
 */
@Composable
internal fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
    sold: Boolean = false,
    short: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = BUTTON_HEIGHT),
        enabled = enabled && !busy,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            // The deep accent in both themes: white on the dark theme's lighter one is too faint to read.
            containerColor = if (sold) Color(BrandPalette.ACCENT_DEEP) else scheme.onSurface,
            contentColor = if (sold) Color(BrandPalette.ON_ACCENT) else scheme.surface,
            disabledContainerColor = scheme.onSurface.copy(alpha = TILE_ALPHA),
            // Plainly not to be pressed: at full strength it read as a second, quieter button.
            disabledContentColor = scheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        BusyRing(busy)
        if (short == null) {
            Text(text = text, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        } else {
            // Measured against the room it has, before it is written: never two lines and then one.
            val measurer = rememberTextMeasurer()
            val style = LocalTextStyle.current.copy(fontWeight = FontWeight.SemiBold)
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val room = constraints.maxWidth
                val fits = remember(text, style, room) { measurer.measure(text, style, softWrap = false).size.width <= room }
                Text(text = if (fits) text else short, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
    }
}

/**
 * The other way out, under the one thing to do: its words alone, in the text's colour, and in
 * the colour of a warning when it is what cannot be taken back.
 */
@Composable
internal fun QuietButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = BUTTON_HEIGHT),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.textButtonColors(contentColor = if (danger) scheme.error else scheme.onSurface)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

/** The least a button is tall: the room of a touch. */
private val BUTTON_HEIGHT = 48.dp
