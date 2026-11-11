package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.feature.chat.components.SettingsMenu
import com.example.magiceightball.feature.chat.components.SettingsOption

@Composable
fun PersonalitySelectionScreen(
    selectedPersonality: Magic8BallPersonality,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    SettingsMenu(title = localizedString(Strings.PersonalityTitle)) {
        Magic8BallPersonality.entries.forEach { personality ->
            SettingsOption(
                label = localizedString(personality.label()),
                isSelected = selectedPersonality == personality,
                onClick = { onPersonalitySelected(personality) }
            )
        }
    }
}

private fun Magic8BallPersonality.label(): LocalizedString = when (this) {
    Magic8BallPersonality.SARCASTIC -> Strings.PersonalitySarcastic
    Magic8BallPersonality.MYSTICAL -> Strings.PersonalityMystical
    Magic8BallPersonality.NEUTRAL -> Strings.PersonalityNeutral
    Magic8BallPersonality.GRANDMA -> Strings.PersonalityGrandma
    Magic8BallPersonality.PIRATE -> Strings.PersonalityPirate
    Magic8BallPersonality.PHILOSOPHER -> Strings.PersonalityPhilosopher
    Magic8BallPersonality.COACH -> Strings.PersonalityCoach
    Magic8BallPersonality.TOXIC_EX -> Strings.PersonalityToxicEx
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun PersonalitySelectionScreenPreview() {
    PersonalitySelectionScreen(
        selectedPersonality = Magic8BallPersonality.SARCASTIC,
        onPersonalitySelected = {}
    )
}
