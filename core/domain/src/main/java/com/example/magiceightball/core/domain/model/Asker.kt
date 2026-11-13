package com.example.magiceightball.core.domain.model

import java.text.Normalizer

/** The twelve signs, by the dates the horoscope pages use. */
enum class Zodiac {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES;

    companion object {
        /** The sign of someone born on [day] of [month], from 1 to 12. */
        fun of(month: Int, day: Int): Zodiac {
            require(month in 1..12) { "No such month: $month" }
            return if (day >= FIRST_DAY[month - 1]) STARTING_IN[month - 1] else STARTING_IN[(month + 10) % 12]
        }

        /** The sign that starts in each month, January first, and the day it starts on. */
        private val STARTING_IN = listOf(
            AQUARIUS, PISCES, ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN
        )
        private val FIRST_DAY = listOf(20, 19, 21, 20, 21, 21, 23, 23, 23, 23, 22, 22)
    }
}

/**
 * What a character may be told about whoever is asking. There is none of it without an account
 * and without the user having agreed, on the screen that says so, to its being sent.
 *
 * The date of birth has no place here: only what was worked out from it on the device.
 *
 * @property firstName as the user typed it; see [nameForAi] for the form in which it is sent.
 * @property birthdayToday whether to say so in this answer: once on the day, not in every one.
 */
data class AskerFacts(
    val firstName: String? = null,
    val zodiac: Zodiac? = null,
    val birthdayToday: Boolean = false
) {
    /**
     * The only form in which the name may leave the device: its letters, at most two words and
     * [MAX_NAME_LENGTH] characters, or [MAX_DENSE_NAME_LENGTH] in a script that writes a whole
     * sentence without a space. Anything else typed into the field is dropped: what is not a
     * letter, letters that only look like punctuation, and marks piled on a letter. What is
     * left can be read as little more than a name, and the instruction says to take it for
     * nothing else. Null when nothing is left.
     */
    val nameForAi: String?
        get() {
            val letters = Normalizer.normalize(firstName ?: return null, Normalizer.Form.NFC)
                .replace(NOT_OF_A_NAME, " ")
                .replace(STACKED_MARKS, "$1")
            val name = letters.split(' ')
                .filter { word -> word.codePoints().anyMatch(Character::isLetter) }
                .take(MAX_NAME_WORDS)
                .joinToString(" ")
            val longest = if (name.codePoints().anyMatch(::isOfADenseScript)) MAX_DENSE_NAME_LENGTH else MAX_NAME_LENGTH
            return cut(name, longest).trim().takeIf { it.isNotEmpty() }
        }

    /**
     * What [personality] makes use of, and nothing more: the rest is not sent. A name is for
     * the characters that talk to someone, a sign for those that read the stars, and a birthday
     * for all of them but the plain one, which knows nobody. Null when there is nothing to tell.
     */
    fun knownTo(personality: Magic8BallPersonality): AskerFacts? {
        if (personality == Magic8BallPersonality.NEUTRAL) return null
        return AskerFacts(
            firstName = nameForAi.takeIf { personality in CALL_BY_NAME },
            zodiac = zodiac.takeIf { personality in READ_THE_STARS },
            birthdayToday = birthdayToday
        ).takeIf { it.firstName != null || it.zodiac != null || it.birthdayToday }
    }

    companion object {
        const val MAX_NAME_LENGTH = 20

        /** Six signs are a name in Chinese, Japanese or Korean, and not yet much of a sentence. */
        const val MAX_DENSE_NAME_LENGTH = 6
        private const val MAX_NAME_WORDS = 2

        /**
         * Everything but letters, their accents, and the two marks a name can carry. The
         * letters that modify others are left out with the rest: some of them are drawn as
         * quotes and colons.
         */
        private val NOT_OF_A_NAME = Regex("[^\\p{L}\\p{M}'’-]+|\\p{Lm}+")

        /** A letter carries one accent: a second and a third are not part of anyone's name. */
        private val STACKED_MARKS = Regex("(\\p{M})\\p{M}+")

        private val DENSE_SCRIPTS = setOf(
            Character.UnicodeScript.HAN,
            Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA,
            Character.UnicodeScript.HANGUL,
            Character.UnicodeScript.BOPOMOFO
        )

        private fun isOfADenseScript(codePoint: Int): Boolean = Character.UnicodeScript.of(codePoint) in DENSE_SCRIPTS

        /** [text] down to its first [length] characters, never half of one. */
        fun cut(text: String, length: Int = MAX_NAME_LENGTH): String =
            if (text.codePointCount(0, text.length) <= length) text else text.substring(0, text.offsetByCodePoints(0, length))

        private val CALL_BY_NAME = setOf(
            Magic8BallPersonality.THERAPIST,
            Magic8BallPersonality.GRANDMA,
            Magic8BallPersonality.COACH,
            Magic8BallPersonality.LOVESTRUCK,
            // A character of the user's own talks to the user, whoever it is.
            Magic8BallPersonality.CUSTOM
        )
        private val READ_THE_STARS = setOf(Magic8BallPersonality.ASTRO, Magic8BallPersonality.MYSTICAL)
    }
}
