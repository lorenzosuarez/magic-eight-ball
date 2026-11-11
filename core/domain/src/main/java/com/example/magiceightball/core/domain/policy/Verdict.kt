package com.example.magiceightball.core.domain.policy

import kotlin.random.Random

/**
 * What the ball answers, apart from how it says it. The app draws it, as shaking the toy would,
 * and the AI service only words it: left to itself a character leans one way every time, and an
 * adoring one would never say no.
 */
enum class Verdict {
    YES,
    NO,
    MAYBE;

    companion object {
        /** Yes and no about as often as each other, and now and then neither. */
        fun draw(random: Random = Random.Default): Verdict {
            val roll = random.nextInt(100)
            return when {
                roll < 43 -> YES
                roll < 85 -> NO
                else -> MAYBE
            }
        }
    }
}
