package com.example.magiceightball.core.common

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiConfig @Inject constructor() {
    /** Used when Remote Config does not name a model (parameter `gemini_model`). */
    val defaultModelId: String = "gemini-3.5-flash-lite"

    /**
     * Upper bound on generated tokens. Answers are capped at 6 words by validation, so this only
     * needs headroom for the model's internal reasoning tokens, which also count towards it.
     */
    val maxOutputTokens: Int = 256

    val maxRetries: Int = 1
}
