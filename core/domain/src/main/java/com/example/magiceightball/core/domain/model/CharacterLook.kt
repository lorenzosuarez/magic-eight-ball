package com.example.magiceightball.core.domain.model

/**
 * What a character's ball wears: what tells one character from another at a glance. The app's
 * own characters each have theirs, under their own name; the rest are for the characters people
 * make, and belong to none of the app's.
 *
 * A look is kept by its name, in small letters, wherever a character is kept: a name is never
 * changed or taken away, and one that this version does not know is [NEUTRAL], which wears nothing.
 */
enum class CharacterLook {
    NEUTRAL,
    SARCASTIC,
    MYSTICAL,
    GRANDMA,
    PIRATE,
    PHILOSOPHER,
    COACH,
    LOVESTRUCK,
    DRAMA,
    ROBOT,
    DETECTIVE,
    CONSPIRACY,
    CAT,
    ZOOMER,
    VILLAIN,
    POET,
    ASTRO,
    THERAPIST,
    DOG,

    // No character of the app's wears these: they are there to be picked.
    SINGER,
    ROCKSTAR,
    CHEF,
    WIZARD,
    COWBOY,
    MAGICIAN,
    NINJA,
    ANGEL,
    GRADUATE,
    FLOWER,
    PARTY,
    MATE,
    BOTTLE,
    SHARK,
    PUNK,
    VIKING,
    MOUSE,
    BUNNY,
    BIRD,
    PROPELLER,
    BOW,
    MUSHROOM,
    APPLE,

    // The look of one of the app's own again, added after those: a name keeps its place.
    GAMER;

    companion object {
        /** The look kept under [name], in whatever case; [NEUTRAL] for none and for one not known here. */
        fun named(name: String?): CharacterLook = entries.find { it.name.equals(name, ignoreCase = true) } ?: NEUTRAL
    }
}

/** What this character of the app's wears: the look of its own name. One of the user's own wears what it was given. */
val Magic8BallPersonality.look: CharacterLook get() = CharacterLook.named(name)
