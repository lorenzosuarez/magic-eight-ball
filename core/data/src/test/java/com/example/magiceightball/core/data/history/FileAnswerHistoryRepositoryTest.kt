package com.example.magiceightball.core.data.history

import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository.Companion.MAX_ENTRIES
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class FileAnswerHistoryRepositoryTest {

    @get:Rule
    val files = TemporaryFolder()

    private val file by lazy { files.root.resolve("history.bin") }

    /** A repository over the same file: what one saves, the next one finds. */
    private fun TestScope.open() = FileAnswerHistoryRepository(file, UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `a history that was never written is empty`() = runTest {
        assertEquals(emptyList<Any>(), open().entries.first())
    }

    @Test
    fun `entries are saved newest first and survive a restart`() = runTest {
        val history = open()
        history.add("Will it rain?", "Without a doubt", askedAtMillis = 10L, fromAi = true)
        history.add("", null, askedAtMillis = 20L, fromAi = false)

        val reopened = open().entries.first()
        assertEquals(listOf("", "Will it rain?"), reopened.map { it.question })
        assertEquals(listOf(null, "Without a doubt"), reopened.map { it.answer })
        assertEquals(listOf(20L, 10L), reopened.map { it.askedAtMillis })
        assertEquals(listOf(false, true), reopened.map { it.fromAi })
    }

    @Test
    fun `only the latest entries are kept, each with an id of its own`() = runTest {
        val history = open()
        repeat(MAX_ENTRIES + 5) { history.add("q$it", "a$it", it.toLong(), fromAi = false) }

        val kept = open().entries.first()
        assertEquals(MAX_ENTRIES, kept.size)
        assertEquals("q${MAX_ENTRIES + 4}", kept.first().question)
        assertEquals("q5", kept.last().question)
        assertEquals(kept.size, kept.map { it.id }.toSet().size)
    }

    @Test
    fun `a removed entry can be put back where it was, once`() = runTest {
        val history = open()
        repeat(3) { history.add("q$it", "a$it", it.toLong(), fromAi = false) }
        val before = history.entries.first()
        val middle = before[1]

        history.remove(middle.id)
        assertEquals(listOf("q2", "q0"), open().entries.first().map { it.question })

        history.restore(middle)
        history.restore(middle)
        assertEquals(before, open().entries.first())
    }

    @Test
    fun `an entry removed and put back keeps its place even if another answer came in between`() = runTest {
        val history = open()
        history.add("first", "a", 1L, fromAi = false)
        history.add("newest", "b", 2L, fromAi = false)
        val newest = history.entries.first().first()

        history.remove(newest.id)
        history.add("in between", "c", 3L, fromAi = false)
        history.restore(newest)

        assertEquals(listOf("in between", "newest", "first"), history.entries.first().map { it.question })
    }

    @Test
    fun `clearing leaves nothing behind, not even the file`() = runTest {
        val history = open()
        history.add("q", "a", 1L, fromAi = false)

        history.clear()

        assertEquals(emptyList<Any>(), open().entries.first())
        assertEquals(false, file.exists())
    }

    @Test
    fun `a damaged file is an empty history`() = runTest {
        file.writeBytes(byteArrayOf(0, 0, 0, 1, 0, 0, 0, 9, 1))

        assertEquals(emptyList<Any>(), open().entries.first())
    }
}
