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
import com.example.magiceightball.core.domain.model.CustomCharacter
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
            val custom = preferences.customCharacter()
            val chosen = Magic8BallPersonality.entries.find { it.name == preferences[PERSONALITY] }
            UserSettings(
                language = AppLanguage.fromCode(preferences[LANGUAGE] ?: deviceLanguageCode()),
                // One of the user's own is chosen only with the character itself beside it.
                personality = chosen?.takeIf { it != Magic8BallPersonality.CUSTOM || custom != null }
                    ?: defaults.personality,
                customCharacter = custom.takeIf { chosen == Magic8BallPersonality.CUSTOM },
                aiAnswersEnabled = preferences[AI_ANSWERS_ENABLED] ?: defaults.aiAnswersEnabled,
                voiceQuestionsEnabled = preferences[VOICE_QUESTIONS_ENABLED]
                    ?: defaults.voiceQuestionsEnabled,
                spokenAnswersEnabled = preferences[SPOKEN_ANSWERS_ENABLED]
                    ?: defaults.spokenAnswersEnabled,
                detailedAnswers = preferences[DETAILED_ANSWERS] ?: defaults.detailedAnswers,
                partyNames = UserSettings.parsePartyNames(preferences[PARTY_NAMES].orEmpty()),
                dailyVerdictEnabled = preferences[DAILY_VERDICT_ENABLED] ?: defaults.dailyVerdictEnabled
            )
        }
        .distinctUntilChanged()

    override suspend fun setLanguage(language: AppLanguage) {
        save { it[LANGUAGE] = language.code }
    }

    override suspend fun setPersonality(personality: Magic8BallPersonality) {
        // One of the user's own is not chosen by its kind alone: see [setCustomCharacter].
        if (personality == Magic8BallPersonality.CUSTOM) return
        save {
            it[PERSONALITY] = personality.name
            it.forgetCustomCharacter()
        }
    }

    override suspend fun setCustomCharacter(character: CustomCharacter?) {
        save {
            if (character == null) {
                if (it[PERSONALITY] == Magic8BallPersonality.CUSTOM.name) it.remove(PERSONALITY)
                it.forgetCustomCharacter()
            } else {
                it[PERSONALITY] = Magic8BallPersonality.CUSTOM.name
                it[CUSTOM_ID] = character.id
                it[CUSTOM_NAME] = character.name
                it[CUSTOM_DESCRIPTION] = character.description
                it[CUSTOM_COLOUR] = character.colour.name
                it[CUSTOM_EMBLEM] = character.emblem.name
            }
        }
    }

    /** The character of the user's own kept with the choice of it; null unless all of it is there. */
    private fun Preferences.customCharacter(): CustomCharacter? {
        val id = this[CUSTOM_ID] ?: return null
        val name = this[CUSTOM_NAME] ?: return null
        val description = this[CUSTOM_DESCRIPTION] ?: return null
        return CustomCharacter(
            id = id,
            name = name,
            description = description,
            colour = lookOf(this[CUSTOM_COLOUR]),
            emblem = lookOf(this[CUSTOM_EMBLEM])
        )
    }

    private fun lookOf(name: String?): Magic8BallPersonality =
        CustomCharacter.LOOKS.find { it.name == name } ?: Magic8BallPersonality.NEUTRAL

    /** Nothing of a character the user made up stays once another is chosen. */
    private fun MutablePreferences.forgetCustomCharacter() {
        listOf(CUSTOM_ID, CUSTOM_NAME, CUSTOM_DESCRIPTION, CUSTOM_COLOUR, CUSTOM_EMBLEM).forEach { remove(it) }
    }

    override suspend fun setAiAnswersEnabled(enabled: Boolean) {
        save { it[AI_ANSWERS_ENABLED] = enabled }
    }

    override suspend fun setVoiceQuestionsEnabled(enabled: Boolean) {
        save { it[VOICE_QUESTIONS_ENABLED] = enabled }
    }

    override suspend fun setSpokenAnswersEnabled(enabled: Boolean) {
        save { it[SPOKEN_ANSWERS_ENABLED] = enabled }
    }

    override suspend fun setDetailedAnswers(enabled: Boolean) {
        save { it[DETAILED_ANSWERS] = enabled }
    }

    override suspend fun setPartyNames(names: List<String>) {
        save { it[PARTY_NAMES] = names.joinToString("\n") }
    }

    /** A choice that cannot be written is dropped: the app keeps showing the saved one. */
    private suspend fun save(change: (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(change)
        } catch (e: IOException) {
            Log.w(TAG, "Could not save the user settings", e)
        }
    }

    override suspend fun setDailyVerdictEnabled(enabled: Boolean) {
        save { it[DAILY_VERDICT_ENABLED] = enabled }
    }

    private companion object {
        const val TAG = "UserSettingsRepository"
        val LANGUAGE = stringPreferencesKey("language")
        val PERSONALITY = stringPreferencesKey("personality")
        val AI_ANSWERS_ENABLED = booleanPreferencesKey("ai_answers_enabled")
        val VOICE_QUESTIONS_ENABLED = booleanPreferencesKey("voice_questions_enabled")
        val SPOKEN_ANSWERS_ENABLED = booleanPreferencesKey("spoken_answers_enabled")
        val DETAILED_ANSWERS = booleanPreferencesKey("detailed_answers")
        val PARTY_NAMES = stringPreferencesKey("party_names")
        val DAILY_VERDICT_ENABLED = booleanPreferencesKey("daily_verdict_enabled")
        val CUSTOM_ID = stringPreferencesKey("custom_character_id")
        val CUSTOM_NAME = stringPreferencesKey("custom_character_name")
        val CUSTOM_DESCRIPTION = stringPreferencesKey("custom_character_description")
        val CUSTOM_COLOUR = stringPreferencesKey("custom_character_colour")
        val CUSTOM_EMBLEM = stringPreferencesKey("custom_character_emblem")
    }
}
