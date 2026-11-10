package com.example.magiceightball.feature.chat.components

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.ceil

object PredictionTriangleDefaults {
    val BackgroundColor = Color(0xFF1E50F3)
    val GlowColor = Color(0xFF4B79FF)
    val BorderColor = Color(0xFF102875).copy(alpha = 0.8f)
    val TextColor = Color.White
    val BorderWidth = 2.dp
}

/**
 * The glowing triangular face of the die. Shows a shimmer placeholder while [isLoading] and
 * cross-fades to [text] once the answer is in.
 *
 * Glow, fill and border are drawn in one pass from objects that are rebuilt only when the size
 * or the colours change; the blurred glow in particular is rendered once into a bitmap.
 */
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
        modifier = modifier.drawWithCache {
            if (size.isEmpty()) return@drawWithCache onDrawBehind {}

            val triangle = InvertedTriangleGeometry(size)
            val path = triangle.toPath()
            val glowRadius = triangle.cornerRadius
            val glow = renderGlow(path, size, glowRadius, glowColor)
            val glowOffset = Offset(-glowRadius, -glowRadius)
            val border = Stroke(width = PredictionTriangleDefaults.BorderWidth.toPx())

            onDrawBehind {
                drawImage(glow, topLeft = glowOffset)
                drawPath(path, backgroundColor)
                drawPath(path, borderColor, style = border)
            }
        }
    ) {
        Crossfade(
            targetState = isLoading,
            modifier = Modifier.matchParentSize(),
            animationSpec = tween(CONTENT_FADE_MILLIS),
            label = "PredictionTriangleContent"
        ) { loading ->
            if (loading) {
                ThinkingShimmer(Modifier.fillMaxSize())
            } else {
                TriangleFittedText(
                    text = text,
                    color = textColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/** Renders the outer glow of [path] once, into a bitmap padded by [radius] on every side. */
private fun renderGlow(path: Path, size: Size, radius: Float, color: Color): ImageBitmap {
    val bitmap = ImageBitmap(
        width = ceil(size.width + 2f * radius).toInt(),
        height = ceil(size.height + 2f * radius).toInt()
    )
    val paint = Paint().apply {
        this.color = color
        asFrameworkPaint().maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER)
    }
    Canvas(bitmap).apply {
        translate(radius, radius)
        drawPath(path, paint)
    }
    return bitmap
}

/**
 * Three placeholder bars with a highlight sweeping across them.
 *
 * The animated value is read only while drawing, and the sweep moves a shader that is created
 * once, so a frame of the animation neither recomposes nor allocates.
 */
@Composable
private fun ThinkingShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ThinkingShimmer")
    val sweep = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_SWEEP_MILLIS, easing = LinearEasing)),
        label = "ThinkingShimmerSweep"
    )

    Spacer(
        modifier.drawWithCache {
            val centerX = size.width / 2f
            val barHeight = size.height * SHIMMER_BAR_HEIGHT_FRACTION
            val barRadius = barHeight / 2f
            val bandWidth = size.width * SHIMMER_BAND_WIDTH_FRACTION
            val widestBar = size.width * ShimmerBarWidthFractions.max()
            val sweepStart = centerX - widestBar / 2f - bandWidth
            val sweepEnd = centerX + widestBar / 2f

            val shader = LinearGradient(
                0f, 0f, bandWidth, 0f, ShimmerColors, null, Shader.TileMode.CLAMP
            )
            val shaderMatrix = Matrix()
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                .apply { this.shader = shader }

            onDrawBehind {
                shaderMatrix.setTranslate(lerp(sweepStart, sweepEnd, sweep.value), 0f)
                shader.setLocalMatrix(shaderMatrix)
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    for (bar in ShimmerBarWidthFractions.indices) {
                        val halfWidth = size.width * ShimmerBarWidthFractions[bar] / 2f
                        val top = size.height * ShimmerBarTopFractions[bar]
                        nativeCanvas.drawRoundRect(
                            centerX - halfWidth,
                            top,
                            centerX + halfWidth,
                            top + barHeight,
                            barRadius,
                            barRadius,
                            paint
                        )
                    }
                }
            }
        }
    )
}

private const val CONTENT_FADE_MILLIS = 250
private const val SHIMMER_SWEEP_MILLIS = 1200
private const val SHIMMER_BAR_HEIGHT_FRACTION = 0.067f
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.4f

/** Bar widths and vertical positions, as fractions of the triangle, narrowing with its shape. */
private val ShimmerBarWidthFractions = floatArrayOf(0.37f, 0.51f, 0.29f)
private val ShimmerBarTopFractions = floatArrayOf(0.23f, 0.38f, 0.53f)

private val ShimmerColors = intArrayOf(
    Color.White.copy(alpha = 0.2f).toArgb(),
    Color.White.copy(alpha = 0.5f).toArgb(),
    Color.White.copy(alpha = 0.2f).toArgb()
)

@Preview(group = "Components")
@Composable
private fun PredictionTrianglePreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PredictionTriangle(
            text = "Signs point to yes",
            modifier = Modifier.size(width = 136.dp, height = 120.dp)
        )
    }
}

@Preview(group = "Components")
@Composable
private fun PredictionTriangleLoadingPreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        PredictionTriangle(
            text = "",
            modifier = Modifier.size(width = 136.dp, height = 120.dp),
            isLoading = true
        )
    }
}
