package com.example.magiceightball.feature.chat.components

import android.annotation.SuppressLint
import android.graphics.text.LineBreaker
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
        
        val searchMaxWidth = (containerWidth * 0.85f).toInt()

        val optimalSize = remember(text, containerWidth, containerHeight) {
            var bestSize = 8.sp
            
            val paint = TextPaint().apply {
                isAntiAlias = true
            }
            
            for (size in 18 downTo 6) {
                val spValue = size.sp
                val px = with(density) { spValue.toPx() }
                paint.textSize = px
                
                val builder = StaticLayout.Builder.obtain(
                    text, 0, text.length, paint, searchMaxWidth.coerceAtLeast(10)
                )
                    .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.1f)
                    .setIncludePad(false)
                    .setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NORMAL)
                    .setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
                    .setMaxLines(maxLines)
                    
                val layout = builder.build()
                
                val actualHeight = layout.height.toFloat()
                var maxLineWidth = 0f
                for (i in 0 until layout.lineCount) {
                    maxLineWidth = maxOf(maxLineWidth, layout.getLineWidth(i))
                }
                maxLineWidth *= 1.1f 

                val idealCenterY = containerHeight * 0.42f
                val topY = idealCenterY - (actualHeight / 2f)
                val bottomY = idealCenterY + (actualHeight / 2f)

                val fitsTop = topY >= (containerHeight * 0.15f)
                
                val availableWidthAtBottom = containerWidth * (1f - (bottomY / containerHeight))
                val fitsBottom = maxLineWidth <= availableWidthAtBottom

                val fullyVisible = layout.lineCount <= maxLines && layout.getLineEnd(layout.lineCount - 1) == text.length

                if (fullyVisible && fitsTop && fitsBottom) {
                    bestSize = spValue
                    break 
                }
            }
            bestSize
        }
        
        content(optimalSize)
    }
}
