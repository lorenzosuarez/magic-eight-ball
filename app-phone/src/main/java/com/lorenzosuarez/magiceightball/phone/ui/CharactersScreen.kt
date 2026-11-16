package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.layout.offset
import com.example.magiceightball.core.domain.model.look
import androidx.compose.ui.semantics.disabled
import androidx.compose.foundation.verticalScroll
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
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
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.characters.CharacterRow
import kotlinx.coroutines.launch

/**
 * Where who answers is chosen: a screen of its own, every character a row on it, a few loaded
 * at a time. At its top, the way out, its name and the way to make a character; under that,
 * which characters are being looked at; and the rest is theirs, group by group.
 *
 * A touch on a character opens it, to be looked at; it answers once that is asked for there.
 * With thousands to look through, nobody wants the one they brushed past to start answering.
 *
 * It only shows what it is given and says what was asked: which shelf, which character, what
 * to open. Whoever hosts it decides what any of that does.
 *
 * @param rows the characters of [shelf], and the names of their groups, as far as they are loaded.
 * @param shelves the shelves to choose among, in order; one alone is not offered as a choice.
 * @param chosen the [CharacterListing.key] of who answers now.
 * @param communityOpen whether other people's characters are to be had yet: where they are
 * not, their shelf says that they are coming.
 * @param onUse told the character that is to answer.
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
    onUse: (CharacterListing) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    communityOpen: Boolean = true,
    onCreate: (() -> Unit)? = null,
    creating: Boolean = false,
    onEdit: ((CharacterListing.Yours) -> Unit)? = null
) {
    // The character that is open, to be looked at.
    var opened by remember { mutableStateOf<CharacterListing?>(null) }
    val title = stringResource(R.string.styles_title)
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.pageGround)
            .semantics { paneTitle = title }
    ) {
        // A list of its own for each shelf, since a shelf is read from its top; the header over it stays.
        val prefetch = LocalListPrefetch.current
        val list = key(shelf) {
            if (prefetch == null) rememberLazyListState() else rememberLazyListState(prefetchStrategy = prefetch)
        }
        val scrolled by remember(list) { derivedStateOf { list.canScrollBackward } }
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .fillMaxWidth()
                .height(TOP_BAR_HEIGHT)
                // Plain marks, whose pictures stand on the page's margins.
                .padding(horizontal = EDGE - BAR_MARK_OUT),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconButton(
                icon = R.drawable.ic_close,
                description = stringResource(R.string.action_close),
                onClick = onClose
            )
            Text(
                text = title,
                modifier = Modifier.weight(1f).semantics { heading() },
                // As every page is named: the name grows with the system's font only as far as its bar has room.
                style = MaterialTheme.typography.titleMedium.let {
                    val fit = (PAGE_TITLE_MAX_SCALE / density.fontScale).coerceAtMost(1f)
                    it.copy(fontSize = it.fontSize * fit, lineHeight = it.lineHeight * fit)
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (onCreate != null) {
                // While what it started is on its way, a ring turns where its mark is: it has heard.
                if (creating) {
                    val create = stringResource(R.string.characters_create)
                    Box(
                        modifier = Modifier.size(48.dp).semantics {
                            contentDescription = create
                            disabled()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        WaitingRing(color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    AppIconButton(
                        icon = R.drawable.ic_add,
                        description = stringResource(R.string.characters_create),
                        onClick = onCreate
                    )
                }
            }
        }
        if (shelves.size > 1) Shelves(shelf = shelf, shelves = shelves, onShelf = onShelf)
        // Only once something has scrolled under the header is there an edge worth drawing.
        val edge by animateColorAsState(
            targetValue = if (scrolled) MaterialTheme.colorScheme.hairline else Color.Transparent,
            animationSpec = choiceTween(),
            label = "header edge"
        )
        HorizontalDivider(thickness = 1.dp, color = edge)
        val refresh = rows.loadState.refresh
        val nothing = rows.itemCount == 0
        when {
            // Other people's, before there are any: said, and not as something that went wrong.
            shelf == CharacterShelf.COMMUNITY && !communityOpen -> Said(
                icon = R.drawable.ic_group,
                title = stringResource(R.string.characters_community_soon),
                text = stringResource(R.string.characters_community_soon_text),
                modifier = Modifier.weight(1f)
            )
            nothing && refresh is LoadState.Error -> Said(
                icon = R.drawable.ic_group,
                title = stringResource(R.string.characters_load_failed),
                text = stringResource(R.string.characters_load_failed_text),
                action = stringResource(R.string.action_retry),
                onAction = rows::retry,
                modifier = Modifier.weight(1f)
            )
            // Nothing here, and nothing more to come: said, with the way to change that.
            nothing && refresh is LoadState.NotLoading && rows.loadState.append.endOfPaginationReached -> Said(
                icon = R.drawable.ic_edit,
                title = stringResource(
                    if (shelf == CharacterShelf.YOURS) R.string.characters_empty_yours else R.string.characters_empty
                ),
                text = stringResource(R.string.characters_empty_yours_text).takeIf { shelf == CharacterShelf.YOURS },
                action = stringResource(R.string.characters_create).takeIf { shelf == CharacterShelf.YOURS && onCreate != null },
                onAction = { onCreate?.invoke() },
                modifier = Modifier.weight(1f)
            )
            else -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    // No wider than it reads well: on a tablet the rows keep their measure and the page its margins.
                    modifier = Modifier.widthIn(max = SCREEN_WIDEST).fillMaxSize(),
                    state = list,
                    contentPadding = PaddingValues(
                        start = EDGE,
                        top = 8.dp,
                        end = EDGE,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(GROUP_GAP)
                ) {
                    // What comes from afar and has not come yet: its places, so the screen is not bare.
                    if (nothing && refresh is LoadState.Loading && shelf == CharacterShelf.COMMUNITY) places(FIRST_PLACES)
                    items(
                        count = rows.itemCount,
                        key = rows.itemKey { it.key },
                        contentType = rows.itemContentType { it::class }
                    ) { index ->
                        // A group is round where it begins and where it ends: at a name, or at either end of what is loaded.
                        val first = index == 0 || rows.peek(index - 1) is CharacterRow.Header
                        val last = index == rows.itemCount - 1 || rows.peek(index + 1) is CharacterRow.Header
                        when (val row = rows[index]) {
                            is CharacterRow.Header -> GroupTitle(stringResource(row.title), top = index > 0)
                            is CharacterRow.Place -> {
                                // Their places kept, in the shape of the rows that are coming: said once, as one thing.
                                val waiting = stringResource(R.string.characters_loading)
                                CharacterRowPlace(
                                    shape = groupedShape(first, last),
                                    modifier = Modifier.clearAndSetSemantics { if (row.index == 0) contentDescription = waiting }
                                )
                            }
                            is CharacterRow.Card -> ListedRow(
                                listing = row.listing,
                                chosen = chosen == row.listing.key,
                                // More on their way carry the group on: it is not rounded off under them.
                                shape = groupedShape(first, last && rows.loadState.append !is LoadState.Loading),
                                onClick = { opened = row.listing }
                            )
                            null -> CharacterRowPlace(shape = groupedShape(first, last))
                        }
                    }
                    when (rows.loadState.append) {
                        is LoadState.Loading -> places(MORE_PLACES, joined = true)
                        is LoadState.Error -> item(key = "failed") {
                            Said(
                                icon = null,
                                title = stringResource(R.string.characters_load_failed),
                                action = stringResource(R.string.action_retry),
                                onAction = rows::retry
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
    opened?.let { listing ->
        CharacterSheet(
            listing = listing,
            answering = chosen == listing.key,
            onUse = { onUse(listing) },
            onEdit = (listing as? CharacterListing.Yours)?.let { yours -> onEdit?.let { edit -> { edit(yours) } } },
            onClose = { opened = null }
        )
    }
}

/**
 * The places of rows that are on their way: said once, as one thing, by the first of them.
 *
 * @param joined whether they follow rows that are there, and so carry on their group.
 */
private fun LazyListScope.places(count: Int, joined: Boolean = false) {
    items(count = count, key = { "waiting:$it" }, contentType = { "place" }) { index ->
        val waiting = stringResource(R.string.characters_loading_more)
        CharacterRowPlace(
            shape = groupedShape(first = index == 0 && !joined, last = index == count - 1),
            modifier = Modifier.clearAndSetSemantics { if (index == 0) contentDescription = waiting }
        )
    }
}

/**
 * Which characters are being looked at: the shelves side by side, outlined, and the one in view
 * filled. They scroll sideways where they do not fit, as on a small phone at large type.
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
            val ground by animateColorAsState(if (picked) scheme.onSurface else Color.Transparent, choiceTween(), label = "shelf ground")
            val line by animateColorAsState(if (picked) scheme.onSurface else scheme.outlineVariant, choiceTween(), label = "shelf line")
            val ink by animateColorAsState(if (picked) scheme.surface else scheme.onSurfaceVariant, choiceTween(), label = "shelf ink")
            CompositionLocalProvider(LocalContentColor provides ink) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(ground)
                        .border(1.dp, line, CircleShape)
                        .selectable(selected = picked, role = Role.Tab, onClick = { onShelf(one) })
                        .heightIn(min = SHELF_HEIGHT)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(one.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
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

/**
 * The name of a group of characters, set in as far as what its rows say.
 *
 * @param top whether there is a group over it to stand clear of.
 */
@Composable
private fun GroupTitle(text: String, top: Boolean) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = GROUP_ROW_INSET, top = if (top) 22.dp else 8.dp, bottom = 8.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Something the screen has to say in place of its rows, and what can be done about it: a mark,
 * what it is, a line more and one thing to do, a little over the middle of the room it is given.
 * It is told to whoever has the screen read out.
 *
 * @param icon its mark; null where it is said among rows, in no more room than it needs.
 */
@Composable
private fun Said(
    @DrawableRes icon: Int?,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            // Two parts of the room over it and three under.
            Spacer(Modifier.weight(2f))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.pageCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = action, onClick = onAction)
        }
        if (icon != null) Spacer(Modifier.weight(3f))
    }
}

/** What [this] is called, whichever kind it is. */
@Composable
private fun CharacterListing.title(): String = when (this) {
    is CharacterListing.Original -> stringResource(style.labelRes())
    is CharacterListing.Yours -> character.name
    is CharacterListing.Community -> name
}

/** The line under its name: for the app's own, something it would say; for anyone else's, what was written of it. */
@Composable
private fun CharacterListing.line(): String = when (this) {
    is CharacterListing.Original -> stringResource(style.sampleRes())
    is CharacterListing.Yours -> character.description.ifBlank { stringResource(R.string.style_custom_sample) }
    is CharacterListing.Community -> description
}

/** The face of [listing], whichever kind it is. */
@Composable
private fun ListedFace(listing: CharacterListing, size: androidx.compose.ui.unit.Dp, chosen: Boolean) {
    val whole: () -> Float = { if (chosen) 1f else 0f }
    when (listing) {
        is CharacterListing.Original -> CharacterFace(listing.style, listing.style.look, size, chosen = whole)
        is CharacterListing.Yours -> CharacterFace(listing.character.colour, listing.character.emblem, size, chosen = whole)
        is CharacterListing.Community -> CharacterFace(listing.colour, listing.emblem, size, chosen = whole)
    }
}

/**
 * The face of [listing] in a row: of somebody else's, with its maker's own face small on its
 * corner, ringed in the row's ground so that it stands off the face it is on. That, before any
 * word is read, is what says a character is somebody's and whose.
 */
@Composable
private fun RowFace(listing: CharacterListing, chosen: Boolean) {
    if (listing !is CharacterListing.Community) {
        ListedFace(listing, ROW_FACE, chosen)
        return
    }
    // The face stays where every other row has its own: the mark hangs off its corner, into the row's own room.
    Box(modifier = Modifier.size(ROW_FACE)) {
        ListedFace(listing, ROW_FACE, chosen)
        // On the corner the ball does not come in from, and out past it: on the other it would cover
        // the ball's own face, and nearer in it would be read with the number as one word.
        AccountAvatar(
            name = listing.author,
            photoUrl = listing.authorPhotoUrl,
            size = MAKER_BADGE,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -MAKER_OUT, y = MAKER_OUT)
                .border(MAKER_RING, MaterialTheme.colorScheme.pageCard, CircleShape)
                .padding(MAKER_RING),
            small = true,
            told = true
        )
    }
}

/**
 * A character in the list: its face, its name and a line about it, and at the end of the row
 * the mark of the one that answers. A touch opens it, and what can be done with it is there.
 */
@Composable
private fun ListedRow(
    listing: CharacterListing,
    chosen: Boolean,
    shape: Shape,
    onClick: () -> Unit
) {
    val title = listing.title()
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.pageCard)
            // Opens it; whether it is the one that answers is said, and shown by its mark.
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { selected = chosen }
            .heightIn(min = GROUP_ROW_HEIGHT)
            .padding(horizontal = GROUP_ROW_INSET, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowFace(listing, chosen)
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listing.line(),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (chosen) {
            // Said by the row, which says it is the one: the mark is for the eye.
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = scheme.onSurface
            )
        }
    }
}

/** The place of a row that is not known yet: a row's own shape and height, a round for its face and a bar for each line. */
@Composable
private fun CharacterRowPlace(shape: Shape, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.pageCard)
            .heightIn(min = GROUP_ROW_HEIGHT)
            .padding(horizontal = GROUP_ROW_INSET, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(ROW_FACE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = PLACE_FACE_ALPHA))
        )
        Column(modifier = Modifier.weight(1f)) {
            // Each line holds its room with words that are not drawn, and its bar lies along it.
            Box(contentAlignment = Alignment.CenterStart) {
                Text(text = " ", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Box(modifier = Modifier.widthIn(max = 160.dp).fillMaxWidth(0.45f).height(PLACE_BAR_HEIGHT).waitingBar())
            }
            Box(contentAlignment = Alignment.CenterStart) {
                Text(text = " ", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Box(modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth(0.75f).height(PLACE_BAR_HEIGHT).waitingBar())
            }
        }
    }
}

/**
 * A character, opened to be looked at: who it is, whose it is, what it is like, and the way to
 * have it answer. Pulling it down, the back gesture and a touch outside put it away.
 *
 * @param answering whether it is the one that answers now: then there is nothing to ask for.
 * @param onEdit on one of the user's own, the way to correct it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterSheet(
    listing: CharacterListing,
    answering: Boolean,
    onUse: () -> Unit,
    onEdit: (() -> Unit)?,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    val putAway: (() -> Unit) -> Unit = { then ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onClose()
            then()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetGround,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        InAppLanguage(language) {
            CharacterDetail(
                listing = listing,
                answering = answering,
                onUse = { putAway(onUse) },
                onEdit = onEdit?.let { edit -> { putAway(edit) } }
            )
        }
    }
}

/** What [CharacterSheet] shows. */
@Composable
internal fun CharacterDetail(
    listing: CharacterListing,
    answering: Boolean,
    onUse: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            // On a short screen, or with much written of it at large type, it is read by scrolling.
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ListedFace(listing, SHEET_FACE, chosen = true)
        Text(
            text = listing.title(),
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )
        // Whose it is: of somebody else's, who that is, with their picture beside their name.
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (listing is CharacterListing.Community) {
                AccountAvatar(name = listing.author, photoUrl = listing.authorPhotoUrl, size = MAKER_FACE, small = true, told = true)
            }
            Text(
                text = when (listing) {
                    is CharacterListing.Original -> stringResource(R.string.character_of_app)
                    is CharacterListing.Yours -> stringResource(R.string.character_of_yours)
                    is CharacterListing.Community -> stringResource(R.string.characters_by, listing.author)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        // What it is like: for the app's own, something it would say; for anyone else's, what was
        // written of it, in its maker's words and as long as they made it.
        if (listing !is CharacterListing.Original) {
            Text(
                text = stringResource(R.string.character_how),
                modifier = Modifier.padding(start = 16.dp, top = 20.dp).align(Alignment.Start),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .padding(top = if (listing is CharacterListing.Original) 20.dp else 8.dp)
                .fillMaxWidth()
                .clip(groupedShape(first = true, last = true))
                .background(scheme.sheetCard)
                .padding(16.dp)
        ) {
            Text(
                text = when (listing) {
                    is CharacterListing.Original -> stringResource(R.string.character_says, stringResource(listing.style.sampleRes()))
                    is CharacterListing.Yours -> listing.character.description.ifBlank { stringResource(R.string.style_custom_sample) }
                    is CharacterListing.Community -> listing.description
                },
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                // A line alone would make a pill of the card, like the button under it: it has the room of two.
                minLines = 2
            )
        }
        if (answering) {
            // Nothing to ask for: said as what is so, not as a button that does nothing.
            Row(
                modifier = Modifier.padding(top = 20.dp).heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = scheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.character_answering),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            PrimaryButton(
                text = stringResource(R.string.character_use),
                onClick = onUse,
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth()
            )
        }
        if (onEdit != null) {
            QuietButton(
                text = stringResource(R.string.character_edit_action),
                onClick = onEdit,
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
            )
        }
    }
}

/**
 * How the list gets the next rows ready before they are scrolled to: null is Compose's own way,
 * which is what the app runs with. It is here to be set in tests on the JVM, where that way
 * waits for a frame that never comes and so never finishes.
 */
@OptIn(ExperimentalFoundationApi::class)
internal val LocalListPrefetch = staticCompositionLocalOf<LazyListPrefetchStrategy?> { null }

/** The room at either side of the screen, and the widest it is laid out for. */
private val EDGE = 16.dp
private val SCREEN_WIDEST = 640.dp

/** The face on a row, and on the sheet a character is opened in. */
private val ROW_FACE = 40.dp
private val SHEET_FACE = 88.dp

/** The picture of whoever made a character, beside their name. */
private val MAKER_FACE = 22.dp

/** The maker's face on the corner of a character's in a row: how large, how far past the corner, and the ring round it. */
private val MAKER_BADGE = 16.dp
private val MAKER_OUT = 6.dp
private val MAKER_RING = 2.dp

/** How tall a shelf is drawn; its touch takes more. */
private val SHELF_HEIGHT = 36.dp

/** A row's place while it is on its way: how strong its face, and how thick its bars. */
private const val PLACE_FACE_ALPHA = 0.14f
private val PLACE_BAR_HEIGHT = 8.dp

/** How many places stand for a first page that has not come, and for more that are on their way. */
private const val FIRST_PLACES = 8
private const val MORE_PLACES = 2
