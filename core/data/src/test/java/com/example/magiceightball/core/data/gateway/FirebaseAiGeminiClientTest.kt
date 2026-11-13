package com.example.magiceightball.core.data.gateway

import com.example.magiceightball.core.domain.model.LlmError
import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.BlockReason
import com.google.firebase.ai.type.Candidate
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FinishReason
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.Part
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.PromptFeedback
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.SerializationException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.UnknownException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

/**
 * What the Firebase SDK hands back, read into the app's own terms. The SDK's replies and errors
 * can only be made by the SDK: the replies here are stand-ins, the errors the real ones.
 */
class FirebaseAiGeminiClientTest {

    @Test
    fun `a reply is handed over in the parts it came in`() {
        val reply = response(candidate("¡NO! ¡Toma bat", "ido!\nTu gemelo malvado ", "robó las respuestas."))

        assertEquals(
            GeminiReply.Written(listOf("¡NO! ¡Toma bat", "ido!\nTu gemelo malvado ", "robó las respuestas.")),
            reply.toReply()
        )
    }

    @Test
    fun `what the model thought on the way is not part of the reply`() {
        val thought = mockk<TextPart> {
            every { isThought } returns true
            every { text } returns "The user wants a yes."
        }
        val reply = response(candidate(parts = listOf(thought, TextPart("Yes."))))

        assertEquals(GeminiReply.Written(listOf("Yes.")), reply.toReply())
    }

    @Test
    fun `a reply with no candidate has nothing written in it`() {
        assertEquals(GeminiReply.Written(emptyList()), response().toReply())
    }

    @Test
    fun `a question the service would not take is blocked`() {
        val feedback = mockk<PromptFeedback> { every { blockReason } returns BlockReason.SAFETY }

        assertEquals(GeminiReply.Blocked, response(feedback = feedback).toReply())
        assertEquals(GeminiReply.Blocked, sdkError<PromptBlockedException>("The prompt could not be submitted").toReply())
    }

    @Test
    fun `an answer stopped for safety is blocked, whatever was written of it`() {
        val stopped = response(candidate("Well, you could", finishReason = FinishReason.SAFETY))

        assertEquals(GeminiReply.Blocked, stopped.toReply())
        assertEquals(GeminiReply.Blocked, stopped(stopped).toReply())
    }

    @Test
    fun `an answer that ran out of room is still read`() {
        val cut = response(candidate("Outlook not so", finishReason = FinishReason.MAX_TOKENS))

        assertEquals(GeminiReply.Written(listOf("Outlook not so")), stopped(cut).toReply())
    }

    @Test
    fun `an answer that ran out of room before a word was written is no answer at all`() {
        val empty = stopped(response(candidate(finishReason = FinishReason.MAX_TOKENS)))

        assertEquals(GeminiReply.Failed(LlmError.Unknown(empty)), empty.toReply())
    }

    @Test
    fun `an allowance used up is told from other refusals of the server`() {
        assertEquals(
            GeminiReply.Failed(LlmError.RateLimited),
            sdkError<QuotaExceededException>("You exceeded your current quota").toReply()
        )
        assertEquals(
            GeminiReply.Failed(LlmError.RateLimited),
            sdkError<ServerException>("Resource exhausted. Please try again later.").toReply()
        )
        assertEquals(GeminiReply.Failed(LlmError.Server), sdkError<ServerException>("Internal error").toReply())
    }

    @Test
    fun `a project that is not set up, or not allowed, is unauthorized`() {
        val errors = listOf(
            sdkError<APINotConfiguredException>("The Gemini Developer API is not enabled"),
            sdkError<ServiceDisabledException>("The Firebase AI Logic API is not enabled"),
            sdkError<InvalidAPIKeyException>("API key not valid")
        )

        for (error in errors) assertEquals(GeminiReply.Failed(LlmError.Unauthorized), error.toReply())
    }

    @Test
    fun `a reply that cannot be read is told from one that never came`() {
        assertEquals(
            GeminiReply.Failed(LlmError.Serialization),
            sdkError<SerializationException>("Error deserializing response, found no valid fields").toReply()
        )
        // This one error also carries what was said before it, which here is nothing.
        val timeout = RequestTimeoutException::class.java
            .getDeclaredConstructor(String::class.java, Throwable::class.java, List::class.java)
            .newInstance("The request failed to complete in the allotted time.", null, emptyList<Content>())
        assertEquals(GeminiReply.Failed(LlmError.Network), timeout.toReply())
    }

    @Test
    fun `no network is found under the error the SDK wraps it in`() {
        val offline = sdkError<UnknownException>("Something unexpected happened.", UnknownHostException("no address"))

        assertEquals(GeminiReply.Failed(LlmError.Network), offline.toReply())
    }

    @Test
    fun `anything else is passed on as it is`() {
        val wrapped = sdkError<UnknownException>("Something unexpected happened.", IllegalStateException("odd"))
        val plain = IllegalStateException("Default FirebaseApp is not initialized")

        assertEquals(GeminiReply.Failed(LlmError.Unknown(wrapped)), wrapped.toReply())
        assertEquals(GeminiReply.Failed(LlmError.Unknown(plain)), plain.toReply())
    }

    private fun candidate(vararg texts: String, finishReason: FinishReason? = FinishReason.STOP): Candidate =
        candidate(texts.map { TextPart(it) }, finishReason)

    private fun candidate(parts: List<Part>, finishReason: FinishReason? = FinishReason.STOP): Candidate =
        mockk {
            every { content } returns Content(role = "model", parts = parts)
            every { this@mockk.finishReason } returns finishReason
        }

    private fun response(vararg candidates: Candidate, feedback: PromptFeedback? = null): GenerateContentResponse =
        mockk {
            every { this@mockk.candidates } returns candidates.toList()
            every { promptFeedback } returns feedback
        }

    private fun stopped(response: GenerateContentResponse): ResponseStoppedException =
        ResponseStoppedException::class.java
            .getDeclaredConstructor(GenerateContentResponse::class.java, Throwable::class.java)
            .newInstance(response, null)

    /** One of the SDK's own errors: only the SDK may make them, so it is made the long way round. */
    private inline fun <reified T : Exception> sdkError(message: String, cause: Throwable? = null): T =
        T::class.java
            .getDeclaredConstructor(String::class.java, Throwable::class.java)
            .newInstance(message, cause)
}
