package com.example.magiceightball.core.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiConfig @Inject constructor() {
    val modelId: String = "gemini-2.5-flash-lite"
    val maxOutputTokens: Int = 40
    val temperature: Float = 0.9f
    val topP: Float = 1.0f
    
    val maxRetries: Int = 1
}
