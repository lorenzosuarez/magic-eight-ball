package com.example.magiceightball.core.data.repository

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock implementation of LlmGateway for development and testing.
 * Simulates network latency and returns a deterministic success response.
 */
class MockLlmGateway @Inject constructor(
    private val config: QueryConfig
) : LlmGateway {

    override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
        delay(config.processingDelayMs)
        return Magic8BallResult.Success("It is certain")
    }
}
