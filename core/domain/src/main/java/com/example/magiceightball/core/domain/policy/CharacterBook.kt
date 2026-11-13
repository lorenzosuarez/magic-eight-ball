package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * Where the characters are read from: who each one is, what it has said, and how an answer is
 * asked for. The app ships with its own ([AppCharacters]); the developer can change any part of
 * them from afar without a release, and whatever is not changed stays the app's.
 */
interface CharacterBook {
    /** Who lives in the ball for a style. The classic style has nobody: it answers like the toy. */
    fun sheet(personality: Magic8BallPersonality): CharacterSheet?

    /** What that character has said in a language, to show its voice. */
    fun lines(personality: Magic8BallPersonality, languageCode: String): CharacterLines?

    /** How an answer is to be written, whoever writes it. */
    val rules: PromptRules get() = PromptRules()
}

/** The characters as they ship with the app. */
object AppCharacters : CharacterBook {
    override fun sheet(personality: Magic8BallPersonality): CharacterSheet? = characterOf(personality)

    override fun lines(personality: Magic8BallPersonality, languageCode: String): CharacterLines? =
        linesOf(personality, languageCode)
}

/**
 * The parts of the instruction that say how an answer is written, apart from who writes it.
 * Each is a run of sentences added to the instruction as it is.
 *
 * What guards the instruction against the question, what a toy does not answer and the limit
 * of six words are not among them: those are not to be loosened from afar.
 *
 * @property craft what every character is held to, whoever it is.
 * @property form how a drawn answer is worded: plain enough for a ball, with room for one turn.
 * @property examples what introduces the lines shown of the character's voice.
 * @property remark how the second line, the comeback to the question, is asked for.
 * @property reading how the second line is asked for when the user wants to hear more.
 * @property questionCraft how a spoken question is played on.
 */
data class PromptRules(
    val craft: String = CRAFT,
    val form: String = FORM,
    val examples: String = EXAMPLES,
    val remark: String = REMARK,
    val reading: String = READING,
    val questionCraft: String = QUESTION_CRAFT
) {
    companion object {
        const val CRAFT = "You are talking to the person holding the ball, about them: do not describe" +
            " yourself or list things from your world. You may tease their habits and choices, never their body," +
            " looks, identity, intelligence or worth. Never assume the asker's gender."

        const val FORM = "Give that answer as only this character would. It is read inside a ball: three to five" +
            " words, six at the very most, nothing cryptic. Its first words make plain whether it is a yes, a no" +
            " or a doubt, said the way this character says it; the rest is one small turn of your own." +
            " It is never the bare verdict alone."

        const val EXAMPLES = "Lines this character has said, to show the voice only. Never use one of them" +
            " or its idea:"

        /**
         * Called the punchline, the second line took the whole joke and left a bare "No." on the
         * ball: line 1 is asked to stand alone. A remark is asked for only when there is a
         * question, since a remark about nothing is one more thing to read and puzzle over; and
         * the service may leave it out rather than write one that says nothing.
         */
        const val REMARK = "Write one or two lines and nothing else. Line 1 is the answer as it shows on the ball:" +
            " every word limit above applies to line 1 only, and line 1 is written as if it stood alone." +
            " Line 2 is the character's comeback to what they asked: one sentence of at most 12 words, said to" +
            " the asker, that names something from their question and would make no sense for any other." +
            " It adds something new and never says line 1 again. If no such line comes, write line 1 alone." +
            " No labels, no quotation marks, no emoji."

        /** The longer second line: what the character makes of it, for those who asked to hear more. */
        const val READING = "Write two lines and nothing else. Line 1 is the answer as it shows on the ball:" +
            " every word limit above applies to line 1 only, and line 1 is written as if it stood alone." +
            " Line 2 is what this character makes of it, said to the asker: two or three short sentences," +
            " 25 to 40 words in all, on one line. When they asked something, it names something from their" +
            " question and would make no sense for any other; when nothing was asked, it says why the ball" +
            " lands there today and what to do about it. It has one turn that makes them smile, ends on" +
            " something they can do or look out for, and never says line 1 again." +
            " No labels, no quotation marks, no emoji, no lists."

        const val QUESTION_CRAFT = "When a word of what they asked fits your character, play on it." +
            " Make it plain whether you lean yes, no or maybe."
    }
}
