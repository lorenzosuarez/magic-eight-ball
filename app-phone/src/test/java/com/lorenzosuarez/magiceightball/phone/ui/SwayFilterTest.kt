package com.lorenzosuarez.magiceightball.phone.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SwayFilterTest {

    private val filter = SwayFilter()
    private var nanos = 0L

    /** One sample every 20 ms, as the sensor gives them. */
    private fun sample(x: Float, y: Float = GRAVITY): Boolean {
        nanos += 20_000_000L
        return filter.onSample(x, y, 0f, nanos)
    }

    @Test
    fun `a phone held still leaves the ball where it is, however it is held`() {
        repeat(100) { assertFalse(sample(0f)) }
        assertEquals(0f, filter.x, 0.01f)
        assertEquals(0f, filter.y, 0.01f)
    }

    @Test
    fun `pushed one way the ball is left behind the other way, and comes back`() {
        repeat(50) { sample(0f) }
        repeat(4) { sample(12f) }
        assertTrue(filter.x < -0.2f)

        repeat(100) { sample(0f) }
        assertEquals(0f, filter.x, 0.02f)
    }

    @Test
    fun `the hardest shake never takes the ball past its limit`() {
        repeat(50) { sample(0f) }
        repeat(200) { index -> sample(if (index / 3 % 2 == 0) 60f else -60f) }
        assertTrue(abs(filter.x) <= 1f)
    }

    @Test
    fun `each turn of a shake is one stroke, and gentle movement is none`() {
        repeat(50) { sample(0f) }
        var strokes = 0
        // Five strokes there and back, 120 ms each.
        repeat(5) { stroke ->
            repeat(6) { if (sample(if (stroke % 2 == 0) 20f else -20f)) strokes++ }
        }
        assertEquals(5, strokes)

        repeat(50) { sample(0f) }
        var gentle = 0
        repeat(60) { index -> if (sample(if (index / 6 % 2 == 0) 2f else -2f)) gentle++ }
        assertEquals(0, gentle)
    }

    private companion object {
        const val GRAVITY = 9.81f
    }
}
