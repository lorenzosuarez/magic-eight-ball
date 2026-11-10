package com.example.magiceightball.core.data.repository

import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Answers come from Gemini only while it is switched on remotely and a key is available;
 * in every other case (switched off, no key, offline, any API error) a bundled answer is used,
 * and no network request is made when Gemini is off.
 */
class Magic8BallRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeminiRemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val geminiSettings: GeminiSettingsSource
) : LlmGateway {

    override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
        geminiSettings.refresh()
        val settings = geminiSettings.current()
        if (settings.isUsable) {
            val remote = withTimeoutOrNull(REMOTE_BUDGET_MS) {
                remoteDataSource.fetchMagic8BallMessage(request, settings.apiKey)
            }
            if (remote is Magic8BallResult.Success) return remote
        }
        return Magic8BallResult.Success(
            localDataSource.getRandomMessage(request.languageCode, request.personality)
        )
    }

    private companion object {
        /** Below the UI's maximum thinking time, so a slow network still ends in a bundled answer. */
        const val REMOTE_BUDGET_MS = 4_500L
    }
}
