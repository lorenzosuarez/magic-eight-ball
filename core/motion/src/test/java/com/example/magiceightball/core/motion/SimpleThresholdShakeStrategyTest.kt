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
        val input = flowOf(
            floatArrayOf(0f, 0f, 9.8f),
            floatArrayOf(0f, 0f, 15f),
            floatArrayOf(0f, 0f, 14f),
            floatArrayOf(0f, 0f, 10f),
            floatArrayOf(0f, 0f, 9.8f)
        )

        val events = strategy.detect(input).toList()

        assertEquals(
            listOf(ShakeEvent.Started, ShakeEvent.Stopped),
            events
        )
    }
}
