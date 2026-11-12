package com.example.magiceightball.core.data.datasource

import android.content.Context
import android.util.Log
import com.example.magiceightball.core.data.config.RemoteAnswers
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
     * [languageCode]: one of the answers the developer keeps in Firebase when
     * this device has them, otherwise one of the bundled ones. Never fails:
     * when no answers can be read it returns a built-in answer in the
     * requested language.
     */
    suspend fun getRandomMessage(
        languageCode: String,
        personality: Magic8BallPersonality
    ): String
}

@Singleton
class JsonLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val remoteAnswers: RemoteAnswers
) : LocalDataSource {

    private val mutex = Mutex()

    // All guarded by [mutex].
    private var bundledData: Magic8BallDataDto? = null
    private var cachedData: Magic8BallDataDto? = null
    private var cachedRemoteText: String? = null
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

    /**
     * The answers in use: the bundled ones, with every list the remote file fills taking the
     * place of the bundled list. Read off the main thread, and worked out again only when the
     * remote file has changed. A failed read of the asset is retried next time.
     */
    private suspend fun loadData(): Magic8BallDataDto? = withContext(Dispatchers.IO) {
        val remoteText = remoteAnswers.json()
        cachedData?.takeIf { remoteText == cachedRemoteText }?.let { return@withContext it }

        val bundled = bundledData ?: parse(ASSET_NAME) {
            context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        }?.also { bundledData = it }
        val remote = remoteText.takeIf { it.isNotBlank() }?.let { text -> parse("remote answers") { text } }

        val data = when {
            bundled == null -> remote?.usable()
            remote == null -> bundled
            else -> bundled.replacedBy(remote.usable())
        }
        if (data != null) {
            cachedData = data
            cachedRemoteText = remoteText
        }
        data
    }

    private fun parse(what: String, read: () -> String): Magic8BallDataDto? = try {
        json.decodeFromString<Magic8BallDataDto>(read())
    } catch (e: IOException) {
        Log.w(TAG, "Could not read $what", e)
        null
    } catch (e: SerializationException) {
        Log.w(TAG, "Could not parse $what", e)
        null
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Unexpected content in $what", e)
        null
    }

    internal companion object {
        private const val TAG = "JsonLocalDataSource"
        private const val ASSET_NAME = "magic_8_ball_responses.json"

        /** How many of the latest answers are kept out of the next draw. */
        const val RECENT_WINDOW = 12

        fun defaultMessage(languageCode: String): String = when (Magic8BallDataDto.baseLanguage(languageCode)) {
            "es" -> "Pregunta más tarde."
            "pt" -> "Pergunte mais tarde."
            else -> "Ask again later."
        }

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
