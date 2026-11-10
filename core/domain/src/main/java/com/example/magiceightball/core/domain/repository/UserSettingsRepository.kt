package com.example.magiceightball.core.domain.repository

import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** Keeps the user's choices across app launches. */
interface UserSettingsRepository {
    /** The saved choices, then every change to them. */
    val settings: Flow<UserSettings>

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setPersonality(personality: Magic8BallPersonality)

    suspend fun setAiAnswersEnabled(enabled: Boolean)
}
