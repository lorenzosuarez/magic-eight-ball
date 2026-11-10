package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.feature.chat.components.ConsoleMenu
import com.example.magiceightball.feature.chat.components.ConsoleOption

@Composable
fun PersonalitySelectionScreen(
    selectedPersonality: Magic8BallPersonality,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    ConsoleMenu(title = localizedString(Strings.PersonalityTitle)) {
        ConsoleOption(
            label = localizedString(Strings.PersonalitySarcastic),
            isSelected = selectedPersonality == Magic8BallPersonality.SARCASTIC,
            onClick = { onPersonalitySelected(Magic8BallPersonality.SARCASTIC) }
        )
        ConsoleOption(
            label = localizedString(Strings.PersonalityMystical),
            isSelected = selectedPersonality == Magic8BallPersonality.MYSTICAL,
            onClick = { onPersonalitySelected(Magic8BallPersonality.MYSTICAL) }
        )
    }
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun PersonalitySelectionScreenPreview() {
    PersonalitySelectionScreen(
        selectedPersonality = Magic8BallPersonality.SARCASTIC,
        onPersonalitySelected = {}
    )
}
