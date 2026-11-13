package com.example.magiceightball.core.domain.repository

import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** Keeps the user's choices across app launches. */
interface UserSettingsRepository {
    /** The saved choices, then every change to them. */
    val settings: Flow<UserSettings>

    suspend fun setLanguage(language: AppLanguage)

    /** Chooses one of the app's characters. One of the user's own is chosen with [setCustomCharacter]. */
    suspend fun setPersonality(personality: Magic8BallPersonality)

    /**
     * Chooses [character], one of the user's own, and keeps it whole. Null gives the choice
     * up: if one of the user's own was chosen, the app's first character is again.
     */
    suspend fun setCustomCharacter(character: CustomCharacter?)

    suspend fun setAiAnswersEnabled(enabled: Boolean)

    suspend fun setVoiceQuestionsEnabled(enabled: Boolean)

    suspend fun setSpokenAnswersEnabled(enabled: Boolean)
    suspend fun setDetailedAnswers(enabled: Boolean)

    suspend fun setPartyNames(names: List<String>)

    suspend fun setDailyVerdictEnabled(enabled: Boolean)
}
