package com.example.magiceightball.core.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiConfig @Inject constructor() {
    // Default to a stable model version string
    // Switching to 2.5 Flash-Lite, currently the fastest model. 
    // Assumes 'Lite' variant skips complex reasoning (CoT) for speed.
    val modelId: String = "gemini-2.5-flash-lite"
    val maxOutputTokens: Int = 40
    val temperature: Float = 0.9f
    val topP: Float = 1.0f
    
    // Safety: we can have a strict retry policy if needed
    val maxRetries: Int = 1
}
