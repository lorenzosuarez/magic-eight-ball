package com.example.magiceightball.feature.chat.components

import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * The triangular face of the die. While [isLoading] it is grey, with placeholder bars that a
 * highlight sweeps across; once the answer is in, the accent washes over it and [text] fades in.
 *
 * Both fills are built once per size; the change of colour only changes how much of each is
 * drawn, so it neither recomposes nor allocates.
 */
@Composable
fun PredictionTriangle(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val answered = animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(COLOUR_FADE_MILLIS),
        label = "PredictionTriangleColour"
    )

    Box(
        modifier = modifier.drawWithCache {
            if (size.isEmpty()) return@drawWithCache onDrawBehind {}

            val path = InvertedTriangleGeometry(size).toPath()

            onDrawBehind {
                val colour = answered.value
                if (colour < 1f) drawPath(path, BallColors.DieThinking, alpha = 1f - colour)
                if (colour > 0f) drawPath(path, BallColors.DieAnswered, alpha = colour)
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
                    color = BallColors.OnAccent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
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
private const val COLOUR_FADE_MILLIS = 450
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
