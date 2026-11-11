package com.example.magiceightball.core.data.datasource

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.domain.policy.Verdict
import com.example.magiceightball.core.network.model.Content
import com.example.magiceightball.core.network.model.GeminiRequest
import com.example.magiceightball.core.network.model.GenerationConfig
import com.example.magiceightball.core.network.model.Part
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GeminiRemoteDataSource @Inject constructor(
    private val apiClient: GeminiApiClient,
    private val config: GeminiConfig,
    private val promptPolicy: PromptPolicy
) {
    /** How the answer to a question is drawn: only a test has reason to replace it. */
    internal var drawVerdict: () -> Verdict = { Verdict.draw() }

    /**
     * Asks Gemini for an answer.
     *
     * The system instruction is always the app's own. The user content is the user's question
     * when the request carries one (capped by [Magic8BallRequest.questionForAi]), and the fixed
     * trigger otherwise. Nothing else about the user is sent.
     */
    suspend fun fetchMagic8BallMessage(
        request: Magic8BallRequest,
        apiKey: String,
        modelId: String
    ): Magic8BallResult {
        return withContext(Dispatchers.IO) {
            val question = request.questionForAi
            val systemPromptText = promptPolicy.getSystemPrompt(
                languageCode = request.languageCode,
                personality = request.personality,
                answersQuestion = question != null,
                // Drawn here, as a shake would: the service words the answer, it does not choose it.
                // Without a question the instruction stays the fixed one it has always been.
                verdict = if (question != null) drawVerdict() else null
            )

            val systemInstruction = Content(parts = listOf(Part(text = systemPromptText)))
            val userContent = Content(
                parts = listOf(Part(text = question ?: request.userTrigger ?: "What is my fortune?"))
            )

            // Sampling parameters are left to the model defaults: Gemini 3.x deprecates temperature/topP.
            val genConfig = GenerationConfig(maxOutputTokens = config.maxOutputTokens)

            val apiRequest = GeminiRequest(
                contents = listOf(userContent),
                systemInstruction = systemInstruction,
                generationConfig = genConfig
            )

            executeWithRetry(apiKey, modelId, apiRequest, systemPromptText)
        }
    }

    private suspend fun executeWithRetry(
        apiKey: String,
        modelId: String,
        initialRequest: GeminiRequest,
        systemPromptText: String,
        attempt: Int = 1
    ): Magic8BallResult {
        try {
            val response = apiClient.generateContent(apiKey, modelId, initialRequest)

            if (response.promptFeedback?.blockReason != null) {
                return Magic8BallResult.Failure(LlmError.SafetyBlocked)
            }
            if (!response.candidates.isNullOrEmpty() && response.candidates?.get(0)?.finishReason == "SAFETY") {
                return Magic8BallResult.Failure(LlmError.SafetyBlocked)
            }

            val text =
                response.candidates?.firstOrNull()?.content?.parts
                    ?.firstNotNullOfOrNull { part -> part.text?.trim()?.takeIf { it.isNotEmpty() } }
                    ?: return Magic8BallResult.Failure(LlmError.Serialization)

            if (!validateWordCount(text)) {
                if (attempt <= config.maxRetries) {
                    val stricterInstruction = Content(
                        parts = listOf(Part(text = "$systemPromptText You MUST use 6 words or less."))
                    )
                    val retryRequest = initialRequest.copy(systemInstruction = stricterInstruction)
                    return executeWithRetry(apiKey, modelId, retryRequest, systemPromptText, attempt + 1)
                } else {
                    return Magic8BallResult.Failure(LlmError.ValidationFailed)
                }
            }

            return Magic8BallResult.Success(text, fromAi = true)
        } catch (e: CancellationException) {
            throw e
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
