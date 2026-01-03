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
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        
        // Geometric Safety:
        // We relax the static width constraint to allow wider text at the top (which is safer in an inverted triangle).
        // We will strictly validate "Does it fit?" inside the loop.
        val searchMaxWidth = (containerWidth * 0.85f).toInt()

        // Binary search / Iterative search for optimal font size
        val optimalSize = remember(text, containerWidth, containerHeight) {
            var bestSize = 8.sp // fallback
            
            val paint = TextPaint().apply {
                isAntiAlias = true
            }
            
            // Search from largest reasonable size down to smallest
            for (size in 18 downTo 6) {
                val spValue = size.sp
                val px = with(density) { spValue.toPx() }
                paint.textSize = px
                
                // Estimate layout using StaticLayout
                val builder = StaticLayout.Builder.obtain(
                    text, 0, text.length, paint, searchMaxWidth.coerceAtLeast(10)
                )
                    .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.1f) // Slight extra spacing for legibility
                    .setIncludePad(false)
                    .setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NORMAL)
                    .setBreakStrategy(android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY)
                    .setMaxLines(maxLines)
                    
                val layout = builder.build()
                
                // --- GEOMETRIC VALIDATION ---
                // 1. Measure actual block dimensions
                val actualHeight = layout.height.toFloat()
                var maxLineWidth = 0f
                for (i in 0 until layout.lineCount) {
                    maxLineWidth = maxOf(maxLineWidth, layout.getLineWidth(i))
                }
                // Add padding visual buffer
                maxLineWidth *= 1.1f 

                // 2. Position logic (Matches TriangleContentLayout)
                val idealCenterY = containerHeight * 0.42f
                val topY = idealCenterY - (actualHeight / 2f)
                val bottomY = idealCenterY + (actualHeight / 2f)

                // 3. Check Constraints
                // A. Top Corner Clearance (15% zone)
                val fitsTop = topY >= (containerHeight * 0.15f)
                
                // B. Bottom Tip Width
                // Available width at bottomY = TotalW * (1 - bottomY/TotalH)
                val availableWidthAtBottom = containerWidth * (1f - (bottomY / containerHeight))
                val fitsBottom = maxLineWidth <= availableWidthAtBottom

                // C. Max Lines and Termination
                val fullyVisible = layout.lineCount <= maxLines && layout.getLineEnd(layout.lineCount - 1) == text.length

                if (fullyVisible && fitsTop && fitsBottom) {
                    // It fits geometrically!
                    bestSize = spValue
                    break 
                }
            }
            bestSize
        }
        
        content(optimalSize)
    }
}
