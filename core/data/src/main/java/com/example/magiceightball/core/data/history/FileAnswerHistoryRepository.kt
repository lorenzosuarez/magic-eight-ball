package com.example.magiceightball.core.data.history

import com.example.magiceightball.core.domain.model.HistoryEntry
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

    override val entries: Flow<List<HistoryEntry>> = flow {
        change { it }
        emitAll(state)
    }

    override suspend fun add(question: String, answer: String?, askedAtMillis: Long, fromAi: Boolean) =
        change { old ->
            val entry = HistoryEntry(
                id = (old.maxOfOrNull { it.id } ?: 0L) + 1,
                question = question,
                answer = answer,
                askedAtMillis = askedAtMillis,
                fromAi = fromAi
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
                loaded = true
            }
            val updated = transform(state.value)
            if (updated != state.value) {
                state.value = updated
                write(updated)
            }
        }
    }

    /** A file that is missing, cut short or from another format is an empty history. */
    private fun read(): List<HistoryEntry> = try {
        DataInputStream(file.inputStream().buffered()).use { input ->
            if (input.readInt() != FORMAT) return emptyList()
            List(input.readInt().coerceIn(0, MAX_ENTRIES)) {
                HistoryEntry(
                    id = input.readLong(),
                    question = input.readUTF(),
                    answer = if (input.readBoolean()) input.readUTF() else null,
                    askedAtMillis = input.readLong(),
                    fromAi = input.readBoolean()
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
                }
            }
            if (!pending.renameTo(file)) pending.delete()
        } catch (_: IOException) {
            // The history stays in memory for this run; a full disk is no reason to stop answering.
        }
    }

    private companion object {
        const val FORMAT = 1
        const val PENDING_SUFFIX = ".tmp"

        /** Far above any question or answer, and far below what writeUTF can hold. */
        const val MAX_TEXT_LENGTH = 2_000
    }
}
