package com.example.magiceightball.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.wear.compose.material3.Text

/**
 * A full-screen page of settings: a quiet title followed by a group of [SettingsOption]s, of
 * which one is selected.
 *
 * @param caption optional note shown right under the title, e.g. why the options are disabled.
 */
@Composable
internal fun SettingsMenu(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    options: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BallColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = MenuSidePadding)
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(OptionSpacing)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = TitleSpacing),
                verticalArrangement = Arrangement.spacedBy(CaptionSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    color = BallColors.OnSurfaceQuiet,
                    style = MaterialTheme.typography.bodySmall
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        color = BallColors.OnSurfaceQuiet,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyExtraSmall
                    )
                }
            }
            options()
        }
    }
}

/**
 * One mutually exclusive choice of a [SettingsMenu]: a dot, filled in the accent when it is the
 * selected one, and its label.
 *
 * A choice that is not [enabled] cannot be picked; it is dimmed unless it is the selected one.
 */
@Composable
internal fun SettingsOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val color = when {
        isSelected -> BallColors.Accent
        enabled -> BallColors.OnSurface
        else -> BallColors.OnSurfaceQuiet.copy(alpha = DISABLED_ALPHA)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = enabled,
                interactionSource = null,
                indication = OptionRipple,
                role = Role.RadioButton,
                onClick = onClick
            )
            // Tall enough for a finger on a wrist, whatever the text needs.
            .heightIn(min = OptionMinHeight)
            .padding(vertical = OptionVerticalPadding, horizontal = OptionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(DotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            Modifier
                .size(DotSize)
                .drawBehind {
                    if (isSelected) {
                        drawCircle(color)
                    } else {
                        val outline = DotOutlineWidth.toPx()
                        drawCircle(color, radius = (size.minDimension - outline) / 2f, style = Stroke(outline))
                    }
                }
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * A setting with more choices than fit a page: what is chosen now, and a tap opens them all.
 */
@Composable
internal fun SettingsLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = null, indication = OptionRipple, role = Role.Button, onClick = onClick)
            .heightIn(min = OptionMinHeight)
            .padding(horizontal = OptionHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = BallColors.Accent,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = "\u203A", color = BallColors.OnSurfaceQuiet, style = MaterialTheme.typography.bodyMedium)
    }
}

private val MenuSidePadding = 28.dp
private val OptionMinHeight = 48.dp
private val TitleSpacing = 4.dp
private val CaptionSpacing = 2.dp
private val OptionSpacing = 2.dp
private val OptionVerticalPadding = 8.dp
private val OptionHorizontalPadding = 8.dp
private val DotSize = 12.dp
private val DotSpacing = 10.dp
private val DotOutlineWidth = 1.5.dp
private const val DISABLED_ALPHA = 0.5f
private val OptionRipple = ripple(color = BallColors.OnSurface)
