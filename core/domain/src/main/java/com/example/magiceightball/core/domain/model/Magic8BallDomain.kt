package com.example.magiceightball.core.domain.model

sealed interface Magic8BallResult {
    /**
     * @property fromAi true when the AI service wrote [message]; false for a bundled answer.
     * @property remark what the character adds under its answer, when one was asked for and written.
     * @property aiAllowanceSpent true when this answer is a bundled one only because the day's
     * answers from the AI service are used up: something the user can be told.
     */
    data class Success(
        val message: String,
        val fromAi: Boolean = false,
        val remark: String? = null,
        val aiAllowanceSpent: Boolean = false
    ) : Magic8BallResult
    data class Failure(val error: LlmError) : Magic8BallResult
}

sealed interface LlmError {
    data object Network : LlmError
    data object Timeout : LlmError
    data object Unauthorized : LlmError
    data object RateLimited : LlmError
    data object Server : LlmError
    data object Serialization : LlmError
    data object SafetyBlocked : LlmError
    data object ValidationFailed : LlmError
    data class Unknown(val cause: Throwable?) : LlmError
}

/**
 * @property useAi the user's choice: when false the answer must not come from the AI service.
 * @property question what the user asked, in their own words, when the app heard a question.
 * Null wherever the app takes no question, and then only [userTrigger] is sent.
 * @property detailed whether that second line may run to a few sentences, and come even when
 * nothing was asked: the user's choice.
 * @property wantsRemark whether a second line may come with the answer: only where there is
 * room to show one.
 * @property asker what the character may be told about whoever is asking: see [AskerFacts].
 * Null wherever nobody is signed in, which is everywhere but a phone with an account.
 * @property custom who answers when [personality] is [Magic8BallPersonality.CUSTOM]. Without
 * it such a request is answered as the classic style is, from the app's own answers.
 * @property premium whether whoever asks has paid for what is sold, which gives them more
 * answers from the AI in a day.
 */
data class Magic8BallRequest(
    val userTrigger: String? = null,
    val languageCode: String = "en",
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC,
    val useAi: Boolean = true,
    val question: String? = null,
    val wantsRemark: Boolean = false,
    val detailed: Boolean = false,
    val asker: AskerFacts? = null,
    val custom: CustomCharacter? = null,
    val premium: Boolean = false
) {
    /**
     * The only form in which [question] may leave the device: one line, without control
     * characters, at most [MAX_QUESTION_LENGTH] characters. Null when nothing is left to ask.
     */
    val questionForAi: String?
        get() = question
            ?.replace(LINE_BREAKS_AND_CONTROLS, " ")
            ?.trim()
            ?.take(MAX_QUESTION_LENGTH)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    companion object {
        const val MAX_QUESTION_LENGTH = 200

        private val LINE_BREAKS_AND_CONTROLS = Regex("[\\s\\p{Cntrl}]+")
    }
}
