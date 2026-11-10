package com.example.magiceightball.core.motion

import com.example.magiceightball.core.domain.model.ShakeEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class PulseCountingShakeStrategyTest {

    private val detector = PulseCountingShakeStrategy(ShakeConfig()).newDetector()
    private val events = mutableListOf<ShakeEvent>()
    private var timeNanos = 0L

    @Test
    fun `a wrist at rest produces no events`() {
        rest(durationMs = 2_000)

        assertEquals(emptyList<ShakeEvent>(), events)
    }

    @Test
    fun `a deliberate shake starts once and stops when the wrist is still again`() {
        rest(durationMs = 500)
        oscillate(durationMs = 1_000, frequencyHz = 3.5f, amplitudeG = 2.5f)
        rest(durationMs = 1_000)

        assertEquals(listOf(ShakeEvent.Started, ShakeEvent.Stopped), events)
    }

    @Test
    fun `a shake that also twists the wrist never dips between strokes and still counts`() {
        rest(durationMs = 500)
        // Two axes out of phase: the magnitude stays above the stop threshold throughout.
        oscillate(durationMs = 1_000, frequencyHz = 3.5f, amplitudeG = 2.5f, secondAxisRatio = 0.6f)

        assertEquals(listOf(ShakeEvent.Started), events)
    }

    @Test
    fun `a fast strong shake whose dips fall between samples still counts`() {
        rest(durationMs = 500)
        oscillate(durationMs = 1_000, frequencyHz = 5f, amplitudeG = 4f)

        assertEquals(listOf(ShakeEvent.Started), events)
    }

    @Test
    fun `a single spike such as raising the wrist is not a shake`() {
        rest(durationMs = 500)
        oscillate(durationMs = 140, frequencyHz = 3.5f, amplitudeG = 2.5f)
        rest(durationMs = 1_000)

        assertEquals(emptyList<ShakeEvent>(), events)
    }

    @Test
    fun `the arm swing of walking is not a shake`() {
        rest(durationMs = 500)
        oscillate(durationMs = 5_000, frequencyHz = 1f, amplitudeG = 0.6f)

        assertEquals(emptyList<ShakeEvent>(), events)
    }

    @Test
    fun `turning the wrist slowly is not a shake`() {
        rest(durationMs = 500)
        repeat(100) { step ->
            val angle = (PI / 2 * step / 100).toFloat()
            sample(x = GRAVITY * sin(angle), z = GRAVITY * sin(PI.toFloat() / 2 - angle))
        }

        assertEquals(emptyList<ShakeEvent>(), events)
    }

    @Test
    fun `strong pulses that are too far apart are not a shake`() {
        rest(durationMs = 500)
        repeat(4) {
            oscillate(durationMs = 140, frequencyHz = 3.5f, amplitudeG = 2.5f)
            rest(durationMs = 700)
        }

        assertEquals(emptyList<ShakeEvent>(), events)
    }

    private fun rest(durationMs: Int) {
        repeat(durationMs / SAMPLE_PERIOD_MS) { sample(x = 0f, z = GRAVITY) }
    }

    private fun oscillate(
        durationMs: Int,
        frequencyHz: Float,
        amplitudeG: Float,
        secondAxisRatio: Float = 0f
    ) {
        repeat(durationMs / SAMPLE_PERIOD_MS) { step ->
            val seconds = step * SAMPLE_PERIOD_MS / 1_000f
            val phase = 2 * PI.toFloat() * frequencyHz * seconds
            val x = amplitudeG * GRAVITY * sin(phase)
            val y = secondAxisRatio * amplitudeG * GRAVITY * sin(phase + SECOND_AXIS_PHASE)
            sample(x = x, y = y, z = GRAVITY)
        }
    }

    private fun sample(x: Float, y: Float = 0f, z: Float) {
        detector.onSample(x, y, z, timeNanos)?.let(events::add)
        timeNanos += SAMPLE_PERIOD_MS * 1_000_000L
    }

    private companion object {
        const val GRAVITY = 9.80665f
        const val SAMPLE_PERIOD_MS = 20
        const val SECOND_AXIS_PHASE = 1.2f
    }
}
