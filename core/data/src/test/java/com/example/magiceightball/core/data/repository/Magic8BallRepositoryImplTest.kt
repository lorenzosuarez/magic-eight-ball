package com.example.magiceightball.core.data.repository

import com.example.magiceightball.core.data.config.DeviceAiAnswerQuota
import com.example.magiceightball.core.data.config.GeminiSettings
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class Magic8BallRepositoryImplTest {

    private class MemoryStore : DeviceAiAnswerQuota.Store {
        override var day: String? = null
        override var used: Int = 0
        override var bonus: Int = 0
    }

    private val remoteDataSource = mockk<GeminiRemoteDataSource> {
        coEvery { fetchMagic8BallMessage(any(), any()) } returns Magic8BallResult.Success(REMOTE_ANSWER)
    }
    private val localDataSource = object : LocalDataSource {
        override suspend fun getRandomMessage(
            languageCode: String,
            personality: Magic8BallPersonality
        ): String = LOCAL_ANSWER
    }
    private val geminiSettings = FakeGeminiSettingsSource()
    private val quota = DeviceAiAnswerQuota(MemoryStore(), today = { today })
    private var today = "2026-11-10"
    private var now = 0L
    private val repository =
        Magic8BallRepositoryImpl(remoteDataSource, localDataSource, geminiSettings, quota) { now }

    @Test
    fun `answers added to the day's allowance are used after it, and do not carry over`() = runTest {
        geminiSettings.settings = geminiSettings.settings.copy(dailyLimit = 1)
        val request = Magic8BallRequest(useAi = true)
        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
        assertEquals(
            Magic8BallResult.Success(LOCAL_ANSWER, aiAllowanceSpent = true),
            repository.generateMagic8BallMessage(request)
        )

        quota.grant(2)
        repeat(2) { assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request)) }
        assertEquals(
            Magic8BallResult.Success(LOCAL_ANSWER, aiAllowanceSpent = true),
            repository.generateMagic8BallMessage(request)
        )

        // The next day there is the daily limit again, and nothing of what was added.
        today = "2026-11-11"
        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
        assertEquals(
            Magic8BallResult.Success(LOCAL_ANSWER, aiAllowanceSpent = true),
            repository.generateMagic8BallMessage(request)
        )
    }

    @Test
    fun `once the project's allowance is used up gemini is left alone for a while`() = runTest {
        val request = Magic8BallRequest(useAi = true)
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Failure(LlmError.RateLimited)

        repeat(3) { assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), repository.generateMagic8BallMessage(request)) }
        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }

        now += 5 * 60_000L
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Success(REMOTE_ANSWER)
        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
    }

    @Test
    fun `past the day's allowance answers are bundled, until the next day`() = runTest {
        geminiSettings.settings = geminiSettings.settings.copy(dailyLimit = 2)
        val request = Magic8BallRequest(useAi = true)

        repeat(2) { assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request)) }
        // And says why, so the user can be told.
        assertEquals(
            Magic8BallResult.Success(LOCAL_ANSWER, aiAllowanceSpent = true),
            repository.generateMagic8BallMessage(request)
        )
        coVerify(exactly = 2) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }

        today = "2026-11-11"
        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
    }

    @Test
    fun `answers that never reach gemini do not use up the allowance`() = runTest {
        geminiSettings.settings = geminiSettings.settings.copy(dailyLimit = 1)

        repeat(3) { repository.generateMagic8BallMessage(Magic8BallRequest(useAi = false)) }

        assertEquals(
            Magic8BallResult.Success(REMOTE_ANSWER),
            repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))
        )
    }

    @Test
    fun `gemini answers when the user allows it and it is usable`() = runTest {
        val request = Magic8BallRequest(useAi = true)

        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(request, geminiSettings.settings) }
    }

    @Test
    fun `a question travels with the request to gemini`() = runTest {
        val request = Magic8BallRequest(userTrigger = "shake", question = "Will it rain?")

        repository.generateMagic8BallMessage(request)

        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(request, geminiSettings.settings) }
    }

    @Test
    fun `a question goes nowhere when the user turned ai answers off`() = runTest {
        val result = repository.generateMagic8BallMessage(
            Magic8BallRequest(useAi = false, question = "Will it rain?")
        )

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }
    }

    @Test
    fun `a character of the user's own is written for with its description, and answers from the app without it`() = runTest {
        val nemo = CustomCharacter("1", "Capitán Nemo", "Un capitán gruñón que todo lo compara con el mar.")
        val described = Magic8BallRequest(useAi = true, personality = Magic8BallPersonality.CUSTOM, custom = nemo)
        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(described))
        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(described, geminiSettings.settings) }

        // Chosen, and nothing said of how it answers: there is no voice to ask the service for.
        val bare = Magic8BallRequest(useAi = true, personality = Magic8BallPersonality.CUSTOM)
        val wordless = bare.copy(custom = nemo.copy(description = " [] "))
        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), repository.generateMagic8BallMessage(bare))
        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), repository.generateMagic8BallMessage(wordless))
        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }
    }

    @Test
    fun `no request is sent to gemini when the user turned ai answers off`() = runTest {
        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = false))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }
    }

    @Test
    fun `no request is sent to gemini while it is switched off remotely`() = runTest {
        geminiSettings.settings = GeminiSettings(enabled = false, modelId = MODEL)

        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }
    }

    @Test
    fun `no request is sent to gemini without a model to ask`() = runTest {
        geminiSettings.settings = GeminiSettings(enabled = true, modelId = "")

        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any()) }
    }

    @Test
    fun `an answer that never came does not use up the day, and one that was turned down does`() = runTest {
        geminiSettings.settings = geminiSettings.settings.copy(dailyLimit = 1)
        val request = Magic8BallRequest(useAi = true)

        // No network, three times over: the day's one answer is still there.
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Failure(LlmError.Network)
        repeat(3) {
            assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), repository.generateMagic8BallMessage(request))
        }

        // The service answered and the app could not use it: that one counts.
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Failure(LlmError.ValidationFailed)
        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), repository.generateMagic8BallMessage(request))
        assertEquals(
            Magic8BallResult.Success(LOCAL_ANSWER, aiAllowanceSpent = true),
            repository.generateMagic8BallMessage(request)
        )
    }

    @Test
    fun `what the service would not answer gets no answer drawn by chance`() = runTest {
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Failure(LlmError.SafetyBlocked)

        assertEquals(
            Magic8BallResult.Failure(LlmError.SafetyBlocked),
            repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true, question = "Should I hurt someone?"))
        )
    }

    @Test
    fun `a failed gemini request ends in a bundled answer`() = runTest {
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any()) } returns
            Magic8BallResult.Failure(LlmError.Network)

        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
    }

    @Test
    fun `every request asks for fresh settings`() = runTest {
        repository.generateMagic8BallMessage(Magic8BallRequest(useAi = false))
        repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(2, geminiSettings.refreshCount)
    }

    private class FakeGeminiSettingsSource : GeminiSettingsSource {
        var settings = GeminiSettings(enabled = true, modelId = MODEL)
        var refreshCount = 0

        override val isAvailable = MutableStateFlow(true)
        override fun current(): GeminiSettings = settings
        override fun refresh() {
            refreshCount++
        }
    }

    private companion object {
        const val MODEL = "gemini-test-model"
        const val REMOTE_ANSWER = "Remote answer"
        const val LOCAL_ANSWER = "Local answer"
    }
}
