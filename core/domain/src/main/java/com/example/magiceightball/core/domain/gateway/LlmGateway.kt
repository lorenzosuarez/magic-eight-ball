package com.example.magiceightball.core.domain.gateway

import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult

interface LlmGateway {
    suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult
}
