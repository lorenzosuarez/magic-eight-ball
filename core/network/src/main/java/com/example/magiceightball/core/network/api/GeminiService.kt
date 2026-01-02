package com.example.magiceightball.core.network.api

import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    @POST("v1beta/models/{modelId}:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Path("modelId") modelId: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
