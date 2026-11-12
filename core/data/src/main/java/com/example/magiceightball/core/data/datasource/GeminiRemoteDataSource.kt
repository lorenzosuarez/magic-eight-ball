package com.example.magiceightball.core.data.datasource

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.domain.policy.AnswerBrief
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
    /** How each answer is drawn before it is asked for: only a test has reason to replace it. */
    internal var drawBrief: (Magic8BallRequest) -> AnswerBrief = { request ->
        AnswerBrief.draw(
            personality = request.personality,
            hasQuestion = request.questionForAi != null,
            withRemark = request.wantsRemark,
            languageCode = request.languageCode,
            question = request.questionForAi
        )
    }

    /**
     * Asks Gemini for an answer.
     *
     * The system instruction is always the app's own, with what the app drew for this answer:
     * nothing of the user's. The user content is the user's question
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
                // It is also all that tells one request from the next when nothing was asked.
                brief = drawBrief(request)
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

            // The service may send one reply cut into several parts, even in the middle of a
            // word: they are one text, and reading only the first shows an answer cut short.
            val written =
                response.candidates?.firstOrNull()?.content?.parts
                    ?.joinToString(separator = "") { part -> part.text.orEmpty() }
                    ?.trim()?.takeIf { it.isNotEmpty() }
                    ?: return Magic8BallResult.Failure(LlmError.Serialization)
            // The first line is the answer; what follows it, when there is anything, is the remark.
            val lines = written.lines().map { it.tidied() }.filter { it.isNotEmpty() }
            val text = lines.firstOrNull() ?: return Magic8BallResult.Failure(LlmError.Serialization)
            val remark = lines.drop(1).joinToString(" ").takeIf { it.isUsableRemark(text) }

            if (!validateWordCount(text)) {
                if (attempt <= config.maxRetries) {
                    val stricterInstruction = Content(
                        parts = listOf(Part(text = "$systemPromptText Line 1 MUST use 6 words or less."))
                    )
                    val retryRequest = initialRequest.copy(systemInstruction = stricterInstruction)
                    return executeWithRetry(apiKey, modelId, retryRequest, systemPromptText, attempt + 1)
                } else {
                    return Magic8BallResult.Failure(LlmError.ValidationFailed)
                }
            }

            return Magic8BallResult.Success(text, fromAi = true, remark = remark)
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

    /** A line as it was meant, without what a model sometimes wraps it in: a label, quotes, a bullet. */
    private fun String.tidied(): String = trim()
        .replace(LINE_LABEL, "")
        .trim()
        // Not asterisks: a character may open with something it does, written between them.
        .trim('"', '\'', '«', '»', '“', '”', '–', '•')
        .trim()

    /** A remark worth showing: something of its own, of a length the screen can hold. */
    private fun String.isUsableRemark(answer: String): Boolean =
        isNotBlank() && length <= MAX_REMARK_LENGTH && !equals(answer, ignoreCase = true) &&
            split(WHITESPACE).count { it.isNotBlank() } <= MAX_REMARK_WORDS

    private fun validateWordCount(text: String): Boolean {
        val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
        return wordCount <= 6
    }

    private companion object {
        /** A little over what is asked for: a remark one word long is not thrown away. */
        const val MAX_REMARK_WORDS = 18
        const val MAX_REMARK_LENGTH = 120
        val WHITESPACE = Regex("\\s+")
        val LINE_LABEL = Regex("^(line|línea|linea)\\s*[12]\\s*[:.)-]\\s*", RegexOption.IGNORE_CASE)
    }
}
