package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.graphicsLayer
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
    onEditYours: (CustomCharacter) -> Unit = {},
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
 * @param yours the characters the user made up, shown first, each with the way to correct it.
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
    onEditYours: (CustomCharacter) -> Unit = {},
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
                            onEdit = { onEditYours(character) }
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
                val start = with(density) { ((cardWidth + STYLE_CARD_GAP) * lead).roundToPx() }
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
        Box(modifier = Modifier.size(CARD_FACE).clip(ButtonShape).waitingBar())
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
 * A character: who it is, as the card it signs shows it, with its name and something it would
 * say. The face is the ball wearing what is theirs, on their colour.
 *
 * Not chosen, the card has a breath of that colour in it; chosen, it is that colour through and
 * through, with the ink the shared card is written in, so the one that answers is told from the
 * rest by how much colour it has and not by which. The colours are light in both themes.
 *
 * A name over one line: cards side by side then have their names on one line and their sayings
 * on the next. The sayings are picked short enough to fit, see the `style_*_sample` strings; one
 * that does not is cut rather than let it push the card wider.
 *
 * @param colour whose colour the card has, and [emblem] what its ball wears: for one of the
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
    val own = shareColor(colour)
    // The choice arrives on one card as it leaves another: ground and ink pass into each other,
    // and nothing of either card moves but the face, which comes forward a little.
    val ground by animateColorAsState(
        targetValue = if (chosen) own else own.copy(alpha = if (scheme.isDark) CARD_TINT_DARK else CARD_TINT_LIGHT),
        animationSpec = choiceTween(),
        label = "card ground"
    )
    val ink by animateColorAsState(
        targetValue = if (chosen) ShareInk else scheme.onSurface,
        animationSpec = choiceTween(),
        label = "card ink"
    )
    val arrived by animateFloatAsState(if (chosen) 1f else 0f, choiceTween(), label = "card chosen")
    // The light of a touch is the ink's: on a card in its own colour the sheet's would not show.
    CompositionLocalProvider(LocalContentColor provides ink) {
        Box(
            modifier = modifier
                .clip(CardShape)
                // Under the colour, the tile every choice in the app stands on: the palest of the
                // colours would otherwise leave a card with no edge on a pale sheet.
                .background(scheme.onSurface.copy(alpha = CARD_BASE_ALPHA * (1f - arrived)))
                .background(ground)
                .selectable(selected = chosen, role = Role.RadioButton, onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(start = CARD_INSET, top = CARD_INSET, end = CARD_END, bottom = CARD_INSET),
                horizontalArrangement = Arrangement.spacedBy(CARD_INSET),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(CARD_FACE)
                        .graphicsLayer {
                            val grown = 1f + FACE_GROWTH * arrived
                            scaleX = grown
                            scaleY = grown
                        }
                        .clip(ButtonShape)
                        .background(own)
                ) {
                    CharacterBall(emblem = emblem, size = CARD_FACE)
                }
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
                        color = ink.copy(alpha = SAMPLE_ALPHA + (CHOSEN_SAMPLE_ALPHA - SAMPLE_ALPHA) * arrived),
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
                        .clickable(onClickLabel = edit, role = Role.Button, onClick = onEdit)
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
private const val STYLE_CARD_SHARE = 0.54f
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
private val EDIT_TARGET = 44.dp
private const val MAX_CARD_GROWTH = 1.5f

/** The face on a card, the room between it and the card's edge, and the room after the words. */
private val CARD_FACE = 44.dp
private val CARD_INSET = 8.dp
private val CARD_END = 14.dp

/** How much of its own colour a card that is not chosen has in it, on a dark ground and on a light one. */
private const val CARD_TINT_DARK = 0.16f
private const val CARD_TINT_LIGHT = 0.3f

/** How much of the text's colour lies under a card's own, while it is not chosen. */
private const val CARD_BASE_ALPHA = 0.05f

/** What a chosen card's second line keeps of its ink: more than elsewhere, to read on the strongest colours. */
private const val CHOSEN_SAMPLE_ALPHA = 0.8f

/** How much the face of the chosen card grows: just enough to be seen arriving. */
private const val FACE_GROWTH = 0.06f
