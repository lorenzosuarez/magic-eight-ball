package com.example.magiceightball.core.domain.model

/**
 * The choices the user makes in the app.
 *
 * @property aiAnswersEnabled whether the user lets answers come from the AI service. On by
 * default, so it takes effect as soon as AI answers become available.
 * @property voiceQuestionsEnabled whether the user lets the app listen to a spoken question.
 * Only the phone app has a microphone feature; the watch app ignores it.
 * @property spokenAnswersEnabled whether answers are also read out loud. Off until asked for:
 * a phone should not start talking on its own. Only the phone app does it.
 * @property partyNames who is playing, in the order they take turns. Fewer than two is nobody:
 * the app is then for one person, as it always was. Only the phone app has it.
 */
data class UserSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC,
    val aiAnswersEnabled: Boolean = true,
    val voiceQuestionsEnabled: Boolean = true,
    val spokenAnswersEnabled: Boolean = false,
    val partyNames: List<String> = emptyList()
) {
    /** Whether several people are playing: see [partyNames]. */
    val isParty: Boolean get() = partyNames.size >= MIN_PARTY

    companion object {
        const val MIN_PARTY = 2
        const val MAX_PARTY = 8
        const val MAX_PARTY_NAME_LENGTH = 12

        /** The names in what someone typed, between commas or one per line: tidied, none twice, not too many. */
        fun parsePartyNames(typed: String): List<String> = typed.split(',', '\n', ';')
            .map { it.trim().take(MAX_PARTY_NAME_LENGTH).trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(MAX_PARTY)
    }
}
