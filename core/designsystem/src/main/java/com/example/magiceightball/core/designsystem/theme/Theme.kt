package com.example.magiceightball.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.example.magiceightball.core.common.BrandPalette

/**
 * The watch's colours, from the [BrandPalette] it shares with the phone: what the system's own
 * pieces on screen, such as the time, are drawn with.
 */
private val Colors = ColorScheme(
    primary = Color(BrandPalette.ON_SURFACE),
    background = Color(BrandPalette.SURFACE),
    onBackground = Color(BrandPalette.ON_SURFACE),
    onSurface = Color(BrandPalette.ON_SURFACE),
    onSurfaceVariant = Color(BrandPalette.ON_SURFACE_QUIET)
)

@Composable
fun MagicEightBallTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Colors,
        content = content
    )
}
