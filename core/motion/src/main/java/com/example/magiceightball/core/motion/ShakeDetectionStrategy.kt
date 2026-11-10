package com.example.magiceightball.core.motion

import com.example.magiceightball.core.domain.model.ShakeEvent

/**
 * Creates [ShakeDetector]s. Detectors are stateful, so every sensor subscription gets its own.
 */
fun interface ShakeDetectionStrategy {
    fun newDetector(): ShakeDetector
}

/**
 * Turns raw accelerometer samples into shake transitions.
 *
 * Implementations are fed from a single thread, once per sample, and must not allocate.
 */
fun interface ShakeDetector {
    /**
     * @param x acceleration on the x axis in m/s², gravity included.
     * @param y acceleration on the y axis in m/s², gravity included.
     * @param z acceleration on the z axis in m/s², gravity included.
     * @param timestampNanos monotonic time of the sample.
     * @return the transition caused by this sample, or null when nothing changed.
     */
    fun onSample(x: Float, y: Float, z: Float, timestampNanos: Long): ShakeEvent?
}
