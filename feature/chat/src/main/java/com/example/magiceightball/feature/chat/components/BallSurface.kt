package com.example.magiceightball.feature.chat.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The screen as the ball itself, with the phone's light: a black sphere lit from above and a
 * little to the left, one soft reflection near the top and a fine line of light along its edge.
 * Most of it stays black, as a watch's screen should.
 */
internal fun Modifier.ballSurface(): Modifier = drawWithCache {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val body = Brush.radialGradient(
        0f to BallColors.BodyLit,
        BODY_MID_STOP to BallColors.BodyMid,
        1f to BallColors.BodyEdge,
        center = Offset(center.x - radius * LIGHT_LEFT, center.y - radius * LIGHT_UP),
        radius = radius * LIGHT_REACH
    )
    // Up and to the left, where the light comes from, and clear of the time at the top.
    val glossCenter = Offset(center.x - radius * GLOSS_LEFT, center.y - radius * GLOSS_UP)
    val glossRadius = radius * GLOSS_RADIUS
    val gloss = Brush.radialGradient(
        0f to Color.White.copy(alpha = GLOSS_ALPHA),
        1f to Color.Transparent,
        center = glossCenter,
        radius = glossRadius
    )
    val edgeStroke = Stroke(width = radius * EDGE_WIDTH)
    val edge = Brush.verticalGradient(
        0f to Color.White.copy(alpha = EDGE_TOP_ALPHA),
        1f to Color.White.copy(alpha = EDGE_BOTTOM_ALPHA)
    )
    onDrawBehind {
        drawRect(BallColors.Background)
        drawCircle(body, radius, center)
        drawCircle(gloss, glossRadius, glossCenter)
        drawCircle(edge, radius - edgeStroke.width / 2f, center, style = edgeStroke)
    }
}

// The phone's ball, with the light pulled in and to the side: on a wrist the sphere fills the
// whole screen, and the top of it belongs to the time.
private const val BODY_MID_STOP = 0.45f
private const val LIGHT_LEFT = 0.38f
private const val LIGHT_UP = 0.42f
private const val LIGHT_REACH = 1.25f
private const val GLOSS_LEFT = 0.46f
private const val GLOSS_UP = 0.5f
private const val GLOSS_RADIUS = 0.3f
private const val GLOSS_ALPHA = 0.14f
private const val EDGE_WIDTH = 0.012f
private const val EDGE_TOP_ALPHA = 0.3f
private const val EDGE_BOTTOM_ALPHA = 0.06f
