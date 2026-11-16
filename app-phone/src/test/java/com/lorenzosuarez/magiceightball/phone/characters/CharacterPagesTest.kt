package com.lorenzosuarez.magiceightball.phone.characters

import android.app.Application
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.paging.testing.asSnapshot
import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterPage
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.example.magiceightball.core.domain.model.CharacterShelf
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.NoCommunityCharacters
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.ui.FakeCommunity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The characters, a page at a time: what is at hand first, and other people's only as they are reached. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CharacterPagesTest {

    private val nemo = CustomCharacter(id = "1", name = "Capitán Nemo", description = "Un capitán gruñón.")
    private val config = PagingConfig(pageSize = 24, enablePlaceholders = false)

    private fun cards(rows: List<CharacterRow>) = rows.mapNotNull { (it as? CharacterRow.Card)?.listing }

    @Test
    fun `with nobody else's, every shelf is whole in one page and asks for nothing more`() = runTest {
        val query = CharactersQuery(yours = listOf(nemo))
        val page = TestPager(config, CharactersPagingSource(query, NoCommunityCharacters)).refresh() as PagingSource.LoadResult.Page

        assertNull(page.nextKey)
        assertEquals(CharacterListing.Yours(nemo), cards(page.data).first())
        assertEquals(CustomCharacter.LOOKS.size + 1, page.data.size)
        assertEquals(page.data.size, page.data.map { it.key }.toSet().size)
    }

    @Test
    fun `each shelf lists its own kind and no other`() {
        val query = CharactersQuery(yours = listOf(nemo))
        assertTrue(cards(query.copy(shelf = CharacterShelf.YOURS).atHand()).all { it is CharacterListing.Yours })
        assertTrue(cards(query.copy(shelf = CharacterShelf.ORIGINALS).atHand()).all { it is CharacterListing.Original })
        assertEquals(CustomCharacter.LOOKS.size, query.copy(shelf = CharacterShelf.ORIGINALS).atHand().size)
        assertTrue(query.copy(shelf = CharacterShelf.COMMUNITY).atHand().isEmpty())
    }

    @Test
    fun `while the user's own are on their way their places are kept, and given up once they are known`() {
        val waiting = CharactersQuery(shelf = CharacterShelf.YOURS, yoursPlaces = 2).atHand()
        assertEquals(listOf(CharacterRow.Place(0), CharacterRow.Place(1)), waiting)
        assertEquals(1, CharactersQuery(shelf = CharacterShelf.YOURS, yours = listOf(nemo), yoursPlaces = 2).atHand().size)
    }

    @Test
    fun `thousands of other people's are asked for a page at a time, each from where the last ended`() = runTest {
        val community = FakeCommunity(5_000)
        val pager = TestPager(config, CharactersPagingSource(CharactersQuery(shelf = CharacterShelf.COMMUNITY), community))

        val first = pager.refresh() as PagingSource.LoadResult.Page
        val second = pager.append() as PagingSource.LoadResult.Page

        // The first load is Paging's larger one; nothing is fetched twice and nothing skipped.
        assertEquals(listOf<Pair<String?, Int>>(null to 72, "72" to 24), community.asked)
        assertEquals("c:0", first.data.first().key)
        assertEquals("c:72", second.data.first().key)
        assertEquals("96", second.nextKey)
    }

    @Test
    fun `on the shelf of everything, other people's follow what is at hand and only when read towards`() = runTest {
        val community = FakeCommunity(30)
        val pager = TestPager(config, CharactersPagingSource(CharactersQuery(), community))

        val atHand = pager.refresh() as PagingSource.LoadResult.Page
        assertTrue(community.asked.isEmpty())
        assertTrue(cards(atHand.data).none { it is CharacterListing.Community })

        val others = pager.append() as PagingSource.LoadResult.Page
        assertEquals(24, others.data.size)
        val last = pager.append() as PagingSource.LoadResult.Page
        assertEquals(6, last.data.size)
        assertNull(last.nextKey)
    }

    @Test
    fun `other people's that cannot be fetched are an error to ask again about, not an empty shelf`() = runTest {
        val community = FakeCommunity(100, failing = true)
        val pager = TestPager(config, CharactersPagingSource(CharactersQuery(shelf = CharacterShelf.COMMUNITY), community))
        assertTrue(pager.refresh() is PagingSource.LoadResult.Error)

        community.failing = false
        val again = TestPager(config, CharactersPagingSource(CharactersQuery(shelf = CharacterShelf.COMMUNITY), community))
        assertTrue(again.refresh() is PagingSource.LoadResult.Page)
    }

    @Test
    fun `one handed out again on a later page, as when the list moved, is listed once`() = runTest {
        // Every page starts one short of where the last ended: its first was the last one's last.
        val moving = object : CommunityCharacters {
            private val all = FakeCommunity(100)
            override val open = true
            override suspend fun page(after: String?, limit: Int): CharacterPage =
                all.page(after?.let { (it.toInt() - 1).toString() }, limit)
        }
        val pager = TestPager(config, CharactersPagingSource(CharactersQuery(shelf = CharacterShelf.COMMUNITY), moving))
        val keys = listOf(pager.refresh(), pager.append(), pager.append())
            .flatMap { (it as PagingSource.LoadResult.Page).data.map(CharacterRow::key) }

        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `groups are named where they begin, and a shelf of one kind names none`() = runTest {
        val pages = CharacterPages(FakeCommunity(10))
        val all = pages.flow(CharactersQuery(yours = listOf(nemo))).asSnapshot { scrollTo(index = 40) }
        val headers = all.filterIsInstance<CharacterRow.Header>().map { it.title }

        assertEquals(R.string.styles_group_yours, headers.first())
        assertEquals(R.string.characters_shelf_community, headers.last())
        assertEquals(headers.size, headers.toSet().size)
        assertEquals(CharacterRow.Header(R.string.styles_group_yours), all.first())

        val community = pages.flow(CharactersQuery(shelf = CharacterShelf.COMMUNITY)).asSnapshot()
        assertTrue(community.none { it is CharacterRow.Header })
    }

    @Test
    fun `the shelf of other people's is always there, and says whether there are any yet`() {
        assertEquals(CharacterShelf.entries, CharacterPages(NoCommunityCharacters).shelves)
        assertTrue(!CharacterPages(NoCommunityCharacters).communityOpen)
        assertTrue(CharacterPages(FakeCommunity(1)).communityOpen)
    }

    @Test
    fun `one of other people's answers as a copy under a key no slot of the user's has`() {
        val listing = FakeCommunity(1).let { runCatching { kotlinx.coroutines.runBlocking { it.page(null, 1) } }.getOrThrow().items.single() }
        val answering = listing.answering()

        assertEquals(listing.key, answering.id)
        assertTrue(answering.id !in CustomCharacter.SLOTS)
        assertEquals(listing.description, answering.description)
    }

    @Test
    fun `a style taken out from afar is in no page`() {
        val shown = cards(CharactersQuery(hidden = setOf(Magic8BallPersonality.CAT)).atHand())
        assertTrue(CharacterListing.Original(Magic8BallPersonality.CAT) !in shown)
    }
}
