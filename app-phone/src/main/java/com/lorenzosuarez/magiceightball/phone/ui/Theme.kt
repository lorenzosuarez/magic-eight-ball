package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.magiceightball.core.common.BrandPalette

/**
 * The app's one palette, built on the [BrandPalette] it shares with the watch: a near-black
 * blue with cool greys over it and one accent. The app looks the same in the system's light and dark settings, as the ball does.
 */
private val Colors = darkColorScheme(
    primary = Color(BrandPalette.ACCENT),
    onPrimary = Color(0xFF06103A),
    primaryContainer = Color(0xFF24357F),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFFB7C2CE),
    onSecondary = Color(0xFF1B2733),
    secondaryContainer = Color(0xFF1F2B37),
    onSecondaryContainer = Color(0xFFE3E9EF),
    tertiary = Color(0xFFB7C2CE),
    onTertiary = Color(0xFF1B2733),
    background = Color(BrandPalette.SURFACE),
    onBackground = Color(BrandPalette.ON_SURFACE),
    surface = Color(BrandPalette.SURFACE),
    onSurface = Color(BrandPalette.ON_SURFACE),
    surfaceVariant = Color(BrandPalette.SURFACE_RAISED),
    onSurfaceVariant = Color(BrandPalette.ON_SURFACE_QUIET),
    surfaceContainerLowest = Color(0xFF01060C),
    surfaceContainerLow = Color(0xFF0C1620),
    surfaceContainer = Color(0xFF111C27),
    surfaceContainerHigh = Color(BrandPalette.SURFACE_RAISED),
    surfaceContainerHighest = Color(0xFF24313E),
    outline = Color(0xFF5C6875),
    outlineVariant = Color(0xFF24313E)
)

/** Where, down the screen, the backdrop has already settled into its darker tone. */
private const val BACKDROP_MIDDLE_STOP = 0.3f

/** What every screen sits on: not a flat colour, but a little lighter towards the top. */
val Backdrop = Brush.verticalGradient(
    0f to Color(BrandPalette.BACKDROP_TOP),
    BACKDROP_MIDDLE_STOP to Color(BrandPalette.BACKDROP_MIDDLE),
    1f to Color(BrandPalette.BACKDROP_BOTTOM)
)

@Composable
fun MagicEightBallPhoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
