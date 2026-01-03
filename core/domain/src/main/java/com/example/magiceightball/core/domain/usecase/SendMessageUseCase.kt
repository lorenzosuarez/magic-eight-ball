package com.example.magiceightball.core.domain.usecase

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val llmGateway: LlmGateway
) {
    suspend operator fun invoke(
        trigger: String, 
        languageCode: String = "en",
        personality: com.example.magiceightball.core.domain.model.Magic8BallPersonality = com.example.magiceightball.core.domain.model.Magic8BallPersonality.SARCASTIC
    ): Magic8BallResult {
        return llmGateway.generateMagic8BallMessage(
            Magic8BallRequest(
                userTrigger = trigger, 
                languageCode = languageCode,
                personality = personality
            )
        )
    }
}
