package com.example.magiceightball.core.data.config

import android.content.Context
import com.example.magiceightball.core.common.GeminiConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the Gemini switch and what goes with it from Firebase Remote Config.
 *
 * Remote Config parameters:
 * - [KEY_ENABLED] (Boolean): turns Gemini answers on or off from the Firebase console. Off when
 *   absent.
 * - [KEY_MODEL] (String, optional): the Gemini model ID; when blank, [GeminiConfig.defaultModelId].
 *   Lets a retired model be replaced from the console without a new release.
 * - [KEY_DAILY_LIMIT] (Number, optional): how many AI answers one device may ask for per day;
 *   when absent or not positive, [GeminiSettings.DEFAULT_DAILY_LIMIT].
 * - [KEY_BACKEND] (String, optional): `agent_platform` or `vertex` when the project is set up for
 *   the Agent Platform Gemini API; anything else, or absent, is the Gemini Developer API.
 * - [KEY_LIMITED_USE_TOKENS] (Boolean, optional): `true` has each request carry a proof of the
 *   app made for it alone, instead of the one that is kept for a while. Off when absent: see
 *   [GeminiSettings.limitedUseTokens] for what it costs.
 * - [KEY_ANSWERS] (JSON, optional): answers that replace the bundled ones; see [RemoteAnswers].
 * - [KEY_CHARACTERS] (JSON, optional): changes to who the characters are and how an answer is
 *   asked for; see [RemoteCharacterBook].
 *
 * No key for the AI service is among them: requests go through Firebase AI Logic, which holds it.
 *
 * When the app is built without `google-services.json`, Firebase is not initialised, there is no
 * project to ask, and Gemini stays off.
 *
 * [isAvailable] starts as false and is published by [refresh]: first from the values cached on
 * the device, then again once the fetch has finished.
 */
@Singleton
class RemoteConfigGeminiSettingsSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiConfig: GeminiConfig
) : GeminiSettingsSource, RemoteAnswers, RemoteCharacters {

    private val availability = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = availability.asStateFlow()

    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) {
            null
        } else {
            FirebaseRemoteConfig.getInstance().also { config ->
                config.setConfigSettingsAsync(
                    FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(MIN_FETCH_INTERVAL_SECONDS)
                        .setFetchTimeoutInSeconds(FETCH_TIMEOUT_SECONDS)
                        .build()
                )
                config.ensureInitialized().addOnCompleteListener { publishAvailability() }
            }
        }
    }

    override fun current(): GeminiSettings {
        val config = remoteConfig
            ?: return GeminiSettings(enabled = false, modelId = geminiConfig.defaultModelId)
        return GeminiSettings(
            enabled = config.getBoolean(KEY_ENABLED),
            modelId = config.getString(KEY_MODEL).trim().ifBlank { geminiConfig.defaultModelId },
            // Absent or not a positive number: the built-in limit.
            dailyLimit = config.getLong(KEY_DAILY_LIMIT).toInt().takeIf { it > 0 }
                ?: GeminiSettings.DEFAULT_DAILY_LIMIT,
            backend = when (config.getString(KEY_BACKEND).trim().lowercase()) {
                "agent_platform", "vertex" -> GeminiSettings.Backend.AGENT_PLATFORM
                else -> GeminiSettings.Backend.DEVELOPER_API
            },
            limitedUseTokens = config.getBoolean(KEY_LIMITED_USE_TOKENS)
        )
    }

    override fun json(): String = remoteConfig?.getString(KEY_ANSWERS).orEmpty()

    override fun charactersJson(): String = remoteConfig?.getString(KEY_CHARACTERS).orEmpty()

    override fun refresh() {
        val config = remoteConfig
        if (config == null) {
            publishAvailability()
            return
        }
        // Throttled by the SDK to one network fetch per MIN_FETCH_INTERVAL_SECONDS. Whether the
        // fetch succeeds or not, the values are only read once they are loaded from disk.
        config.fetchAndActivate()
            .continueWithTask { config.ensureInitialized() }
            .addOnCompleteListener { publishAvailability() }
    }

    private fun publishAvailability() {
        availability.value = current().isUsable
    }

    private companion object {
        const val KEY_ENABLED = "gemini_enabled"
        const val KEY_MODEL = "gemini_model"
        const val KEY_DAILY_LIMIT = "gemini_daily_limit"
        const val KEY_BACKEND = "gemini_backend"
        const val KEY_LIMITED_USE_TOKENS = "gemini_limited_use_tokens"
        const val KEY_ANSWERS = "answers_json"
        const val KEY_CHARACTERS = "characters_json"
        const val MIN_FETCH_INTERVAL_SECONDS = 3600L
        const val FETCH_TIMEOUT_SECONDS = 10L
    }
}
