package com.lorenzosuarez.magiceightball.phone.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Ads in the app: one full-screen ad now and then, at a moment when the user has finished with
 * an answer, and never while they are asking or reading.
 */
interface AdsController {
    /**
     * Whether the user must be offered a way back to their ad privacy choices, which the law
     * requires in some regions once they have been asked.
     */
    val privacyOptionsRequired: StateFlow<Boolean>

    /** Asks for consent where it is needed and gets ready to show ads. Does nothing if ads are off. */
    fun start(activity: Activity)

    /** The user has closed an answer: a good moment for an ad, if one is due. */
    fun onAnswerDismissed(activity: Activity)

    /** Opens the ad privacy choices again. */
    fun showPrivacyOptions(activity: Activity)

    /**
     * Whether there is an ad the user may choose to watch, right now, in exchange for more
     * answers from the AI service today. Never shown unasked.
     */
    val rewardedReady: StateFlow<Boolean>

    /**
     * Shows that ad. [onEarned] is called with how many answers it was worth, once and only if
     * the user watched it through.
     */
    fun showRewarded(activity: Activity, onEarned: (answers: Int) -> Unit)
}
