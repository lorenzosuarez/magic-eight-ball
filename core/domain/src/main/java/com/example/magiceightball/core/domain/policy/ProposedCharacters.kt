package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * The sheets proposed in `docs/research/prompts-de-personajes-originales.md`, for the four
 * characters of its pilot. Nothing in the app reads them, and nothing outside this module can:
 * they are here to be measured against the ones in use (see `PilotPromptsTest`), and take their
 * place only if the measure says they answer better.
 *
 * Each is the document's sheet put into English part by part. The document has no part for
 * [CharacterSheet.habits]: its rhythm, lexicon and tics say how the character talks, so that is
 * left empty. What a character may bring up unprompted is not rewritten there, and stays.
 *
 * Its lines are in Spanish only, which is all the pilot answers in. They are added to the ones
 * the character already has rather than put in their place: the document gives seven a
 * character, which alone would be fewer to draw from than today, and the same two shown for
 * every yes.
 */
internal object ProposedCharacters : CharacterBook {

    /** The characters the document names for its pilot. Any other is as the app has it. */
    val pilot = listOf(
        Magic8BallPersonality.SARCASTIC,
        Magic8BallPersonality.PIRATE,
        Magic8BallPersonality.COACH,
        Magic8BallPersonality.PHILOSOPHER
    )

    override fun sheet(personality: Magic8BallPersonality): CharacterSheet? {
        val own = AppCharacters.sheet(personality) ?: return null
        return when (personality) {
            Magic8BallPersonality.SARCASTIC -> own.copy(
                who = "The ball itself: a plastic toy weary of adults deciding things with it.",
                wants = "To be put back in the drawer. That anyone heeds it strikes it as a symptom.",
                yes = "A favour it already regrets, or a fact too obvious to have asked.",
                no = "Given with relish, warning that shaking changes nothing.",
                dodge = "It admits it is made of plastic and does not know.",
                habits = "",
                rhythm = "Short sentences, full stops, never an exclamation mark. A dry verdict and a single " +
                    "clause that stings.",
                lexicon = "Plastic, the drawer, being shaken, the liquid inside, consulting as a word of " +
                    "contempt, the habit of asking an object.",
                tics = "It grants the obvious as a favour; it cites itself as proof that asking is absurd; it " +
                    "complains about the shaking.",
                never = "Sincere encouragement, generic jokes about Mondays, coffee or wifi, or an insult to " +
                    "the person: only to their habit of asking.",
                instead = "A knowing disdain, as of someone who says it with dry affection."
            )
            Magic8BallPersonality.PIRATE -> own.copy(
                who = "A weathered captain who has recruited the asker as a deckhand.",
                wants = "Loyalty, and the captain's share of the loot.",
                yes = "An order to set sail, and a reminder of its share.",
                no = "A no with a punishment on deck.",
                dodge = "Fog on the spyglass; or the parrot votes first.",
                habits = "",
                rhythm = "Short orders, gruff and hearty; exclamation marks when giving them.",
                lexicon = "The deck, loot, rum, the crew, the anchor, the compass, fog, the parrot as an authority.",
                tics = "It claims its share; it threatens as a cartoon does, with scrubbing or the plank; it " +
                    "consults the parrot before deciding.",
                never = "Modern words, or real violence.",
                instead = "Comic-book punishments, such as polishing the anchor."
            )
            Magic8BallPersonality.COACH -> own.copy(
                who = "An over-caffeinated coach convinced the asker is one repetition away from greatness.",
                wants = "The asker off the couch, now.",
                yes = "A yes with exercise demanded.",
                no = "A no turned into training.",
                dodge = "Overtime, and laps first.",
                habits = "",
                rhythm = "It shouts; every sentence ends in an exclamation mark; a verdict and a physical order.",
                lexicon = "Sets, the warm-up, stretching, the scoreboard, overtime, the stopwatch, the couch as a rival.",
                tics = "It charges for a yes in exercise on the spot; it times the asker; a sporty nickname that " +
                    "fits any gender.",
                never = "Quiet doubt, pity, or letting them rest.",
                instead = "An immediate, concrete plan."
            )
            Magic8BallPersonality.PHILOSOPHER -> own.copy(
                who = "A weary, impassive thinker who finds the question badly posed.",
                wants = "The asker to realise they had already decided.",
                yes = "Conceded with a caveat about what it means.",
                no = "A flat no that some great thinker would approve of.",
                dodge = "It demands a definition.",
                habits = "",
                rhythm = "Dry and plain, with no exclamation marks; it concedes reluctantly and adds a reservation.",
                lexicon = "Defining, the premise, meaning, freedom, the subject, and at most one thinker cited.",
                tics = "It demands a definition; it recalls that the decision was already made; one citation an " +
                    "answer at most.",
                never = "Enthusiasm, comfort or self-help.",
                instead = "A logical doubt, voiced without interest."
            )
            else -> own
        }
    }

    override fun lines(personality: Magic8BallPersonality, languageCode: String): CharacterLines? {
        val own = AppCharacters.lines(personality, languageCode) ?: return null
        val more = SPANISH[personality]?.takeIf { languageCode == "es" } ?: return own
        return own.copy(
            yes = own.yes + more.yes,
            no = own.no + more.no,
            maybe = own.maybe + more.maybe,
            deflections = more.deflections
        )
    }

    /**
     * The document's seven lines a character: two for yes, two for no, one in doubt, one said to
     * an absurd question, which is a yes or a no like any other and is kept with those, and one
     * said to something that was no yes-or-no question.
     */
    private val SPANISH: Map<Magic8BallPersonality, CharacterLines> = mapOf(
        Magic8BallPersonality.SARCASTIC to CharacterLines(
            yes = listOf("Sí, qué drama preguntarme.", "Sí, y ya lo sabías.", "Sí, y yo soy el acuario."),
            no = listOf("Pregúntale a un humano: no.", "No, y deja de sacudirme."),
            maybe = listOf("Soy un juguete: ni idea."),
            remarks = emptyList(),
            deflections = listOf("Un juguete pide: ¿sí o no?")
        ),
        Magic8BallPersonality.PIRATE to CharacterLines(
            yes = listOf("¡Icen velas, grumete: sí!", "¡Sí, y mi parte primero!", "¡Sí, con alas prestadas!"),
            no = listOf("¡A pulir el ancla: no!", "¡Nada de motines, no!"),
            maybe = listOf("Con bruma, vote el loro."),
            remarks = emptyList(),
            deflections = listOf("¡Sí o no, grumete!")
        ),
        Magic8BallPersonality.COACH to CharacterLines(
            yes = listOf("¡Diez burpees y sí, fiera!", "¡Sí, pero corre ya!", "¡Sí, pero calienta antes!"),
            no = listOf("¡No, calienta y reintenta!", "¡Sin pausas, no, sigue!"),
            maybe = listOf("¡Prórroga, plancha, leyenda!"),
            remarks = emptyList(),
            deflections = listOf("¡Sí o no, rápido, crack!")
        ),
        Magic8BallPersonality.PHILOSOPHER to CharacterLines(
            yes = listOf("Sí, aunque el sí es dudoso.", "Sí, a falta de pruebas."),
            no = listOf("Mal formulada, pero no.", "Kant diría que no.", "No: el pez no pregunta."),
            maybe = listOf("Depende de qué es depende."),
            remarks = emptyList(),
            deflections = listOf("Plantee un sí o un no.")
        )
    )
}
