package com.lorenzosuarez.magiceightball.phone.di

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
    fun `each could have been made in the app, and each is somebody's`() {
        all.forEach { made ->
            assertEquals(made.name, made.name, CustomCharacter.nameOrNull(made.name))
            assertEquals(made.name, made.description, CustomCharacter.descriptionOrNull(made.description))
            assertTrue(made.name, Regex("usuario [1-9][0-9]?").matches(made.author))
        }
    }

    @Test
    fun `no two are called the same, and they are not all as long`() {
        assertEquals(all.size, all.map { it.name.lowercase() }.toSet().size)
        assertEquals(all.size, all.map { it.key }.toSet().size)
        val lengths = all.map { it.description.length }
        assertTrue("shortest ${lengths.min()}", lengths.min() < 120)
        assertTrue("longest ${lengths.max()}", lengths.max() > 220)
    }

    @Test
    fun `they are handed out a page at a time, each once, to the last`() = runTest {
        val first = SampleCommunityCharacters.page(after = null, limit = 24)
        val second = SampleCommunityCharacters.page(after = first.next, limit = 24)
        val third = second.next?.let { SampleCommunityCharacters.page(after = it, limit = 24) }

        val handed = first.items + second.items + third?.items.orEmpty()
        assertEquals(all, handed)
        assertEquals(24, first.items.size)
    }
}
