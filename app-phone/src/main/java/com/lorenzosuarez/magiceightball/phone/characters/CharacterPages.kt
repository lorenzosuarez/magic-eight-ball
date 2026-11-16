package com.lorenzosuarez.magiceightball.phone.characters

import androidx.annotation.StringRes
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.insertSeparators
import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.ui.StyleGroups
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * What is being looked at where characters are chosen among: which shelf, and what of the
 * user's and of the app's there is to put on it. Two that are equal list the same characters.
 *
 * @property yours the characters the user made; empty when they have none, or none are known.
 * @property yoursPlaces how many places to keep for the user's own while they are on their way.
 * @property hidden the app's own that are taken out from afar. The one in use is never among
 * them: whoever builds this leaves it out, so that it stays listed wherever it is.
 */
data class CharactersQuery(
    val shelf: CharacterShelf = CharacterShelf.ALL,
    val yours: List<CustomCharacter> = emptyList(),
    val yoursPlaces: Int = 0,
    val hidden: Set<Magic8BallPersonality> = emptySet()
)

/** One thing in the list of characters, top to bottom. */
sealed interface CharacterRow {
    val key: String

    /** The name of the group that the cards after it belong to. */
    data class Header(@StringRes val title: Int) : CharacterRow {
        override val key: String get() = "h:$title"
    }

    /** A character. */
    data class Card(val listing: CharacterListing) : CharacterRow {
        override val key: String get() = listing.key
    }

    /** The place of one of the user's own that is still on its way. */
    data class Place(val index: Int) : CharacterRow {
        override val key: String get() = "p:$index"
    }
}

/**
 * The characters of a [CharactersQuery], a few at a time.
 *
 * What the app knows by heart, its own characters and the user's, is the first page and costs
 * nothing. After it come other people's, as many at a time as are asked for and only while
 * the list is being read towards them: there may be thousands, and nobody reads thousands.
 */
class CharacterPages @Inject constructor(private val community: CommunityCharacters) {

    /** Which shelves there are to choose among: other people's only once there are any. */
    val shelves: List<CharacterShelf>
        get() = CharacterShelf.entries.filter { it != CharacterShelf.COMMUNITY || community.open }

    fun flow(query: CharactersQuery): Flow<PagingData<CharacterRow>> =
        Pager(PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PAGE_SIZE / 2, enablePlaceholders = false)) {
            CharactersPagingSource(query, community)
        }.flow.map { page ->
            // A shelf of one kind needs no names over its groups: it is named by the shelf.
            if (query.shelf == CharacterShelf.YOURS || query.shelf == CharacterShelf.COMMUNITY) page
            else page.insertSeparators { before, after ->
                val group = after?.group()
                if (group != null && group != before?.group()) CharacterRow.Header(group) else null
            }
        }

    private companion object {
        /** Twelve rows of two: more than a screen, so the next ones are asked for before they are reached. */
        const val PAGE_SIZE = 24
    }
}

/** Under which name a row is listed, when it is listed under one. */
@StringRes
private fun CharacterRow.group(): Int? = when (this) {
    is CharacterRow.Header -> null
    is CharacterRow.Place -> R.string.styles_group_yours
    is CharacterRow.Card -> when (val listing = listing) {
        is CharacterListing.Yours -> R.string.styles_group_yours
        is CharacterListing.Community -> R.string.characters_shelf_community
        is CharacterListing.Original -> StyleGroups.first { listing.style in it.styles }.title
    }
}

/**
 * Loads the characters of [query]: with no key, what the app has at hand, and with one, the
 * page of other people's that the key stands for. Only ever read forwards.
 */
internal class CharactersPagingSource(
    private val query: CharactersQuery,
    private val community: CommunityCharacters
) : PagingSource<String, CharacterRow>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CharacterRow> {
        val key = params.key
        if (key == null && query.shelf != CharacterShelf.COMMUNITY) {
            val more = query.shelf == CharacterShelf.ALL && community.open
            return LoadResult.Page(data = query.atHand(), prevKey = null, nextKey = FIRST.takeIf { more })
        }
        if (!community.open) return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        return try {
            val page = community.page(after = key?.takeUnless { it == FIRST }, limit = params.loadSize)
            LoadResult.Page(data = page.items.map(CharacterRow::Card), prevKey = null, nextKey = page.next)
        } catch (failed: IOException) {
            LoadResult.Error(failed)
        }
    }

    /** Refreshed, the list starts over: what is at hand is first, and is all there at once. */
    override fun getRefreshKey(state: PagingState<String, CharacterRow>): String? = null

    private companion object {
        /** Stands for the first page of other people's, where they follow what is at hand. */
        const val FIRST = ""
    }
}

/** What of [this] the app has without asking anyone: the user's own, and then its own in their groups. */
internal fun CharactersQuery.atHand(): List<CharacterRow> = buildList {
    if (shelf == CharacterShelf.ALL || shelf == CharacterShelf.YOURS) {
        if (yours.isEmpty()) repeat(yoursPlaces) { add(CharacterRow.Place(it)) }
        yours.forEach { add(CharacterRow.Card(CharacterListing.Yours(it))) }
    }
    if (shelf == CharacterShelf.ALL || shelf == CharacterShelf.ORIGINALS) {
        StyleGroups.forEach { group ->
            group.styles.filter { it !in hidden }.forEach { add(CharacterRow.Card(CharacterListing.Original(it))) }
        }
    }
}
