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
 * matter. What is left pushes a weight held by a soft spring the other way, as inertia would:
 * the ball swings with the hand and, when the hand stops, goes on for a moment and settles.
 * That last swing is the part that is seen, since nobody reads a screen they are shaking. What
 * is shown passes through a curve that flattens out, so the hardest shake still moves the ball
 * only so far.
 */
internal class SwayFilter {
    val x: Float get() = tanh(placeX)
    val y: Float get() = tanh(placeY)

    /** The weight on its spring: where it is and how fast it is going, each way. */
    private var placeX = 0f
    private var placeY = 0f
    private var speedX = 0f
    private var speedY = 0f

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
        // A long gap between samples is no reason for the weight to leap.
        val seconds = (elapsed / NANOS_PER_SECOND).coerceAtMost(MAX_STEP_SECONDS)
        speedX += (-DRIVE * pushX - STIFFNESS * placeX - DAMPING * speedX) * seconds
        speedY += (DRIVE * pushY - STIFFNESS * placeY - DAMPING * speedY) * seconds
        placeX += speedX * seconds
        placeY += speedY * seconds

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
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MAX_STEP_SECONDS = 0.05f

        /**
         * A spring that swings about three and a half times a second and loses most of a swing
         * each time: quick enough to keep up with a shaking hand, and at rest again within half
         * a second of it. [DRIVE] is how hard the phone's movement pushes on it.
         */
        const val STIFFNESS = 480f
        const val DAMPING = 13f
        const val DRIVE = 26f
        const val STROKE_START = 9f
        const val STROKE_END = 3f

        /** No more ticks than a hand can shake: a buzz would be noise, not an answer. */
        const val STROKE_GAP_NANOS = 90_000_000L
    }
}
