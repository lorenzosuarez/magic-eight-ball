package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.animation.Crossfade
import androidx.compose.ui.unit.Dp
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.R

/** The styles that belong together, under the name of what they share. */
internal class StyleGroup(@StringRes val title: Int, val styles: List<Magic8BallPersonality>)

/** Every style, once, in the order they are listed where they are chosen among, group by group. */
internal val StyleGroups = listOf(
    StyleGroup(
        R.string.styles_group_classics,
        listOf(
            Magic8BallPersonality.SARCASTIC,
            Magic8BallPersonality.MYSTICAL,
            Magic8BallPersonality.ASTRO,
            Magic8BallPersonality.NEUTRAL,
            Magic8BallPersonality.PHILOSOPHER,
            // With the thinkers: groups of an even number leave no card alone on its line.
            Magic8BallPersonality.POET
        )
    ),
    StyleGroup(
        R.string.styles_group_heart,
        listOf(
            Magic8BallPersonality.LOVESTRUCK,
            Magic8BallPersonality.GRANDMA,
            Magic8BallPersonality.THERAPIST,
            Magic8BallPersonality.DRAMA
        )
    ),
    StyleGroup(
        R.string.styles_group_movies,
        listOf(
            Magic8BallPersonality.PIRATE,
            Magic8BallPersonality.DETECTIVE,
            Magic8BallPersonality.VILLAIN,
            Magic8BallPersonality.ROBOT
        )
    ),
    StyleGroup(
        R.string.styles_group_attitude,
        listOf(
            Magic8BallPersonality.COACH,
            Magic8BallPersonality.ZOOMER,
            Magic8BallPersonality.CAT,
            Magic8BallPersonality.DOG,
            Magic8BallPersonality.CONSPIRACY
        )
    )
)

/**
 * The face of a character wherever it is shown small: the ball wearing what is theirs, on their
 * colour, in a round. It is the shared card's own drawing and it is seen as the card shows it,
 * close: the ball runs off the lower corner and what it wears comes to the middle, so that at
 * this size it is a mark, and what is worn is what tells one from another.
 *
 * The colour is muted, towards a grey of the theme: that much while the character is one among
 * many, and less on the one that is chosen or that answers.
 *
 * @param chosen from 0, one among many, to 1, the one: read while drawing, so a face can take
 * its colour without anything composing again.
 * @param ground how much of the colour's round there is, from nothing to all of it: less than
 * all lets what the face stands on show through, where the face is to be quieter still.
 */
@Composable
internal fun CharacterFace(
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    size: Dp,
    modifier: Modifier = Modifier,
    chosen: () -> Float = { 1f },
    ground: Float = 1f
) {
    val dark = MaterialTheme.colorScheme.isDark
    val whole = shareColor(colour)
    val neutral = if (dark) FACE_NEUTRAL_DARK else FACE_NEUTRAL_LIGHT
    // On a pale ground the palest of the colours needs an edge; on a dark one none does.
    val edge = if (dark) Color.Transparent else ShareInk.copy(alpha = FACE_EDGE_ALPHA)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .drawBehind {
                drawRect(lerp(whole, neutral, FACE_MUTING + (FACE_MUTING_CHOSEN - FACE_MUTING) * chosen()), alpha = ground)
            }
            .border(1.dp, edge, CircleShape)
    ) {
        CharacterBall(emblem = emblem, size = size, share = FACE_BALL, right = FACE_BALL_RIGHT, down = FACE_BALL_DOWN)
    }
}

/**
 * Who answers now, under the ball: a chip, their face and their name side by side with the mark
 * of something that opens, on a ground that is barely there. It says who it is and that it can
 * be changed, and no more: the page is the ball's. One touch leads to everyone, to choose among
 * and to make one's own. When who answers changes, the one passes into the other.
 *
 * @param named whether the name is written beside the face: beside another button it is not,
 * and is then only said to whoever cannot see the face.
 */
@Composable
internal fun CharacterChip(
    name: String,
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    named: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    val said = stringResource(R.string.character_now, name)
    // Beside a button it is that button's height and ground: the two are one row of things to touch.
    // Alone, the faintest that still has an edge, which on a light page takes a little more.
    val ground = when {
        !named -> BAR_DISC_ALPHA
        scheme.isDark -> CHIP_GROUND_ALPHA
        else -> CHIP_GROUND_ALPHA_LIGHT
    }
    Row(
        modifier = modifier
            // The room of a touch whatever the pill's height: it rests at one height in every state.
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(scheme.onSurface.copy(alpha = ground))
            .clickable(onClickLabel = stringResource(R.string.styles_subtitle), role = Role.Button, onClick = onClick)
            // Said once, as a sentence: the name alone would not say what it is the name of.
            .semantics(mergeDescendants = true) { contentDescription = said }
            // At least: a name in large type makes the pill taller rather than be cut.
            .heightIn(min = if (named) CHIP_HEIGHT else CHIP_HEIGHT_BESIDE)
            .padding(start = CHIP_INSET, end = CHIP_END),
        // The mark belongs to the name: nearer to it than the name is to the face.
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(targetState = Triple(name.takeIf { named }, colour, emblem), animationSpec = choiceTween(), label = "who answers") { (name, colour, emblem) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // As quiet as a face gets: one among many, and a little of the page through it.
                CharacterFace(colour = colour, emblem = emblem, size = CHIP_HEIGHT - CHIP_INSET * 2, chosen = { 0f }, ground = CHIP_FACE_GROUND)
                if (name != null) {
                    Text(
                        text = name,
                        modifier = Modifier.widthIn(max = CHIP_NAME_WIDEST).clearAndSetSemantics {},
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_expand_more),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = scheme.onSurfaceVariant
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
    Magic8BallPersonality.ASTRO -> R.string.style_astro
    Magic8BallPersonality.THERAPIST -> R.string.style_therapist
    Magic8BallPersonality.DOG -> R.string.style_dog
    // What one of the user's own is called where its own name is not known.
    Magic8BallPersonality.CUSTOM -> R.string.style_custom
}

/** Something the style would say, to tell it from the others at a glance. */
@StringRes
internal fun Magic8BallPersonality.sampleRes(): Int = when (this) {
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
    Magic8BallPersonality.ASTRO -> R.string.style_astro_sample
    Magic8BallPersonality.THERAPIST -> R.string.style_therapist_sample
    Magic8BallPersonality.DOG -> R.string.style_dog_sample
    Magic8BallPersonality.CUSTOM -> R.string.style_custom_sample
}

/**
 * Who answers, under the ball: how tall the chip is, the room round its face and after its mark,
 * the longest name it shows whole, how little of the text's colour its ground has, and how much
 * of its colour's round the face keeps.
 */
private val CHIP_HEIGHT = 44.dp
private val CHIP_HEIGHT_BESIDE = 48.dp
private val CHIP_INSET = 6.dp
private val CHIP_END = 12.dp
private val CHIP_NAME_WIDEST = 180.dp
private const val CHIP_GROUND_ALPHA = 0.06f
private const val CHIP_GROUND_ALPHA_LIGHT = 0.08f
private const val CHIP_FACE_GROUND = 0.8f

/**
 * What a face's colour is drawn towards, and how far: a cool grey that the ball still reads on,
 * a step darker in the dark where a pale round would glare. That far while the character is one
 * among many, which evens out colours that are not all as strong, and less far on the one.
 */
private val FACE_NEUTRAL_LIGHT = Color(0xFFC6CCD4)
private val FACE_NEUTRAL_DARK = Color(0xFFAAB2BD)
private const val FACE_MUTING = 0.7f
private const val FACE_MUTING_CHOSEN = 0.3f

/** How the ball stands in a face: how much of it the ball takes, and how far its middle is to the right and down, in its own widths. */
private const val FACE_BALL = 0.8f
private const val FACE_BALL_RIGHT = 0.1f
private const val FACE_BALL_DOWN = 0.275f

/** The line round a face. */
private const val FACE_EDGE_ALPHA = 0.14f
