package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.magiceightball.core.common.BrandPalette

/**
 * The dark palette, built on the [BrandPalette] the app shares with the watch: near-black with
 * cool greys over it and one accent.
 */
private val DarkColors = darkColorScheme(
    // Deeper than the die's own blue, so that white reads on it: buttons, switches, what is chosen.
    primary = Color(0xFF5B74F0),
    onPrimary = Color(0xFFFFFFFF),
    // The one warm colour, kept for what is wrong or used up: a red that still reads on the dark.
    error = Color(0xFFFF8A80),
    // What a snackbar is drawn in: the light surface over the dark app, with the accent that reads on it.
    inverseSurface = Color(0xFFE6E9F2),
    inverseOnSurface = Color(0xFF1B1F2A),
    inversePrimary = Color(BrandPalette.ACCENT_DEEP),
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
    surfaceContainerLow = Color(0xFF0D1218),
    surfaceContainer = Color(0xFF111C27),
    surfaceContainerHigh = Color(0xFF1B222B),
    surfaceContainerHighest = Color(0xFF24313E),
    // Light enough to edge a field on a sheet as well as on the page.
    outline = Color(0xFF6F7B88),
    outlineVariant = Color(0xFF2C3540)
)

/** The light palette: the same cool greys and accent, turned over onto an off-white. */
private val LightColors = lightColorScheme(
    primary = Color(BrandPalette.ACCENT_DEEP),
    onPrimary = Color(BrandPalette.ON_ACCENT),
    error = Color(0xFFB3261E),
    inverseSurface = Color(0xFF1B1F2A),
    inverseOnSurface = Color(0xFFE6E9F2),
    inversePrimary = Color(BrandPalette.ACCENT),
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
    surfaceContainerLow = Color(0xFFF1F3F7),
    surfaceContainer = Color(0xFFEEF2F6),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFDFE6ED),
    outline = Color(0xFF7A8693),
    outlineVariant = Color(0xFFE1E5EB)
)

/** Whether this is the dark palette: the one whose text is light. */
internal val ColorScheme.isDark: Boolean get() = onSurface.luminance() > 0.5f

/** What a page stands on, where it is not the backdrop itself: the options and what was asked before. */
internal val ColorScheme.pageGround: Color get() = surfaceContainerLow

/** What is carded on a page: one step off it. */
internal val ColorScheme.pageCard: Color get() = surfaceContainerHigh

/**
 * What a sheet stands on. A sheet comes up over the page and has to be told from it at a
 * glance, so it is never the page's own ground: a step lighter than the page in the dark, and
 * white over the page's grey in the light.
 */
internal val ColorScheme.sheetGround: Color get() = if (isDark) SheetGroundDark else surfaceContainerLowest

/** What is carded on a sheet: one step off the sheet, as the page's cards are one step off the page. */
internal val ColorScheme.sheetCard: Color get() = if (isDark) surfaceContainerHighest else surfaceContainerLow

/**
 * The ink of a quiet button on a sheet, one that is words alone. On the lifted ground of a dark
 * sheet the page's own accent no longer reads, so there it is the lighter one.
 */
internal val ColorScheme.sheetAction: Color get() = if (isDark) Color(BrandPalette.ACCENT) else primary

/**
 * The line between two rows, or under a header that things have scrolled under: a little of
 * the text's own colour, so that it shows the same on the page, on a card and on a sheet.
 */
internal val ColorScheme.hairline: Color get() = onSurface.copy(alpha = HAIRLINE_ALPHA)

private val SheetGroundDark = Color(0xFF18212C)
private const val HAIRLINE_ALPHA = 0.12f

/**
 * What the ball's own page sits on: the ground of every other page, so that the ball and the
 * pages beside it are one surface and nothing changes under a swipe from one to the next. The
 * launch screen is of the same colour and runs into it.
 */
val Backdrop: Brush
    @Composable
    @ReadOnlyComposable
    get() = SolidColor(MaterialTheme.colorScheme.pageGround)

/** Material's type with nothing added between its letters, which is what sets it apart from the system's own. */
private val AppTypography = Typography().run {
    copy(
        titleLarge = titleLarge.copy(letterSpacing = 0.sp),
        titleMedium = titleMedium.copy(letterSpacing = 0.sp),
        titleSmall = titleSmall.copy(letterSpacing = 0.sp),
        bodyLarge = bodyLarge.copy(letterSpacing = 0.sp),
        bodyMedium = bodyMedium.copy(letterSpacing = 0.sp),
        bodySmall = bodySmall.copy(letterSpacing = 0.sp),
        labelLarge = labelLarge.copy(letterSpacing = 0.sp),
        labelMedium = labelMedium.copy(letterSpacing = 0.sp),
        labelSmall = labelSmall.copy(letterSpacing = 0.sp)
    )
}

@Composable
fun MagicEightBallPhoneTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, typography = AppTypography) {
        CompositionLocalProvider(
            // Rows and everything else that is simply clickable answer a touch the same way.
            LocalIndication provides PressHighlight,
            // What is written straight on the backdrop, and the light of a touch on it.
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            content = content
        )
    }
}
