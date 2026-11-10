package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The app's one palette: a near-black blue with cool greys over it and the die's indigo as the
 * accent. The app looks the same in the system's light and dark settings, as the ball does.
 */
private val Colors = darkColorScheme(
    primary = Color(0xFF7C96FF),
    onPrimary = Color(0xFF06103A),
    primaryContainer = Color(0xFF24357F),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFFB7C2CE),
    onSecondary = Color(0xFF1B2733),
    secondaryContainer = Color(0xFF1F2B37),
    onSecondaryContainer = Color(0xFFE3E9EF),
    tertiary = Color(0xFFB7C2CE),
    onTertiary = Color(0xFF1B2733),
    background = Color(0xFF030B14),
    onBackground = Color(0xFFF2F5F8),
    surface = Color(0xFF030B14),
    onSurface = Color(0xFFF2F5F8),
    surfaceVariant = Color(0xFF1A2632),
    onSurfaceVariant = Color(0xFF9AA7B4),
    surfaceContainerLowest = Color(0xFF01060C),
    surfaceContainerLow = Color(0xFF0C1620),
    surfaceContainer = Color(0xFF111C27),
    surfaceContainerHigh = Color(0xFF1A2632),
    surfaceContainerHighest = Color(0xFF24313E),
    outline = Color(0xFF5C6875),
    outlineVariant = Color(0xFF24313E)
)

/** What every screen sits on: not a flat colour, but a little lighter towards the top. */
val Backdrop = Brush.verticalGradient(
    0f to Color(0xFF0E1B27),
    0.3f to Color(0xFF040C16),
    1f to Color(0xFF01060C)
)

@Composable
fun MagicEightBallPhoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
