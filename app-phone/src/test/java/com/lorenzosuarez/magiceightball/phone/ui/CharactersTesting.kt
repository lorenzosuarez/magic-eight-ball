@file:OptIn(ExperimentalFoundationApi::class)

package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.layout.NestedPrefetchScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterLook
import com.example.magiceightball.core.domain.model.CharacterPage
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.example.magiceightball.core.domain.repository.NoCommunityCharacters
import com.lorenzosuarez.magiceightball.phone.characters.CharacterPages
import com.lorenzosuarez.magiceightball.phone.characters.CharactersQuery
import java.io.IOException

/**
 * Other people's characters, [count] of them, handed out in order and a page at a time as the
 * real ones will be. Every call is written down in [asked], and while [failing] none succeeds.
 */
internal class FakeCommunity(private val count: Int, var failing: Boolean = false) : CommunityCharacters {
    val asked = mutableListOf<Pair<String?, Int>>()
    override val open: Boolean = true

    override suspend fun page(after: String?, limit: Int): CharacterPage {
        asked += after to limit
        if (failing) throw IOException("no connection")
        val from = after?.toInt() ?: 0
        val until = minOf(from + limit, count)
        return CharacterPage(
            items = (from until until).map { number ->
                val look = CustomCharacter.LOOKS[number % CustomCharacter.LOOKS.size]
                CharacterListing.Community(
                    id = "$number",
                    name = "Personaje ${number + 1}",
                    description = "Responde como el personaje número ${number + 1}, con sus manías.",
                    colour = look,
                    emblem = CharacterLook.entries[(number * 7) % CharacterLook.entries.size],
                    author = "usuario ${number % 9 + 1}"
                )
            },
            next = until.takeIf { it < count }?.toString()
        )
    }
}

/** Gets nothing ready ahead: see [LocalGridPrefetch]. */
internal object NoPrefetch : LazyListPrefetchStrategy {
    override fun LazyListPrefetchScope.onScroll(delta: Float, layoutInfo: LazyListLayoutInfo) = Unit
    override fun LazyListPrefetchScope.onVisibleItemsUpdated(layoutInfo: LazyListLayoutInfo) = Unit
    override fun NestedPrefetchScope.onNestedPrefetch(firstVisibleItemIndex: Int) = Unit
}

/**
 * The screen of characters over what [query] lists, as the app shows it, with nothing behind it:
 * the shelf follows what is touched, and what else is touched is told to whoever asks.
 */
@Composable
internal fun CharactersShown(
    query: CharactersQuery = CharactersQuery(),
    community: CommunityCharacters = NoCommunityCharacters,
    chosen: String? = CharacterListing.Original(Magic8BallPersonality.SARCASTIC).key,
    onUse: (CharacterListing) -> Unit = {},
    onClose: () -> Unit = {},
    onCreate: (() -> Unit)? = null,
    onEdit: ((CharacterListing.Yours) -> Unit)? = null
) {
    val pages = remember(community) { CharacterPages(community) }
    var shelf by remember { mutableStateOf(query.shelf) }
    val shown = query.copy(shelf = shelf)
    CompositionLocalProvider(LocalListPrefetch provides NoPrefetch) {
      CharactersScreen(
        rows = remember(shown) { pages.flow(shown) }.collectAsLazyPagingItems(),
        shelf = shelf,
        shelves = pages.shelves,
        onShelf = { shelf = it },
        chosen = chosen,
        onUse = onUse,
        communityOpen = pages.communityOpen,
        onClose = onClose,
        onCreate = onCreate,
        onEdit = onEdit
      )
    }
}
