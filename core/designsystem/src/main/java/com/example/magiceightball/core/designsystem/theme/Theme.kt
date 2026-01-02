package com.example.magiceightball.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme

@Composable
fun MagicEightBallTheme(
    content: @Composable () -> Unit
) {
    // Uses default Material 3 Wear colors for now.
    // Can be customized with ColorScheme()
    MaterialTheme(
        content = content
    )
}
