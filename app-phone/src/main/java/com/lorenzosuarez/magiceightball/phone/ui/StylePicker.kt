package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.animation.Crossfade
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.config.AppConfig

/** The styles that belong together, under the name of what they share. */
internal class StyleGroup(@StringRes val title: Int, val styles: List<Magic8BallPersonality>)

/** Every style, once, in the order the sheet of styles shows them: a group to a line. */
internal val StyleGroups = listOf(
    StyleGroup(
        R.string.styles_group_classics,
        listOf(
            Magic8BallPersonality.SARCASTIC,
            Magic8BallPersonality.MYSTICAL,
            Magic8BallPersonality.ASTRO,
            Magic8BallPersonality.NEUTRAL,
            Magic8BallPersonality.PHILOSOPHER
        )
    ),
    StyleGroup(
        R.string.styles_group_heart,
        listOf(
            Magic8BallPersonality.LOVESTRUCK,
            Magic8BallPersonality.GRANDMA,
            Magic8BallPersonality.THERAPIST,
            Magic8BallPersonality.DRAMA,
            Magic8BallPersonality.POET
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
 * The styles the options show without asking: [featured], led by [selected] when it is not one
 * of them, so the choice is always in sight. The plain style is always among them, whatever was
 * set from afar: whoever tires of a character should not have to look for the way back.
 */
internal fun stylesInRow(
    selected: Magic8BallPersonality,
    featured: List<Magic8BallPersonality> = AppConfig.DEFAULT_FEATURED,
    hidden: Set<Magic8BallPersonality> = emptySet()
): List<Magic8BallPersonality> {
    val shown = (featured - hidden).let { if (Magic8BallPersonality.NEUTRAL in it) it else it + Magic8BallPersonality.NEUTRAL }
    if (selected in shown) return shown
    // The chosen one takes the place of the last of the others, never of the plain style.
    val making = shown.lastOrNull { it != Magic8BallPersonality.NEUTRAL }
    return listOf(selected) + if (shown.size > 1 && making != null) shown - making else shown
}

/** The groups without the styles taken out from afar; the one in use stays, wherever it is. */
internal fun styleGroups(
    selected: Magic8BallPersonality,
    hidden: Set<Magic8BallPersonality>
): List<StyleGroup> = StyleGroups
    .map { group -> StyleGroup(group.title, group.styles.filter { it == selected || it !in hidden }) }
    .filter { it.styles.isNotEmpty() }

/**
 * The style of the answers, as a row of the options: a few styles side by side, each with a line
 * in its own voice, and after them the way to all the rest.
 */
@Composable
internal fun StyleRow(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onSeeAll: () -> Unit,
    featured: List<Magic8BallPersonality> = AppConfig.DEFAULT_FEATURED,
    hidden: Set<Magic8BallPersonality> = emptySet(),
    custom: CustomCharacter? = null
) {
    val scroll = rememberScrollState()
    // Where each card starts and how wide it is, within the row.
    val spans = remember { mutableStateMapOf<Magic8BallPersonality, IntRange>() }
    var shown by remember { mutableStateOf(false) }
    // What was just touched is shown as chosen in the frame of the touch: saving a choice and
    // hearing it back takes a moment, and a card that waited for it would answer late. It
    // follows what is saved whenever that changes, from here or from anywhere else.
    var touched by remember(selected) { mutableStateOf(selected) }
    // The chosen style is brought to the middle of the row, with its neighbours on either side:
    // at once when the row first shows, so it is simply there, and gliding after every choice,
    // so a card touched at the edge comes fully into view.
    val span = spans[touched]
    LaunchedEffect(touched, span, scroll.viewportSize) {
        if (span == null || scroll.viewportSize == 0) return@LaunchedEffect
        val middle = (span.first + span.last) / 2 - scroll.viewportSize / 2
        val target = middle.coerceIn(0, scroll.maxValue)
        if (shown) scroll.animateScrollTo(target, tween(ROW_GLIDE_MS)) else scroll.scrollTo(target)
        shown = true
    }
    val edge = with(LocalDensity.current) { ROW_EDGE.roundToPx() }
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
                .fadingRow(scroll)
                .padding(horizontal = ROW_EDGE)
                .height(IntrinsicSize.Min)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stylesInRow(selected, featured, hidden).forEach { style ->
                // One of the user's own, when it is the one chosen, under the name they gave it.
                val own = custom.takeIf { style == Magic8BallPersonality.CUSTOM }
                StyleCard(
                    title = own?.name ?: stringResource(style.labelRes()),
                    // What the user wrote of it is theirs and long: a line of the app's says whose words it speaks in.
                    sample = stringResource(style.sampleRes()),
                    colour = own?.colour ?: style,
                    emblem = own?.emblem ?: style,
                    chosen = style == touched,
                    onClick = {
                        touched = style
                        onSelected(style)
                    },
                    // As wide as what it says: in a row that scrolls, nothing has to be cut short.
                    modifier = Modifier
                        .widthIn(min = STYLE_CARD_MIN_WIDTH)
                        .fillMaxHeight()
                        .onPlaced { placed ->
                            val start = placed.positionInParent().x.roundToInt() + edge
                            spans[style] = start..start + placed.size.width
                        }
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.hairline)
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

/**
 * Every style, in a sheet that comes up from the bottom over half the screen and, pulled or
 * scrolled up, becomes the whole screen: its corners square off and its handle goes, since there
 * is nowhere further to pull it. Pulling it down, the close button, the back gesture and a touch
 * outside all put it away; so does choosing a style, once the choice has been seen to take.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StylesSheet(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onClose: () -> Unit,
    hidden: Set<Magic8BallPersonality> = emptySet(),
    onCreate: (() -> Unit)? = null,
    yours: List<CustomCharacter> = emptyList(),
    chosenOfYours: String? = null,
    onYoursSelected: (CustomCharacter) -> Unit = {},
    onEditYours: ((CustomCharacter) -> Unit)? = {},
    yoursPlaces: Int = 0,
    creating: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val full = sheetState.targetValue == SheetValue.Expanded
    val corner by animateDpAsState(if (full) 0.dp else SHEET_CORNER, label = "sheet corner")
    val putAway: () -> Unit = { scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() } }
    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = corner, topEnd = corner),
        containerColor = MaterialTheme.colorScheme.sheetGround,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        // The sheet draws its own handle and keeps clear of the system bars itself: see [StylesContent].
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        InAppLanguage(language) {
            StylesContent(
                selected = selected,
                onSelected = {
                    onSelected(it)
                    putAway()
                },
                onClose = putAway,
                full = full,
                hidden = hidden,
                onCreate = onCreate,
                yoursPlaces = yoursPlaces,
                creating = creating,
                yours = yours,
                chosenOfYours = chosenOfYours,
                onYoursSelected = {
                    onYoursSelected(it)
                    putAway()
                },
                onEditYours = onEditYours
            )
        }
    }
}

/**
 * What [StylesSheet] shows: the way out, which stays put, and under it the title and the styles.
 * Each group is a line of its own that scrolls sideways, its cards all one width, so the one cut
 * off at the edge says there are more; the page itself only scrolls up and down.
 *
 * @param full whether the sheet takes the whole screen: the header then clears the status bar.
 * @param onCreate the way to make a character of one's own; null where that is not offered, and
 * then nothing is shown for it.
 * @param yours the characters the user made up, shown first, each with the way to correct it
 * where [onEditYours] is given: where they are only chosen among, it is null.
 * @param chosenOfYours the slot of the one that answers, when one of them does.
 * @param yoursPlaces how many places to keep for the user's own while they are on their way:
 * as many as there were, or none when there is no reason to think there are any.
 * @param creating while "Create" waits for the account to be heard of: it says it heard.
 */
@Composable
internal fun StylesContent(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    full: Boolean = false,
    hidden: Set<Magic8BallPersonality> = emptySet(),
    onCreate: (() -> Unit)? = null,
    yours: List<CustomCharacter> = emptyList(),
    chosenOfYours: String? = null,
    onYoursSelected: (CustomCharacter) -> Unit = {},
    onEditYours: ((CustomCharacter) -> Unit)? = {},
    yoursPlaces: Int = 0,
    creating: Boolean = false
) {
    // As in the row on the options page: what was just touched is chosen in the frame of the
    // touch, and follows what is saved whenever that changes. One of the user's own is told
    // from the others by its slot.
    var touched by remember(selected, chosenOfYours) {
        mutableStateOf(selected to chosenOfYours.takeIf { selected == Magic8BallPersonality.CUSTOM })
    }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTop by animateDpAsState(if (full) statusBar else 0.dp, label = "sheet header")
    val handleAlpha by animateFloatAsState(if (full) 0f else 1f, label = "sheet handle")
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    // A card and most of the next across the sheet, whatever its width: the one that is cut
    // off, fading at the edge, is what says the row goes on. Wider with larger text, and never
    // so wide that nothing of the next shows.
    val across = LocalConfiguration.current.screenWidthDp.dp.coerceAtMost(SHEET_WIDEST)
    val cardWidth = ((across * STYLE_CARD_SHARE).coerceIn(STYLE_CARD_WIDTH, STYLE_CARD_WIDEST) *
        density.fontScale.coerceIn(1f, MAX_CARD_GROWTH)).coerceAtMost(across - STYLE_CARD_PEEK)
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(top = headerTop + 8.dp)
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .alpha(handleAlpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = HANDLE_ALPHA))
        )
        // The way out at one end and, where characters can be made, the way to make one at the other.
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            BarIconButton(
                icon = R.drawable.ic_close,
                description = stringResource(R.string.action_close),
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            if (onCreate != null) {
                BarTextButton(
                    icon = R.drawable.ic_add,
                    label = stringResource(R.string.action_create),
                    description = stringResource(R.string.characters_create),
                    onClick = onCreate,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    busy = creating
                )
            }
        }
        // Only once something has scrolled under the header is there an edge worth drawing.
        HorizontalDivider(
            thickness = 1.dp,
            color = if (scroll.value > 0) MaterialTheme.colorScheme.hairline else Color.Transparent
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .selectableGroup()
        ) {
            Text(
                text = stringResource(R.string.styles_title),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.styles_subtitle),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, end = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (yours.isEmpty() && yoursPlaces > 0) {
                val waiting = stringResource(R.string.characters_loading)
                Text(
                    text = stringResource(R.string.styles_group_yours),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                        .semantics { heading() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Their places kept, in the shape of the cards that are coming: said once, as one thing.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadingRow(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .clearAndSetSemantics { contentDescription = waiting },
                    horizontalArrangement = Arrangement.spacedBy(STYLE_CARD_GAP)
                ) {
                    repeat(yoursPlaces) { StyleCardPlace(Modifier.width(cardWidth)) }
                }
            }
            if (yours.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.styles_group_yours),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                        .semantics { heading() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // One row, of cards as wide as the app's own: there are five at most.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadingRow(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(STYLE_CARD_GAP)
                ) {
                    yours.forEach { character ->
                        StyleCard(
                            title = character.name,
                            sample = stringResource(R.string.style_custom_sample),
                            colour = character.colour,
                            emblem = character.emblem,
                            chosen = touched == Magic8BallPersonality.CUSTOM to character.id,
                            onClick = {
                                touched = Magic8BallPersonality.CUSTOM to character.id
                                onYoursSelected(character)
                            },
                            modifier = Modifier.width(cardWidth).fillMaxHeight(),
                            onEdit = onEditYours?.let { edit -> { edit(character) } }
                        )
                    }
                }
            }
            styleGroups(selected, hidden).forEach { group ->
                Text(
                    text = stringResource(group.title),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                        .semantics { heading() },
                    // Named as the subjects on the options page are: one way of heading a group.
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Two rows to a group, so that most of it shows at once: the group opens on the
                // chosen style when it holds it, with the column before in sight.
                val columns = group.styles.chunked(STYLE_ROWS)
                val lead = (group.styles.indexOf(selected) / STYLE_ROWS - 1).coerceAtLeast(0)
                val start = with(density) { ((cardWidth + STYLE_CARD_GAP) * lead - ROW_EDGE).roundToPx().coerceAtLeast(0) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fadingRow(rememberScrollState(start))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(STYLE_CARD_GAP)
                ) {
                    columns.forEach { column ->
                        Column(
                            modifier = Modifier.width(cardWidth),
                            verticalArrangement = Arrangement.spacedBy(STYLE_CARD_GAP)
                        ) {
                            column.forEach { style ->
                                StyleCard(
                                    title = stringResource(style.labelRes()),
                                    sample = stringResource(style.sampleRes()),
                                    colour = style,
                                    emblem = style,
                                    chosen = touched == style to null,
                                    onClick = {
                                        touched = style to null
                                        onSelected(style)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The place of a card that is not known yet: a card's own shape, as tall as a card is at this
 * type size, with a bar where its face will be, one where its name will and one where its line will.
 */
@Composable
private fun StyleCardPlace(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA))
            .padding(start = CARD_INSET, top = CARD_INSET, end = CARD_END, bottom = CARD_INSET),
        horizontalArrangement = Arrangement.spacedBy(CARD_INSET),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Where the face will be, in the shape it will have.
        Box(
            modifier = Modifier
                .size(CARD_FACE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = PLACE_FACE_ALPHA))
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Each line holds its room with words that are not drawn, and its bar lies along it.
            listOf(MaterialTheme.typography.titleSmall to PLACE_NAME_WIDTH, MaterialTheme.typography.bodySmall to PLACE_LINE_WIDTH)
                .forEach { (style, width) ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(text = " ", style = style, maxLines = 1)
                        Box(modifier = Modifier.fillMaxWidth(width).height(PLACE_BAR_HEIGHT).waitingBar())
                    }
                }
        }
    }
}

/**
 * A character: its face, its name, and something it would say.
 *
 * The card is the app's own, as every other choice is: a quiet tile, and the chosen one filled
 * with the text's colour. A character's colour is kept to its face, which is small, and is
 * muted there for as long as the character is one among many: with twenty of them side by side,
 * twenty colours at full strength would be all anyone saw. The one that answers has its colour
 * whole, so the eye finds it by that as well as by the card.
 *
 * A name over one line: cards side by side then have their names on one line and their sayings
 * on the next. The sayings are picked short enough to fit, see the `style_*_sample` strings; one
 * that does not is cut rather than let it push the card wider.
 *
 * @param colour whose colour the face has, and [emblem] what its ball wears: for one of the
 * app's own both are the character itself, and for one the user made, what they picked.
 */
@Composable
private fun StyleCard(
    title: String,
    sample: String,
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    // The choice arrives on one card as it leaves another: ground and ink pass into each other,
    // the face takes its colour, and nothing of either card moves.
    val ground by animateColorAsState(
        targetValue = if (chosen) scheme.onSurface else scheme.onSurface.copy(alpha = TILE_ALPHA),
        animationSpec = choiceTween(),
        label = "card ground"
    )
    val ink by animateColorAsState(
        targetValue = if (chosen) scheme.surface else scheme.onSurface,
        animationSpec = choiceTween(),
        label = "card ink"
    )
    val arrived by animateFloatAsState(if (chosen) 1f else 0f, choiceTween(), label = "card chosen")
    // The light of a touch is the ink's: on the chosen card the page's own would not show.
    CompositionLocalProvider(LocalContentColor provides ink) {
        Box(
            modifier = modifier
                .clip(CardShape)
                .background(ground)
                .selectable(selected = chosen, role = Role.RadioButton, onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(start = CARD_INSET, top = CARD_INSET, end = CARD_END, bottom = CARD_INSET),
                horizontalArrangement = Arrangement.spacedBy(CARD_GAP),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterFace(colour = colour, emblem = emblem, size = CARD_FACE, vivid = { arrived })
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sample,
                        // Clear of the pencil, where there is one.
                        modifier = Modifier.padding(end = if (onEdit == null) 0.dp else EDIT_GLYPH + 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = ink.copy(alpha = SAMPLE_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // On a character of the user's own: the way to correct it, a touch of its own, at the
            // end of the second line. The card stays as wide and as tall as the app's.
            if (onEdit != null) {
                val edit = stringResource(R.string.character_edit, title)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(EDIT_TARGET)
                        .clip(CircleShape)
                        // Named once, by what it says it is: said again as what a touch does, it is heard twice.
                        .clickable(role = Role.Button, onClick = onEdit)
                        .semantics { contentDescription = edit },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = null,
                        modifier = Modifier.size(EDIT_GLYPH),
                        tint = ink
                    )
                }
            }
        }
    }
}

/**
 * The face of a character wherever it is shown small: the ball wearing what is theirs, on their
 * colour, in a round. It is the drawing the shared card signs with, so a character looks the
 * same on a card to choose, on the ball's page and in a chat.
 *
 * @param vivid how much of its colour the face has, from 0, muted as one among many, to 1, the
 * colour whole: read while drawing, so a face can take its colour without anything composing again.
 */
@Composable
internal fun CharacterFace(
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    size: Dp,
    modifier: Modifier = Modifier,
    vivid: () -> Float = { 1f }
) {
    val whole = shareColor(colour)
    val muted = remember(whole) { lerp(whole, FACE_NEUTRAL, FACE_MUTING) }
    // A line round it, barely there: the palest of the colours has an edge on a pale card too.
    val edge = ShareInk.copy(alpha = FACE_EDGE_ALPHA)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .drawBehind { drawRect(lerp(muted, whole, vivid())) }
            .border(1.dp, edge, CircleShape)
    ) {
        CharacterBall(emblem = emblem, size = size)
    }
}

/**
 * Who answers now, under the ball: a disc as the buttons at the top of the page are, a size up
 * since it is the one thing to do down here, with the face of who answers in it and under it
 * their name, with the mark of something that opens. One touch leads to everyone else. When who
 * answers changes, the one face passes into the other.
 *
 * @param named whether the name is written under the disc: beside another button it is not,
 * and is then only said to whoever cannot see the face.
 */
@Composable
internal fun CharacterButton(
    name: String,
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    named: Boolean = true
) {
    val said = stringResource(R.string.character_now, name)
    val touch = remember { MutableInteractionSource() }
    val disc = if (named) WHO_DISC else WHO_DISC_BESIDE
    Column(
        // One thing to touch and one thing said: the disc lights up, and the name is its name.
        modifier = modifier
            .clickable(
                interactionSource = touch,
                indication = null,
                onClickLabel = stringResource(R.string.styles_subtitle),
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) { contentDescription = said },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(disc)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA))
                .indication(touch, PressHighlight),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = colour to emblem, animationSpec = choiceTween(), label = "who answers") { (colour, emblem) ->
                CharacterFace(colour = colour, emblem = emblem, size = disc * WHO_FACE_SHARE)
            }
        }
        if (named) {
            Row(
                modifier = Modifier.clearAndSetSemantics {},
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    modifier = Modifier.widthIn(max = WHO_NAME_WIDEST),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

private val STYLE_CARD_MIN_WIDTH = 168.dp
private val ROW_EDGE = 16.dp

/** How much quieter than its name a style's line is. */
private const val SAMPLE_ALPHA = 0.66f

/** Material's corner for a sheet that is not yet the whole screen. */
private val SHEET_CORNER = 28.dp
private const val HANDLE_ALPHA = 0.3f

/** Two cards and the start of a third across a phone: what tells that a line goes on. */
private val STYLE_CARD_WIDTH = 196.dp

/** How much of the sheet's width a card takes, the widest it gets, and the least of the next one that shows. */
private const val STYLE_CARD_SHARE = 0.5f
private val STYLE_CARD_WIDEST = 260.dp
private val STYLE_CARD_PEEK = 88.dp

/** As wide as a sheet gets on a large screen. */
private val SHEET_WIDEST = 640.dp
private val STYLE_CARD_GAP = 8.dp

/** The bars on the place kept for a card: how thick, and how much of the card's width its name and its line take. */
private val PLACE_BAR_HEIGHT = 8.dp
private const val PLACE_NAME_WIDTH = 0.55f
private const val PLACE_LINE_WIDTH = 0.8f
private const val STYLE_ROWS = 2

/** The pencil on a card of the user's own, and the room round it that takes the touch. */
private val EDIT_GLYPH = 18.dp
private val EDIT_TARGET = 48.dp
private const val MAX_CARD_GROWTH = 1.5f

/** The face on a card, the room between it and the card's edge and its words, and the room after the words. */
private val CARD_FACE = 40.dp
private val CARD_INSET = 8.dp
private val CARD_GAP = 10.dp
private val CARD_END = 14.dp

/** Who answers, under the ball: the disc, how much of it the face takes, and the longest name shown whole. */
private val WHO_DISC = 56.dp
private val WHO_DISC_BESIDE = 48.dp
private const val WHO_FACE_SHARE = 0.68f
private val WHO_NAME_WIDEST = 200.dp

/** How strong the place of a face that is still on its way. */
private const val PLACE_FACE_ALPHA = 0.14f

/**
 * What a face's colour is drawn towards while it is one among many, and how far: a cool grey
 * of the colours' own lightness, so the ball on it reads as before and only the colour is less.
 */
private val FACE_NEUTRAL = Color(0xFFC6CCD4)
private const val FACE_MUTING = 0.55f

/** The line round a face. */
private const val FACE_EDGE_ALPHA = 0.14f
