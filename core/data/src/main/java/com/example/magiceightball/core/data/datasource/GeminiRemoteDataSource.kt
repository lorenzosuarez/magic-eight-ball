package com.example.magiceightball.core.data.datasource

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.network.model.Content
import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GenerationConfig
import com.example.magiceightball.core.network.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class GeminiRemoteDataSource @Inject constructor(
    private val apiClient: GeminiApiClient,
    private val config: GeminiConfig,
    private val promptPolicy: PromptPolicy,
    @Named("GeminiApiKey") private val apiKey: String
) {
    suspend fun fetchMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
        return withContext(Dispatchers.IO) {
            val systemPromptText = promptPolicy.getSystemPrompt(
                request.languageCode,
                request.personality
            )

            val systemInstruction = Content(parts = listOf(Part(text = systemPromptText)))
            val userContent =
                Content(parts = listOf(Part(text = request.userTrigger ?: "What is my fortune?")))

            val genConfig = GenerationConfig(
                temperature = config.temperature,
                topP = config.topP,
                maxOutputTokens = config.maxOutputTokens
            )

            val apiRequest = GeminiRequest(
                contents = listOf(userContent),
                systemInstruction = systemInstruction,
                generationConfig = genConfig
            )

            executeWithRetry(apiRequest, request.languageCode, request.personality)
        }
    }

    private suspend fun executeWithRetry(
        initialRequest: GeminiRequest,
        languageCode: String,
        personality: Magic8BallPersonality,
        attempt: Int = 1
    ): Magic8BallResult {
        try {
            val response = apiClient.generateContent(apiKey, config.modelId, initialRequest)

            if (response.promptFeedback?.blockReason != null) {
                return Magic8BallResult.Failure(LlmError.SafetyBlocked)
            }
            if (!response.candidates.isNullOrEmpty() && response.candidates?.get(0)?.finishReason == "SAFETY") {
                return Magic8BallResult.Failure(LlmError.SafetyBlocked)
            }

            val text =
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    ?: return Magic8BallResult.Failure(LlmError.Serialization)

            if (attempt <= config.maxRetries) {
                val currentPrompt = promptPolicy.getSystemPrompt(languageCode, personality)
                val stricterInstruction = Content(
                    parts = listOf(Part(text = "$currentPrompt You MUST use 6 words or less."))
                )
                val retryRequest = initialRequest.copy(systemInstruction = stricterInstruction)
                return executeWithRetry(retryRequest, languageCode, personality, attempt + 1)
            } else {
                return Magic8BallResult.Failure(LlmError.ValidationFailed)
            }


        } catch (e: Exception) {
            return when (e) {
                is IOException -> Magic8BallResult.Failure(LlmError.Network)
                is HttpException -> {
                    when (e.code()) {
                        401 -> Magic8BallResult.Failure(LlmError.Unauthorized)
                        429 -> {
                            Magic8BallResult.Failure(LlmError.RateLimited)
                        }

                        in 500..599 -> Magic8BallResult.Failure(LlmError.Server)
                        else -> Magic8BallResult.Failure(LlmError.Unknown(e))
                    }
                }

                else -> Magic8BallResult.Failure(LlmError.Unknown(e))
            }
        }
    }

    private fun validateWordCount(text: String): Boolean {
        val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
        return wordCount <= 6
    }
}
