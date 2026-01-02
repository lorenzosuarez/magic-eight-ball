package com.example.magiceightball.core.motion

import android.hardware.Sensor
import android.hardware.SensorEvent
import com.example.magiceightball.core.domain.model.ShakeEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import javax.inject.Inject
import kotlin.math.sqrt

class SimpleThresholdShakeStrategy @Inject constructor(
    private val config: ShakeConfig
) : ShakeDetectionStrategy {

    companion object {
        private const val STANDARD_GRAVITY = 9.81f
    }

    private val startThreshold = config.startThresholdGravity * STANDARD_GRAVITY
    private val stopThreshold = config.stopThresholdGravity * STANDARD_GRAVITY

    /**
     * Detects shake events based on accelerometer magnitude thresholds.
     * Uses a simple state machine (IDLE <-> SHAKING) with hysteresis.
     * Ignores the initial state to prevent false triggering on startup.
     */
    override fun detect(sensorData: Flow<FloatArray>): Flow<ShakeEvent> {
        return sensorData
            .map { values ->
                val x = values[0]
                val y = values[1]
                val z = values[2]
                sqrt(x * x + y * y + z * z)
            }
            .runningFold(ShakeState.IDLE) { currentState, magnitude ->
                when (currentState) {
                    ShakeState.IDLE -> {
                        if (magnitude > startThreshold) ShakeState.SHAKING else ShakeState.IDLE
                    }
                    ShakeState.SHAKING -> {
                        if (magnitude < stopThreshold) ShakeState.IDLE else ShakeState.SHAKING
                    }
                }
            }
            .map { state ->
                when (state) {
                    ShakeState.IDLE -> ShakeEvent.Stopped
                    ShakeState.SHAKING -> ShakeEvent.Started
                }
            }
            .distinctUntilChanged()
            .drop(1)
    }

    private enum class ShakeState {
        IDLE, SHAKING
    }
}
