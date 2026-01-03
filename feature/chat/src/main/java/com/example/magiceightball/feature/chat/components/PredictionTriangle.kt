package com.example.magiceightball.feature.chat.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

object PredictionTriangleDefaults {
    val BackgroundColor = Color(0xFF1E50F3)
    val GlowColor = Color(0xFF4B79FF)
    val BorderColor = Color(0xFF102875).copy(alpha = 0.8f)
    val TextColor = Color.White

    val PaddingHorizontal = 16.dp // Reduced from 30.dp to allow more text width
    
    val TextSize = 10.sp
    val LineHeight = 22.sp

    const val MAX_LINES = 3
    const val BLUR_RADIUS = 45f
}

class InvertedTriangleShape(private val cornerRadius: Float = 20f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Top Left Corner
            moveTo(cornerRadius, 0f)
            lineTo(size.width - cornerRadius, 0f)
            quadraticTo(size.width, 0f, size.width - (cornerRadius * 0.5f), cornerRadius)
            
            // Bottom Tip
            lineTo(size.width / 2f + (cornerRadius * 0.5f), size.height - cornerRadius)
            quadraticTo(size.width / 2f, size.height, size.width / 2f - (cornerRadius * 0.5f), size.height - cornerRadius)
            
            // Top Left Closing
            lineTo((cornerRadius * 0.5f), cornerRadius)
            quadraticTo(0f, 0f, cornerRadius, 0f)
            
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun PredictionTriangle(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    backgroundColor: Color = PredictionTriangleDefaults.BackgroundColor,
    glowColor: Color = PredictionTriangleDefaults.GlowColor,
    borderColor: Color = PredictionTriangleDefaults.BorderColor,
    textColor: Color = PredictionTriangleDefaults.TextColor
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }

            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint().apply {
                    color = glowColor.toArgb()
                    maskFilter = BlurMaskFilter(
                        PredictionTriangleDefaults.BLUR_RADIUS,
                        BlurMaskFilter.Blur.OUTER
                    )
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
            }
        }

        val triangleShape = InvertedTriangleShape(cornerRadius = 15f)

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(triangleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, triangleShape)
                .padding(
                    start = PredictionTriangleDefaults.PaddingHorizontal,
                    end = PredictionTriangleDefaults.PaddingHorizontal,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                val shimmerColors = listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.5f),
                    Color.White.copy(alpha = 0.2f)
                )
                val transition = rememberInfiniteTransition(label = "shimmer")
                val translateAnim = transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = LinearEasing)
                    ),
                    label = "shimmerTranslate"
                )
                val brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset.Zero,
                    end = Offset(translateAnim.value, translateAnim.value)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .size(50.dp, 8.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                    Box(
                        Modifier
                            .size(70.dp, 8.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                    Box(
                        Modifier
                            .size(40.dp, 8.dp)
                            .background(brush, RoundedCornerShape(4.dp))
                    )
                }

            } else {
                // Intelligent Auto-Sizing Text
                // Dynamically calculates the best font size to fit within the triangle's geometric bounds
                AutoSizingTextContainer(
                    text = text.uppercase(),
                    modifier = Modifier.fillMaxSize(),
                    maxLines = PredictionTriangleDefaults.MAX_LINES
                ) { optimalFontSize ->
                    TriangleContentLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            text = text.uppercase(),
                            color = textColor,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = optimalFontSize,
                                lineHeight = optimalFontSize * 1.3f, // Proportional line height
                                hyphens = Hyphens.Auto,
                                lineBreak = LineBreak.Paragraph
                            ),
                            maxLines = PredictionTriangleDefaults.MAX_LINES
                        )
                    }
                }
            }
        }
    }
}





// AutoSizingTextContainer is now externalized


@Composable
private fun TriangleContentLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints)
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        val contentWidth = placeable.width.toFloat()
        val contentHeight = placeable.height.toFloat()

        // 1. Define the ideal visual center (e.g., 45% down looks balanced in an inverted triangle)
        val idealCenterY = containerHeight * 0.45f

        // 2. Calculate the lowest safe position based on geometry
        // The triangle width at any Y is: W * (1 - Y/H)
        // We need the width at the BOTTOM of the text to be >= contentWidth (with padding)
        // Constraint: contentWidth <= containerWidth * (1 - (centerY + h/2) / H)
        // Solving for centerY:
        // contentWidth / containerWidth <= 1 - (bottomY / H)
        // bottomY / H <= 1 - contentWidth/containerWidth
        // bottomY <= H * (1 - contentWidth/containerWidth)
        
        // Add a safety buffer (e.g. 1.1x width) to avoid touching edges
        val safeBottomY = containerHeight * (1f - (contentWidth * 1.2f / containerWidth))
        val maxSafeCenterY = safeBottomY - (contentHeight / 2f)

        // 3. The actual Y is the ideal center, but pushed up if necessary to fit
        val finalCenterY = minOf(idealCenterY, maxSafeCenterY)

        val y = (finalCenterY - (contentHeight / 2f)).toInt().coerceAtLeast(containerHeight.toInt() / 10) // Don't go too high (top 10%)
        val x = (constraints.maxWidth - placeable.width) / 2
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(x, y)
        }
    }
}

@Preview(group = "Components")
@Composable
fun PredictionTrianglePreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PredictionTriangle(
            text = "Signs\nPoint To\nYes",
            modifier = Modifier.size(150.dp)
        )
    }
}
