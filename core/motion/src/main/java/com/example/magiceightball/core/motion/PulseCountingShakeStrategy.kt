package com.example.magiceightball.core.motion

import com.example.magiceightball.core.domain.model.ShakeEvent
import javax.inject.Inject

/**
 * Detects a deliberate shake as several strong back-and-forth pulses in a short time.
 *
 * Gravity is estimated with a low-pass filter and subtracted, so the orientation of the wrist
 * does not matter. A pulse starts when the remaining (linear) acceleration exceeds the start
 * threshold, either after it had dropped below the stop threshold or after it has swung round to
 * the opposite direction. The second rule matters: a real shake also rotates the wrist, so the
 * magnitude often never dips between two strokes, and a dip can fall between two samples.
 * [ShakeEvent.Started] is reported once enough pulses fall inside the configured window,
 * [ShakeEvent.Stopped] once the wrist has been still for the quiet period.
 */
class PulseCountingShakeStrategy @Inject constructor(
    private val config: ShakeConfig
) : ShakeDetectionStrategy {

    override fun newDetector(): ShakeDetector = PulseCountingShakeDetector(config)
}

private class PulseCountingShakeDetector(config: ShakeConfig) : ShakeDetector {

    private val startThresholdSquared = squaredMetersPerSecond(config.startThresholdG)
    private val stopThresholdSquared = squaredMetersPerSecond(config.stopThresholdG)
    private val pulseWindowNanos = config.pulseWindowMs * NANOS_PER_MILLI
    private val quietPeriodNanos = config.quietPeriodMs * NANOS_PER_MILLI
    private val gravityTimeConstantNanos = (config.gravityTimeConstantMs * NANOS_PER_MILLI).toFloat()

    /** Start times of the most recent pulses, used as a ring buffer. */
    private val pulseTimes = LongArray(config.requiredPulses)
    private var pulseCount = 0
    private var nextPulseIndex = 0

    private var hasGravity = false
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f
    private var lastTimestampNanos = 0L

    private var inPulse = false

    /** Linear acceleration at the start of the current pulse, and its squared magnitude. */
    private var pulseX = 0f
    private var pulseY = 0f
    private var pulseZ = 0f
    private var pulseMagnitudeSquared = 0f

    private var shaking = false
    private var lastActiveNanos = 0L

    override fun onSample(x: Float, y: Float, z: Float, timestampNanos: Long): ShakeEvent? {
        if (!hasGravity) {
            hasGravity = true
            gravityX = x
            gravityY = y
            gravityZ = z
            lastTimestampNanos = timestampNanos
            lastActiveNanos = timestampNanos
            return null
        }

        val elapsedNanos = (timestampNanos - lastTimestampNanos).coerceAtLeast(0L).toFloat()
        lastTimestampNanos = timestampNanos
        val alpha = elapsedNanos / (gravityTimeConstantNanos + elapsedNanos)
        gravityX += alpha * (x - gravityX)
        gravityY += alpha * (y - gravityY)
        gravityZ += alpha * (z - gravityZ)

        val linearX = x - gravityX
        val linearY = y - gravityY
        val linearZ = z - gravityZ
        val magnitudeSquared = linearX * linearX + linearY * linearY + linearZ * linearZ

        if (magnitudeSquared >= stopThresholdSquared) lastActiveNanos = timestampNanos

        var pulseStarted = false
        if (magnitudeSquared > startThresholdSquared &&
            (!inPulse || isReversal(linearX, linearY, linearZ, magnitudeSquared))
        ) {
            inPulse = true
            pulseStarted = true
            pulseX = linearX
            pulseY = linearY
            pulseZ = linearZ
            pulseMagnitudeSquared = magnitudeSquared
            recordPulse(timestampNanos)
        } else if (inPulse && magnitudeSquared < stopThresholdSquared) {
            inPulse = false
        }

        return when {
            !shaking && pulseStarted && hasEnoughRecentPulses(timestampNanos) -> {
                shaking = true
                ShakeEvent.Started
            }
            shaking && timestampNanos - lastActiveNanos >= quietPeriodNanos -> {
                shaking = false
                pulseCount = 0
                ShakeEvent.Stopped
            }
            else -> null
        }
    }

    /**
     * True when the acceleration points more than 120 degrees away from where it pointed at the
     * start of the current pulse: the stroke has turned back. Compared squared, to avoid a root.
     */
    private fun isReversal(x: Float, y: Float, z: Float, magnitudeSquared: Float): Boolean {
        val dot = x * pulseX + y * pulseY + z * pulseZ
        return dot < 0f &&
            dot * dot > REVERSAL_COSINE_SQUARED * magnitudeSquared * pulseMagnitudeSquared
    }

    private fun recordPulse(timestampNanos: Long) {
        pulseTimes[nextPulseIndex] = timestampNanos
        nextPulseIndex = (nextPulseIndex + 1) % pulseTimes.size
        if (pulseCount < pulseTimes.size) pulseCount++
    }

    /** True when the ring buffer is full and its oldest pulse is still inside the window. */
    private fun hasEnoughRecentPulses(nowNanos: Long): Boolean =
        pulseCount == pulseTimes.size && nowNanos - pulseTimes[nextPulseIndex] <= pulseWindowNanos

    private companion object {
        const val STANDARD_GRAVITY = 9.80665f
        const val NANOS_PER_MILLI = 1_000_000L

        /** cos²(120°). */
        const val REVERSAL_COSINE_SQUARED = 0.25f

        fun squaredMetersPerSecond(g: Float): Float = (g * STANDARD_GRAVITY).let { it * it }
    }
}
