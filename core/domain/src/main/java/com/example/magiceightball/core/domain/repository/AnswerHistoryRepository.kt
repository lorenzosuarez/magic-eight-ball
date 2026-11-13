package com.example.magiceightball.core.domain.repository

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * The latest answers the ball gave, kept on this device and nowhere else. At most
 * [MAX_ENTRIES] are kept: a new one pushes the oldest out.
 */
interface AnswerHistoryRepository {
    /** The saved entries, newest first, then every change to them. */
    val entries: Flow<List<HistoryEntry>>

    /**
     * @param style the style that gave the answer, when one did.
     * @param customName the name of the character, when it was one of the user's own.
     */
    suspend fun add(
        question: String,
        answer: String?,
        askedAtMillis: Long,
        fromAi: Boolean,
        style: Magic8BallPersonality? = null,
        customName: String? = null
    )

    suspend fun remove(id: Long)

    /** Puts back an entry that was removed, where it was. Does nothing if it is still there. */
    suspend fun restore(entry: HistoryEntry)

    suspend fun clear()

    companion object {
        const val MAX_ENTRIES = 50
    }
}
