package com.lorenzosuarez.magiceightball.phone.config

import android.content.Context
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What the developer can change about the phone app without a new release, from Firebase Remote
 * Config. Every value has a default here, which is what the app does until a parameter is set,
 * and what it falls back to when one is set to something that makes no sense.
 *
 * | Parameter | Type | Default | What it does |
 * |---|---|---|---|
 * | `ads_min_answers` | Number | 4 | Answers closed before a full-screen ad may show |
 * | `ads_min_interval_seconds` | Number | 180 | Least time between two such ads |
 * | `ai_remark_enabled` | Boolean | true | Whether AI answers come with a second line |
 * | `styles_featured` | String | see [DEFAULT_FEATURED] | Styles in the row of the options, in order, by name, between commas |
 * | `styles_hidden` | String | (none) | Styles taken out of the app, by name, between commas |
 * | `streak_min_days_shown` | Number | 2 | From which day a run of days is mentioned |
 * | `notifications_ask_after_days` | Number | 3 | Best run of days after which notifications are asked for; 0 never asks |
 * | `ball_sway_enabled` | Boolean | true | Whether the ball moves with the phone |
 * | `daily_verdict_enabled` | Boolean | true | Whether the day's verdict is given as a notification |
 * | `daily_verdict_hour` | Number | 10 | At about what hour of the device's day it is given, 0 to 23 |
 *
 * The parameters about the AI service and about ads being on at all are read where they are used:
 * see RemoteConfigGeminiSettingsSource and AdMobAdsController.
 */
data class AppConfig(
    val adsMinAnswers: Int = 4,
    val adsMinIntervalSeconds: Int = 180,
    val aiRemarkEnabled: Boolean = true,
    val featuredStyles: List<Magic8BallPersonality> = DEFAULT_FEATURED,
    val hiddenStyles: Set<Magic8BallPersonality> = emptySet(),
    val streakMinDaysShown: Int = 2,
    val notificationsAskAfterDays: Int = 3,
    val ballSwayEnabled: Boolean = true,
    val dailyVerdictEnabled: Boolean = true,
    val dailyVerdictHour: Int = 10,
    val accountEnabled: Boolean = false
) {
    companion object {
        const val KEY_ADS_MIN_ANSWERS = "ads_min_answers"
        const val KEY_ADS_MIN_INTERVAL_SECONDS = "ads_min_interval_seconds"
        const val KEY_AI_REMARK_ENABLED = "ai_remark_enabled"
        const val KEY_STYLES_FEATURED = "styles_featured"
        const val KEY_STYLES_HIDDEN = "styles_hidden"
        const val KEY_STREAK_MIN_DAYS_SHOWN = "streak_min_days_shown"
        const val KEY_NOTIFICATIONS_ASK_AFTER_DAYS = "notifications_ask_after_days"
        const val KEY_BALL_SWAY_ENABLED = "ball_sway_enabled"
        const val KEY_DAILY_VERDICT_ENABLED = "daily_verdict_enabled"
        const val KEY_DAILY_VERDICT_HOUR = "daily_verdict_hour"
        const val KEY_ACCOUNT_ENABLED = "account_enabled"

        val KEYS = listOf(
            KEY_ADS_MIN_ANSWERS,
            KEY_ADS_MIN_INTERVAL_SECONDS,
            KEY_AI_REMARK_ENABLED,
            KEY_STYLES_FEATURED,
            KEY_STYLES_HIDDEN,
            KEY_STREAK_MIN_DAYS_SHOWN,
            KEY_NOTIFICATIONS_ASK_AFTER_DAYS,
            KEY_BALL_SWAY_ENABLED,
            KEY_DAILY_VERDICT_ENABLED,
            KEY_DAILY_VERDICT_HOUR,
            KEY_ACCOUNT_ENABLED
        )

        /** One of each kind of character, the liveliest first, with the plain one always at hand. */
        val DEFAULT_FEATURED = listOf(
            Magic8BallPersonality.SARCASTIC,
            Magic8BallPersonality.NEUTRAL,
            Magic8BallPersonality.LOVESTRUCK,
            Magic8BallPersonality.CAT,
            Magic8BallPersonality.DRAMA
        )

        /**
         * The configuration in [values], as Remote Config hands them over: text, with nothing for
         * a parameter that is not set. Anything missing, misspelt or out of bounds is the default,
         * so a slip in the console can dull a setting but never break the app.
         */
        fun from(values: (String) -> String?): AppConfig {
            val defaults = AppConfig()
            fun text(key: String): String? = values(key)?.trim()?.takeIf { it.isNotEmpty() }
            fun number(key: String, default: Int, range: IntRange): Int =
                text(key)?.toIntOrNull()?.takeIf { it in range } ?: default
            fun flag(key: String, default: Boolean): Boolean = when (text(key)?.lowercase()) {
                "true" -> true
                "false" -> false
                else -> default
            }
            fun styles(key: String): List<Magic8BallPersonality> = text(key).orEmpty()
                .split(',')
                .mapNotNull { name -> Magic8BallPersonality.entries.find { it.name.equals(name.trim(), ignoreCase = true) } }
                // The user's own are theirs to show and hide: not a style to be named from afar.
                .filter { it != Magic8BallPersonality.CUSTOM }
                .distinct()

            // The plain style and the default one are never taken out: there must be somewhere to land.
            val hidden = styles(KEY_STYLES_HIDDEN).toSet() - Magic8BallPersonality.NEUTRAL - Magic8BallPersonality.SARCASTIC
            return AppConfig(
                adsMinAnswers = number(KEY_ADS_MIN_ANSWERS, defaults.adsMinAnswers, 1..100),
                adsMinIntervalSeconds = number(KEY_ADS_MIN_INTERVAL_SECONDS, defaults.adsMinIntervalSeconds, 30..86_400),
                aiRemarkEnabled = flag(KEY_AI_REMARK_ENABLED, defaults.aiRemarkEnabled),
                featuredStyles = (styles(KEY_STYLES_FEATURED) - hidden)
                    .ifEmpty { defaults.featuredStyles - hidden }
                    .take(MAX_FEATURED),
                hiddenStyles = hidden,
                streakMinDaysShown = number(KEY_STREAK_MIN_DAYS_SHOWN, defaults.streakMinDaysShown, 1..365),
                notificationsAskAfterDays = number(KEY_NOTIFICATIONS_ASK_AFTER_DAYS, defaults.notificationsAskAfterDays, 0..365),
                ballSwayEnabled = flag(KEY_BALL_SWAY_ENABLED, defaults.ballSwayEnabled),
                dailyVerdictEnabled = flag(KEY_DAILY_VERDICT_ENABLED, defaults.dailyVerdictEnabled),
                dailyVerdictHour = number(KEY_DAILY_VERDICT_HOUR, defaults.dailyVerdictHour, 0..23),
                accountEnabled = flag(KEY_ACCOUNT_ENABLED, defaults.accountEnabled)
            )
        }

        private const val MAX_FEATURED = 8
    }
}

/** Where the [AppConfig] in force comes from. It changes while the app runs. */
interface AppConfigSource {
    val config: StateFlow<AppConfig>

    /** The defaults and nothing else: for tests and previews. */
    object Defaults : AppConfigSource {
        override val config: StateFlow<AppConfig> = MutableStateFlow(AppConfig())
    }
}

/**
 * [AppConfigSource] over Firebase Remote Config.
 *
 * It publishes what is cached on the device as soon as that is loaded, and again whenever the
 * console's values change: Remote Config tells the app while it is open, so a change in the
 * console reaches a running app within moments. Without Firebase in the build, the defaults stand.
 */
@Singleton
class RemoteAppConfigSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AppConfigSource {

    private val state = MutableStateFlow(AppConfig())
    override val config: StateFlow<AppConfig> = state.asStateFlow()

    init {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            val remote = FirebaseRemoteConfig.getInstance()
            remote.ensureInitialized().addOnCompleteListener { publish(remote) }
            remote.addOnConfigUpdateListener(
                object : ConfigUpdateListener {
                    override fun onUpdate(update: ConfigUpdate) {
                        remote.activate().addOnCompleteListener { publish(remote) }
                    }

                    // The values the app already has still stand.
                    override fun onError(error: FirebaseRemoteConfigException) = Unit
                }
            )
        }
    }

    private fun publish(remote: FirebaseRemoteConfig) {
        state.value = AppConfig.from { key -> remote.getString(key) }
    }
}
