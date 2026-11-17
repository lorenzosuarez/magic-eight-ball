package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A page's name, large, at the top of what the page lists: the first thing there, which leaves
 * with the rest as the page is read down, as on a page of the system's own settings. While it is
 * in view the bar over the page says no name; once it has left under the bar, the bar says it
 * small: see [BarTitle]. A page has one name on it at a time.
 *
 * It stands where it is put, which is the margin the page's rows begin at and the pictures of
 * the bar's marks stand on. While it is there it is the page's heading for whoever has the
 * screen read out.
 */
@Composable
internal fun LargeTitle(text: String, modifier: Modifier = Modifier) {
    // ponytail: listed with the page, it rests wherever the hand leaves it, part of it under the bar too.
    Text(
        text = text,
        modifier = modifier
            .padding(vertical = LARGE_TITLE_AIR)
            .semantics { heading() },
        // Where it takes two lines it is broken as a heading is: no word left alone on the last.
        style = MaterialTheme.typography.headlineMedium.copy(lineBreak = LineBreak.Heading).forPageName(),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * A page's name in the bar over it, small: there once the page's [LargeTitle] has left under the
 * bar, and gone again when that comes back. It comes and goes in the time of a glance, so that
 * the name is never missed and never read twice.
 *
 * @param shown whether the large name has left under the bar.
 */
@Composable
internal fun BarTitle(text: String, shown: Boolean, modifier: Modifier = Modifier) {
    // Another name in its place stands as it is, at once: only a name's own coming and going is a fade.
    val seen by key(text) { designedFloatAsState(if (shown) 1f else 0f, tween(BAR_TITLE_MS, easing = LinearEasing)) }
    Text(
        text = text,
        modifier = modifier
            .graphicsLayer { alpha = seen }
            // One heading to a page: this one while it is shown, and the large name while it is not.
            .then(if (shown) Modifier.semantics { heading() } else Modifier.clearAndSetSemantics {}),
        style = MaterialTheme.typography.titleMedium.forPageName(),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        // The bar has room for two lines of it beside a mark, and no more.
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Where a page says that its large name has left under the bar, for whoever draws the bar over
 * it: the name that has left, and none while it is in view. There is nowhere to say it where a
 * page is shown with no bar over it.
 */
internal val LocalTitleAway = staticCompositionLocalOf<MutableState<String?>?> { null }

/**
 * Says to the bar over the page whether [title], large at the top of what the page scrolls, has
 * left under it. A page takes back only what it said itself, so that one on its way out does
 * not unsay the name of the one that has come in its place.
 */
@Composable
internal fun TitleAwayEffect(title: String, away: Boolean) {
    val said = LocalTitleAway.current ?: return
    SideEffect {
        if (away) said.value = title else if (said.value == title) said.value = null
    }
}

/**
 * [this] as far as a page's name follows the system's font size. The small name has a line's
 * room in its bar, and the large one is large enough already: grown further, it would only take
 * from the page what it names.
 */
@Composable
private fun TextStyle.forPageName(): TextStyle {
    val fit = (PAGE_TITLE_MAX_SCALE / LocalDensity.current.fontScale).coerceAtMost(1f)
    return copy(fontSize = fontSize * fit, lineHeight = lineHeight * fit)
}

/**
 * The air over and under a page's large name, beyond what its own line leaves: little, since the
 * bar above it ends in room of its own and what it names begins with some. It is the page's
 * name, not a banner over it.
 */
private val LARGE_TITLE_AIR = 4.dp

/** How long the small name takes to come into the bar, and to leave it. */
private const val BAR_TITLE_MS = 150
