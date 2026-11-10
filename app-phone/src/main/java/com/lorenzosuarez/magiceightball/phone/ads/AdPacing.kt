package com.lorenzosuarez.magiceightball.phone.ads

/**
 * When an ad is due: not before the user has closed [minAnswers] answers since the last one,
 * and not before [minIntervalMillis] have passed since it. The first ad of a run waits for
 * [minAnswers] answers too, so nobody is met with an ad.
 */
class AdPacing(
    private val minAnswers: Int = MIN_ANSWERS,
    private val minIntervalMillis: Long = MIN_INTERVAL_MILLIS
) {
    private var answersSinceAd = 0
    private var lastAdAtMillis: Long? = null

    /** Counts one closed answer and tells whether an ad may be shown now. */
    fun answerClosed(nowMillis: Long): Boolean {
        answersSinceAd++
        val waitedLongEnough = lastAdAtMillis?.let { nowMillis - it >= minIntervalMillis } ?: true
        return answersSinceAd >= minAnswers && waitedLongEnough
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
