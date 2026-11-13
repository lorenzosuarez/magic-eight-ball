package com.example.magiceightball.core.data.appcheck

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppCheckTest {

    private val onScreen = MutableStateFlow(false)
    private val aiAvailable = MutableStateFlow(false)
    private val userAllowsAi = MutableStateFlow(true)

    @Test
    fun `the proof is made ahead each time the app comes on screen with ai answers on`() =
        runTest(UnconfinedTestDispatcher()) {
            var made = 0
            val job = proofWanted(onScreen, aiAvailable, userAllowsAi).onEach { made++ }.launchIn(this)

            // Started by a widget or a notification, with the switch already known to be on.
            aiAvailable.value = true
            assertEquals(0, made)

            onScreen.value = true
            assertEquals(1, made)

            // Left and come back to: asked for again, which costs nothing while the last one holds.
            onScreen.value = false
            onScreen.value = true
            assertEquals(2, made)

            job.cancel()
        }

    @Test
    fun `the proof is not made for a user, or by a developer, who has ai answers off`() =
        runTest(UnconfinedTestDispatcher()) {
            var made = 0
            val job = proofWanted(onScreen, aiAvailable, userAllowsAi).onEach { made++ }.launchIn(this)

            onScreen.value = true
            assertEquals(0, made)

            userAllowsAi.value = false
            aiAvailable.value = true
            assertEquals(0, made)

            // Turned on while the app is open: made then, in time for the next answer.
            userAllowsAi.value = true
            assertEquals(1, made)

            job.cancel()
        }
}
