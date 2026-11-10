package com.example.magiceightball.core.domain.usecase

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val llmGateway: LlmGateway
) {
    /**
     * @param useAi the user's choice; when false the answer never comes from the AI service.
     */
    suspend operator fun invoke(
        trigger: String,
        languageCode: String,
        personality: Magic8BallPersonality,
        useAi: Boolean
    ): Magic8BallResult {
        return llmGateway.generateMagic8BallMessage(
            Magic8BallRequest(
                userTrigger = trigger,
                languageCode = languageCode,
                personality = personality,
                useAi = useAi
            )
        )
    }
}
