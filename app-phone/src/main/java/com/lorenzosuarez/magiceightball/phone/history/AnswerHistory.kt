package com.lorenzosuarez.magiceightball.phone.history

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One answer the ball gave.
 *
 * @property question what was asked out loud; empty for a tap or a shake.
 * @property answer the answer as shown; null when none could be obtained and the app gave its own.
 * @property fromAi true when the AI service wrote the answer, false for a bundled one.
 */
data class HistoryEntry(
    val id: Long,
    val question: String,
    val answer: String?,
    val askedAtMillis: Long,
    val fromAi: Boolean
)

/**
 * The latest [MAX_ENTRIES] answers, newest first, kept in one private file on this device and
 * nowhere else: the file is excluded from backups and nothing here touches the network.
 */
@Singleton
class AnswerHistory(private val file: File, private val io: CoroutineDispatcher) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(File(context.filesDir, FILE_NAME), Dispatchers.IO)

    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val entries: StateFlow<List<HistoryEntry>> = _entries.asStateFlow()

    private val mutex = Mutex()
    private var loaded = false

    /** Reads the saved entries. Every other call does it too when this one has not run yet. */
    suspend fun load() = change { it }

    suspend fun add(question: String, answer: String?, askedAtMillis: Long, fromAi: Boolean) = change { old ->
        val entry = HistoryEntry(
            id = (old.maxOfOrNull { it.id } ?: 0L) + 1,
            question = question,
            answer = answer,
            askedAtMillis = askedAtMillis,
            fromAi = fromAi
        )
        (listOf(entry) + old).take(MAX_ENTRIES)
    }

    suspend fun remove(id: Long) = change { old -> old.filterNot { it.id == id } }

    suspend fun clear() = change { emptyList() }

    private suspend fun change(transform: (List<HistoryEntry>) -> List<HistoryEntry>) = mutex.withLock {
        withContext(io) {
            if (!loaded) {
                _entries.value = read()
                loaded = true
            }
            val updated = transform(_entries.value)
            if (updated != _entries.value) {
                _entries.value = updated
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
            val pending = File(file.path + ".tmp")
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

    companion object {
        const val MAX_ENTRIES = 50

        private const val FILE_NAME = "history.bin"
        private const val FORMAT = 1

        /** Far above any question or answer, and far below what writeUTF can hold. */
        private const val MAX_TEXT_LENGTH = 2_000
    }
}
