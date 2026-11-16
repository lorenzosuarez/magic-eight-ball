package com.example.magiceightball.core.domain.model

/**
 * A character as it is listed where characters are chosen among: one of the app's own, one the
 * user made, or one somebody else made and gave to everyone.
 *
 * The three are kept apart because they are not the same thing to the app: its own are known by
 * heart and named in the app's languages, the user's are kept with their account, and the
 * others' come from afar, a few at a time, and there may be any number of them.
 */
sealed interface CharacterListing {
    /** What tells this listing from every other, of whatever kind: fit to key a list by. */
    val key: String

    /** One of the app's own. */
    data class Original(val style: Magic8BallPersonality) : CharacterListing {
        override val key: String get() = "o:${style.name}"
    }

    /** One the user made. */
    data class Yours(val character: CustomCharacter) : CharacterListing {
        override val key: String get() = "y:${character.id}"
    }

    /**
     * One somebody else made.
     *
     * @property description how it answers, as its maker wrote it: what the AI service is told of
     * it, and what is read of it where characters are listed.
     * @property author the name its maker goes by, as it is shown.
     * @property authorPhotoUrl where its maker's picture is, when they have one.
     * @property uses how many times it has been chosen, by anyone.
     */
    data class Community(
        val id: String,
        val name: String,
        val description: String,
        val colour: Magic8BallPersonality,
        val emblem: CharacterLook,
        val author: String,
        val authorPhotoUrl: String? = null,
        val uses: Long = 0
    ) : CharacterListing {
        override val key: String get() = "$KEY_PREFIX$id"

        /**
         * This character as one that answers: the copy that is kept as the choice, under its
         * [key], which no character of the user's own has for a slot.
         */
        fun answering(): CustomCharacter = CustomCharacter(
            id = key,
            name = name,
            description = description,
            colour = colour,
            emblem = emblem
        )

        companion object {
            /** What the key of every one of other people's starts with, and of none of anyone else's. */
            const val KEY_PREFIX = "c:"
        }
    }
}

/** Which characters are being looked at. */
enum class CharacterShelf {
    /** Every one: the user's, the app's, and then everyone else's. */
    ALL,

    /** The app's own. */
    ORIGINALS,

    /** What other people made. */
    COMMUNITY,

    /** What the user made. */
    YOURS
}

/**
 * Some of the characters other people made, in the order they are listed.
 *
 * @property next what to ask for the ones after these with; null when these are the last.
 */
data class CharacterPage(val items: List<CharacterListing.Community>, val next: String?)
