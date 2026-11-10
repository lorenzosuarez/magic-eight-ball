package com.example.magiceightball.core.data.repository

import com.example.magiceightball.core.data.config.GeminiSettings
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
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

    private val remoteDataSource = mockk<GeminiRemoteDataSource> {
        coEvery { fetchMagic8BallMessage(any(), any(), any()) } returns Magic8BallResult.Success(REMOTE_ANSWER)
    }
    private val localDataSource = object : LocalDataSource {
        override suspend fun getRandomMessage(
            languageCode: String,
            personality: Magic8BallPersonality
        ): String = LOCAL_ANSWER
    }
    private val geminiSettings = FakeGeminiSettingsSource()
    private val repository = Magic8BallRepositoryImpl(remoteDataSource, localDataSource, geminiSettings)

    @Test
    fun `gemini answers when the user allows it and it is usable`() = runTest {
        val request = Magic8BallRequest(useAi = true)

        assertEquals(Magic8BallResult.Success(REMOTE_ANSWER), repository.generateMagic8BallMessage(request))
        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(request, API_KEY, MODEL) }
    }

    @Test
    fun `a question travels with the request to gemini`() = runTest {
        val request = Magic8BallRequest(userTrigger = "shake", question = "Will it rain?")

        repository.generateMagic8BallMessage(request)

        coVerify(exactly = 1) { remoteDataSource.fetchMagic8BallMessage(request, API_KEY, MODEL) }
    }

    @Test
    fun `a question goes nowhere when the user turned ai answers off`() = runTest {
        val result = repository.generateMagic8BallMessage(
            Magic8BallRequest(useAi = false, question = "Will it rain?")
        )

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any(), any()) }
    }

    @Test
    fun `no request is sent to gemini when the user turned ai answers off`() = runTest {
        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = false))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any(), any()) }
    }

    @Test
    fun `no request is sent to gemini while it is switched off remotely`() = runTest {
        geminiSettings.settings = GeminiSettings(enabled = false, apiKey = API_KEY, modelId = MODEL)

        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any(), any()) }
    }

    @Test
    fun `no request is sent to gemini without a key`() = runTest {
        geminiSettings.settings = GeminiSettings(enabled = true, apiKey = "", modelId = MODEL)

        val result = repository.generateMagic8BallMessage(Magic8BallRequest(useAi = true))

        assertEquals(Magic8BallResult.Success(LOCAL_ANSWER), result)
        coVerify(exactly = 0) { remoteDataSource.fetchMagic8BallMessage(any(), any(), any()) }
    }

    @Test
    fun `a failed gemini request ends in a bundled answer`() = runTest {
        coEvery { remoteDataSource.fetchMagic8BallMessage(any(), any(), any()) } returns
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
        var settings = GeminiSettings(enabled = true, apiKey = API_KEY, modelId = MODEL)
        var refreshCount = 0

        override val isAvailable = MutableStateFlow(true)
        override fun current(): GeminiSettings = settings
        override fun refresh() {
            refreshCount++
        }
    }

    private companion object {
        const val API_KEY = "key"
        const val MODEL = "gemini-test-model"
        const val REMOTE_ANSWER = "Remote answer"
        const val LOCAL_ANSWER = "Local answer"
    }
}
