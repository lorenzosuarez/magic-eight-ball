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
 * @property examples lines the character has said for this verdict, in the language of the
 * answer. They show the voice; drawn anew each time so that none is seen often enough to be copied.
 * @property remarkExample a comeback the character has made to some other question.
 */
data class AnswerBrief(
    val verdict: Verdict,
    val motif: String? = null,
    val withRemark: Boolean = false,
    val examples: List<String> = emptyList(),
    val remarkExample: SampleRemark? = null
) {
    companion object {
        /**
         * @param hasQuestion whether the user asked something. A question is all the answer needs
         * to be about, and the only thing a remark can be about: without one there is a motif to
         * keep two answers from being alike, and no remark, which would have nothing to land on.
         */
        fun draw(
            personality: Magic8BallPersonality,
            hasQuestion: Boolean,
            withRemark: Boolean,
            random: Random = Random.Default,
            languageCode: String = "en"
        ): AnswerBrief {
            val verdict = Verdict.draw(random)
            val lines = linesOf(personality, languageCode)
            val remarked = withRemark && hasQuestion
            return AnswerBrief(
                verdict = verdict,
                motif = if (hasQuestion) null else motifsOf(personality).randomOrNull(random),
                withRemark = remarked,
                examples = lines?.saying(verdict).orEmpty().shuffled(random).take(EXAMPLES_SHOWN),
                remarkExample = if (remarked) lines?.remarks?.randomOrNull(random) else null
            )
        }

        /** What each character would bring up unprompted. The plain style has none: it stays plain. */
        internal fun motifsOf(personality: Magic8BallPersonality): List<String> =
            characterOf(personality)?.angles.orEmpty()

        private const val EXAMPLES_SHOWN = 2
    }
}
