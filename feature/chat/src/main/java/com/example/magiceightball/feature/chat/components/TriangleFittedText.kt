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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.wear.compose.material3.MaterialTheme
import com.example.magiceightball.feature.chat.LocalAppLanguage
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Draws [text], as it is written, inside an inverted triangle that fills this composable, at the
 * largest font size at which every line stays clear of the slanted edges and, when the die is
 * seen through a round [glass], inside that too. It is never written smaller than a wrist can
 * read: an answer too long for that is wrapped along the die instead.
 *
 * The text is measured with the exact style it is drawn with, once per size of the triangle,
 * and drawn directly: there is no subcomposition and no extra layout pass.
 */
@Composable
internal fun TriangleFittedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    glass: GlassOverDie? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val baseStyle = MaterialTheme.typography.bodyMedium
    // Line breaking follows the language of the app, not the one of the device.
    val locale = Locale(LocalAppLanguage.current.code)
    val style = remember(baseStyle, color, locale) {
        baseStyle.copy(
            color = color,
            fontWeight = FontWeight.SemiBold,
            localeList = LocaleList(locale),
            textAlign = TextAlign.Center,
            lineBreak = LineBreak.Heading
        )
    }

    Spacer(
        modifier
            .semantics {
                contentDescription = text
                liveRegion = LiveRegionMode.Polite
            }
            .drawWithCache {
                val fitted = if (size.isEmpty()) {
                    emptyList()
                } else {
                    fitTextInTriangle(
                        text = text,
                        style = style,
                        textMeasurer = textMeasurer,
                        triangle = InvertedTriangleGeometry(size),
                        glass = glass,
                        density = this
                    )
                }
                onDrawBehind {
                    fitted.forEach { drawText(it.layout, topLeft = it.topLeft) }
                }
            }
    )
}

/**
 * The round glass the die is seen through, in the die's own measures: its centre is on the
 * die's axis, [centerY] of the die's height down from its top, and its radius is [radius] of
 * that height.
 */
class GlassOverDie(val centerY: Float, val radius: Float)

/** A layout in its place: the whole answer, or one line of an answer wrapped along the die. */
private class FittedText(val layout: TextLayoutResult, val topLeft: Offset)

/**
 * From the largest font size down, tries a few wrapping widths and a few heights, and keeps the
 * first layout whose every line is whole and inside the die (and the glass). So a short answer is large,
 * on as many lines as suit it, near the middle; a long one is smaller and uses the whole width
 * near the top.
 *
 * Nothing is written smaller than [MinFontSize]. What does not fit at that size is wrapped
 * along the die, line by line, each as wide as the die is at its height, and a word too long
 * for its line is hyphenated; only what is still too long after that is ellipsized.
 */
private fun fitTextInTriangle(
    text: String,
    style: TextStyle,
    textMeasurer: TextMeasurer,
    triangle: InvertedTriangleGeometry,
    glass: GlassOverDie?,
    density: Density
): List<FittedText> {
    val height = triangle.size.height
    val centerX = triangle.size.width / 2f
    val edgePadding = triangle.size.width * EDGE_PADDING_FRACTION
    val smallestPx = with(density) { MinFontSize.toPx() }
    val largestPx = maxOf(height * MAX_FONT_HEIGHT_FRACTION, smallestPx)

    /** Half the room for a line whose box runs from [top] to [bottom]. */
    fun halfRoom(top: Float, bottom: Float): Float {
        var half = min(triangle.widthAt(top), triangle.widthAt(bottom)) / 2f
        if (glass != null) {
            val fromCenter = maxOf(abs(top - height * glass.centerY), abs(bottom - height * glass.centerY))
            val radius = height * glass.radius
            half = min(half, sqrt((radius * radius - fromCenter * fromCenter).coerceAtLeast(0f)))
        }
        return half - edgePadding
    }

    fun layoutAt(fontPx: Float, maxWidth: Float): TextLayoutResult {
        // Sized from the triangle, not from the user's font scale: the text always fills the
        // shape as far as it can, and a larger scale could only push it out of the shape.
        val fontSize = with(density) { fontPx.toSp() }
        return textMeasurer.measure(
            text = text,
            style = style.copy(fontSize = fontSize, lineHeight = fontSize * LINE_HEIGHT_FACTOR),
            overflow = TextOverflow.Ellipsis,
            maxLines = MAX_LINES,
            constraints = Constraints(maxWidth = maxWidth.toInt().coerceAtLeast(1))
        )
    }

    fun TextLayoutResult.fitsAt(top: Float): Boolean = !hasVisualOverflow &&
        (0 until lineCount).all { line ->
            // Words stay whole: one cut at a line's end reads worse than a smaller answer.
            val breaksAWord = line < lineCount - 1 && !text[getLineEnd(line) - 1].isWhitespace()
            !breaksAWord &&
                (getLineRight(line) - getLineLeft(line)) / 2f <=
                halfRoom(top + getLineTop(line), top + getLineBottom(line))
        }

    val middle = height * (glass?.centerY ?: VISUAL_CENTER_FRACTION)
    val highestTop = height * MIN_TOP_FRACTION

    for (step in 0 until FONT_STEPS) {
        val fontPx = lerp(largestPx, smallestPx, step / (FONT_STEPS - 1f))
        for (widthFraction in WRAP_WIDTHS) {
            val layout = layoutAt(fontPx, (triangle.maxWidth - 2f * edgePadding) * widthFraction)
            // As near the middle of what is seen as the lines allow, then higher, where the
            // die is wider.
            val centered = middle - layout.size.height / 2f
            for (lift in TOP_LIFTS) {
                val top = centered - height * lift
                if (top >= highestTop && layout.fitsAt(top)) {
                    return listOf(FittedText(layout, Offset(centerX - layout.size.width / 2f, top)))
                }
            }
        }
    }

    val smallest = with(density) { MinFontSize.toSp() }
    val wrappingStyle = style.copy(
        fontSize = smallest,
        lineHeight = smallest * LINE_HEIGHT_FACTOR,
        // Each line is filled before the next is started, and only a word too long for a line
        // of its own is cut, at one of the places [withSoftHyphens] marks.
        lineBreak = LineBreak.Simple,
        hyphens = Hyphens.Auto
    )
    val lineHeight = smallestPx * LINE_HEIGHT_FACTOR
    val narrowestLine = smallestPx * NARROWEST_LINE_EMS

    fun roomAt(lineTop: Float): Float = 2f * halfRoom(lineTop, lineTop + lineHeight)

    /**
     * The text from [top] down, in at most [lines] lines, each as wide as the die is at its
     * height. Null when that is not room enough for it, unless it may be [cutShort].
     */
    fun wrapAlongDie(top: Float, lines: Int, cutShort: Boolean = false): List<FittedText>? {
        val pieces = ArrayList<FittedText>(lines)
        var rest = text.withSoftHyphens()
        for (line in 0 until lines) {
            val lineTop = top + line * lineHeight
            val room = roomAt(lineTop).toInt()
            if (room < narrowestLine) return null
            val isCutShort = cutShort && line == lines - 1
            // Where a line this wide ends is for the platform to say: what is left is laid out
            // in the line's width, and the end of its first line is the end of this one.
            val end = if (isCutShort) {
                rest.length
            } else {
                textMeasurer.measure(rest, wrappingStyle, constraints = Constraints(maxWidth = room)).getLineEnd(0)
            }
            val words = rest.substring(0, end)
            val last = words.lastOrNull() ?: return null
            val cutsAWord = end < rest.length && !last.isWhitespace() && !words.endsWith(HYPHEN)
            // Cut anywhere else than at a mark, a word reads badly: there may be a place on
            // the die that does better.
            if (cutsAWord && last != SOFT_HYPHEN && !cutShort) return null
            // The line is written without the marks, which letter spacing would show as gaps,
            // and with the hyphen of a word that goes on below.
            val written = words.filterNot { it == SOFT_HYPHEN }.trimEnd() + if (cutsAWord) HYPHEN else ""
            val layout = textMeasurer.measure(
                text = written,
                style = wrappingStyle,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                constraints = if (isCutShort) Constraints(maxWidth = room) else Constraints()
            )
            pieces += FittedText(layout, Offset(centerX - layout.size.width / 2f, lineTop))
            if (end == rest.length) return pieces
            rest = rest.substring(end)
        }
        return null
    }

    // In as few lines as it takes, and as near the middle as they allow.
    for (lines in 1..MAX_LINES) {
        val centered = middle - lines * lineHeight / 2f
        for (lift in TOP_LIFTS) {
            val top = centered - height * lift
            if (top >= highestTop) wrapAlongDie(top, lines)?.let { return it }
        }
    }
    val linesFromTheTop = (0 until MAX_LINES).count { roomAt(highestTop + it * lineHeight) >= narrowestLine }
    return wrapAlongDie(highestTop, linesFromTheTop, cutShort = true).orEmpty()
}

/**
 * This text with a mark at every place where a word may be cut and go on, after a hyphen, on
 * the next line: before a single consonant between two vowels, which starts a syllable in each
 * of the app's languages, and never within two letters of either end of a word. A word that is
 * marked is cut at its marks only, so how it reads does not depend on the watch knowing the
 * hyphenation rules of its language.
 */
internal fun String.withSoftHyphens(): String = buildString(length) {
    val text = this@withSoftHyphens
    for (index in text.indices) {
        val startsASyllable = index >= 2 && index + 2 < text.length &&
            text[index - 2].isLetter() && text[index - 1].isVowel() &&
            text[index].isLetter() && !text[index].isVowel() &&
            text[index + 1].isVowel() && text[index + 2].isLetter()
        if (startsASyllable) append(SOFT_HYPHEN)
        append(text[index])
    }
}

private fun Char.isVowel(): Boolean = lowercaseChar() in VOWELS

private const val SOFT_HYPHEN = '\u00AD'
private const val HYPHEN = "-"

/** The vowels of English, Spanish and Portuguese, as they are written. */
private const val VOWELS = "aeiouáéíóúàâêôãõü"

/** Clear of the die's edges and of the glass's, as a share of the die's width. */
private const val EDGE_PADDING_FRACTION = 0.05f
private const val MAX_LINES = 6
private const val LINE_HEIGHT_FACTOR = 1.16f
private const val FONT_STEPS = 14
private const val MAX_FONT_HEIGHT_FRACTION = 0.19f

/**
 * The smallest an answer is written. In dp rather than as a share of the die: it is what an
 * eye needs at arm's length, whatever the size of the watch.
 */
private val MinFontSize = 11.dp

/** A line with less room than this many letters' height is no line: it would hold a syllable. */
private const val NARROWEST_LINE_EMS = 3f
private const val MIN_TOP_FRACTION = 0.06f
private const val VISUAL_CENTER_FRACTION = 0.36f
private val WRAP_WIDTHS = floatArrayOf(1f, 0.85f, 0.7f, 0.55f)
private val TOP_LIFTS = floatArrayOf(0f, 0.04f, 0.08f, 0.12f, 0.16f, 0.2f)
