package com.example.magiceightball.core.network.api

import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiService {
    /** The key travels in a header (never in the URL) so it cannot end up in request logs. */
    @POST("v1beta/models/{modelId}:generateContent")
    suspend fun generateContent(
        @Path("modelId") modelId: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
