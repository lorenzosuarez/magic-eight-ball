package com.example.magiceightball.feature.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.magiceightball.feature.chat.ChatStateMachine

/**
 * Orchestrates the "Magic Die" motion: entering, shaking, and settling.
 * Uses GraphicsLayer for high-performance translation.
 */
@Composable
fun PredictionTriangleAnimator(
    machineState: ChatStateMachine,
    content: @Composable (Modifier, Boolean) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    LaunchedEffect(machineState) {
        if (machineState is ChatStateMachine.Completed) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }

    Box {
        content(
            Modifier,
            machineState is ChatStateMachine.Running
        )
    }
}
