package com.example.magiceightball.core.domain.model

enum class Magic8BallPersonality {
    SARCASTIC,
    MYSTICAL,

    /** Plain answers, as the original toy gives them: no jokes and no riddles. */
    NEUTRAL,

    /** Characters, each with a voice of its own. Added after the three above: keep the order. */
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

    /**
     * One of the user's own: see [CustomCharacter], which says which one and how it answers.
     * Not a character the app can describe, list or offer: without that record it answers as
     * the classic one. Only the phone has them; the watch never shows this.
     */
    CUSTOM
}
