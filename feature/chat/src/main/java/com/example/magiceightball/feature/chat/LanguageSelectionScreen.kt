package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.feature.chat.components.SettingsMenu
import com.example.magiceightball.feature.chat.components.SettingsOption

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    SettingsMenu(title = localizedString(Strings.LanguageTitle)) {
        SettingsOption(
            label = localizedString(Strings.LanguageEnglish),
            isSelected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
        )
        SettingsOption(
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
