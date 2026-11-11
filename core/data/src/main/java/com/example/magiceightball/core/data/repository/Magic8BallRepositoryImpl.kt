package com.example.magiceightball.core.data.repository

import android.os.SystemClock
import com.example.magiceightball.core.data.config.AiAnswerQuota
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Answers come from Gemini only while the user allows it, it is switched on remotely and a key
 * is available and the device still has some of its daily allowance; in every other case (turned
 * off by the user, switched off remotely, no key, allowance used up, offline, any API error) a
 * bundled answer is used, and no request is sent to Gemini when it is
 * off. A question in the request goes to Gemini only; a bundled answer never depends on it.
 */
class Magic8BallRepositoryImpl(
    private val remoteDataSource: GeminiRemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val geminiSettings: GeminiSettingsSource,
    private val aiAnswerQuota: AiAnswerQuota,
    private val nowMillis: () -> Long
) : LlmGateway {

    @Inject
    constructor(
        remoteDataSource: GeminiRemoteDataSource,
        localDataSource: LocalDataSource,
        geminiSettings: GeminiSettingsSource,
        aiAnswerQuota: AiAnswerQuota
    ) : this(remoteDataSource, localDataSource, geminiSettings, aiAnswerQuota, SystemClock::elapsedRealtime)

    /** Until when the AI service is left alone, after it said the key's allowance is used up. */
    @Volatile
    private var restingUntilMillis = 0L

    override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
        geminiSettings.refresh()
        if (request.useAi) {
            // Remote Config may touch disk the first time it is read.
            val settings = withContext(Dispatchers.IO) { geminiSettings.current() }
            // Past the day's allowance the ball goes on answering, from its own list.
            // While the key's own allowance is used up, asking again only costs the user time.
            val resting = nowMillis() < restingUntilMillis
            if (settings.isUsable && !resting && aiAnswerQuota.tryTake(settings.dailyLimit)) {
                val remote = withTimeoutOrNull(REMOTE_BUDGET_MS) {
                    remoteDataSource.fetchMagic8BallMessage(request, settings.apiKey, settings.modelId)
                }
                if (remote is Magic8BallResult.Success) return remote
                if (remote == Magic8BallResult.Failure(LlmError.RateLimited)) {
                    restingUntilMillis = nowMillis() + RATE_LIMIT_REST_MS
                }
            }
        }
        return Magic8BallResult.Success(
            localDataSource.getRandomMessage(request.languageCode, request.personality)
        )
    }

    private companion object {
        /** Below the UI's maximum thinking time, so a slow network still ends in a bundled answer. */
        const val REMOTE_BUDGET_MS = 4_500L

        /** Long enough to spare a drained key, short enough to notice a per-minute limit lifting. */
        const val RATE_LIMIT_REST_MS = 5 * 60_000L
    }
}
