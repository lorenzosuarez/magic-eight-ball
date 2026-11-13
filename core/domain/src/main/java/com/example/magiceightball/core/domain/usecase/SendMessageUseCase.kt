package com.example.magiceightball.core.domain.usecase

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.Magic8BallRequest
import com.example.magiceightball.core.domain.model.Magic8BallResult
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val llmGateway: LlmGateway
) {
    /**
     * @param useAi the user's choice; when false the answer never comes from the AI service.
     * @param question what the user asked, where the app hears questions; null everywhere else.
     * @param detailed whether that line may run to a few sentences: see [Magic8BallRequest.detailed].
     * @param wantsRemark whether the screen has room for a second line under the answer.
     * @param asker what the character may be told about whoever is asking: see [AskerFacts].
     * @param custom who answers when [personality] is one of the user's own.
     */
    suspend operator fun invoke(
        trigger: String,
        languageCode: String,
        personality: Magic8BallPersonality,
        useAi: Boolean,
        question: String? = null,
        wantsRemark: Boolean = false,
        detailed: Boolean = false,
        asker: AskerFacts? = null,
        custom: CustomCharacter? = null
    ): Magic8BallResult {
        return llmGateway.generateMagic8BallMessage(
            Magic8BallRequest(
                userTrigger = trigger,
                languageCode = languageCode,
                personality = personality,
                useAi = useAi,
                question = question,
                wantsRemark = wantsRemark,
                detailed = detailed,
                asker = asker,
                custom = custom
            )
        )
    }
}
