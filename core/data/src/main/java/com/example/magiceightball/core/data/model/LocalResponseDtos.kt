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
    val mystical: List<String> = emptyList()
) {
    fun forPersonality(personality: Magic8BallPersonality): List<String> = when (personality) {
        Magic8BallPersonality.SARCASTIC -> sarcastic
        Magic8BallPersonality.MYSTICAL -> mystical
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

    /** Accepts plain codes and locale tags ("es", "ES", "es-MX", "es_419"). */
    private fun forLanguage(languageCode: String): PersonalityResponsesDto =
        if (isSpanish(languageCode)) es else en

    companion object {
        fun isSpanish(languageCode: String): Boolean =
            languageCode.trim().lowercase().substringBefore('-').substringBefore('_') == "es"
    }
}
