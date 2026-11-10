package com.example.magiceightball.core.domain.model

/**
 * The choices the user makes in the app.
 *
 * @property aiAnswersEnabled whether the user lets answers come from the AI service. On by
 * default, so it takes effect as soon as AI answers become available.
 * @property voiceQuestionsEnabled whether the user lets the app listen to a spoken question.
 * Only the phone app has a microphone feature; the watch app ignores it.
 */
data class UserSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val personality: Magic8BallPersonality = Magic8BallPersonality.SARCASTIC,
    val aiAnswersEnabled: Boolean = true,
    val voiceQuestionsEnabled: Boolean = true
)
