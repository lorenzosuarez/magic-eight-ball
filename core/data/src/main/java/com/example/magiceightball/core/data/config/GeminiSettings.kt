package com.example.magiceightball.core.data.config

/** Whether answers may come from Gemini, and with which key. */
data class GeminiSettings(val enabled: Boolean, val apiKey: String) {
    val isUsable: Boolean get() = enabled && apiKey.isNotBlank()
}

interface GeminiSettingsSource {
    /** Returns the latest known settings without waiting for the network. */
    fun current(): GeminiSettings

    /** Asks for fresh settings in the background; safe to call often. */
    fun refresh()
}
