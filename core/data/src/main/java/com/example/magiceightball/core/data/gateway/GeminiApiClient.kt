package com.example.magiceightball.core.data.gateway

import com.example.magiceightball.core.data.config.GeminiSettings
import com.example.magiceightball.core.domain.model.LlmError

/**
 * The AI service, as the app needs it: one instruction and one line from the user in, one reply
 * out. Whatever carries the request, and whatever vouches for the app, stays behind this.
 */
interface GeminiApiClient {
    /**
     * Asks once. A request that fails is a [GeminiReply.Failed], never an exception: only a
     * cancelled wait is thrown, as a cancellation.
     */
    suspend fun generateContent(
        settings: GeminiSettings,
        systemInstruction: String,
        userText: String,
        maxOutputTokens: Int
    ): GeminiReply
}

/** What came back from the AI service for one request. */
sealed interface GeminiReply {
    /**
     * What the service wrote, in the pieces it came in: they are one text, and a piece may end
     * in the middle of a word.
     */
    data class Written(val parts: List<String>) : GeminiReply

    /** The service would not answer this, for the safety of who asks or of others. */
    data object Blocked : GeminiReply

    data class Failed(val error: LlmError) : GeminiReply
}
