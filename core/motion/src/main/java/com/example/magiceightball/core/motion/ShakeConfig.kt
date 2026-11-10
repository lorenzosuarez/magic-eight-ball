package com.example.magiceightball.core.motion

/**
 * Tuning of the shake detector and of the accelerometer feeding it.
 *
 * Thresholds apply to linear acceleration (gravity removed), expressed in g.
 *
 * @property startThresholdG a movement counts as a shake pulse once it exceeds this.
 * @property stopThresholdG a pulse ends, and the wrist counts as still, below this. A pulse
 * also ends when the movement turns back on itself while still above [startThresholdG].
 * @property requiredPulses pulses needed inside [pulseWindowMs] to report a shake. A single
 * spike (raising the wrist, an arm swing) is therefore never a shake.
 * @property pulseWindowMs time window in which [requiredPulses] must occur.
 * @property quietPeriodMs time below [stopThresholdG] after which a shake is reported as over.
 * @property gravityTimeConstantMs time constant of the low-pass filter that estimates gravity.
 * @property samplingPeriodUs accelerometer sampling period.
 * @property maxReportLatencyUs how long the sensor hub may batch samples before waking the CPU.
 */
data class ShakeConfig(
    val startThresholdG: Float = 1.0f,
    val stopThresholdG: Float = 0.5f,
    val requiredPulses: Int = 3,
    val pulseWindowMs: Long = 800L,
    val quietPeriodMs: Long = 300L,
    val gravityTimeConstantMs: Long = 400L,
    val samplingPeriodUs: Int = 20_000,
    val maxReportLatencyUs: Int = 100_000
) {
    init {
        require(stopThresholdG > 0f && startThresholdG > stopThresholdG) {
            "Thresholds must satisfy 0 < stop < start"
        }
        require(requiredPulses >= 1) { "requiredPulses must be at least 1" }
    }
}
