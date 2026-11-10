package com.example.magiceightball.core.domain.model

sealed interface Magic8BallResult {
    data class Success(val message: String) : Magic8BallResult
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
 * Null wherever the app takes no question (the watch), and then only [userTrigger] is sent.
 */
data class Magic8BallRequest(
    val userTrigger: String? = null,
    val languageCode: String = "en",
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC,
    val useAi: Boolean = true,
    val question: String? = null
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
