package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.magiceightball.core.common.BrandPalette

/**
 * The dark palette, built on the [BrandPalette] the app shares with the watch: near-black with
 * cool greys over it and one accent.
 */
private val DarkColors = darkColorScheme(
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

/** The light palette: the same cool greys and accent, turned over onto an off-white. */
private val LightColors = lightColorScheme(
    primary = Color(BrandPalette.ACCENT_DEEP),
    onPrimary = Color(BrandPalette.ON_ACCENT),
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0B1A5C),
    secondary = Color(0xFF52606E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE6F2),
    onSecondaryContainer = Color(0xFF15202B),
    tertiary = Color(0xFF52606E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF9F9),
    onBackground = Color(0xFF141B23),
    surface = Color(0xFFFAF9F9),
    onSurface = Color(0xFF141B23),
    surfaceVariant = Color(0xFFE3E9F0),
    onSurfaceVariant = Color(0xFF55616E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F6F9),
    surfaceContainer = Color(0xFFEEF2F6),
    surfaceContainerHigh = Color(0xFFE7ECF2),
    surfaceContainerHighest = Color(0xFFDFE6ED),
    outline = Color(0xFF7A8693),
    outlineVariant = Color(0xFFC9D2DC)
)

/** How far down the screen stays plain before the colour starts to rise from the bottom. */
private const val BACKDROP_PLAIN_STOP = 0.45f

/** Where the rising colour is about halfway to its full strength. */
private const val BACKDROP_RISING_STOP = 0.76f

private val DarkBackdrop = Brush.verticalGradient(
    0f to Color(0xFF01040A),
    BACKDROP_PLAIN_STOP to Color(0xFF01040A),
    BACKDROP_RISING_STOP to Color(0xFF09122E),
    1f to Color(0xFF14276A)
)

private val LightBackdrop = Brush.verticalGradient(
    0f to Color(0xFFFAF9F9),
    BACKDROP_PLAIN_STOP to Color(0xFFFAF9F9),
    BACKDROP_RISING_STOP to Color(0xFFE0EFFC),
    1f to Color(0xFFBCDFFD)
)

private val LocalBackdrop = staticCompositionLocalOf { DarkBackdrop }

/**
 * What every screen sits on: plain for its upper half, with a glow of blue rising from the
 * bottom edge. Its plain part is the window's own colour, so the launch screen runs into it.
 */
val Backdrop: Brush
    @Composable
    @ReadOnlyComposable
    get() = LocalBackdrop.current

@Composable
fun MagicEightBallPhoneTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        CompositionLocalProvider(
            // Rows and everything else that is simply clickable answer a touch the same way.
            LocalIndication provides PressHighlight,
            // What is written straight on the backdrop, and the light of a touch on it.
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            LocalBackdrop provides if (dark) DarkBackdrop else LightBackdrop,
            content = content
        )
    }
}
