package com.example.magiceightball.core.data.gateway

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.policy.DefaultPromptPolicy
import com.example.magiceightball.core.network.model.Candidate
import com.example.magiceightball.core.network.model.Content
import com.example.magiceightball.core.network.model.GeminiResponse
import com.example.magiceightball.core.network.model.Part
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GeminiLlmGatewayTest {

    private val apiClient: GeminiApiClient = mockk()
    private val config: GeminiConfig = GeminiConfig()
    private val promptPolicy = DefaultPromptPolicy()
    private val apiKey = "test-key"

    private val gateway = GeminiLlmGateway(apiClient, config, promptPolicy, apiKey)

    @Test
    fun `generateMagic8BallMessage success returns valid message`() = runTest {
        val validText = "Yes perfectly valid."
        coEvery { apiClient.generateContent(any(), any(), any()) } returns createResponse(validText)

        val result = gateway.generateMagic8BallMessage(Magic8BallRequest("trigger"))

        assertTrue(result is Magic8BallResult.Success)
        assertEquals(validText, (result as Magic8BallResult.Success).message)
    }

    @Test
    fun `generateMagic8BallMessage validation failure retries and succeeds`() = runTest {
        val longText = "This message is definitely way too long for the magic eight ball constraint."
        val validText = "Start again now."
        
        coEvery { apiClient.generateContent(any(), any(), any()) } returnsMany listOf(
            createResponse(longText),
            createResponse(validText)
        )

        val result = gateway.generateMagic8BallMessage(Magic8BallRequest("trigger"))

        assertTrue(result is Magic8BallResult.Success)
        assertEquals(validText, (result as Magic8BallResult.Success).message)
    }

    @Test
    fun `generateMagic8BallMessage validation failure fails after max retries`() = runTest {
        val longText = "This message is definitely way too long for the magic eight ball constraint."
        
        coEvery { apiClient.generateContent(any(), any(), any()) } returns createResponse(longText)

        val result = gateway.generateMagic8BallMessage(Magic8BallRequest("trigger"))

        assertTrue(result is Magic8BallResult.Failure)
        assertEquals(LlmError.ValidationFailed, (result as Magic8BallResult.Failure).error)
    }

    @Test
    fun `generateMagic8BallMessage network error maps key correctly`() = runTest {
        coEvery { apiClient.generateContent(any(), any(), any()) } throws IOException("No net")

        val result = gateway.generateMagic8BallMessage(Magic8BallRequest("trigger"))

        assertTrue(result is Magic8BallResult.Failure)
        assertEquals(LlmError.Network, (result as Magic8BallResult.Failure).error)
    }

    private fun createResponse(text: String): GeminiResponse {
        return GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = Content(parts = listOf(Part(text = text)))
                )
            )
        )
    }
}
