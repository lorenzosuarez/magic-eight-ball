package com.example.magiceightball.core.data.model

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import kotlinx.serialization.Serializable

/**
 * Offline answers of one language, split by personality.
 *
 * Every list defaults to empty so a partially filled asset still decodes; the
 * lookup in [Magic8BallDataDto.messagesFor] then falls back to another list.
 */
@Serializable
data class PersonalityResponsesDto(
    val sarcastic: List<String> = emptyList(),
    val mystical: List<String> = emptyList(),
    val neutral: List<String> = emptyList()
) {
    /** Only what can be shown on the ball: see [Magic8BallDataDto.usable]. */
    fun usable(): PersonalityResponsesDto = PersonalityResponsesDto(
        sarcastic = sarcastic.usableAnswers(),
        mystical = mystical.usableAnswers(),
        neutral = neutral.usableAnswers()
    )

    /** These lists, with every one that [other] fills taking the place of this one's. */
    fun replacedBy(other: PersonalityResponsesDto): PersonalityResponsesDto = PersonalityResponsesDto(
        sarcastic = other.sarcastic.ifEmpty { sarcastic },
        mystical = other.mystical.ifEmpty { mystical },
        neutral = other.neutral.ifEmpty { neutral }
    )

    private fun List<String>.usableAnswers(): List<String> = map { it.trim() }
        .filter { answer ->
            answer.isNotEmpty() && answer.length <= Magic8BallDataDto.MAX_ANSWER_LENGTH &&
                answer.split(WHITESPACE).size <= Magic8BallDataDto.MAX_ANSWER_WORDS
        }
        .distinct()

    fun forPersonality(personality: Magic8BallPersonality): List<String> = when (personality) {
        Magic8BallPersonality.SARCASTIC -> sarcastic
        Magic8BallPersonality.MYSTICAL -> mystical
        Magic8BallPersonality.NEUTRAL -> neutral
    }
}

/** Root of `assets/magic_8_ball_responses.json`. */
@Serializable
data class Magic8BallDataDto(
    val en: PersonalityResponsesDto = PersonalityResponsesDto(),
    val es: PersonalityResponsesDto = PersonalityResponsesDto()
) {
    /**
     * Answers for [languageCode] and [personality].
     *
     * Fallback order when a list is missing or empty: the same personality in
     * English (keeps the voice), then the other personality in the requested
     * language, then the other personality in English. Returns an empty list
     * only when the asset holds no usable answer at all.
     */
    fun messagesFor(languageCode: String, personality: Magic8BallPersonality): List<String> {
        val requested = forLanguage(languageCode)
        val other = Magic8BallPersonality.entries.filter { it != personality }
        val candidates = sequence {
            yield(requested.forPersonality(personality))
            yield(en.forPersonality(personality))
            other.forEach { yield(requested.forPersonality(it)) }
            other.forEach { yield(en.forPersonality(it)) }
        }
        return candidates
            .map { list -> list.filter { it.isNotBlank() } }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
    }

    /**
     * Only the answers that can be shown on the ball: trimmed, not empty, not repeated, and
     * within [MAX_ANSWER_WORDS] words and [MAX_ANSWER_LENGTH] characters. Answers that come
     * from outside the app go through this, so a slip in the console never reaches the die.
     */
    fun usable(): Magic8BallDataDto = Magic8BallDataDto(en = en.usable(), es = es.usable())

    /** These answers, with every list that [other] fills taking the place of this one's. */
    fun replacedBy(other: Magic8BallDataDto): Magic8BallDataDto =
        Magic8BallDataDto(en = en.replacedBy(other.en), es = es.replacedBy(other.es))

    /** Accepts plain codes and locale tags ("es", "ES", "es-MX", "es_419"). */
    private fun forLanguage(languageCode: String): PersonalityResponsesDto =
        if (isSpanish(languageCode)) es else en

    companion object {
        /** As many words as the AI service is allowed, and what still reads well on the die. */
        const val MAX_ANSWER_WORDS = 6
        const val MAX_ANSWER_LENGTH = 40

        fun isSpanish(languageCode: String): Boolean =
            languageCode.trim().lowercase().substringBefore('-').substringBefore('_') == "es"
    }
}

private val WHITESPACE = Regex("\\s+")
