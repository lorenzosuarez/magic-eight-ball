package com.lorenzosuarez.magiceightball.phone.config

import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {

    private fun from(vararg values: Pair<String, String>) = AppConfig.from(values.toMap()::get)

    @Test
    fun `with nothing set, the defaults stand`() {
        assertEquals(AppConfig(), from())
        assertEquals(AppConfig(), AppConfig.from { "" })
    }

    @Test
    fun `what is set is taken`() {
        val config = from(
            AppConfig.KEY_ADS_MIN_ANSWERS to "6",
            AppConfig.KEY_ADS_MIN_INTERVAL_SECONDS to "300",
            AppConfig.KEY_AI_REMARK_ENABLED to "false",
            AppConfig.KEY_STYLES_FEATURED to "pirate, DOG ,cat",
            AppConfig.KEY_STYLES_HIDDEN to "conspiracy",
            AppConfig.KEY_STREAK_MIN_DAYS_SHOWN to "3",
            AppConfig.KEY_NOTIFICATIONS_ASK_AFTER_DAYS to "0",
            AppConfig.KEY_BALL_SWAY_ENABLED to "FALSE"
        )

        assertEquals(
            AppConfig(
                adsMinAnswers = 6,
                adsMinIntervalSeconds = 300,
                aiRemarkEnabled = false,
                featuredStyles = listOf(Magic8BallPersonality.PIRATE, Magic8BallPersonality.DOG, Magic8BallPersonality.CAT),
                hiddenStyles = setOf(Magic8BallPersonality.CONSPIRACY),
                streakMinDaysShown = 3,
                notificationsAskAfterDays = 0,
                ballSwayEnabled = false
            ),
            config
        )
    }

    @Test
    fun `a slip in the console falls back to the default, value by value`() {
        val config = from(
            AppConfig.KEY_ADS_MIN_ANSWERS to "0",
            AppConfig.KEY_ADS_MIN_INTERVAL_SECONDS to "five",
            AppConfig.KEY_AI_REMARK_ENABLED to "yes please",
            AppConfig.KEY_STYLES_FEATURED to "unicorn, , ",
            AppConfig.KEY_STREAK_MIN_DAYS_SHOWN to "-4",
            AppConfig.KEY_NOTIFICATIONS_ASK_AFTER_DAYS to "9999"
        )

        assertEquals(AppConfig(), config)
    }

    @Test
    fun `the plain style and the default one cannot be taken out, and a hidden style is never featured`() {
        val config = from(
            AppConfig.KEY_STYLES_HIDDEN to "neutral, sarcastic, cat",
            AppConfig.KEY_STYLES_FEATURED to "cat, pirate"
        )

        assertEquals(setOf(Magic8BallPersonality.CAT), config.hiddenStyles)
        assertEquals(listOf(Magic8BallPersonality.PIRATE), config.featuredStyles)
        assertTrue(Magic8BallPersonality.CAT !in from(AppConfig.KEY_STYLES_HIDDEN to "cat").featuredStyles)
    }

    @Test
    fun `every parameter has a name the console accepts, and none twice`() {
        assertEquals(AppConfig.KEYS.size, AppConfig.KEYS.distinct().size)
        AppConfig.KEYS.forEach { assertTrue(it, Regex("[a-z][a-z0-9_]*").matches(it)) }
    }
}
