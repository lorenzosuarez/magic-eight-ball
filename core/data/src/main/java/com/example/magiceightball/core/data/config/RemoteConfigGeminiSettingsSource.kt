package com.example.magiceightball.core.data.config

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Reads the Gemini switch and API key from Firebase Remote Config.
 *
 * Remote Config parameters (both default to "off" when absent):
 * - [KEY_ENABLED] (Boolean): turns Gemini answers on or off from the Firebase console.
 * - [KEY_API_KEY] (String): the Gemini API key; when blank, the build-time key is used instead.
 *
 * When the app is built without `google-services.json`, Firebase is not initialised and the
 * build-time key alone decides: Gemini is used only if that key is present.
 */
@Singleton
class RemoteConfigGeminiSettingsSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("BuildGeminiApiKey") private val buildApiKey: String
) : GeminiSettingsSource {

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
            }
        }
    }

    override fun current(): GeminiSettings {
        val config = remoteConfig
            ?: return GeminiSettings(enabled = buildApiKey.isNotBlank(), apiKey = buildApiKey)
        return GeminiSettings(
            enabled = config.getBoolean(KEY_ENABLED),
            apiKey = config.getString(KEY_API_KEY).trim().ifBlank { buildApiKey }
        )
    }

    override fun refresh() {
        // Throttled by the SDK to one network fetch per MIN_FETCH_INTERVAL_SECONDS.
        remoteConfig?.fetchAndActivate()
    }

    private companion object {
        const val KEY_ENABLED = "gemini_enabled"
        const val KEY_API_KEY = "gemini_api_key"
        const val MIN_FETCH_INTERVAL_SECONDS = 3600L
        const val FETCH_TIMEOUT_SECONDS = 10L
    }
}
