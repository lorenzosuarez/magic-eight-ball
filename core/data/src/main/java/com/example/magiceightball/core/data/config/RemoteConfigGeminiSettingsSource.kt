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
import javax.inject.Named
import javax.inject.Singleton

/**
 * Reads the Gemini switch and API key from Firebase Remote Config.
 *
 * Remote Config parameters (both default to "off" when absent):
 * - [KEY_ENABLED] (Boolean): turns Gemini answers on or off from the Firebase console.
 * - [KEY_API_KEY] (String): the Gemini API key; when blank, the build-time key is used instead.
 * - [KEY_MODEL] (String, optional): the Gemini model ID; when blank, [GeminiConfig.defaultModelId].
 *   Lets a retired model be replaced from the console without a new release.
 * - [KEY_DAILY_LIMIT] (Number, optional): how many AI answers one device may ask for per day;
 *   when absent or not positive, [GeminiSettings.DEFAULT_DAILY_LIMIT].
 * - [KEY_USE_PAID_KEY] (Boolean, optional) and [KEY_PAID_API_KEY] (String, optional): when the
 *   switch is on and the paid key is not blank, requests use the paid key instead of
 *   [KEY_API_KEY]. Off by default, so the app runs on the free key until the developer says so.
 * - [KEY_ANSWERS] (JSON, optional): answers that replace the bundled ones; see [RemoteAnswers].
 *
 * When the app is built without `google-services.json`, Firebase is not initialised and the
 * build-time key alone decides: Gemini is used only if that key is present.
 *
 * [isAvailable] starts as false and is published by [refresh]: first from the values cached on
 * the device, then again once the fetch has finished.
 */
@Singleton
class RemoteConfigGeminiSettingsSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("BuildGeminiApiKey") private val buildApiKey: String,
    private val geminiConfig: GeminiConfig
) : GeminiSettingsSource, RemoteAnswers {

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
            ?: return GeminiSettings(
                enabled = buildApiKey.isNotBlank(),
                apiKey = buildApiKey,
                modelId = geminiConfig.defaultModelId
            )
        return GeminiSettings(
            enabled = config.getBoolean(KEY_ENABLED),
            apiKey = paidKey(config) ?: config.getString(KEY_API_KEY).trim().ifBlank { buildApiKey },
            modelId = config.getString(KEY_MODEL).trim().ifBlank { geminiConfig.defaultModelId },
            // Absent or not a positive number: the built-in limit.
            dailyLimit = config.getLong(KEY_DAILY_LIMIT).toInt().takeIf { it > 0 }
                ?: GeminiSettings.DEFAULT_DAILY_LIMIT
        )
    }

    /** The paid key, only when the developer has switched to it and it is there to use. */
    private fun paidKey(config: FirebaseRemoteConfig): String? =
        config.getString(KEY_PAID_API_KEY).trim()
            .takeIf { config.getBoolean(KEY_USE_PAID_KEY) && it.isNotBlank() }

    override fun json(): String = remoteConfig?.getString(KEY_ANSWERS).orEmpty()

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
        const val KEY_API_KEY = "gemini_api_key"
        const val KEY_MODEL = "gemini_model"
        const val KEY_DAILY_LIMIT = "gemini_daily_limit"
        const val KEY_USE_PAID_KEY = "gemini_use_paid_key"
        const val KEY_PAID_API_KEY = "gemini_paid_api_key"
        const val KEY_ANSWERS = "answers_json"
        const val MIN_FETCH_INTERVAL_SECONDS = 3600L
        const val FETCH_TIMEOUT_SECONDS = 10L
    }
}
