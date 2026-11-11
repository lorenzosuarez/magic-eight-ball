package com.example.magiceightball.core.data.config

import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability

/**
 * Whether answers may come from Gemini, with which key and which model, and how many of them a
 * device may ask for in one day.
 */
data class GeminiSettings(
    val enabled: Boolean,
    val apiKey: String,
    val modelId: String,
    val dailyLimit: Int = DEFAULT_DAILY_LIMIT
) {
    val isUsable: Boolean get() = enabled && apiKey.isNotBlank() && modelId.isNotBlank()

    /** Keeps the key out of logs and crash reports. */
    override fun toString(): String =
        "GeminiSettings(enabled=$enabled, apiKey=${if (apiKey.isBlank()) "<none>" else "<set>"}, " +
            "modelId=$modelId, dailyLimit=$dailyLimit)"

    companion object {
        /** Generous for a toy that is asked a few questions at a time, and a ceiling on its cost. */
        const val DEFAULT_DAILY_LIMIT = 40
    }
}

/**
 * The answers the developer keeps in Firebase to replace the bundled ones without a release.
 */
interface RemoteAnswers {
    /**
     * The latest answers file known on this device, in the format of the bundled one; blank
     * when there is none. Never waits for the network: it is what the last fetch left on disk.
     */
    fun json(): String
}

/**
 * Source of the [GeminiSettings]. Its [isAvailable] follows [GeminiSettings.isUsable] and is
 * brought up to date by [refresh].
 */
interface GeminiSettingsSource : AiAnswersAvailability {
    /** Returns the latest known settings without waiting for the network. */
    fun current(): GeminiSettings

    /** Asks for fresh settings in the background; safe to call often. */
    fun refresh()
}
