package com.example.magiceightball.core.data.config

import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability

/**
 * Whether answers may come from Gemini, from which model and by which of Google's two services,
 * and how many of them a device may ask for in one day.
 *
 * @property premiumDailyLimit how many a day for whoever has paid for what is sold: more, and
 * still a ceiling on what one device can cost.
 * @property limitedUseTokens whether each request carries a proof of the app made for it alone,
 * which Firebase can then refuse to take twice. Off unless switched on from afar: making one
 * takes Google Play a few seconds, every time, out of the few an answer is waited for, and Google
 * Play makes only so many a day for the whole app. See AppCheck.
 */
data class GeminiSettings(
    val enabled: Boolean,
    val modelId: String,
    val dailyLimit: Int = DEFAULT_DAILY_LIMIT,
    val premiumDailyLimit: Int = DEFAULT_PREMIUM_DAILY_LIMIT,
    val backend: Backend = Backend.DEVELOPER_API,
    val limitedUseTokens: Boolean = false
) {
    val isUsable: Boolean get() = enabled && modelId.isNotBlank()

    /** The day's allowance of whoever asks: never less for having paid, whatever was set from afar. */
    fun dailyLimitFor(premium: Boolean): Int = if (premium) maxOf(premiumDailyLimit, dailyLimit) else dailyLimit

    /**
     * The service Firebase passes a request on to. Which one the project is set up for is
     * chosen in the Firebase console, and the app has to ask for the same one.
     */
    enum class Backend {
        /** The Gemini Developer API. */
        DEVELOPER_API,

        /** The Agent Platform Gemini API of Google Cloud, which was Vertex AI. */
        AGENT_PLATFORM
    }

    companion object {
        /** Plenty for a toy that is asked a few questions at a time, and a ceiling on its cost. */
        const val DEFAULT_DAILY_LIMIT = 20

        /**
         * Twice as many for whoever paid. Not more: it is paid for once, and every answer costs
         * something every day after that.
         */
        const val DEFAULT_PREMIUM_DAILY_LIMIT = 40
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
 * What the developer keeps in Firebase about the characters, to change them without a release:
 * see [RemoteCharacterBook] for what it may hold.
 */
interface RemoteCharacters {
    /**
     * The latest characters file known on this device; blank when there is none. Never waits
     * for the network: it is what the last fetch left on disk.
     */
    fun charactersJson(): String
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
