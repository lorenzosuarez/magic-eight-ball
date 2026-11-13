package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import kotlin.random.Random

/**
 * What changes from one answer to the next, drawn by the app before the AI service is asked.
 * Asked the same thing in the same words, the service answers the same thing: this is what makes
 * every request a different one, with nothing in it that comes from the user.
 *
 * @property verdict what the answer is; the service only words it.
 * @property motif something the character holds against the asker or does, to build the answer
 * around when there is no question to build it around.
 * @property withRemark whether a second line is wanted under the answer: the character's
 * comeback to what was asked, where the screen has room to show it.
 * @property detailed whether that second line is a few sentences rather than one.
 * @property examples lines the character has said for this verdict, in the language of the
 * answer. They show the voice; drawn anew each time so that none is seen often enough to be copied.
 * @property remarkExample a comeback the character has made to some other question.
 */
data class AnswerBrief(
    val verdict: Verdict,
    val motif: String? = null,
    val withRemark: Boolean = false,
    val detailed: Boolean = false,
    val examples: List<String> = emptyList(),
    val remarkExample: SampleRemark? = null
) {
    companion object {
        /**
         * @param hasQuestion whether the user asked something. A question is all the answer needs
         * to be about, and the only thing a remark can be about: without one there is a motif to
         * keep two answers from being alike, and no remark, which would have nothing to land on.
         * @param question what was asked, only to leave out a sample comeback made to a question
         * much like it: shown that one, the service gives it back word for word.
         * @param characters where the character's angles and lines are read from.
         */
        fun draw(
            personality: Magic8BallPersonality,
            hasQuestion: Boolean,
            withRemark: Boolean,
            random: Random = Random.Default,
            languageCode: String = "en",
            question: String? = null,
            characters: CharacterBook = AppCharacters,
            detailed: Boolean = false
        ): AnswerBrief {
            val verdict = Verdict.draw(random)
            val lines = characters.lines(personality, languageCode)
            // A few sentences can stand on the answer alone; one line needs a question to come back to.
            val remarked = withRemark && (hasQuestion || detailed)
            return AnswerBrief(
                verdict = verdict,
                motif = if (hasQuestion) null else anglesOf(personality, characters).randomOrNull(random),
                withRemark = remarked,
                detailed = remarked && detailed,
                examples = lines?.saying(verdict).orEmpty().shuffled(random).take(EXAMPLES_SHOWN),
                remarkExample = if (remarked && !detailed) {
                    lines?.remarks?.filterNot { it.question.isMuchLike(question) }?.randomOrNull(random)
                } else null
            )
        }

        /**
         * What a character may bring up when nothing was asked. One of the user's own has none
         * written for it, and is given some that any character could make its own: without one,
         * every answer to a shake would be asked for in the same words, and come back the same.
         */
        private fun anglesOf(personality: Magic8BallPersonality, characters: CharacterBook): List<String> =
            if (personality == Magic8BallPersonality.CUSTOM) ANYONE_S_ANGLES else characters.sheet(personality)?.angles.orEmpty()

        private val ANYONE_S_ANGLES = listOf(
            "they already knew the answer", "they will ask again until they like the answer",
            "the last thing they asked was stranger", "the hour at which they are asking",
            "how hard they shook", "whoever is next to them right now",
            "what they are putting off by asking", "what they will do the moment they put the ball down"
        )

        /** What each character would bring up unprompted. The plain style has none: it stays plain. */
        internal fun motifsOf(personality: Magic8BallPersonality): List<String> =
            characterOf(personality)?.angles.orEmpty()

        /** Two questions sharing this many words of some length are taken for the same question. */
        private fun String.isMuchLike(other: String?): Boolean =
            other != null && (wordsOf(this) intersect wordsOf(other)).size >= SHARED_WORDS

        private fun wordsOf(text: String): Set<String> =
            text.lowercase().split(Regex("[^\\p{L}]+")).filter { it.length >= WORD_LENGTH }.toSet()

        private const val EXAMPLES_SHOWN = 2
        private const val SHARED_WORDS = 2
        private const val WORD_LENGTH = 3
    }
}
