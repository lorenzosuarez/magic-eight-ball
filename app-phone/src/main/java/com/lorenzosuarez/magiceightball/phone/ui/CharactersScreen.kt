package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.LazyGridPrefetchStrategy
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.characters.CharacterRow

/**
 * Where who answers is chosen: a screen of its own, every character on it as a card, a few
 * loaded at a time. At its top, the way out, its name and the way to make a character; under
 * that, which characters are being looked at; and the rest is theirs.
 *
 * It only shows what it is given and says what was touched: which shelf, which character, what
 * to open. Whoever hosts it decides what any of that does.
 *
 * @param rows the characters of [shelf], and the names of their groups, as far as they are loaded.
 * @param shelves the shelves to choose among, in order; one alone is not offered as a choice.
 * @param chosen the [CharacterListing.key] of who answers now.
 * @param onCreate the way to make a character of one's own; null where that is not offered.
 * @param creating while "Create" waits for the account to be heard of: it says it heard.
 * @param onEdit the way to correct one of the user's own; null where they are only chosen among.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CharactersScreen(
    rows: LazyPagingItems<CharacterRow>,
    shelf: CharacterShelf,
    shelves: List<CharacterShelf>,
    onShelf: (CharacterShelf) -> Unit,
    chosen: String?,
    onChoose: (CharacterListing) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onCreate: (() -> Unit)? = null,
    creating: Boolean = false,
    onEdit: ((CustomCharacter) -> Unit)? = null
) {
    // What was just touched is chosen in the frame of the touch, and follows what is saved
    // whenever that changes.
    var touched by remember(chosen) { mutableStateOf(chosen) }
    val title = stringResource(R.string.styles_title)
    val density = LocalDensity.current
    // Two cards across, and one where two would be too narrow for what they say: on a small
    // phone, or with large text.
    val across = LocalConfiguration.current.screenWidthDp.dp.coerceAtMost(SCREEN_WIDEST)
    val columns = if ((across - EDGE * 2 - CARD_GAP) / 2 / density.fontScale >= CARD_LEAST) 2 else 1
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.pageGround)
            .semantics { paneTitle = title }
    ) {
        // A grid of its own for each shelf: a shelf is read from its top.
        key(shelf) {
            val prefetch = LocalGridPrefetch.current
            val grid = if (prefetch == null) rememberLazyGridState() else rememberLazyGridState(prefetchStrategy = prefetch)
            val scrolled by remember { derivedStateOf { grid.canScrollBackward } }
            Row(
                modifier = Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = EDGE, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarIconButton(
                    icon = R.drawable.ic_close,
                    description = stringResource(R.string.action_close),
                    onClick = onClose
                )
                Text(
                    text = title,
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (onCreate != null) {
                    BarTextButton(
                        icon = R.drawable.ic_add,
                        label = stringResource(R.string.action_create),
                        description = stringResource(R.string.characters_create),
                        onClick = onCreate,
                        busy = creating
                    )
                }
            }
            if (shelves.size > 1) Shelves(shelf = shelf, shelves = shelves, onShelf = onShelf)
            // Only once something has scrolled under the header is there an edge worth drawing.
            HorizontalDivider(
                thickness = 1.dp,
                color = if (scrolled) MaterialTheme.colorScheme.hairline else Color.Transparent
            )
            val refresh = rows.loadState.refresh
            val nothing = rows.itemCount == 0
            when {
                nothing && refresh is LoadState.Error -> Said(
                    text = stringResource(R.string.characters_load_failed),
                    action = stringResource(R.string.action_retry),
                    onAction = rows::retry
                )
                // Nothing here, and nothing more to come: said, with the way to change that.
                nothing && refresh is LoadState.NotLoading && rows.loadState.append.endOfPaginationReached -> Said(
                    text = stringResource(
                        if (shelf == CharacterShelf.YOURS) R.string.characters_empty_yours else R.string.characters_empty
                    ),
                    action = stringResource(R.string.characters_create).takeIf { shelf == CharacterShelf.YOURS && onCreate != null },
                    onAction = { onCreate?.invoke() }
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.weight(1f).fillMaxWidth().selectableGroup(),
                    state = grid,
                    contentPadding = PaddingValues(
                        start = EDGE,
                        top = 12.dp,
                        end = EDGE,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 32.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(CARD_GAP),
                    verticalArrangement = Arrangement.spacedBy(CARD_GAP)
                ) {
                    // What comes from afar and has not come yet: its places, so the screen is not bare.
                    if (nothing && refresh is LoadState.Loading && shelf == CharacterShelf.COMMUNITY) places(FIRST_PLACES)
                    items(
                        count = rows.itemCount,
                        key = rows.itemKey { it.key },
                        span = { index -> GridItemSpan(if (rows.peek(index) is CharacterRow.Header) maxLineSpan else 1) },
                        contentType = rows.itemContentType { it::class }
                    ) { index ->
                        when (val row = rows[index]) {
                            is CharacterRow.Header -> GroupTitle(stringResource(row.title))
                            is CharacterRow.Place -> {
                                // Their places kept, in the shape of the cards that are coming: said once, as one thing.
                                val waiting = stringResource(R.string.characters_loading)
                                CharacterCardPlace(
                                    modifier = Modifier.clearAndSetSemantics { if (row.index == 0) contentDescription = waiting }
                                )
                            }
                            is CharacterRow.Card -> ListedCard(
                                listing = row.listing,
                                chosen = touched == row.listing.key,
                                onClick = {
                                    touched = row.listing.key
                                    onChoose(row.listing)
                                },
                                onEdit = onEdit
                            )
                            null -> CharacterCardPlace()
                        }
                    }
                    when (rows.loadState.append) {
                        is LoadState.Loading -> places(columns)
                        is LoadState.Error -> item(key = "failed", span = { GridItemSpan(maxLineSpan) }) {
                            Said(
                                text = stringResource(R.string.characters_load_failed),
                                action = stringResource(R.string.action_retry),
                                onAction = rows::retry,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

/** The places of cards that are on their way: said once, as one thing, by the first of them. */
private fun LazyGridScope.places(count: Int) {
    items(count = count, key = { "waiting:$it" }, contentType = { "place" }) { index ->
        val waiting = stringResource(R.string.characters_loading_more)
        CharacterCardPlace(
            modifier = Modifier.clearAndSetSemantics { if (index == 0) contentDescription = waiting }
        )
    }
}

/**
 * Which characters are being looked at: the shelves side by side, the one in view filled as a
 * chosen card is. They scroll sideways where they do not fit, as on a small phone at large type.
 */
@Composable
private fun Shelves(shelf: CharacterShelf, shelves: List<CharacterShelf>, onShelf: (CharacterShelf) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fadingEdges(scroll)
            .horizontalScroll(scroll)
            .padding(horizontal = EDGE)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        shelves.forEach { one ->
            val scheme = MaterialTheme.colorScheme
            val picked = one == shelf
            val ground by animateColorAsState(
                targetValue = if (picked) scheme.onSurface else scheme.onSurface.copy(alpha = TILE_ALPHA),
                animationSpec = choiceTween(),
                label = "shelf ground"
            )
            val ink by animateColorAsState(if (picked) scheme.surface else scheme.onSurface, choiceTween(), label = "shelf ink")
            CompositionLocalProvider(LocalContentColor provides ink) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(ground)
                        .selectable(selected = picked, role = Role.Tab, onClick = { onShelf(one) })
                        .heightIn(min = SHELF_HEIGHT)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(one.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ink,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@StringRes
private fun CharacterShelf.labelRes(): Int = when (this) {
    CharacterShelf.ALL -> R.string.characters_shelf_all
    CharacterShelf.ORIGINALS -> R.string.characters_shelf_originals
    CharacterShelf.COMMUNITY -> R.string.characters_shelf_community
    CharacterShelf.YOURS -> R.string.styles_group_yours
}

/** The name of a group of characters: as the subjects on the options page are named. */
@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp).semantics { heading() },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Something the screen has to say in place of its cards, and what can be done about it. */
@Composable
private fun Said(text: String, action: String?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(ButtonShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** The card of [listing], whichever kind it is: what each kind has to show is worked out here. */
@Composable
private fun ListedCard(
    listing: CharacterListing,
    chosen: Boolean,
    onClick: () -> Unit,
    onEdit: ((CustomCharacter) -> Unit)?
) {
    when (listing) {
        is CharacterListing.Original -> CharacterCard(
            title = stringResource(listing.style.labelRes()),
            sample = stringResource(listing.style.sampleRes()),
            colour = listing.style,
            emblem = listing.style,
            chosen = chosen,
            onClick = onClick,
            // The app's own sayings are written to fit on one line.
            sampleLines = 1
        )
        is CharacterListing.Yours -> CharacterCard(
            title = listing.character.name,
            // In their own words, which is what tells one of theirs from another.
            sample = listing.character.description.ifBlank { stringResource(R.string.style_custom_sample) },
            colour = listing.character.colour,
            emblem = listing.character.emblem,
            chosen = chosen,
            onClick = onClick,
            onEdit = onEdit?.let { edit -> { edit(listing.character) } }
        )
        is CharacterListing.Community -> CharacterCard(
            title = listing.name,
            sample = listing.sample,
            colour = listing.colour,
            emblem = listing.emblem,
            chosen = chosen,
            onClick = onClick,
            credit = stringResource(R.string.characters_by, listing.author)
        )
    }
}

/**
 * A character: its face, its name, and something it would say.
 *
 * The card is the app's own, as every other choice is: a quiet tile, and the chosen one filled
 * with the text's colour. The face stands over the words, large enough that what the ball wears
 * can be told at a glance, since with thousands to look through that is what the eye goes by.
 * A character's colour is kept to that face and muted there; the one that answers has more of it.
 *
 * Every card of a kind is as tall as the next: a name on one line and a saying on [sampleLines],
 * cut where they run over rather than let one card stand taller than its neighbour.
 *
 * @param credit who made it, on one of other people's.
 * @param onEdit on one of the user's own, the way to correct it: a touch of its own, in the
 * corner over the words.
 */
@Composable
private fun CharacterCard(
    title: String,
    sample: String,
    colour: Magic8BallPersonality,
    emblem: Magic8BallPersonality,
    chosen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sampleLines: Int = 2,
    credit: String? = null,
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
    val ink by animateColorAsState(if (chosen) scheme.surface else scheme.onSurface, choiceTween(), label = "card ink")
    val arrived by animateFloatAsState(if (chosen) 1f else 0f, choiceTween(), label = "card chosen")
    // The light of a touch is the ink's: on the chosen card the page's own would not show.
    CompositionLocalProvider(LocalContentColor provides ink) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(ground)
                .selectable(selected = chosen, role = Role.RadioButton, onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(CARD_INSET)) {
                CharacterFace(colour = colour, emblem = emblem, size = CARD_FACE, chosen = { arrived })
                Spacer(Modifier.height(10.dp))
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
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = SAMPLE_ALPHA),
                    minLines = sampleLines,
                    maxLines = sampleLines,
                    overflow = TextOverflow.Ellipsis
                )
                if (credit != null) {
                    Text(
                        text = credit,
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ink.copy(alpha = SAMPLE_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onEdit != null) {
                val edit = stringResource(R.string.character_edit, title)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(EDIT_INSET)
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
 * The place of a card that is not known yet: a card's own shape, as tall as a card is at this
 * type size, with a round where its face will be and a bar where each of its lines will.
 */
@Composable
private fun CharacterCardPlace(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA))
            .padding(CARD_INSET)
    ) {
        Box(
            modifier = Modifier
                .size(CARD_FACE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = PLACE_FACE_ALPHA))
        )
        Spacer(Modifier.height(10.dp))
        // Each line holds its room with words that are not drawn, and its bar lies along it.
        Box(contentAlignment = Alignment.CenterStart) {
            Text(text = " ", style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Box(modifier = Modifier.fillMaxWidth(PLACE_NAME_WIDTH).height(PLACE_BAR_HEIGHT).waitingBar())
        }
        Box(modifier = Modifier.padding(top = 2.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = " ", style = MaterialTheme.typography.bodySmall, minLines = 2, maxLines = 2)
            Box(modifier = Modifier.widthIn(max = 200.dp).fillMaxWidth(PLACE_LINE_WIDTH).height(PLACE_BAR_HEIGHT).waitingBar())
        }
    }
}

/**
 * How the grid gets the next cards ready before they are scrolled to: null is Compose's own way,
 * which is what the app runs with. It is here to be set in tests on the JVM, where that way
 * waits for a frame that never comes and so never finishes.
 */
@OptIn(ExperimentalFoundationApi::class)
internal val LocalGridPrefetch = staticCompositionLocalOf<LazyGridPrefetchStrategy?> { null }

/** The room at either side of the screen, and the widest it is laid out for. */
private val EDGE = 16.dp
private val SCREEN_WIDEST = 640.dp

/** Between cards, the narrowest a card may be before one a row is better, and the room inside one. */
private val CARD_GAP = 8.dp
private val CARD_LEAST = 150.dp
private val CARD_INSET = 12.dp

/** The face on a card. */
private val CARD_FACE = 48.dp

/** How much of the ink what a character says has: the name is told from it at a glance. */
private const val SAMPLE_ALPHA = 0.66f

/** The pencil on one of the user's own: how far in from the corner, the room of its touch, and how large it is drawn. */
private val EDIT_INSET = 2.dp
private val EDIT_TARGET = 48.dp
private val EDIT_GLYPH = 18.dp

/** How tall a shelf is drawn; its touch takes more. */
private val SHELF_HEIGHT = 36.dp

/** A card's place while it is on its way: how strong its face, how thick its bars and how far each runs. */
private const val PLACE_FACE_ALPHA = 0.14f
private val PLACE_BAR_HEIGHT = 8.dp
private const val PLACE_NAME_WIDTH = 0.6f
private const val PLACE_LINE_WIDTH = 0.85f

/** How many places stand for a first page that has not come. */
private const val FIRST_PLACES = 6
