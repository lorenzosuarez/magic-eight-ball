package com.example.magiceightball.core.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.model.ShakeEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * [MotionGateway] backed by the accelerometer.
 *
 * The sensor is registered only while [shakeEvents] is collected. Samples are delivered in
 * batches to a background thread and reduced to shake transitions right there, so the main
 * thread and the collector only ever see the few [ShakeEvent]s a shake produces.
 */
class MotionGatewayImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val strategy: ShakeDetectionStrategy,
    private val config: ShakeConfig
) : MotionGateway {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override val shakeEvents: Flow<ShakeEvent> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val detector = strategy.newDetector()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // The event object is recycled by the framework: read it here, never pass it on.
                val values = event.values
                detector.onSample(values[0], values[1], values[2], event.timestamp)
                    ?.let { trySend(it) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val sensorThread = HandlerThread(SENSOR_THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND)
            .apply { start() }
        val registered = sensorManager.registerListener(
            listener,
            sensor,
            config.samplingPeriodUs,
            config.maxReportLatencyUs,
            Handler(sensorThread.looper)
        )
        if (!registered) {
            sensorThread.quitSafely()
            close()
            return@callbackFlow
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
            sensorThread.quitSafely()
        }
    }

    private companion object {
        const val SENSOR_THREAD_NAME = "ShakeSensor"
    }
}
