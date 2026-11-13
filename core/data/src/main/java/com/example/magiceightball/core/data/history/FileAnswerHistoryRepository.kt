package com.example.magiceightball.core.data.history

import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository.Companion.MAX_ENTRIES
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException

/**
 * Keeps the history in one private [file]. Nothing here touches the network, and the app that
 * uses it excludes its files from backups, so the history never leaves the device.
 *
 * The file is read once, the first time it is needed, and rewritten on every change, on [io].
 */
class FileAnswerHistoryRepository(
    private val file: File,
    private val io: CoroutineDispatcher
) : AnswerHistoryRepository {

    private val state = MutableStateFlow<List<HistoryEntry>>(emptyList())
    private val mutex = Mutex()
    private var loaded = false

    /**
     * The highest id given out so far. An id is never given twice, even after its entry was
     * removed, so an entry that is put back can never find its place taken.
     */
    private var lastId = 0L

    override val entries: Flow<List<HistoryEntry>> = flow {
        change { it }
        emitAll(state)
    }

    override suspend fun add(
        question: String,
        answer: String?,
        askedAtMillis: Long,
        fromAi: Boolean,
        style: Magic8BallPersonality?,
        customName: String?
    ) =
        change { old ->
            val entry = HistoryEntry(
                id = ++lastId,
                question = question,
                answer = answer,
                askedAtMillis = askedAtMillis,
                fromAi = fromAi,
                style = style,
                customName = customName?.takeIf { style == Magic8BallPersonality.CUSTOM && it.isNotBlank() }
            )
            (listOf(entry) + old).take(MAX_ENTRIES)
        }

    override suspend fun remove(id: Long) = change { old -> old.filterNot { it.id == id } }

    override suspend fun restore(entry: HistoryEntry) = change { old ->
        if (old.any { it.id == entry.id }) old else (old + entry).sortedByDescending { it.id }.take(MAX_ENTRIES)
    }

    override suspend fun clear() = change { emptyList() }

    private suspend fun change(transform: (List<HistoryEntry>) -> List<HistoryEntry>) = mutex.withLock {
        withContext(io) {
            if (!loaded) {
                state.value = read()
                lastId = state.value.maxOfOrNull { it.id } ?: 0L
                loaded = true
            }
            val updated = transform(state.value)
            if (updated != state.value) {
                state.value = updated
                write(updated)
            }
        }
    }

    /**
     * A file that is missing, cut short or from an unknown format is an empty history. One
     * written before entries kept their style, or the name of a character of the user's own, is
     * read as it is, without them.
     */
    private fun read(): List<HistoryEntry> = try {
        DataInputStream(file.inputStream().buffered()).use { input ->
            val format = input.readInt()
            if (format !in FIRST_FORMAT..FORMAT) return emptyList()
            List(input.readInt().coerceIn(0, MAX_ENTRIES)) {
                HistoryEntry(
                    id = input.readLong(),
                    question = input.readUTF(),
                    answer = if (input.readBoolean()) input.readUTF() else null,
                    askedAtMillis = input.readLong(),
                    fromAi = input.readBoolean(),
                    style = if (format >= STYLED_FORMAT) {
                        input.readUTF().let { name -> Magic8BallPersonality.entries.firstOrNull { it.name == name } }
                    } else null,
                    customName = if (format >= NAMED_FORMAT) input.readUTF().takeIf { it.isNotEmpty() } else null
                )
            }
        }
    } catch (_: IOException) {
        emptyList()
    }

    /** Written beside the file and moved over it, so a write cut short never loses the history. */
    private fun write(entries: List<HistoryEntry>) {
        try {
            if (entries.isEmpty()) {
                file.delete()
                return
            }
            val pending = File(file.path + PENDING_SUFFIX)
            DataOutputStream(pending.outputStream().buffered()).use { output ->
                output.writeInt(FORMAT)
                output.writeInt(entries.size)
                entries.forEach { entry ->
                    output.writeLong(entry.id)
                    output.writeUTF(entry.question.take(MAX_TEXT_LENGTH))
                    output.writeBoolean(entry.answer != null)
                    entry.answer?.let { output.writeUTF(it.take(MAX_TEXT_LENGTH)) }
                    output.writeLong(entry.askedAtMillis)
                    output.writeBoolean(entry.fromAi)
                    output.writeUTF(entry.style?.name.orEmpty())
                    output.writeUTF(entry.customName.orEmpty().take(MAX_TEXT_LENGTH))
                }
            }
            if (!pending.renameTo(file)) pending.delete()
        } catch (_: IOException) {
            // The history stays in memory for this run; a full disk is no reason to stop answering.
        }
    }

    private companion object {
        const val FIRST_FORMAT = 1

        /** Since this format every entry is followed by the name of its style, empty for none. */
        const val STYLED_FORMAT = 2

        /** Since this format the name of a character of the user's own follows, empty for none. */
        const val NAMED_FORMAT = 3
        const val FORMAT = NAMED_FORMAT
        const val PENDING_SUFFIX = ".tmp"

        /** Far above any question or answer, and far below what writeUTF can hold. */
        const val MAX_TEXT_LENGTH = 2_000
    }
}
