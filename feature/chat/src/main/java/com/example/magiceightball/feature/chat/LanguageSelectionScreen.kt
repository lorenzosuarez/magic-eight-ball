package com.example.magiceightball.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.magiceightball.feature.chat.components.ConsoleMenu
import com.example.magiceightball.feature.chat.components.ConsoleOption

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    ConsoleMenu(title = stringResource(R.string.lang_select_title)) {
        ConsoleOption(
            label = stringResource(R.string.lang_english),
            isSelected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
        )
        ConsoleOption(
            label = stringResource(R.string.lang_spanish),
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
