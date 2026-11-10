package com.example.magiceightball.core.data.datasource

import android.content.Context
import android.util.Log
import com.example.magiceightball.core.data.model.Magic8BallDataDto
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface LocalDataSource {
    /**
     * Returns a random offline answer written for [personality] in
     * [languageCode]. Never fails: when the bundled answers cannot be read it
     * returns a built-in answer in the requested language.
     */
    suspend fun getRandomMessage(
        languageCode: String,
        personality: Magic8BallPersonality
    ): String
}

@Singleton
class JsonLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : LocalDataSource {

    private val mutex = Mutex()

    // Both guarded by [mutex].
    private var cachedData: Magic8BallDataDto? = null
    private val recentMessages = ArrayDeque<String>()

    override suspend fun getRandomMessage(
        languageCode: String,
        personality: Magic8BallPersonality
    ): String = mutex.withLock {
        val messages = loadData()?.messagesFor(languageCode, personality).orEmpty()
        val message = pickAvoidingRepeat(messages, recentMessages)
            ?: defaultMessage(languageCode)
        recentMessages.addLast(message)
        while (recentMessages.size > RECENT_WINDOW) recentMessages.removeFirst()
        message
    }

    /** Reads and parses the asset once, off the main thread. A failed read is retried next time. */
    private suspend fun loadData(): Magic8BallDataDto? {
        cachedData?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
                json.decodeFromString<Magic8BallDataDto>(text)
            } catch (e: IOException) {
                Log.w(TAG, "Could not read $ASSET_NAME", e)
                null
            } catch (e: SerializationException) {
                Log.w(TAG, "Could not parse $ASSET_NAME", e)
                null
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unexpected content in $ASSET_NAME", e)
                null
            }
        }?.also { cachedData = it }
    }

    internal companion object {
        private const val TAG = "JsonLocalDataSource"
        private const val ASSET_NAME = "magic_8_ball_responses.json"

        /** How many of the latest answers are kept out of the next draw. */
        const val RECENT_WINDOW = 12

        fun defaultMessage(languageCode: String): String =
            if (Magic8BallDataDto.isSpanish(languageCode)) "Pregunta más tarde." else "Ask again later."

        /**
         * Picks a random element that is not among the [recent] answers (oldest
         * first). When the list is too short for that, the oldest answers are
         * allowed again first, so the latest one is never repeated while the
         * list offers an alternative. Returns null when the list is empty.
         */
        fun pickAvoidingRepeat(messages: List<String>, recent: Collection<String>): String? {
            if (messages.isEmpty()) return null
            var excluded = recent.toList()
            while (true) {
                val fresh = messages.filter { it !in excluded }
                if (fresh.isNotEmpty()) return fresh.random()
                if (excluded.isEmpty()) return messages.random()
                excluded = excluded.drop(1)
            }
        }
    }
}
