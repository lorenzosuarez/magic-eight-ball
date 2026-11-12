package com.lorenzosuarez.magiceightball.phone.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.hypot
import kotlin.math.tanh

/**
 * How far the ball has been left behind by the phone's own movement, each way, from -1 to 1.
 * Read where the ball is drawn, so that moving it composes nothing again.
 */
@Stable
internal class BallSway {
    var x by mutableFloatStateOf(0f)
        internal set
    var y by mutableFloatStateOf(0f)
        internal set
}

/**
 * The ball as something with weight inside the phone: shaken, it lags a little behind the hand
 * and settles when the hand does, and each stroke of a shake is felt as a light tick. It is the
 * answer to "is it hearing my shake?", given before the ball starts to think.
 *
 * The sensor is listened to only while [active] and the screen is in front of the user.
 *
 * @param ticks whether strokes are also felt: only while a shake can still ask something.
 */
@Composable
internal fun rememberBallSway(active: Boolean, ticks: Boolean): BallSway {
    val sway = remember { BallSway() }
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val feelsStrokes by rememberUpdatedState(ticks)

    DisposableEffect(active, lifecycle) {
        val sensors = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val filter = SwayFilter()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val stroke = filter.onSample(event.values[0], event.values[1], event.values[2], event.timestamp)
                sway.x = filter.x
                sway.y = filter.y
                if (stroke && feelsStrokes) {
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CLOCK_TICK)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        var listening = false
        fun listen(wanted: Boolean) {
            if (wanted == listening || accelerometer == null) return
            listening = wanted
            if (wanted) {
                sensors.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sensors.unregisterListener(listener)
                sway.x = 0f
                sway.y = 0f
            }
        }
        val observer = LifecycleEventObserver { _, _ ->
            listen(active && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        }
        lifecycle.addObserver(observer)
        listen(active && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose {
            lifecycle.removeObserver(observer)
            listen(false)
        }
    }
    return sway
}

/**
 * Turns accelerometer samples into where the ball sits and when a stroke of a shake begins.
 *
 * Gravity is estimated with a slow filter and taken out, so how the phone is held does not
 * matter. What is left pushes the ball the other way, as inertia would, through a curve that
 * flattens out: a gentle move shows, and the hardest shake still moves the ball only so far.
 */
internal class SwayFilter {
    var x = 0f
        private set
    var y = 0f
        private set

    private var hasGravity = false
    private var gravityX = 0f
    private var gravityY = 0f
    private var lastNanos = 0L

    private var inStroke = false
    private var strokeX = 0f
    private var strokeY = 0f
    private var lastStrokeNanos = 0L

    /** @return true when this sample begins a stroke: the hand has set off, or turned back. */
    fun onSample(rawX: Float, rawY: Float, rawZ: Float, nanos: Long): Boolean {
        if (!hasGravity) {
            hasGravity = true
            gravityX = rawX
            gravityY = rawY
            lastNanos = nanos
            return false
        }
        val elapsed = (nanos - lastNanos).coerceAtLeast(0L).toFloat()
        lastNanos = nanos
        val settle = elapsed / (GRAVITY_NANOS + elapsed)
        gravityX += settle * (rawX - gravityX)
        gravityY += settle * (rawY - gravityY)
        val pushX = rawX - gravityX
        val pushY = rawY - gravityY

        // The screen's y grows downwards, the sensor's upwards: pushed up, the ball is left below.
        val follow = elapsed / (FOLLOW_NANOS + elapsed)
        x += follow * (-tanh(pushX / SOFT_PUSH) - x)
        y += follow * (tanh(pushY / SOFT_PUSH) - y)

        val strength = hypot(pushX, pushY)
        if (strength < STROKE_END) {
            inStroke = false
            return false
        }
        val turnedBack = pushX * strokeX + pushY * strokeY < 0f
        if (strength > STROKE_START && (!inStroke || turnedBack) && nanos - lastStrokeNanos >= STROKE_GAP_NANOS) {
            inStroke = true
            strokeX = pushX
            strokeY = pushY
            lastStrokeNanos = nanos
            return true
        }
        return false
    }

    private companion object {
        const val GRAVITY_NANOS = 300_000_000f
        const val FOLLOW_NANOS = 55_000_000f

        /** In m/s²: about where the curve bends, so everyday movement stays well under the limit. */
        const val SOFT_PUSH = 14f
        const val STROKE_START = 9f
        const val STROKE_END = 3f

        /** No more ticks than a hand can shake: a buzz would be noise, not an answer. */
        const val STROKE_GAP_NANOS = 90_000_000L
    }
}
