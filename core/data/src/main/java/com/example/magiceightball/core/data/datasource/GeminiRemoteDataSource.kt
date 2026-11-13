package com.example.magiceightball.core.data.datasource

import com.example.magiceightball.core.common.GeminiConfig
import com.example.magiceightball.core.data.config.GeminiSettings
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.data.gateway.GeminiReply
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.policy.AppCharacters
import com.example.magiceightball.core.domain.policy.CharacterBook
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.domain.policy.AnswerBrief
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeminiRemoteDataSource @Inject constructor(
    private val apiClient: GeminiApiClient,
    private val config: GeminiConfig,
    private val promptPolicy: PromptPolicy,
    private val characters: CharacterBook = AppCharacters
) {
    /** How each answer is drawn before it is asked for: only a test has reason to replace it. */
    internal var drawBrief: (Magic8BallRequest) -> AnswerBrief = { request ->
        AnswerBrief.draw(
            personality = request.personality,
            hasQuestion = request.questionForAi != null,
            withRemark = request.wantsRemark,
            detailed = request.detailed,
            languageCode = request.languageCode,
            question = request.questionForAi,
            characters = characters
        )
    }

    /**
     * Asks Gemini for an answer.
     *
     * The system instruction is always the app's own, with what the app drew for this answer:
     * nothing of the user's. The user content is the user's question
     * when the request carries one (capped by [Magic8BallRequest.questionForAi]), and the fixed
     * trigger otherwise. Nothing else about the user is sent.
     *
     * A request that fails comes back as a failure with its reason, never as an exception.
     */
    suspend fun fetchMagic8BallMessage(
        request: Magic8BallRequest,
        settings: GeminiSettings
    ): Magic8BallResult {
        return withContext(Dispatchers.IO) {
            val question = request.questionForAi
            val brief = drawBrief(request)
            val systemPromptText = promptPolicy.getSystemPrompt(
                languageCode = request.languageCode,
                personality = request.personality,
                answersQuestion = question != null,
                // Drawn here, as a shake would: the service words the answer, it does not choose it.
                // It is also all that tells one request from the next when nothing was asked.
                brief = brief
            )
            val userText = question ?: request.userTrigger ?: "What is my fortune?"

            executeWithRetry(settings, systemPromptText, userText, brief.detailed)
        }
    }

    private suspend fun executeWithRetry(
        settings: GeminiSettings,
        systemPromptText: String,
        userText: String,
        detailed: Boolean,
        attempt: Int = 1
    ): Magic8BallResult {
        val instruction =
            if (attempt == 1) systemPromptText else "$systemPromptText Line 1 MUST use 6 words or less."
        val parts = when (
            val reply = apiClient.generateContent(settings, instruction, userText, config.maxOutputTokens)
        ) {
            GeminiReply.Blocked -> return Magic8BallResult.Failure(LlmError.SafetyBlocked)
            is GeminiReply.Failed -> return Magic8BallResult.Failure(reply.error)
            is GeminiReply.Written -> reply.parts
        }

        // The service may send one reply cut into several parts, even in the middle of a
        // word: they are one text, and reading only the first shows an answer cut short.
        val written = parts.joinToString(separator = "").trim().takeIf { it.isNotEmpty() }
            ?: return Magic8BallResult.Failure(LlmError.Serialization)
        // The first line is the answer; what follows it, when there is anything, is the remark.
        val lines = written.lines().map { it.tidied() }.filter { it.isNotEmpty() }
        val text = lines.firstOrNull() ?: return Magic8BallResult.Failure(LlmError.Serialization)
        val remark = lines.drop(1).joinToString(" ").takeIf { it.isUsableRemark(text, detailed) }

        if (!validateWordCount(text)) {
            return if (attempt <= config.maxRetries) {
                executeWithRetry(settings, systemPromptText, userText, detailed, attempt + 1)
            } else {
                Magic8BallResult.Failure(LlmError.ValidationFailed)
            }
        }

        return Magic8BallResult.Success(text, fromAi = true, remark = remark)
    }

    /** A line as it was meant, without what a model sometimes wraps it in: a label, quotes, a bullet. */
    private fun String.tidied(): String = trim()
        .replace(LINE_LABEL, "")
        .trim()
        // Not asterisks: a character may open with something it does, written between them.
        .trim('"', '\'', '«', '»', '“', '”', '–', '•')
        .trim()

    /** A remark worth showing: something of its own, of a length the screen can hold. */
    private fun String.isUsableRemark(answer: String, detailed: Boolean): Boolean =
        isNotBlank() && !equals(answer, ignoreCase = true) &&
            length <= (if (detailed) MAX_READING_LENGTH else MAX_REMARK_LENGTH) &&
            split(WHITESPACE).count { it.isNotBlank() } <= (if (detailed) MAX_READING_WORDS else MAX_REMARK_WORDS)

    private fun validateWordCount(text: String): Boolean {
        val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
        return wordCount <= 6
    }

    private companion object {
        /** A little over what is asked for: a remark one word long is not thrown away. */
        const val MAX_REMARK_WORDS = 18
        const val MAX_REMARK_LENGTH = 120
        /** The same for the few sentences a user asked for: what the screen under the ball can hold. */
        const val MAX_READING_WORDS = 55
        const val MAX_READING_LENGTH = 340
        val WHITESPACE = Regex("\\s+")
        val LINE_LABEL = Regex("^(line|línea|linea)\\s*[12]\\s*[:.)-]\\s*", RegexOption.IGNORE_CASE)
    }
}
