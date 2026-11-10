package com.example.magiceightball.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Full-screen, terminal-styled menu: a title followed by a group of [ConsoleOption]s.
 *
 * @param caption optional note shown right under the title, e.g. why the options are disabled.
 */
@Composable
internal fun ConsoleMenu(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    options: @Composable ColumnScope.() -> Unit
) {
    val bodyMedium = MaterialTheme.typography.bodyMedium
    val titleStyle = remember(bodyMedium) {
        bodyMedium.copy(fontFamily = FontFamily.Monospace, color = TitleColor)
    }
    val bodyExtraSmall = MaterialTheme.typography.bodyExtraSmall
    val captionStyle = remember(bodyExtraSmall) {
        bodyExtraSmall.copy(fontFamily = FontFamily.Monospace, color = CaptionColor)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(CaptionSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = titleStyle)
                if (caption != null) {
                    Text(text = caption, textAlign = TextAlign.Center, style = captionStyle)
                }
            }
            options()
        }
    }
}

/**
 * One mutually exclusive choice of a [ConsoleMenu], rendered as `[x] LABEL` / `[ ] LABEL`.
 *
 * A choice that is not [enabled] cannot be picked; it is dimmed unless it is the selected one.
 */
@Composable
internal fun ConsoleOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val bodySmall = MaterialTheme.typography.bodySmall
    val style = remember(bodySmall, isSelected, enabled) {
        bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = when {
                isSelected -> SelectedColor
                enabled -> UnselectedColor
                else -> DisabledColor
            }
        )
    }
    val text = remember(label, isSelected) {
        "${if (isSelected) SELECTED_INDICATOR else UNSELECTED_INDICATOR} $label"
    }

    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = OptionRipple,
                role = Role.RadioButton,
                onClick = onClick
            )
            // Read out the label alone: the selection is already part of the semantics.
            .semantics { contentDescription = label }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        style = style
    )
}

private const val SELECTED_INDICATOR = "[x]"
private const val UNSELECTED_INDICATOR = "[ ]"

private val BackgroundColor = Color(0xFF3F3F3F)
private val TitleColor = Color.Gray
private val CaptionColor = Color.LightGray
private val CaptionSpacing = 2.dp
private val SelectedColor = Color(0xFF00FF00)
private val UnselectedColor = Color.LightGray
private val DisabledColor = Color.Gray
private val OptionRipple = ripple(color = Color.White)
