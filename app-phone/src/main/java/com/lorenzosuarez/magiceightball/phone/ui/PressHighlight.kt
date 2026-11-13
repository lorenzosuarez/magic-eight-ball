package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.annotation.DrawableRes
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.currentValueOf
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
    Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

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
        if (strength > 0f) drawRect(currentValueOf(LocalContentColor), alpha = PRESSED_ALPHA * strength)
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
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(ICON_BUTTON_SIZE)
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
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

/**
 * A button at the top of a screen: [AppIconButton] on a quiet disc, so that it reads as a
 * button with no word beside it. Every screen's top buttons are this one, each at the side
 * that what it leads to lies on.
 */
@Composable
internal fun BarIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    AppIconButton(
        icon = icon,
        description = description,
        onClick = onClick,
        tint = tint,
        enabled = enabled,
        modifier = modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA), CircleShape)
    )
}

/**
 * A top button that needs a word, as "Create" does: the disc of [BarIconButton] drawn out to
 * hold its name, so the two read as the same kind of thing.
 *
 * @param description what the button does when its name alone does not say it.
 */
@Composable
internal fun BarTextButton(
    @DrawableRes icon: Int,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false
) {
    Row(
        modifier = modifier
            .heightIn(min = ICON_BUTTON_SIZE)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA))
            .clickable(
                enabled = !busy,
                interactionSource = null,
                indication = PressHighlight,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = description }
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // While what it started is on its way, a ring turns where its mark is: it has heard.
        if (busy) {
            WaitingRing(color = MaterialTheme.colorScheme.onSurface)
        } else {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** How much of the ink the disc behind a top button, or behind a word, takes. */
internal const val BAR_DISC_ALPHA = 0.1f

/**
 * How long a choice takes to show where it was made, and to leave where it was, instead of
 * jumping. Short: it is the answer to a touch, and an answer that takes its time reads as a
 * screen that is slow.
 */
internal const val CHOICE_MS = 120

/**
 * How a choice arrives: at full speed from the frame of the touch, and settling. One pace for
 * every card, tile and mark that is chosen, so that they all answer alike.
 */
internal fun <T> choiceTween(): TweenSpec<T> = tween(CHOICE_MS, easing = LinearOutSlowInEasing)

/** How long a row takes to bring what was chosen into the middle of it. */
internal const val ROW_GLIDE_MS = 220
