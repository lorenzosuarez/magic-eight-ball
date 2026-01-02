package com.example.magiceightball.core.data.gateway

import com.example.magiceightball.core.network.api.GeminiService
import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GeminiResponse
import javax.inject.Inject

class RetrofitGeminiApiClient @Inject constructor(
    private val service: GeminiService
) : GeminiApiClient {
    override suspend fun generateContent(apiKey: String, modelId: String, request: GeminiRequest): GeminiResponse {
        return service.generateContent(modelId, apiKey, request)
    }
}
