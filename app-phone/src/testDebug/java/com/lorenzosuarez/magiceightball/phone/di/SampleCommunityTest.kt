package com.lorenzosuarez.magiceightball.phone.di

import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CustomCharacter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The made-up characters of the build that is tried out are what a person could have made with
 * the app: they fit where a character is kept, so that what is tried is what will be.
 */
class SampleCommunityTest {

    private val all = SampleCommunityCharacters.all

    @Test
    fun `each could have been made in the app, and each is somebody's, by a handle as people pick them`() {
        all.forEach { made ->
            assertEquals(made.name, made.name, CustomCharacter.nameOrNull(made.name))
            assertEquals(made.name, made.description, CustomCharacter.descriptionOrNull(made.description))
            // As the platforms let a handle be: no capitals, accents or spaces.
            assertTrue(made.author, HANDLE.matches(made.author))
            // And no year so recent that it reads as a child's age.
            assertTrue(made.author, YEAR.findAll(made.author).none { it.value.toInt() in 2008..2026 })
        }
    }

    @Test
    fun `no two are called the same, and they are not all as long`() {
        assertEquals(all.size, all.map { it.name.lowercase() }.toSet().size)
        assertEquals(all.size, all.map { it.key }.toSet().size)
        val lengths = all.map { it.description.length }
        assertTrue("one-liners ${lengths.count { it < 100 }}", lengths.count { it < 100 } >= 8)
        assertTrue("longest ${lengths.max()}", lengths.max() > 260)
    }

    @Test
    fun `they are handed out a page at a time, each once, to the last, and there are several pages of them`() = runTest {
        val handed = mutableListOf<CharacterListing.Community>()
        var pages = 0
        var after: String? = null
        do {
            val page = SampleCommunityCharacters.page(after = after, limit = 24)
            // Every page but the last is full.
            if (page.next != null) assertEquals(24, page.items.size)
            handed += page.items
            after = page.next
            pages++
        } while (after != null && pages < 50)

        assertEquals(all, handed)
        assertTrue("pages $pages", pages >= 4)
    }

    private companion object {
        val HANDLE = Regex("[a-z][a-z0-9._]{2,29}")
        val YEAR = Regex("(?<![0-9])[0-9]{4}(?![0-9])")
    }
}
