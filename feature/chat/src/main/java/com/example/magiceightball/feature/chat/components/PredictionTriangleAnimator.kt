package com.example.magiceightball.feature.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.magiceightball.feature.chat.ChatStateMachine
import kotlin.math.abs

/**
 * Orchestrates the "Magic Die" motion: entering, shaking, and settling.
 * Uses GraphicsLayer for high-performance translation.
 */
@Composable
fun PredictionTriangleAnimator(
    machineState: ChatStateMachine,
    content: @Composable (Modifier, Boolean) -> Unit
) {
    val screenWidthPx = with(LocalDensity.current) { 200.dp.toPx() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Start hidden to the LEFT (-screenWidthPx)
    val offsetX = remember { Animatable(-screenWidthPx) }
    
    LaunchedEffect(machineState) {
        when (machineState) {
            ChatStateMachine.Idle -> {
                // Reset/Hide to Left
                offsetX.snapTo(-screenWidthPx) 
            }
            ChatStateMachine.Running -> {
                // Animate Left -> Center with Spring
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.7f, // Slightly less bouncy to look controlled
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            is ChatStateMachine.Completed -> {
                // Ensure we are at Center
                offsetX.snapTo(0f)
                

                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            }
        }
    }

    Box {
        content(
            Modifier.graphicsLayer {
                translationX = offsetX.value
                
                // We keep alpha 1 to ensure visibility unless Idle
                alpha = if (machineState is ChatStateMachine.Idle) 0f else 1f
            },
            machineState is ChatStateMachine.Running
        )
    }
}
