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
        val remoteResult = remoteDataSource.fetchMagic8BallMessage(request)

        return when (remoteResult) {
            is Magic8BallResult.Success -> remoteResult
            is Magic8BallResult.Failure -> {
                if (shouldFallback(remoteResult.error)) {
                    val fallbackMessage = localDataSource.getRandomMessage(request.languageCode)
                    Magic8BallResult.Success(fallbackMessage)
                } else {
                    val fallbackMessage = localDataSource.getRandomMessage(request.languageCode)
                    Magic8BallResult.Success(fallbackMessage)
                }
            }
        }
    }

    private fun shouldFallback(error: LlmError): Boolean {
        return true 
    }
}
