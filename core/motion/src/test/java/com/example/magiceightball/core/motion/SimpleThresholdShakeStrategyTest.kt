package com.example.magiceightball.core.motion

import com.example.magiceightball.core.domain.model.ShakeEvent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.magiceightball.core.motion.ShakeConfig

class SimpleThresholdShakeStrategyTest {

    private val strategy = SimpleThresholdShakeStrategy(ShakeConfig())

    @Test
    fun `detects shake start and stop`() = runTest {
        // Mock data: Idle, Start (>13), Shaking (>13), Stop (<11), Idle
        val input = flowOf(
            floatArrayOf(0f, 0f, 9.8f), // 9.8 (Idle)
            floatArrayOf(0f, 0f, 15f),  // 15 (Start)
            floatArrayOf(0f, 0f, 14f),  // 14 (Shaking)
            floatArrayOf(0f, 0f, 10f),  // 10 (Stop)
            floatArrayOf(0f, 0f, 9.8f)  // 9.8 (Idle)
        )

        val events = strategy.detect(input).toList()

        // Expected: Stopped (initial idle), Started, Stopped
        // Note: Strategy emits initial state?
        // runningFold(IDLE) -> map(IDLE->Stopped) -> distinctUntilChanged.
        // First emission is Stopped.
        // Then Start (15) -> Started.
        // Then Shaking (14) -> Started (filtered by distinct)
        // Then Stop (10) -> Stopped.
        // Then Idle (9.8) -> Stopped (filtered)

        assertEquals(
            listOf(ShakeEvent.Started, ShakeEvent.Stopped),
            events
        )
    }
}
