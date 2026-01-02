package com.example.magiceightball.core.motion

/**
 * Configuration for shake detection thresholds and sampling rates.
 */
data class ShakeConfig(
    val startThresholdGravity: Float = 1.3f,
    val stopThresholdGravity: Float = 1.1f,
    val samplingPeriodUs: Int = 20000
)
