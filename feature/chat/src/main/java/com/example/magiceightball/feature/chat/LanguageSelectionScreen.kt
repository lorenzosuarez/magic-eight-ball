package com.example.magiceightball.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3F3F3F), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Start, // Console usually aligns left-ish, but center is safer for Wear
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp) // Extra margin for round screen
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                text = stringResource(R.string.lang_select_title),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            )

            ConsoleOption(
                label = stringResource(R.string.lang_english),
                isSelected = selectedLanguage == "en",
                onClick = { onLanguageSelected("en") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConsoleOption(
                label = stringResource(R.string.lang_spanish),
                isSelected = selectedLanguage == "es",
                onClick = { onLanguageSelected("es") }
            )
        }
    }
}

@Composable
private fun ConsoleOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val indicator = if (isSelected) "[x]" else "[ ]"
    val color = if (isSelected) Color(0xFF00FF00) else Color.LightGray // Matrix/Console Green for selected

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White), // Use new ripple API
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp), // Ripple area padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start // Console look align left
    ) {
        Text(
            text = "$indicator $label",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Preview(
    widthDp = 200,
    heightDp = 200,
    showBackground = true
)
@Composable
fun LanguageSelectionScreenPreview() {
    LanguageSelectionScreen(
        selectedLanguage = "en",
        onLanguageSelected = {}
    )
}
