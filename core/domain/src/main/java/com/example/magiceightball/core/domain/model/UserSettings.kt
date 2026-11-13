package com.example.magiceightball.core.domain.model

/**
 * The choices the user makes in the app.
 *
 * @property aiAnswersEnabled whether the user lets answers come from the AI service. On by
 * default, so it takes effect as soon as AI answers become available.
 * @property voiceQuestionsEnabled whether the user lets the app listen to a spoken question.
 * Only the phone app has a microphone feature; the watch app ignores it.
 * @property detailedAnswers whether the AI service is asked to say more under its answer: a few
 * sentences rather than one line, and also when nothing was asked. Off until asked for.
 * @property spokenAnswersEnabled whether answers are also read out loud. Off until asked for:
 * a phone should not start talking on its own. Only the phone app does it.
 * @property dailyVerdictEnabled whether the phone may give the day's verdict as a notification.
 * On until switched off, and nothing is shown without the system's own permission either way.
 * @property partyNames who is playing, in the order they take turns. Fewer than two is nobody:
 * the app is then for one person, as it always was. Only the phone app has it.
 * @property customCharacter the character of the user's own that is chosen, kept whole so that
 * it answers without a connection to where the others are. Never null while [personality] is
 * [Magic8BallPersonality.CUSTOM], and null otherwise. Only the phone app has them.
 */
data class UserSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC,
    val aiAnswersEnabled: Boolean = true,
    val voiceQuestionsEnabled: Boolean = true,
    val spokenAnswersEnabled: Boolean = false,
    val detailedAnswers: Boolean = false,
    val partyNames: List<String> = emptyList(),
    val dailyVerdictEnabled: Boolean = true,
    val customCharacter: CustomCharacter? = null
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
