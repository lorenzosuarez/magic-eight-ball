package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.feature.chat.components.ConsoleMenu
import com.example.magiceightball.feature.chat.components.ConsoleOption

@Composable
fun PersonalitySelectionScreen(
    selectedPersonality: Magic8BallPersonality,
    language: AppLanguage,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH

    ConsoleMenu(
        title = stringResource(
            if (isSpanish) R.string.personality_title_es else R.string.personality_title
        )
    ) {
        ConsoleOption(
            label = stringResource(
                if (isSpanish) R.string.personality_sarcastic_es else R.string.personality_sarcastic
            ),
            isSelected = selectedPersonality == Magic8BallPersonality.SARCASTIC,
            onClick = { onPersonalitySelected(Magic8BallPersonality.SARCASTIC) }
        )
        ConsoleOption(
            label = stringResource(
                if (isSpanish) R.string.personality_mystical_es else R.string.personality_mystical
            ),
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
        language = AppLanguage.ENGLISH,
        onPersonalitySelected = {}
    )
}
