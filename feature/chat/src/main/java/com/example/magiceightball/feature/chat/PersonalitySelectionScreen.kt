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
import com.example.magiceightball.core.domain.model.Magic8BallPersonality

@Composable
fun PersonalitySelectionScreen(
    selectedPersonality: Magic8BallPersonality,
    language: AppLanguage,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    val titleRes = if (language == AppLanguage.SPANISH) R.string.personality_title_es else R.string.personality_title
    val sarcasticRes = if (language == AppLanguage.SPANISH) R.string.personality_sarcastic_es else R.string.personality_sarcastic
    val mysticalRes = if (language == AppLanguage.SPANISH) R.string.personality_mystical_es else R.string.personality_mystical

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3F3F3F), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            )

            ConsoleOption(
                label = stringResource(sarcasticRes),
                isSelected = selectedPersonality == Magic8BallPersonality.SARCASTIC,
                onClick = { onPersonalitySelected(Magic8BallPersonality.SARCASTIC) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConsoleOption(
                label = stringResource(mysticalRes),
                isSelected = selectedPersonality == Magic8BallPersonality.MYSTICAL,
                onClick = { onPersonalitySelected(Magic8BallPersonality.MYSTICAL) }
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
    val color = if (isSelected) Color(0xFF00FF00) else Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$indicator $label",
            style = MaterialTheme.typography.bodySmall.copy(
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
fun PersonalitySelectionScreenPreview() {
    PersonalitySelectionScreen(
        selectedPersonality = Magic8BallPersonality.SARCASTIC,
        language = AppLanguage.ENGLISH,
        onPersonalitySelected = {}
    )
}
