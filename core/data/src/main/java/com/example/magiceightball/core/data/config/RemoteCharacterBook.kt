package com.example.magiceightball.core.data.config

import android.util.Log
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.policy.AppCharacters
import com.example.magiceightball.core.domain.policy.CharacterBook
import com.example.magiceightball.core.domain.policy.CharacterLines
import com.example.magiceightball.core.domain.policy.CharacterSheet
import com.example.magiceightball.core.domain.policy.PromptRules
import com.example.magiceightball.core.domain.policy.SampleRemark
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The characters as the developer last left them in Firebase, over the ones the app ships with.
 *
 * The file may hold any part of a character and nothing else needs to be there: whatever it
 * leaves out, and whatever it gets wrong, stays the app's own. So a slip in the console can make
 * a character a little worse; it cannot leave the app without one.
 *
 * ```
 * {
 *   "rules": { "craft": "...", "form": "...", "examples": "...", "remark": "...", "question_craft": "..." },
 *   "characters": {
 *     "pirate": {
 *       "who": "...", "wants": "...", "yes": "...", "no": "...", "dodge": "...", "habits": "...", "never": "...",
 *       "angles": ["...", "..."],
 *       "lines": {
 *         "es": {
 *           "yes": ["...", "..."], "no": ["...", "..."], "maybe": ["...", "..."],
 *           "remarks": [{ "q": "a question", "a": "the comeback" }]
 *         }
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * A character is named by its style in lower case. The classic style has no character and
 * takes none from here.
 */
@Singleton
class RemoteCharacterBook @Inject constructor(
    private val remote: RemoteCharacters,
    private val json: Json
) : CharacterBook {

    // Worked out again only when the file has changed. Guarded by this.
    private var readText: String? = null
    private var read: CharactersDto? = null

    override fun sheet(personality: Magic8BallPersonality): CharacterSheet? {
        val own = AppCharacters.sheet(personality) ?: return null
        val theirs = changesTo(personality) ?: return own
        return own.copy(
            who = theirs.who.usableText() ?: own.who,
            wants = theirs.wants.usableText() ?: own.wants,
            yes = theirs.yes.usableText() ?: own.yes,
            no = theirs.no.usableText() ?: own.no,
            dodge = theirs.dodge.usableText() ?: own.dodge,
            habits = theirs.habits.usableText() ?: own.habits,
            never = theirs.never.usableText() ?: own.never,
            angles = theirs.angles.usableList(MIN_ANGLES, MAX_ANGLE_LENGTH) ?: own.angles
        )
    }

    override fun lines(personality: Magic8BallPersonality, languageCode: String): CharacterLines? {
        val own = AppCharacters.lines(personality, languageCode) ?: return null
        val theirs = changesTo(personality)?.lines?.get(languageCode) ?: return own
        return own.copy(
            yes = theirs.yes.usableAnswers() ?: own.yes,
            no = theirs.no.usableAnswers() ?: own.no,
            maybe = theirs.maybe.usableAnswers() ?: own.maybe,
            remarks = theirs.remarks.orEmpty()
                .filter { it.q.isNotBlank() && it.a.isNotBlank() && it.a.wordCount() <= MAX_REMARK_WORDS }
                .map { SampleRemark(it.q.trim(), it.a.trim()) }
                .takeIf { it.isNotEmpty() } ?: own.remarks
        )
    }

    override val rules: PromptRules
        get() {
            val own = PromptRules()
            val theirs = current()?.rules ?: return own
            return own.copy(
                craft = theirs.craft.usableText(MAX_RULE_LENGTH) ?: own.craft,
                form = theirs.form.usableText(MAX_RULE_LENGTH) ?: own.form,
                examples = theirs.examples.usableText(MAX_RULE_LENGTH) ?: own.examples,
                remark = theirs.remark.usableText(MAX_RULE_LENGTH) ?: own.remark,
                questionCraft = theirs.questionCraft.usableText(MAX_RULE_LENGTH) ?: own.questionCraft
            )
        }

    private fun changesTo(personality: Magic8BallPersonality): CharacterDto? =
        current()?.characters?.get(personality.name.lowercase())

    /** The file as last read. One that cannot be read is no file: it is not tried again until it changes. */
    @Synchronized
    private fun current(): CharactersDto? {
        val text = remote.charactersJson()
        if (text == readText) return read
        readText = text
        read = text.takeIf { it.isNotBlank() }?.let {
            try {
                json.decodeFromString<CharactersDto>(it)
            } catch (e: SerializationException) {
                Log.w(TAG, "Could not parse the remote characters", e)
                null
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unexpected content in the remote characters", e)
                null
            }
        }
        return read
    }

    /** A sentence or two of the instruction: there, and not so long that it drowns the rest. */
    private fun String?.usableText(max: Int = MAX_FIELD_LENGTH): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && it.length <= max }

    private fun List<String>?.usableList(min: Int, maxLength: Int): List<String>? =
        this?.map { it.trim() }
            ?.filter { it.isNotEmpty() && it.length <= maxLength }
            ?.distinct()
            ?.takeIf { it.size >= min }

    /** Lines shown as the voice are held to what an answer is held to, or they teach the wrong length. */
    private fun List<String>?.usableAnswers(): List<String>? =
        this?.map { it.trim() }
            ?.filter { it.isNotEmpty() && it.wordCount() <= MAX_ANSWER_WORDS && it.length <= MAX_ANSWER_LENGTH }
            ?.distinct()
            ?.takeIf { it.size >= MIN_ANSWERS }

    private fun String.wordCount(): Int = trim().split(WHITESPACE).count { it.isNotBlank() }

    private companion object {
        const val TAG = "RemoteCharacterBook"
        val WHITESPACE = Regex("\\s+")

        const val MAX_FIELD_LENGTH = 600
        const val MAX_RULE_LENGTH = 1200
        const val MIN_ANGLES = 3
        const val MAX_ANGLE_LENGTH = 120

        /** Two are shown at a time: fewer than that is not a list to draw from. */
        const val MIN_ANSWERS = 2
        const val MAX_ANSWER_WORDS = 6
        const val MAX_ANSWER_LENGTH = 40
        const val MAX_REMARK_WORDS = 12
    }
}

@Serializable
internal data class CharactersDto(
    val rules: RulesDto? = null,
    val characters: Map<String, CharacterDto> = emptyMap()
)

@Serializable
internal data class RulesDto(
    val craft: String? = null,
    val form: String? = null,
    val examples: String? = null,
    val remark: String? = null,
    @SerialName("question_craft") val questionCraft: String? = null
)

@Serializable
internal data class CharacterDto(
    val who: String? = null,
    val wants: String? = null,
    val yes: String? = null,
    val no: String? = null,
    val dodge: String? = null,
    val habits: String? = null,
    val never: String? = null,
    val angles: List<String>? = null,
    val lines: Map<String, LinesDto> = emptyMap()
)

@Serializable
internal data class LinesDto(
    val yes: List<String>? = null,
    val no: List<String>? = null,
    val maybe: List<String>? = null,
    val remarks: List<RemarkDto>? = null
)

@Serializable
internal data class RemarkDto(val q: String = "", val a: String = "")
