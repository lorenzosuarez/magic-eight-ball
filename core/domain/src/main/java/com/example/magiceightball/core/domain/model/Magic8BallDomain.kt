package com.example.magiceightball.core.domain.model

import com.example.magiceightball.core.common.Result

sealed interface Magic8BallResult {
    data class Success(val message: String) : Magic8BallResult
    data class Failure(val error: LlmError) : Magic8BallResult
}

sealed interface LlmError {
    data object Network : LlmError
    data object Timeout : LlmError
    data object Unauthorized : LlmError
    data object RateLimited : LlmError
    data object Server : LlmError
    data object Serialization : LlmError
    data object SafetyBlocked : LlmError
    data object ValidationFailed : LlmError
    data class Unknown(val cause: Throwable?) : LlmError
}

data class Magic8BallRequest(
    val userTrigger: String? = null, // context if needed
    val languageCode: String = "en",
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC
)
