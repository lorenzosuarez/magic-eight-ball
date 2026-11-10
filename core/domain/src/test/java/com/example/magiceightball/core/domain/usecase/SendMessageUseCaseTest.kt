package com.example.magiceightball.core.domain.usecase

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SendMessageUseCaseTest {

    private val gateway = RecordingLlmGateway()
    private val sendMessage = SendMessageUseCase(gateway)

    @Test
    fun `the request carries exactly what the caller chose`() = runTest {
        sendMessage(
            trigger = "shake",
            languageCode = "es",
            personality = Magic8BallPersonality.MYSTICAL,
            useAi = false
        )

        assertEquals(
            Magic8BallRequest(
                userTrigger = "shake",
                languageCode = "es",
                personality = Magic8BallPersonality.MYSTICAL,
                useAi = false
            ),
            gateway.requests.single()
        )
    }

    @Test
    fun `no question is sent unless the caller passes one`() = runTest {
        ask()

        assertNull(gateway.requests.single().question)
    }

    @Test
    fun `a question is carried to the gateway`() = runTest {
        sendMessage(
            trigger = "shake",
            languageCode = "en",
            personality = Magic8BallPersonality.SARCASTIC,
            useAi = true,
            question = "Will it rain?"
        )

        assertEquals("Will it rain?", gateway.requests.single().question)
    }

    @Test
    fun `the gateway's answer is returned as it is`() = runTest {
        gateway.result = Magic8BallResult.Success("Outlook good")

        assertEquals(Magic8BallResult.Success("Outlook good"), ask())
    }

    @Test
    fun `the gateway's failure is returned as it is`() = runTest {
        gateway.result = Magic8BallResult.Failure(LlmError.Network)

        assertEquals(Magic8BallResult.Failure(LlmError.Network), ask())
    }

    private suspend fun ask(): Magic8BallResult = sendMessage(
        trigger = "shake",
        languageCode = "en",
        personality = Magic8BallPersonality.SARCASTIC,
        useAi = true
    )

    private class RecordingLlmGateway : LlmGateway {
        val requests = mutableListOf<Magic8BallRequest>()
        var result: Magic8BallResult = Magic8BallResult.Success("It is certain")

        override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
            requests += request
            return result
        }
    }
}
