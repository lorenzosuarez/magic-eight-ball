package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * What a touch looks like everywhere in the app: the touched thing lights up at once and the
 * light fades when the finger leaves.
 *
 * It stands in for the ripple, which is an animation and so shows nothing at all on a device
 * whose animations are turned off; this follows the display's frames instead, so a touch
 * always answers. It fills the bounds of what it is applied to: clip those to give it a shape.
 */
internal object PressHighlight : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressHighlightNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = javaClass.hashCode()
}

private class PressHighlightNode(private val interactionSource: InteractionSource) :
    Modifier.Node(), DrawModifierNode {

    private var strength = 0f

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> show(1f)
                    is PressInteraction.Release, is PressInteraction.Cancel -> fade()
                }
            }
        }
    }

    private fun show(value: Float) {
        strength = value
        invalidateDraw()
    }

    /** Lingers long enough for the quickest tap to be seen, then fades, frame by frame. */
    private suspend fun fade() {
        val start = withFrameNanos { it }
        while (strength > 0f) {
            withFrameNanos { now ->
                val sinceRelease = (now - start) / NANOS_PER_MILLI - LINGER_MS
                show((1f - sinceRelease.toFloat() / FADE_MS).coerceIn(0f, 1f))
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (strength > 0f) drawRect(Color.White, alpha = PRESSED_ALPHA * strength)
        drawContent()
    }

    private companion object {
        const val PRESSED_ALPHA = 0.14f
        const val LINGER_MS = 90L
        const val FADE_MS = 220L
        const val NANOS_PER_MILLI = 1_000_000L
    }
}

/**
 * The app's icon button: a round, 48dp target with [PressHighlight] for its touch.
 *
 * @param description what the button does, for accessibility.
 */
@Composable
internal fun AppIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .size(ICON_BUTTON_SIZE)
            .clip(CircleShape)
            .clickable(
                interactionSource = null,
                indication = PressHighlight,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(icon), contentDescription = description, tint = tint)
    }
}

private val ICON_BUTTON_SIZE = 48.dp
