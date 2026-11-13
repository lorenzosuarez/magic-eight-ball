package com.example.magiceightball.core.data.datasource

import android.content.Context
import android.util.Log
import com.example.magiceightball.core.data.config.RemoteAnswers
import com.example.magiceightball.core.data.model.Magic8BallDataDto
import com.example.magiceightball.core.data.model.PersonalAnswersDto
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallResult
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
import kotlin.random.Random

interface LocalDataSource {
    /**
     * Returns a random offline answer written for [personality] in
     * [languageCode]: one of the answers the developer keeps in Firebase when
     * this device has them, otherwise one of the bundled ones. Never fails:
     * when no answers can be read it returns a built-in answer in the
     * requested language.
     *
     * Now and then the answer speaks to the [asker]: by name, of their sign or, on the day, of
     * their birthday, which the result then says. Only what [personality] may know is used
     * (see [AskerFacts.knownTo]), only in lines that come with the app, and nothing of it is
     * kept or sent anywhere. Without an asker every answer is an ordinary one.
     */
    suspend fun getRandomMessage(
        languageCode: String,
        personality: Magic8BallPersonality,
        asker: AskerFacts? = null
    ): Magic8BallResult.Success
}

/** @param random where chance comes from: the app's own everywhere but in a test. */
@Singleton
class JsonLocalDataSource(
    private val context: Context,
    private val json: Json,
    private val remoteAnswers: RemoteAnswers,
    private val random: Random
) : LocalDataSource {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        json: Json,
        remoteAnswers: RemoteAnswers
    ) : this(context, json, remoteAnswers, Random.Default)

    private val mutex = Mutex()

    // All guarded by [mutex].
    private var bundledData: Magic8BallDataDto? = null
    private var cachedData: Magic8BallDataDto? = null
    private var cachedRemoteText: String? = null
    private var personalData: Map<String, PersonalAnswersDto>? = null
    private val recentMessages = ArrayDeque<String>()

    override suspend fun getRandomMessage(
        languageCode: String,
        personality: Magic8BallPersonality,
        asker: AskerFacts?
    ): Magic8BallResult.Success = mutex.withLock {
        val ordinary = loadData()?.messagesFor(languageCode, personality).orEmpty()
        val known = asker?.knownTo(personality)
        // Read only once there is someone to speak to: never on the watch.
        val personal = known?.let { loadPersonal() }
            ?.let { it[Magic8BallDataDto.baseLanguage(languageCode)] ?: it["en"] }
        val lines = personal?.styles?.get(personality.name.lowercase())
        // Handed over already down to its letters: the first word of it is what someone is called
        // by, with the capital a name has even when it was typed without.
        val name = known?.firstName?.substringBefore(' ')?.replaceFirstChar { it.titlecase() }
        val signs = personal?.signs.orEmpty()
        val sign = known?.zodiac?.let { signs[it.name.lowercase()] }

        // One of [written] as it reads for this asker: only where it fits, and never twice running.
        fun said(written: List<String>?): String? =
            pickAvoidingRepeat(written.orEmpty().mapNotNull { filled(it, name, sign) }, recentMessages, random)
                ?.takeIf { it != recentMessages.lastOrNull() }

        val signOneIn = if (personality == Magic8BallPersonality.ASTRO) ASTRO_SIGN_ONE_IN else SIGN_ONE_IN
        // A character that knows the sign does not guess at one.
        // Unless every line it has names one, as a list sent from afar might: then they stand.
        val unguessing = ordinary.filterNot { sign != null && namesOneOf(it, signs.values) }.ifEmpty { ordinary }
        val birthday = if (known?.birthdayToday == true) said(lines?.birthday) else null
        val message = birthday
            ?: said(lines?.name?.takeIf { name != null && random.nextInt(NAME_ONE_IN) == 0 })
            ?: said(lines?.sign?.takeIf { sign != null && random.nextInt(signOneIn) == 0 })
            ?: pickAvoidingRepeat(unguessing, recentMessages, random)
            ?: defaultMessage(languageCode)
        recentMessages.addLast(message)
        while (recentMessages.size > RECENT_WINDOW) recentMessages.removeFirst()
        Magic8BallResult.Success(message, birthdayLine = birthday != null)
    }

    /**
     * The answers in use: the bundled ones, with every list the remote file fills taking the
     * place of the bundled list. Read off the main thread, and worked out again only when the
     * remote file has changed. A failed read of the asset is retried next time.
     */
    private suspend fun loadData(): Magic8BallDataDto? = withContext(Dispatchers.IO) {
        val remoteText = remoteAnswers.json()
        cachedData?.takeIf { remoteText == cachedRemoteText }?.let { return@withContext it }

        val bundled = bundledData
            ?: parse<Magic8BallDataDto>(ASSET_NAME) { asset(ASSET_NAME) }?.also { bundledData = it }
        val remote = remoteText.takeIf { it.isNotBlank() }
            ?.let { text -> parse<Magic8BallDataDto>("remote answers") { text } }

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

    /**
     * The lines that speak to the asker, by language: they come with the app only, and are read
     * once, off the main thread. A failed read is retried next time.
     */
    private suspend fun loadPersonal(): Map<String, PersonalAnswersDto>? =
        personalData ?: withContext(Dispatchers.IO) {
            parse<Map<String, PersonalAnswersDto>>(PERSONAL_ASSET_NAME) { asset(PERSONAL_ASSET_NAME) }
        }?.also { personalData = it }

    private fun asset(name: String): String = context.assets.open(name).bufferedReader().use { it.readText() }

    private inline fun <reified T> parse(what: String, read: () -> String): T? = try {
        json.decodeFromString<T>(read())
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
        private const val PERSONAL_ASSET_NAME = "magic_8_ball_personal.json"
        private const val NAME = "{name}"
        private const val SIGN = "{sign}"
        private val NOT_A_LETTER = Regex("\\P{L}+")

        /** How many of the latest answers are kept out of the next draw. */
        const val RECENT_WINDOW = 12

        /** As long as a bundled line may be to read well on the ball: with a name or a sign in it, once filled. */
        const val MAX_PERSONAL_LENGTH = 28

        /** One answer in this many calls the asker by name: now and then, as someone would. */
        const val NAME_ONE_IN = 3

        /** One answer in this many speaks of the asker's sign: the astrologer's trade, a habit of the oracle's. */
        const val ASTRO_SIGN_ONE_IN = 2
        const val SIGN_ONE_IN = 4

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
        fun pickAvoidingRepeat(
            messages: List<String>,
            recent: Collection<String>,
            random: Random = Random.Default
        ): String? {
            if (messages.isEmpty()) return null
            var excluded = recent.toList()
            while (true) {
                val fresh = messages.filter { it !in excluded }
                if (fresh.isNotEmpty()) return fresh.random(random)
                if (excluded.isEmpty()) return messages.random(random)
                excluded = excluded.drop(1)
            }
        }

        /**
         * [line] as it reads with the asker's [name] and [sign] in it. Null when it asks for one
         * that is not known, or when it no longer fits the ball: a long name is left out, not cut.
         */
        fun filled(line: String, name: String?, sign: String?): String? {
            if ((NAME in line && name == null) || (SIGN in line && sign == null)) return null
            val text = line.replace(NAME, name.orEmpty()).replace(SIGN, sign.orEmpty())
            val fits = text.codePointCount(0, text.length) <= MAX_PERSONAL_LENGTH &&
                text.split(' ').size <= Magic8BallDataDto.MAX_ANSWER_WORDS
            return text.takeIf { fits }
        }

        /**
         * Whether one of [signs] is among the words of [line], in capitals or not, of one person
         * or of many ("Virgos"). A word that only begins like a sign, as "library" does, is not one.
         */
        fun namesOneOf(line: String, signs: Collection<String>): Boolean =
            line.split(NOT_A_LETTER).any { word ->
                signs.any { it.equals(word, ignoreCase = true) || "${it}s".equals(word, ignoreCase = true) }
            }
    }
}
