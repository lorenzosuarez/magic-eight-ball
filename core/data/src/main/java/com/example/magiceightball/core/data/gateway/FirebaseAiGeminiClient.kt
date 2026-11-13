package com.example.magiceightball.core.data.gateway

import android.content.Context
import android.util.Log
import com.example.magiceightball.core.data.config.GeminiSettings
import com.example.magiceightball.core.domain.model.LlmError
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.FinishReason
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.SerializationException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import javax.inject.Inject

/**
 * Asks Gemini through Firebase AI Logic.
 *
 * No key for the AI service travels with the app or is handed to it: Firebase holds it, and lets
 * a request through on the proof App Check sends along that it comes from this app (see
 * AppCheck). The request goes to the project named in `google-services.json`; built without that
 * file there is no project to ask, and nothing is sent.
 */
class FirebaseAiGeminiClient @Inject constructor(
    @ApplicationContext private val context: Context
) : GeminiApiClient {

    override suspend fun generateContent(
        settings: GeminiSettings,
        systemInstruction: String,
        userText: String,
        maxOutputTokens: Int
    ): GeminiReply {
        if (FirebaseApp.getApps(context).isEmpty()) return GeminiReply.Failed(LlmError.Unauthorized)
        return try {
            // The SDK takes the instruction when a model is made, not with a request, and the
            // instruction is drawn anew for every answer: so is the model.
            Firebase.ai(
                backend = when (settings.backend) {
                    GeminiSettings.Backend.DEVELOPER_API -> GenerativeBackend.googleAI()
                    GeminiSettings.Backend.AGENT_PLATFORM -> GenerativeBackend.agentPlatform()
                },
                useLimitedUseAppCheckTokens = settings.limitedUseTokens
            ).generativeModel(
                modelName = settings.modelId,
                // Sampling parameters are left to the model defaults: Gemini 3.x and later ignore them.
                generationConfig = generationConfig { this.maxOutputTokens = maxOutputTokens },
                systemInstruction = content { text(systemInstruction) }
            ).generateContent(userText).toReply()
        } catch (e: Exception) {
            // A wait that was called off is not a request that failed: it is passed on as it is.
            currentCoroutineContext().ensureActive()
            // What went wrong says what is left to set up: the project, the proof, the model.
            Log.w(TAG, "The AI service did not answer", e)
            e.toReply()
        }
    }

    private companion object {
        const val TAG = "FirebaseAiGeminiClient"
    }
}

/** The reply in the app's own terms. Thoughts the model may send along are not part of it. */
internal fun GenerateContentResponse.toReply(): GeminiReply {
    val candidate = candidates.firstOrNull()
    if (promptFeedback?.blockReason != null || candidate?.finishReason == FinishReason.SAFETY) {
        return GeminiReply.Blocked
    }
    return GeminiReply.Written(
        candidate?.content?.parts.orEmpty()
            .filterIsInstance<TextPart>()
            .filterNot { it.isThought }
            .map { it.text }
    )
}

/** Why a request failed, in the app's own terms. */
internal fun Exception.toReply(): GeminiReply = when (this) {
    is PromptBlockedException -> GeminiReply.Blocked
    // To the SDK a reply that did not end by itself is an error. It is still a reply: it is read
    // like any other, so that one cut short for safety is told from one that ran out of room.
    // One that ran out before a word was written is no answer, and not one of the day's.
    is ResponseStoppedException -> response.toReply().let { reply ->
        if (reply is GeminiReply.Written && reply.parts.all { it.isBlank() }) {
            GeminiReply.Failed(LlmError.Unknown(this))
        } else {
            reply
        }
    }
    is QuotaExceededException -> GeminiReply.Failed(LlmError.RateLimited)
    is SerializationException -> GeminiReply.Failed(LlmError.Serialization)
    is RequestTimeoutException -> GeminiReply.Failed(LlmError.Network)
    is InvalidAPIKeyException,
    is APINotConfiguredException,
    is ServiceDisabledException -> GeminiReply.Failed(LlmError.Unauthorized)
    // The SDK keeps the service's message and drops the status it came with: an allowance used
    // up is known only by its words when they do not say "quota".
    is ServerException -> GeminiReply.Failed(
        if (message.orEmpty().contains("exhausted", ignoreCase = true)) LlmError.RateLimited else LlmError.Server
    )
    // No network, or one that gave way: the SDK hands these over wrapped in an error of its own.
    else -> GeminiReply.Failed(
        if (generateSequence<Throwable>(this) { it.cause }.take(MAX_CAUSES).any { it is IOException }) LlmError.Network
        else LlmError.Unknown(this)
    )
}

/** Further down than this nothing new is learnt, and two errors may name each other as the cause. */
private const val MAX_CAUSES = 8
