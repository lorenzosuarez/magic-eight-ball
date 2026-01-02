package com.example.magiceightball.core.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.model.ShakeEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.magiceightball.core.common.DispatcherProvider

/**
 * Implementation of MotionGateway using Android SensorManager.
 * emits accelerometer data streams converted to domain flow.
 */
class MotionGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val strategy: ShakeDetectionStrategy,
    private val dispatcherProvider: DispatcherProvider
) : MotionGateway {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override val shakeEvents: Flow<ShakeEvent>
        get() = strategy.detect(sensorStream().map { it.values.clone() })
            .flowOn(dispatcherProvider.default)

    private fun sensorStream(): Flow<SensorEvent> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
