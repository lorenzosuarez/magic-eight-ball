package com.example.magiceightball.core.data.config

import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability

/** Whether answers may come from Gemini, with which key and which model. */
data class GeminiSettings(val enabled: Boolean, val apiKey: String, val modelId: String) {
    val isUsable: Boolean get() = enabled && apiKey.isNotBlank() && modelId.isNotBlank()

    /** Keeps the key out of logs and crash reports. */
    override fun toString(): String =
        "GeminiSettings(enabled=$enabled, apiKey=${if (apiKey.isBlank()) "<none>" else "<set>"}, modelId=$modelId)"
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
