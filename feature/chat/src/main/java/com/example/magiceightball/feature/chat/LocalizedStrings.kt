package com.example.magiceightball.feature.chat

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.example.magiceightball.core.domain.model.AppLanguage

/**
 * A text of the app in each of its languages.
 *
 * The app follows its own language setting, not the device locale, so its texts cannot be
 * picked by the resource system: each one is declared once in [Strings] and resolved for the
 * language in use.
 */
internal class LocalizedString(
    @StringRes private val english: Int,
    @StringRes private val spanish: Int
) {
    @StringRes
    fun resId(language: AppLanguage): Int = when (language) {
        AppLanguage.ENGLISH -> english
        AppLanguage.SPANISH -> spanish
    }
}

/** Every user-visible text of the feature. */
internal object Strings {
    val Title = LocalizedString(R.string.title_main, R.string.title_main_es)
    val ShakeHint = LocalizedString(R.string.shake_hint, R.string.shake_hint_es)
    val ShakeHintAi = LocalizedString(R.string.shake_hint_ai, R.string.shake_hint_ai_es)
    val HoldHint = LocalizedString(R.string.hold_hint, R.string.hold_hint_es)
    val AskByVoice = LocalizedString(R.string.ask_by_voice, R.string.ask_by_voice_es)
    val Listening = LocalizedString(R.string.listening, R.string.listening_es)
    val AnswerFallback = LocalizedString(R.string.answer_fallback, R.string.answer_fallback_es)

    val LanguageTitle = LocalizedString(R.string.lang_title, R.string.lang_title_es)
    // Each language is listed under its own name, whatever the language in use.
    val LanguageEnglish = LocalizedString(R.string.lang_english, R.string.lang_english)
    val LanguageSpanish = LocalizedString(R.string.lang_spanish, R.string.lang_spanish)

    val PersonalityTitle = LocalizedString(R.string.personality_title, R.string.personality_title_es)
    val PersonalitySarcastic =
        LocalizedString(R.string.personality_sarcastic, R.string.personality_sarcastic_es)
    val PersonalityMystical =
        LocalizedString(R.string.personality_mystical, R.string.personality_mystical_es)

    val PersonalityNeutral =
        LocalizedString(R.string.personality_neutral, R.string.personality_neutral_es)
    val PersonalityGrandma =
        LocalizedString(R.string.personality_grandma, R.string.personality_grandma_es)
    val PersonalityPirate =
        LocalizedString(R.string.personality_pirate, R.string.personality_pirate_es)
    val PersonalityPhilosopher =
        LocalizedString(R.string.personality_philosopher, R.string.personality_philosopher_es)
    val PersonalityCoach =
        LocalizedString(R.string.personality_coach, R.string.personality_coach_es)
    val PersonalityLovestruck =
        LocalizedString(R.string.personality_lovestruck, R.string.personality_lovestruck_es)
    val PersonalityDrama =
        LocalizedString(R.string.personality_drama, R.string.personality_drama_es)
    val PersonalityRobot =
        LocalizedString(R.string.personality_robot, R.string.personality_robot_es)
    val PersonalityDetective =
        LocalizedString(R.string.personality_detective, R.string.personality_detective_es)
    val PersonalityConspiracy =
        LocalizedString(R.string.personality_conspiracy, R.string.personality_conspiracy_es)
    val PersonalityCat =
        LocalizedString(R.string.personality_cat, R.string.personality_cat_es)
    val PersonalityZoomer =
        LocalizedString(R.string.personality_zoomer, R.string.personality_zoomer_es)
    val PersonalityVillain =
        LocalizedString(R.string.personality_villain, R.string.personality_villain_es)
    val PersonalityPoet =
        LocalizedString(R.string.personality_poet, R.string.personality_poet_es)
    val PersonalityAstro =
        LocalizedString(R.string.personality_astro, R.string.personality_astro_es)

    val AiTitle = LocalizedString(R.string.ai_title, R.string.ai_title_es)
    val AiOn = LocalizedString(R.string.ai_on, R.string.ai_on_es)
    val AiOff = LocalizedString(R.string.ai_off, R.string.ai_off_es)
    val AiUnavailable = LocalizedString(R.string.ai_unavailable, R.string.ai_unavailable_es)
}

/** The language chosen in the app, provided by [ChatScreen] to everything it shows. */
internal val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

/** Returns [text] in the language chosen in the app. */
@Composable
@ReadOnlyComposable
internal fun localizedString(text: LocalizedString): String =
    stringResource(text.resId(LocalAppLanguage.current))
