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
    @StringRes private val spanish: Int,
    @StringRes private val portuguese: Int
) {
    @StringRes
    fun resId(language: AppLanguage): Int = when (language) {
        AppLanguage.ENGLISH -> english
        AppLanguage.SPANISH -> spanish
        AppLanguage.PORTUGUESE -> portuguese
    }
}

/** Every user-visible text of the feature. */
internal object Strings {
    val Title = LocalizedString(
        R.string.title_main,
        R.string.title_main_es,
        R.string.title_main_pt
    )
    val AskHint = LocalizedString(
        R.string.ask_hint,
        R.string.ask_hint_es,
        R.string.ask_hint_pt
    )
    val AskHintAi = LocalizedString(
        R.string.ask_hint_ai,
        R.string.ask_hint_ai_es,
        R.string.ask_hint_ai_pt
    )
    val HoldHint = LocalizedString(
        R.string.hold_hint,
        R.string.hold_hint_es,
        R.string.hold_hint_pt
    )
    val Ask = LocalizedString(
        R.string.ask,
        R.string.ask_es,
        R.string.ask_pt
    )
    val AskByVoice = LocalizedString(
        R.string.ask_by_voice,
        R.string.ask_by_voice_es,
        R.string.ask_by_voice_pt
    )
    val Listening = LocalizedString(
        R.string.listening,
        R.string.listening_es,
        R.string.listening_pt
    )
    val AnswerFallback = LocalizedString(
        R.string.answer_fallback,
        R.string.answer_fallback_es,
        R.string.answer_fallback_pt
    )
    val Report = LocalizedString(
        R.string.report,
        R.string.report_es,
        R.string.report_pt
    )
    val Reported = LocalizedString(
        R.string.reported,
        R.string.reported_es,
        R.string.reported_pt
    )
    val ReportLabel = LocalizedString(
        R.string.report_label,
        R.string.report_label_es,
        R.string.report_label_pt
    )

    val LanguageTitle = LocalizedString(
        R.string.lang_title,
        R.string.lang_title_es,
        R.string.lang_title_pt
    )
    // Each language is listed under its own name, whatever the language in use.
    val LanguageEnglish = LocalizedString(R.string.lang_english, R.string.lang_english, R.string.lang_english)
    val LanguageSpanish = LocalizedString(R.string.lang_spanish, R.string.lang_spanish, R.string.lang_spanish)
    val LanguagePortuguese =
        LocalizedString(R.string.lang_portuguese, R.string.lang_portuguese, R.string.lang_portuguese)

    val PersonalityTitle = LocalizedString(
        R.string.personality_title,
        R.string.personality_title_es,
        R.string.personality_title_pt
    )
    val PersonalitySarcastic =
        LocalizedString(
        R.string.personality_sarcastic,
        R.string.personality_sarcastic_es,
        R.string.personality_sarcastic_pt
    )
    val PersonalityMystical =
        LocalizedString(
        R.string.personality_mystical,
        R.string.personality_mystical_es,
        R.string.personality_mystical_pt
    )

    val PersonalityNeutral =
        LocalizedString(
        R.string.personality_neutral,
        R.string.personality_neutral_es,
        R.string.personality_neutral_pt
    )
    val PersonalityGrandma =
        LocalizedString(
        R.string.personality_grandma,
        R.string.personality_grandma_es,
        R.string.personality_grandma_pt
    )
    val PersonalityPirate =
        LocalizedString(
        R.string.personality_pirate,
        R.string.personality_pirate_es,
        R.string.personality_pirate_pt
    )
    val PersonalityPhilosopher =
        LocalizedString(
        R.string.personality_philosopher,
        R.string.personality_philosopher_es,
        R.string.personality_philosopher_pt
    )
    val PersonalityCoach =
        LocalizedString(
        R.string.personality_coach,
        R.string.personality_coach_es,
        R.string.personality_coach_pt
    )
    val PersonalityLovestruck =
        LocalizedString(
        R.string.personality_lovestruck,
        R.string.personality_lovestruck_es,
        R.string.personality_lovestruck_pt
    )
    val PersonalityDrama =
        LocalizedString(
        R.string.personality_drama,
        R.string.personality_drama_es,
        R.string.personality_drama_pt
    )
    val PersonalityRobot =
        LocalizedString(
        R.string.personality_robot,
        R.string.personality_robot_es,
        R.string.personality_robot_pt
    )
    val PersonalityDetective =
        LocalizedString(
        R.string.personality_detective,
        R.string.personality_detective_es,
        R.string.personality_detective_pt
    )
    val PersonalityConspiracy =
        LocalizedString(
        R.string.personality_conspiracy,
        R.string.personality_conspiracy_es,
        R.string.personality_conspiracy_pt
    )
    val PersonalityCat =
        LocalizedString(
        R.string.personality_cat,
        R.string.personality_cat_es,
        R.string.personality_cat_pt
    )
    val PersonalityZoomer =
        LocalizedString(
        R.string.personality_zoomer,
        R.string.personality_zoomer_es,
        R.string.personality_zoomer_pt
    )
    val PersonalityVillain =
        LocalizedString(
        R.string.personality_villain,
        R.string.personality_villain_es,
        R.string.personality_villain_pt
    )
    val PersonalityPoet =
        LocalizedString(
        R.string.personality_poet,
        R.string.personality_poet_es,
        R.string.personality_poet_pt
    )
    val PersonalityAstro =
        LocalizedString(
        R.string.personality_astro,
        R.string.personality_astro_es,
        R.string.personality_astro_pt
    )
    val PersonalityTherapist =
        LocalizedString(
        R.string.personality_therapist,
        R.string.personality_therapist_es,
        R.string.personality_therapist_pt
    )
    val PersonalityDog =
        LocalizedString(
        R.string.personality_dog,
        R.string.personality_dog_es,
        R.string.personality_dog_pt
    )

    val AiTitle = LocalizedString(
        R.string.ai_title,
        R.string.ai_title_es,
        R.string.ai_title_pt
    )
    val AiOn = LocalizedString(
        R.string.ai_on,
        R.string.ai_on_es,
        R.string.ai_on_pt
    )
    val AiOff = LocalizedString(
        R.string.ai_off,
        R.string.ai_off_es,
        R.string.ai_off_pt
    )
    val AiUnavailable = LocalizedString(
        R.string.ai_unavailable,
        R.string.ai_unavailable_es,
        R.string.ai_unavailable_pt
    )
    val AiClassic = LocalizedString(
        R.string.ai_classic,
        R.string.ai_classic_es,
        R.string.ai_classic_pt
    )
    val AiSpent = LocalizedString(
        R.string.ai_spent,
        R.string.ai_spent_es,
        R.string.ai_spent_pt
    )
}

/** The language chosen in the app, provided by [ChatScreen] to everything it shows. */
internal val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

/** Returns [text] in the language chosen in the app. */
@Composable
@ReadOnlyComposable
internal fun localizedString(text: LocalizedString): String =
    stringResource(text.resId(LocalAppLanguage.current))
