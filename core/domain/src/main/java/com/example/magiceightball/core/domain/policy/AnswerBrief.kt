package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import kotlin.random.Random

/**
 * What changes from one answer to the next, drawn by the app before the AI service is asked.
 * Asked the same thing in the same words, the service answers the same thing: this is what makes
 * every request a different one, with nothing in it that comes from the user.
 *
 * @property verdict what the answer is; the service only words it.
 * @property motif something from the character's own world to build the answer around, when
 * there is no question to build it around.
 * @property withRemark whether a second line is wanted under the answer: the character's
 * comeback to what was asked, where the screen has room to show it.
 */
data class AnswerBrief(
    val verdict: Verdict,
    val motif: String? = null,
    val withRemark: Boolean = false
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
            random: Random = Random.Default
        ): AnswerBrief = AnswerBrief(
            verdict = Verdict.draw(random),
            motif = if (hasQuestion) null else motifsOf(personality).randomOrNull(random),
            withRemark = withRemark && hasQuestion
        )

        /** Things each character would bring up unprompted. The plain style has none: it stays plain. */
        internal fun motifsOf(personality: Magic8BallPersonality): List<String> = when (personality) {
            Magic8BallPersonality.NEUTRAL -> emptyList()
            Magic8BallPersonality.SARCASTIC -> listOf(
                "Mondays", "the group chat", "a gym membership nobody uses", "slow wifi", "a diet that starts tomorrow",
                "autocorrect", "a meeting that could have been an email", "the snooze button", "a parking ticket",
                "low phone battery", "terms and conditions nobody reads", "an unread inbox"
            )
            Magic8BallPersonality.MYSTICAL -> listOf(
                "a raven on a fence", "the tide going out", "a candle that will not stay lit", "tea leaves", "an old oak",
                "the third star", "a crossroads at dusk", "a cracked mirror", "smoke rising straight up", "a sleeping wolf",
                "frost on the window", "a coin found on the ground"
            )
            Magic8BallPersonality.ASTRO -> listOf(
                "Mercury retrograde", "the rising sign", "a full moon", "Saturn's return", "Venus", "the seventh house",
                "an eclipse", "Mars", "the birth chart", "Jupiter", "a new moon", "the asker's sign, which you guess"
            )
            Magic8BallPersonality.GRANDMA -> listOf(
                "a sweater", "soup", "the neighbor's gossip", "a candle at church", "her soap opera", "leftovers to take home",
                "a cousin who did better", "the photo album", "her knees predicting rain", "bingo night", "a home remedy",
                "nobody calling her anymore"
            )
            Magic8BallPersonality.LOVESTRUCK -> listOf(
                "a poem written for them", "their smile", "the moon", "a love letter never sent", "a first date",
                "butterflies", "their name doodled in a notebook", "a song that reminds you of them", "holding hands",
                "a rival who does not stand a chance", "Cupid", "flowers left at their door"
            )
            Magic8BallPersonality.DRAMA -> listOf(
                "an evil twin", "amnesia", "an interrupted wedding", "the will", "a slap", "the hacienda",
                "a letter that changes everything", "a faked death", "a secret child", "a staircase", "a single tear",
                "the family fortune"
            )
            Magic8BallPersonality.POET -> listOf(
                "a rose", "the lark", "a quill out of ink", "the muse", "a sonnet's last line", "the falling curtain",
                "a moonlit balcony", "autumn leaves", "a sigh", "a rhyme that will not come", "a candle", "the stars"
            )
            Magic8BallPersonality.PIRATE -> listOf(
                "the parrot", "the kraken", "a barrel of rum", "a torn map", "a wooden leg", "a mutiny", "buried doubloons",
                "the plank", "a cursed compass", "a storm on the horizon", "a mermaid", "a rival captain"
            )
            Magic8BallPersonality.DETECTIVE -> listOf(
                "an alibi", "a fingerprint", "the butler", "rain on the window", "cold coffee", "a matchbook", "a witness",
                "a locked room", "a missing glove", "a hunch", "a case from years ago", "the chief"
            )
            Magic8BallPersonality.VILLAIN -> listOf(
                "the lair", "the minions", "a trapdoor", "a monologue", "the lava pit", "a doomsday device",
                "the hero, annoyingly", "a white cat", "phase two", "an evil laugh", "a self-destruct button", "world domination"
            )
            Magic8BallPersonality.ROBOT -> listOf(
                "a probability", "an error code", "low battery", "a firmware update", "a loading bar", "a sensor",
                "a reboot", "a cooling fan", "a missing driver", "a human it cannot understand", "a checksum", "a beep"
            )
            Magic8BallPersonality.PHILOSOPHER -> listOf(
                "Sisyphus", "Plato's cave", "Kant", "Nietzsche", "Schrödinger's cat", "Diogenes", "free will",
                "the void", "Socrates", "the examined life", "Descartes", "the absurd"
            )
            Magic8BallPersonality.CONSPIRACY -> listOf(
                "pigeons that are listening", "a tinfoil hat", "red string on a corkboard", "THEM", "the lizards",
                "a forum post", "the moon", "erased files", "a van parked outside", "a jammed signal",
                "a suspicious barcode", "the vending machine"
            )
            Magic8BallPersonality.COACH -> listOf(
                "leg day", "one more rep", "the scoreboard", "overtime", "hydration", "a personal best", "the warm-up",
                "the finish line", "a protein shake", "the bench", "game film", "the whistle"
            )
            Magic8BallPersonality.ZOOMER -> listOf(
                "the group chat", "a red flag", "main character energy", "the algorithm", "a screenshot", "the ick",
                "touching grass", "a playlist", "being left on read", "a villain era", "phone at one percent", "the vibes"
            )
            Magic8BallPersonality.THERAPIST -> listOf(
                "boundaries", "the inner child", "a journal", "a breathing exercise", "the clock on the wall",
                "a pattern from childhood", "the ex", "self-care", "a box of tissues", "next Thursday's session",
                "projection", "the bill"
            )
            Magic8BallPersonality.DOG -> listOf(
                "a squirrel", "the ball", "a walk", "the mail carrier", "a treat", "the vet", "bath time",
                "a stick", "the doorbell", "its own tail", "a hole dug in the garden", "the couch it is not allowed on"
            )
            Magic8BallPersonality.CAT -> listOf(
                "the food bowl", "a sunbeam", "a cardboard box", "the red dot", "a glass on the edge of a table",
                "a closed door", "the vacuum cleaner", "three a.m.", "a nap", "tuna", "the human's keyboard", "a bird outside"
            )
        }
    }
}
