package com.example.magiceightball.core.domain.model

/**
 * A character the user made up: a name, how it answers in the user's own words, and what it
 * looks like, picked among what the app's own characters wear. It belongs to an account and
 * nobody else ever sees it. There are at most [MAX_COUNT] to an account, one to a slot.
 *
 * It answers under [Magic8BallPersonality.CUSTOM], which says only that the character is one of
 * the user's own; which one it is, is this.
 *
 * @property id the slot it is kept in, one of [SLOTS].
 * @property name as the user typed it, tidied: see [nameOrNull].
 * @property description how it answers, as the user typed it, tidied: see [descriptionOrNull].
 * @property colour whose colour its card has, among the app's characters.
 * @property emblem whose accessory its ball wears; [Magic8BallPersonality.NEUTRAL] for none.
 */
data class CustomCharacter(
    val id: String,
    val name: String,
    val description: String,
    val colour: Magic8BallPersonality = Magic8BallPersonality.NEUTRAL,
    val emblem: Magic8BallPersonality = Magic8BallPersonality.NEUTRAL
) {
    /**
     * The only form in which the name is put in what the AI service is asked: its letters and
     * digits, on one line. Null when nothing is left, and then the character has no name there.
     */
    val nameForAi: String?
        get() = name.replace(NOT_OF_A_NAME, " ").trim().replace(SPACES, " ").takeIf { it.isNotEmpty() }

    /**
     * The only form in which the description is put in what the AI service is asked: one line,
     * with no control characters and none of the brackets it is quoted between, so that it
     * cannot close its own quotation. Null when nothing is left to say how it answers.
     */
    val descriptionForAi: String?
        get() = description
            .replace(BRACKETS, " ")
            .replace(LINE_BREAKS_AND_CONTROLS, " ")
            .trim()
            .takeIf { it.any(Char::isLetter) }

    companion object {
        const val MAX_COUNT = 5
        const val MAX_NAME_LENGTH = 16
        const val MAX_DESCRIPTION_LENGTH = 280

        /** Where the characters of an account are kept, and so how many there can be. */
        val SLOTS: List<String> = (1..MAX_COUNT).map(Int::toString)

        private val SPACES = Regex("\\s+")
        private val LINE_BREAKS_AND_CONTROLS = Regex("[\\s\\p{Cntrl}]+")
        private val BRACKETS = Regex("[\\[\\]]+")
        private val NOT_OF_A_NAME = Regex("[^\\p{L}\\p{M}\\p{Nd} '’-]+")

        /** [typed] as a name is kept, or null when there is no name in it: one line, a letter or a digit at least. */
        fun nameOrNull(typed: String): String? =
            AskerFacts.cut(typed.replace(LINE_BREAKS_AND_CONTROLS, " ").trim(), MAX_NAME_LENGTH).trim()
                .takeIf { name -> name.any(Char::isLetterOrDigit) }

        /** [typed] as a description is kept, or null when it says nothing: a few words at least. */
        fun descriptionOrNull(typed: String): String? =
            AskerFacts.cut(typed.trim(), MAX_DESCRIPTION_LENGTH).trim()
                .takeIf { text -> text.count(Char::isLetter) >= MIN_DESCRIPTION_LETTERS }

        /** Fewer letters than this say nothing about how anyone answers. */
        private const val MIN_DESCRIPTION_LETTERS = 10

        /** Which of the app's characters may lend a colour or an accessory: all but the user's own. */
        val LOOKS: List<Magic8BallPersonality> = Magic8BallPersonality.entries - Magic8BallPersonality.CUSTOM
    }
}
