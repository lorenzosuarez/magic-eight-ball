package com.example.magiceightball.core.data.settings

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Keeps the user's choices in a Preferences DataStore file on the device.
 *
 * Until the user picks a language, the app follows the device: Spanish when
 * [deviceLanguageCode] is Spanish, English otherwise. A file that cannot be read counts as
 * empty, so the defaults are always delivered.
 */
class DataStoreUserSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val deviceLanguageCode: () -> String
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { e ->
            if (e !is IOException) throw e
            Log.w(TAG, "Could not read the user settings", e)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val defaults = UserSettings()
            UserSettings(
                language = AppLanguage.fromCode(preferences[LANGUAGE] ?: deviceLanguageCode()),
                personality = Magic8BallPersonality.entries
                    .find { it.name == preferences[PERSONALITY] }
                    ?: defaults.personality,
                aiAnswersEnabled = preferences[AI_ANSWERS_ENABLED] ?: defaults.aiAnswersEnabled,
                voiceQuestionsEnabled = preferences[VOICE_QUESTIONS_ENABLED]
                    ?: defaults.voiceQuestionsEnabled
            )
        }
        .distinctUntilChanged()

    override suspend fun setLanguage(language: AppLanguage) {
        save { it[LANGUAGE] = language.code }
    }

    override suspend fun setPersonality(personality: Magic8BallPersonality) {
        save { it[PERSONALITY] = personality.name }
    }

    override suspend fun setAiAnswersEnabled(enabled: Boolean) {
        save { it[AI_ANSWERS_ENABLED] = enabled }
    }

    override suspend fun setVoiceQuestionsEnabled(enabled: Boolean) {
        save { it[VOICE_QUESTIONS_ENABLED] = enabled }
    }

    /** A choice that cannot be written is dropped: the app keeps showing the saved one. */
    private suspend fun save(change: (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(change)
        } catch (e: IOException) {
            Log.w(TAG, "Could not save the user settings", e)
        }
    }

    private companion object {
        const val TAG = "UserSettingsRepository"
        val LANGUAGE = stringPreferencesKey("language")
        val PERSONALITY = stringPreferencesKey("personality")
        val AI_ANSWERS_ENABLED = booleanPreferencesKey("ai_answers_enabled")
        val VOICE_QUESTIONS_ENABLED = booleanPreferencesKey("voice_questions_enabled")
    }
}
