package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.feature.chat.components.SettingsMenu
import com.example.magiceightball.feature.chat.components.SettingsOption

/**
 * Lets the user choose whether answers come from the AI service.
 *
 * While AI answers are not [isAvailable] the choice would change nothing, so the screen says so
 * and shows what actually happens (off) instead of a working toggle; the user's own choice is
 * kept and applies again as soon as AI answers become available.
 *
 * The choice stays the user's where it is the style or the day that keeps the service out of
 * the answers ([isClassicStyle], [isSpentForToday]): the screen then says why "on" does nothing.
 */
@Composable
fun AiAnswersSelectionScreen(
    isEnabled: Boolean,
    isAvailable: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    isClassicStyle: Boolean = false,
    isSpentForToday: Boolean = false
) {
    val isOn = isEnabled && isAvailable

    SettingsMenu(
        title = localizedString(Strings.AiTitle),
        caption = when {
            !isAvailable -> localizedString(Strings.AiUnavailable)
            !isOn -> null
            isClassicStyle -> localizedString(Strings.AiClassic)
            isSpentForToday -> localizedString(Strings.AiSpent)
            else -> null
        }
    ) {
        SettingsOption(
            label = localizedString(Strings.AiOn),
            isSelected = isOn,
            onClick = { onEnabledChange(true) },
            enabled = isAvailable
        )
        SettingsOption(
            label = localizedString(Strings.AiOff),
            isSelected = !isOn,
            onClick = { onEnabledChange(false) },
            enabled = isAvailable
        )
    }
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun AiAnswersSelectionScreenPreview() {
    AiAnswersSelectionScreen(isEnabled = true, isAvailable = true, onEnabledChange = {})
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun AiAnswersSelectionScreenUnavailablePreview() {
    AiAnswersSelectionScreen(isEnabled = true, isAvailable = false, onEnabledChange = {})
}
