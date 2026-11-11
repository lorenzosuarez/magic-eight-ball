package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.R

/** The styles that belong together, under the name of what they share. */
internal class StyleGroup(@StringRes val title: Int, val styles: List<Magic8BallPersonality>)

/** Every style, once, in the order the page of styles shows them. */
internal val StyleGroups = listOf(
    StyleGroup(
        R.string.styles_group_classics,
        listOf(Magic8BallPersonality.SARCASTIC, Magic8BallPersonality.MYSTICAL, Magic8BallPersonality.NEUTRAL)
    ),
    StyleGroup(
        R.string.styles_group_heart,
        listOf(
            Magic8BallPersonality.LOVESTRUCK,
            Magic8BallPersonality.GRANDMA,
            Magic8BallPersonality.DRAMA,
            Magic8BallPersonality.POET
        )
    ),
    StyleGroup(
        R.string.styles_group_movies,
        listOf(Magic8BallPersonality.PIRATE, Magic8BallPersonality.DETECTIVE, Magic8BallPersonality.VILLAIN)
    ),
    StyleGroup(
        R.string.styles_group_minds,
        listOf(Magic8BallPersonality.PHILOSOPHER, Magic8BallPersonality.ROBOT, Magic8BallPersonality.CONSPIRACY)
    ),
    StyleGroup(
        R.string.styles_group_attitude,
        listOf(Magic8BallPersonality.COACH, Magic8BallPersonality.ZOOMER, Magic8BallPersonality.CAT)
    )
)

/** The few styles the options show without asking: one from each group, the liveliest first. */
private val FeaturedStyles = listOf(
    Magic8BallPersonality.SARCASTIC,
    Magic8BallPersonality.LOVESTRUCK,
    Magic8BallPersonality.CAT,
    Magic8BallPersonality.DRAMA,
    Magic8BallPersonality.ROBOT
)

/** The featured styles, led by [selected] when it is not one of them, so the choice is always in sight. */
internal fun stylesInRow(selected: Magic8BallPersonality): List<Magic8BallPersonality> =
    if (selected in FeaturedStyles) FeaturedStyles else listOf(selected) + FeaturedStyles.dropLast(1)

/**
 * The style of the answers, as a row of the options: a few styles side by side, each with a line
 * in its own voice, and after them the way to all the rest.
 */
@Composable
internal fun StyleRow(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.settings_style),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stylesInRow(selected).forEach { style ->
                StyleCard(
                    style = style,
                    chosen = style == selected,
                    onClick = { onSelected(style) },
                    // As wide as what it says: in a row that scrolls, nothing has to be cut short.
                    modifier = Modifier.widthIn(min = STYLE_CARD_MIN_WIDTH).fillMaxHeight()
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(ButtonShape)
                    .clickable(role = Role.Button, onClick = onSeeAll)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.styles_see_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Every style, over the whole screen. Choosing one closes it: there is nothing else to do here. */
@Composable
internal fun StylesDialog(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        StylesContent(selected = selected, onSelected = onSelected, onClose = onClose)
    }
}

/** What [StylesDialog] shows: the styles by group, two to a line. */
@Composable
internal fun StylesContent(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 32.dp)
                .selectableGroup()
        ) {
            Text(
                text = stringResource(R.string.styles_title),
                modifier = Modifier.fillMaxWidth().semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.styles_subtitle),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            StyleGroups.forEach { group ->
                Text(
                    text = stringResource(group.title),
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.styles.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { style ->
                                StyleCard(
                                    style = style,
                                    chosen = style == selected,
                                    onClick = { onSelected(style) },
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        AppIconButton(
            icon = R.drawable.ic_close,
            description = stringResource(R.string.action_close),
            onClick = onClose,
            modifier = Modifier.padding(4.dp)
        )
    }
}

/** A style: its name, and something it would say. The chosen one is filled with the text's own colour. */
@Composable
private fun StyleCard(
    style: Magic8BallPersonality,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ink = if (chosen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier
            .clip(ButtonShape)
            .background(
                if (chosen) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA)
                }
            )
            .selectable(selected = chosen, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(style.labelRes()),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(style.sampleRes()),
            style = MaterialTheme.typography.bodySmall,
            color = ink.copy(alpha = SAMPLE_ALPHA),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@StringRes
internal fun Magic8BallPersonality.labelRes(): Int = when (this) {
    Magic8BallPersonality.SARCASTIC -> R.string.style_sarcastic
    Magic8BallPersonality.MYSTICAL -> R.string.style_mystical
    Magic8BallPersonality.NEUTRAL -> R.string.style_neutral
    Magic8BallPersonality.GRANDMA -> R.string.style_grandma
    Magic8BallPersonality.PIRATE -> R.string.style_pirate
    Magic8BallPersonality.PHILOSOPHER -> R.string.style_philosopher
    Magic8BallPersonality.COACH -> R.string.style_coach
    Magic8BallPersonality.LOVESTRUCK -> R.string.style_lovestruck
    Magic8BallPersonality.DRAMA -> R.string.style_drama
    Magic8BallPersonality.ROBOT -> R.string.style_robot
    Magic8BallPersonality.DETECTIVE -> R.string.style_detective
    Magic8BallPersonality.CONSPIRACY -> R.string.style_conspiracy
    Magic8BallPersonality.CAT -> R.string.style_cat
    Magic8BallPersonality.ZOOMER -> R.string.style_zoomer
    Magic8BallPersonality.VILLAIN -> R.string.style_villain
    Magic8BallPersonality.POET -> R.string.style_poet
}

/** Something the style would say, to tell it from the others at a glance. */
@StringRes
private fun Magic8BallPersonality.sampleRes(): Int = when (this) {
    Magic8BallPersonality.SARCASTIC -> R.string.style_sarcastic_sample
    Magic8BallPersonality.MYSTICAL -> R.string.style_mystical_sample
    Magic8BallPersonality.NEUTRAL -> R.string.style_neutral_sample
    Magic8BallPersonality.GRANDMA -> R.string.style_grandma_sample
    Magic8BallPersonality.PIRATE -> R.string.style_pirate_sample
    Magic8BallPersonality.PHILOSOPHER -> R.string.style_philosopher_sample
    Magic8BallPersonality.COACH -> R.string.style_coach_sample
    Magic8BallPersonality.LOVESTRUCK -> R.string.style_lovestruck_sample
    Magic8BallPersonality.DRAMA -> R.string.style_drama_sample
    Magic8BallPersonality.ROBOT -> R.string.style_robot_sample
    Magic8BallPersonality.DETECTIVE -> R.string.style_detective_sample
    Magic8BallPersonality.CONSPIRACY -> R.string.style_conspiracy_sample
    Magic8BallPersonality.CAT -> R.string.style_cat_sample
    Magic8BallPersonality.ZOOMER -> R.string.style_zoomer_sample
    Magic8BallPersonality.VILLAIN -> R.string.style_villain_sample
    Magic8BallPersonality.POET -> R.string.style_poet_sample
}

private val STYLE_CARD_MIN_WIDTH = 132.dp

/** How much quieter than its name a style's line is. */
private const val SAMPLE_ALPHA = 0.66f
