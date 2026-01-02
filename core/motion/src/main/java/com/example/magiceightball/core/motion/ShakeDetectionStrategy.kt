package com.example.magiceightball.core.motion

import android.hardware.SensorEvent
import com.example.magiceightball.core.domain.model.ShakeEvent
import kotlinx.coroutines.flow.Flow

interface ShakeDetectionStrategy {
    fun detect(sensorData: Flow<FloatArray>): Flow<ShakeEvent>
}
