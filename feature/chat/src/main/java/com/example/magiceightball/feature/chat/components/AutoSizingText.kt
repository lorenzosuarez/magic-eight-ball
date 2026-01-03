package com.example.magiceightball.feature.chat.components

import android.annotation.SuppressLint
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A container that calculates the optimal font size for a given text to fit within
 * a geometric constraint (specifically tailored for the Inverted Triangle centroid width).
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AutoSizingTextContainer(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    content: @Composable (TextUnit) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val containerWidth = constraints.maxWidth
        val containerHeight = constraints.maxHeight
        
        // Geometric Safety:
        // The triangle gets narrower. We need to ensure the text fits in the "bottom-most" part of the text block.
        // Assuming the text is centered at ~35% height, and might span ~15-20% height.
        // The bottom of the text would be around 45-50% of the container.
        // Let's constrain width to what is available at 50% height to be safe for all lines.
        // Inverted triangle width at 50% height is 50% of top width.
        // We apply a safe padding factor (0.9) to that.
        val safeMaxWidth = (containerWidth * 0.5f * 0.9f).toInt()

        // Binary search / Iterative search for optimal font size
        // We want the largest size that fits in safeMaxWidth with maxLines
        
        val optimalSize = remember(text, containerWidth, containerHeight) {
            var bestSize = 8.sp // fallback
            
            val paint = TextPaint().apply {
                isAntiAlias = true
            }
            
            // Search from largest reasonable size down to smallest
            // Reduced max size from 20 to 16 per user request for short phrases
            for (size in 16 downTo 6) {
                val spValue = size.sp
                val px = with(density) { spValue.toPx() }
                paint.textSize = px
                
                // Estimate layout using StaticLayout
                val builder = StaticLayout.Builder.obtain(
                    text, 0, text.length, paint, safeMaxWidth.coerceAtLeast(10)
                )
                    .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NORMAL)
                    .setBreakStrategy(android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY)
                    .setMaxLines(maxLines)
                    
                val layout = builder.build()
                
                // Check if it fits visual bounds
                if (layout.lineCount <= maxLines && layout.getLineEnd(layout.lineCount - 1) == text.length) {
                    // It fits!
                    bestSize = spValue
                    break // Found largest
                }
            }
            bestSize
        }
        
        content(optimalSize)
    }
}
