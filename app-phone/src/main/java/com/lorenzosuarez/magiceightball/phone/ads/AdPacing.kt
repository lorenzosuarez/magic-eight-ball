package com.lorenzosuarez.magiceightball.phone.ads

/**
 * When an ad is due: not before the user has closed [minAnswers] answers since the last one,
 * and not before [minIntervalMillis] have passed since it. The first ad of a run waits for
 * [minAnswers] answers too, so nobody is met with an ad. Both limits are asked for each time,
 * so a change made from afar takes effect at the next answer.
 *
 * Never for whoever is [adFree], which is asked each time as well: having paid takes the ads
 * away from the next answer on.
 */
class AdPacing(
    private val minAnswers: () -> Int = { MIN_ANSWERS },
    private val minIntervalMillis: () -> Long = { MIN_INTERVAL_MILLIS },
    private val adFree: () -> Boolean = { false }
) {
    private var answersSinceAd = 0
    private var lastAdAtMillis: Long? = null

    /** Counts one closed answer and tells whether an ad may be shown now. */
    fun answerClosed(nowMillis: Long): Boolean {
        if (adFree()) {
            // Nor is one owed the moment that ends: the count starts again, as after an ad.
            answersSinceAd = 0
            return false
        }
        answersSinceAd++
        val waitedLongEnough = lastAdAtMillis?.let { nowMillis - it >= minIntervalMillis() } ?: true
        return answersSinceAd >= minAnswers() && waitedLongEnough
    }

    /** An ad was shown at [nowMillis]: the count starts again. */
    fun adShown(nowMillis: Long) {
        answersSinceAd = 0
        lastAdAtMillis = nowMillis
    }

    private companion object {
        const val MIN_ANSWERS = 4
        const val MIN_INTERVAL_MILLIS = 3 * 60_000L
    }
}
