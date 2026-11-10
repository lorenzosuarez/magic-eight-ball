package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.feature.chat.components.ConsoleMenu
import com.example.magiceightball.feature.chat.components.ConsoleOption

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    ConsoleMenu(title = localizedString(Strings.LanguageTitle)) {
        ConsoleOption(
            label = localizedString(Strings.LanguageEnglish),
            isSelected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
        )
        ConsoleOption(
            label = localizedString(Strings.LanguageSpanish),
            isSelected = selectedLanguage == AppLanguage.SPANISH,
            onClick = { onLanguageSelected(AppLanguage.SPANISH) }
        )
    }
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
private fun LanguageSelectionScreenPreview() {
    LanguageSelectionScreen(
        selectedLanguage = AppLanguage.ENGLISH,
        onLanguageSelected = {}
    )
}
