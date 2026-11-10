package com.example.magiceightball.feature.chat.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import com.example.magiceightball.feature.chat.LocalAppLanguage
import kotlin.math.max
import kotlin.math.min

/**
 * Draws [text] in upper case inside an inverted triangle that fills this composable, at the
 * largest font size at which every line stays clear of the slanted edges.
 *
 * The text is measured with the exact style it is drawn with, once per size of the triangle,
 * and drawn directly: there is no subcomposition and no extra layout pass.
 */
@Composable
internal fun TriangleFittedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val baseStyle = MaterialTheme.typography.bodyMedium
    // Upper case and hyphenation follow the language of the app, not the one of the device.
    val locale = Locale(LocalAppLanguage.current.code)
    val style = remember(baseStyle, color, locale) {
        baseStyle.copy(
            color = color,
            fontWeight = FontWeight.Bold,
            localeList = LocaleList(locale),
            textAlign = TextAlign.Center,
            hyphens = Hyphens.Auto,
            lineBreak = LineBreak.Paragraph
        )
    }
    val displayText = remember(text, locale) { text.toUpperCase(locale) }

    Spacer(
        modifier
            .semantics {
                contentDescription = text
                liveRegion = LiveRegionMode.Polite
            }
            .drawWithCache {
                val fitted = if (size.isEmpty()) {
                    null
                } else {
                    fitTextInTriangle(
                        text = displayText,
                        style = style,
                        textMeasurer = textMeasurer,
                        triangle = InvertedTriangleGeometry(size),
                        density = this
                    )
                }
                onDrawBehind {
                    if (fitted != null) drawText(fitted.layout, topLeft = fitted.topLeft)
                }
            }
    )
}

private class FittedText(val layout: TextLayoutResult, val topLeft: Offset)

/**
 * Binary search over the candidate font sizes for the largest one that fits. Falls back to the
 * smallest size, ellipsized, when even that one does not fit.
 */
private fun fitTextInTriangle(
    text: String,
    style: TextStyle,
    textMeasurer: TextMeasurer,
    triangle: InvertedTriangleGeometry,
    density: Density
): FittedText {
    val height = triangle.size.height
    val edgePadding = with(density) { EDGE_PADDING.toPx() }
    val textWidth = (triangle.maxWidth - 2f * edgePadding).toInt().coerceAtLeast(1)
    val left = (triangle.size.width - textWidth) / 2f
    val minFontPx = height * MIN_FONT_HEIGHT_FRACTION
    val fontStepPx = height * (MAX_FONT_HEIGHT_FRACTION - MIN_FONT_HEIGHT_FRACTION) / (FONT_STEPS - 1)

    fun layoutAt(step: Int): TextLayoutResult {
        // Sized from the triangle, not from the user's font scale: the text always fills the
        // shape as far as it can, and a larger scale could only push it out of the shape.
        val fontSize = with(density) { (minFontPx + fontStepPx * step).toSp() }
        return textMeasurer.measure(
            text = text,
            style = style.copy(fontSize = fontSize, lineHeight = fontSize * LINE_HEIGHT_FACTOR),
            overflow = TextOverflow.Ellipsis,
            maxLines = MAX_LINES,
            constraints = Constraints.fixedWidth(textWidth)
        )
    }

    var best: FittedText? = null
    var low = 0
    var high = FONT_STEPS - 1
    while (low <= high) {
        val step = (low + high) / 2
        val layout = layoutAt(step)
        val top = if (layout.hasVisualOverflow) null else topInside(triangle, layout, edgePadding)
        if (top != null) {
            best = FittedText(layout, Offset(left, top))
            low = step + 1
        } else {
            high = step - 1
        }
    }
    return best ?: FittedText(layoutAt(0), Offset(left, height * MIN_TOP_FRACTION))
}

/**
 * Vertical position at which every line of [layout] fits inside [triangle], as close as
 * possible to the visual centre of the triangle, or null if there is no such position.
 */
private fun topInside(
    triangle: InvertedTriangleGeometry,
    layout: TextLayoutResult,
    edgePadding: Float
): Float? {
    val height = triangle.size.height
    var minTop = height * MIN_TOP_FRACTION
    var maxTop = Float.POSITIVE_INFINITY
    for (line in 0 until layout.lineCount) {
        val requiredWidth = layout.getLineRight(line) - layout.getLineLeft(line) + 2f * edgePadding
        minTop = max(minTop, triangle.highestYWithWidth(requiredWidth) - layout.getLineTop(line))
        maxTop = min(maxTop, triangle.lowestYWithWidth(requiredWidth) - layout.getLineBottom(line))
    }
    if (maxTop < minTop) return null

    val centeredTop = height * VISUAL_CENTER_FRACTION - layout.size.height / 2f
    return centeredTop.coerceIn(minTop, maxTop)
}

private val EDGE_PADDING = 6.dp
private const val MAX_LINES = 6
private const val LINE_HEIGHT_FACTOR = 1.3f
private const val FONT_STEPS = 13
private const val MAX_FONT_HEIGHT_FRACTION = 0.15f
private const val MIN_FONT_HEIGHT_FRACTION = 0.05f
private const val MIN_TOP_FRACTION = 0.15f
private const val VISUAL_CENTER_FRACTION = 0.42f
