package com.example.magiceightball.core.data.repository

import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.LlmError
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import javax.inject.Inject

class Magic8BallRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeminiRemoteDataSource,
    private val localDataSource: LocalDataSource
) : LlmGateway {

    override suspend fun generateMagic8BallMessage(request: Magic8BallRequest): Magic8BallResult {
        // 1. Try Remote
        val remoteResult = remoteDataSource.fetchMagic8BallMessage(request)

        return when (remoteResult) {
            is Magic8BallResult.Success -> remoteResult
            is Magic8BallResult.Failure -> {
                // 2. Decide if fallback is appropriate
                if (shouldFallback(remoteResult.error)) {
                    val fallbackMessage = localDataSource.getRandomMessage(request.languageCode)
                    Magic8BallResult.Success(fallbackMessage)
                } else {
                    remoteResult // Return error (e.g. strict safety block might shouldn't be overridden? Or always fallback?)
                    // User request implies "if service returns error or no internet", so basically any failure except maybe logic ones.
                    // Let's fallback on everything for a robust "toy" experience.
                    val fallbackMessage = localDataSource.getRandomMessage(request.languageCode)
                    Magic8BallResult.Success(fallbackMessage)
                }
            }
        }
    }

    private fun shouldFallback(error: LlmError): Boolean {
        // In a real app we might differentiate.
        // For this Magic 8 Ball, we almost always want a result.
        return true 
    }
}
