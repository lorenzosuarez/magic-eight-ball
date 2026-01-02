package com.example.magiceightball.core.data.gateway

import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GeminiResponse

interface GeminiApiClient {
    suspend fun generateContent(apiKey: String, modelId: String, request: GeminiRequest): GeminiResponse
}
