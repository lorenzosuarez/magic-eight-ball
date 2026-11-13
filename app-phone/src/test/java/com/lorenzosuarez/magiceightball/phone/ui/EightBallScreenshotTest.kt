package com.lorenzosuarez.magiceightball.phone.ui

import com.lorenzosuarez.magiceightball.phone.config.AppConfig
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.Profile
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.Notice
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.VoiceStatus
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

/**
 * Renders the real screen on the JVM (no device or emulator) with fixed state, at Google Play's
 * screenshot sizes. `./gradlew :app-phone:recordRoborazziDebug` writes the PNGs to
 * `app-phone/build/outputs/roborazzi/`; a plain unit-test run only checks that the screen renders.
 *
 * A plain [Application] stands in for the Hilt one, so nothing is injected and nothing touches the network.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], application = Application::class, qualifiers = PHONE)
class EightBallScreenshotTest {

    @Test
    fun phone_idle_light() = capture("phone_1_ask_light", EightBallUiState(history = HISTORY))

    @Test
    @Config(qualifiers = "+night")
    fun phone_idle_spanish_no_history_dark() = capture(
        name = "phone_19_ask_spanish_no_history_dark",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH))
    )

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_idle_history_spanish_large_font() = capture(
        name = "phone_20_small_phone_history_spanish_large_font",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH), history = HISTORY),
        fontScale = 2f,
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_history() = capture(
        name = "phone_21_history",
        state = EightBallUiState(history = HISTORY),
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_history_empty_spanish() = capture(
        name = "phone_22_history_empty_spanish",
        state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH)),
        initialPage = HISTORY_PAGE
    )

    @Test
    fun phone_listening_light() = capture(
        name = "phone_2_listening_light",
        state = EightBallUiState(phase = BallPhase.Listening("Should I order pizza")),
        voiceLevel = 0.7f
    )

    @Test
    fun phone_answer_light() = capture("phone_3_answer_light", ANSWERED)

    @Test
    @Config(qualifiers = "+night")
    fun phone_party_spanish() = capture(
        name = "phone_25_party_spanish",
        state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH, partyNames = listOf("Ana", "Beto", "Caro")),
            turn = 1
        )
    )

    @Test
    fun share_card() = captureRoboImage(filePath = path("phone_24_share_card")) {
        ShareCard(
            question = "Should I order pizza tonight?",
            answer = "Yes. Screenshot this.",
            layer = rememberGraphicsLayer(),
            visible = true,
            styleLabel = "Sarcastic",
                personality = Magic8BallPersonality.SARCASTIC
        )
    }

    /** The longest it gets: a question on two lines, a full answer and a remark. */
    @Test
    fun share_card_full_spanish() = captureRoboImage(filePath = path("phone_35_share_card_full_spanish")) {
        ProvideAppLanguage(AppLanguage.SPANISH) {
            ShareCard(
                question = "¿Me va a escribir esta noche o ya me olvido de una vez por todas del asunto?",
                answer = "Mercurio retrógrado dice no.",
                layer = rememberGraphicsLayer(),
                    visible = true,
                remark = "Y tu ascendente opina que borres el número antes del viernes.",
                styleLabel = "Horóscopo",
                personality = Magic8BallPersonality.ASTRO
            )
        }
    }

    /** Every style in its colour, wearing what it wears. */
    @Test
    fun share_card_every_style() = CustomCharacter.LOOKS.forEach { style ->
        captureRoboImage(filePath = path("phone_37_share_card_${style.name.lowercase()}")) {
            ShareCard(
                question = "Will they text me tonight?",
                answer = "Not a chance.",
                layer = rememberGraphicsLayer(),
                visible = true,
                styleLabel = style.name.lowercase().replaceFirstChar(Char::uppercase),
                personality = style
            )
        }
    }

    /** One long word is made smaller, never cut in two. */
    @Test
    fun share_card_long_word() = captureRoboImage(filePath = path("phone_38_share_card_long_word")) {
        ShareCard(
            question = "",
            answer = "Necessariamente. Inquestionavelmente.",
            layer = rememberGraphicsLayer(),
            visible = true,
            styleLabel = "Fil\u00f3sofo",
            personality = Magic8BallPersonality.PHILOSOPHER
        )
    }

    /** The barest: a tap, a bundled answer. */
    @Test
    fun share_card_bare() = captureRoboImage(filePath = path("phone_36_share_card_bare")) {
        ProvideAppLanguage(AppLanguage.SPANISH) {
            ShareCard(
                question = "",
                answer = "Obvio que sí.",
                layer = rememberGraphicsLayer(),
                visible = true,
                styleLabel = "Amor ciego",
                personality = Magic8BallPersonality.LOVESTRUCK
            )
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun phone_idle_dark() = capture("phone_4_ask_dark", EightBallUiState())

    @Test
    @Config(qualifiers = "+night")
    fun phone_listening_dark() = capture(
        name = "phone_5_listening_dark",
        state = EightBallUiState(phase = BallPhase.Listening("Will I finish my book")),
        voiceLevel = 0.5f
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_answer_dark() = capture(
        name = "phone_6_answer_dark",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "Will I finish my book this month?",
                answer = AnswerText.Plain("A definitive maybe.")
            )
        )
    )

    @Test
    fun phone_ai_answer_spanish() = capture(
        name = "phone_23_ai_answer_spanish",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "¿Me conviene pedir pizza hoy?",
                answer = AnswerText.Plain("Tu estómago ya decidió.", fromAi = true)
            ),
            settings = UserSettings(language = AppLanguage.SPANISH),
            aiAnswersAvailable = true
        )
    )

    @Test
    fun phone_ai_answer_rated_bad() = capture(
        name = "phone_24_ai_answer_rated_bad",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "¿Me conviene pedir pizza hoy?",
                answer = AnswerText.Plain("Tu estómago ya decidió.", fromAi = true),
                style = Magic8BallPersonality.DETECTIVE,
                liked = false
            ),
            settings = UserSettings(language = AppLanguage.SPANISH),
            aiAnswersAvailable = true
        )
    )

    @Test
    fun phone_answer_rated_good() = capture(
        name = "phone_25_answer_rated_good",
        state = ANSWERED.copy(phase = (ANSWERED.phase as BallPhase.Answered).copy(liked = true))
    )

    @Test
    fun phone_settings_light() = captureWithSettings("phone_7_settings_light", ANSWERED)

    @Test
    @Config(qualifiers = "+night")
    fun phone_settings_dark() = captureWithSettings(
        name = "phone_8_settings_dark",
        state = EightBallUiState(
            settings = UserSettings(
                language = AppLanguage.SPANISH,
                personality = Magic8BallPersonality.MYSTICAL
            ),
            aiAnswersAvailable = true,
            voice = VoiceStatus.PermissionDenied
        )
    )

    @Test
    fun phone_spanish_tap_only() = capture(
        name = "phone_9_spanish_tap_only",
        state = EightBallUiState(
            phase = BallPhase.Answered(id = 0, question = "", answer = AnswerText.Plain("Ni en tus sueños.")),
            settings = UserSettings(language = AppLanguage.SPANISH, voiceQuestionsEnabled = false),
            voice = VoiceStatus.Off
        )
    )

    @Test
    fun phone_long_answer_large_font() = capture(
        name = "phone_10_long_answer_large_font",
        state = EightBallUiState(
            phase = BallPhase.Answered(
                id = 0,
                question = "Is this really going to be the year that everything finally changes for me?",
                answer = AnswerText.Plain("Absolutely not, according to my sources.")
            )
        ),
        fontScale = 2f
    )

    @Test
    fun phone_longest_bundled_answer() = capture(
        name = "phone_11_longest_bundled_answer",
        state = answered("Odds are good. Suspiciously.")
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_longest_bundled_word_spanish() = capture(
        name = "phone_12_longest_bundled_word_spanish",
        state = answered("Sí, sorprendentemente.", AppLanguage.SPANISH)
    )

    @Test
    fun phone_six_word_ai_answer_spanish() = capture(
        name = "phone_13_six_word_ai_answer_spanish",
        state = answered("Definitivamente no, según mis fuentes confiables.", AppLanguage.SPANISH)
    )

    @Test
    fun phone_microphone_denied_notice() = capture(
        name = "phone_14_microphone_denied_notice",
        state = EightBallUiState(voice = VoiceStatus.PermissionDenied, notice = Notice.MicrophoneDenied)
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_voice_unsupported_notice_spanish() = capture(
        name = "phone_15_voice_unsupported_notice_spanish",
        state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            voice = VoiceStatus.Unsupported,
            notice = Notice.VoiceUnsupported
        )
    )

    /** The day's AI answers are used up and there is an ad to watch for more: the offer, in its place. */
    @Test
    fun phone_settings_more_ai_answers_spanish() {
        val state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            aiAnswersAvailable = true,
            aiAllowanceSpent = true
        )
        captureRoboImage(filePath = path("phone_31_settings_more_ai_answers_spanish")) {
            MagicEightBallPhoneTheme(dark = true) {
                ProvideAppLanguage(state.settings.language) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        SettingsContent(
                            state = state,
                            onLanguageSelected = {},
                            onPersonalitySelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {},
                            onGetMoreAiAnswers = {}
                        )
                    }
                }
            }
        }
    }

    /** The sheet's content on its own: under a font scale the sheet's window is not rendered on the JVM. */
    @Test
    fun phone_settings_spanish_large_font() {
        val state = EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH, aiAnswersEnabled = false),
            aiAnswersAvailable = true
        )
        captureRoboImage(filePath = path("phone_16_settings_spanish_large_font")) {
            WithFontScale(2f) {
                MagicEightBallPhoneTheme {
                    ProvideAppLanguage(state.settings.language) {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            SettingsContent(
                                state = state,
                                onLanguageSelected = {},
                                onPersonalitySelected = {},
                                onAiAnswersChange = {},
                                onVoiceQuestionsChange = {},
                                onAllowMicrophone = {},
                                onShareApp = {},
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_answer_large_font() = capture(
        name = "phone_17_small_phone_large_font",
        state = answered("Odds are good. Suspiciously."),
        fontScale = 2f
    )

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_answer_light() = capture("tablet_1_answer_light", ANSWERED)

    @Test
    @Config(qualifiers = TABLET_LANDSCAPE)
    fun tablet_landscape_idle_dark() = capture("tablet_2_ask_landscape_dark", EightBallUiState())

    /** A style from the far end of the row: the row opens with it in the middle, whole. */
    @Test
    fun phone_settings_chosen_style_in_view() = captureWithSettings(
        "phone_23_settings_chosen_style_in_view",
        EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH, personality = Magic8BallPersonality.CAT))
    )

    @Test
    fun phone_answer_with_remark_spanish() = capture(
        "phone_26_answer_with_remark_spanish",
        EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH, personality = Magic8BallPersonality.PIRATE),
            aiAnswersAvailable = true,
            phase = BallPhase.Answered(
                id = 0,
                question = "¿Me va a escribir?",
                answer = AnswerText.Plain(
                    "No, mapa roto.",
                    fromAi = true,
                    remark = "Diecinueve islas hundidas guarden ese secreto maldito."
                ),
                style = Magic8BallPersonality.PIRATE
            )
        )
    )

    /** The few sentences of an answer that says more: under the ball, and on the card that is shared. */
    @Test
    fun phone_answer_with_reading_spanish() = capture(
        "phone_40_answer_with_reading_spanish",
        EightBallUiState(
            settings = UserSettings(
                language = AppLanguage.SPANISH,
                personality = Magic8BallPersonality.GRANDMA,
                detailedAnswers = true
            ),
            aiAnswersAvailable = true,
            phase = BallPhase.Answered(
                id = 0,
                question = "¿Le escribo esta noche?",
                answer = AnswerText.Plain("No, y abrígate.", fromAi = true, remark = LONG_READING),
                style = Magic8BallPersonality.GRANDMA
            )
        )
    )

    @Test
    fun share_card_reading_spanish() = captureRoboImage(filePath = path("phone_41_share_card_reading_spanish")) {
        ProvideAppLanguage(AppLanguage.SPANISH) {
            ShareCard(
                question = "¿Le escribo esta noche?",
                answer = "No, y abrígate.",
                layer = rememberGraphicsLayer(),
                visible = true,
                remark = LONG_READING,
                styleLabel = "Abuela",
                personality = Magic8BallPersonality.GRANDMA
            )
        }
    }

    /** As much as the page ever holds: a long question, an answer too long for the die, a reading and a signature. */
    private fun crowded() = EightBallUiState(
        settings = UserSettings(
            language = AppLanguage.SPANISH,
            personality = Magic8BallPersonality.CONSPIRACY,
            detailedAnswers = true
        ),
        aiAnswersAvailable = true,
        phase = BallPhase.Answered(
            id = 0,
            question = "¿De verdad este va a ser el año en que por fin todo cambie para mí y para los míos?",
            answer = AnswerText.Plain(
                "Definitivamente no, según mis fuentes confidenciales.",
                fromAi = true,
                remark = LONG_READING
            ),
            style = Magic8BallPersonality.CONSPIRACY
        )
    )

    @Test
    fun phone_crowded_answer_spanish() = capture("phone_42_crowded_answer_spanish", crowded())

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_crowded_answer_spanish() = capture("phone_43_small_phone_crowded_answer_spanish", crowded())

    @Test
    fun phone_crowded_answer_large_font() =
        capture("phone_44_crowded_answer_large_font", crowded(), fontScale = 1.5f)

    /** The sheet where characters can be made: the way to make one is at its top, on the right. */
    @Test
    fun phone_characters_with_create_spanish() =
        captureStyles("phone_46_characters_with_create_spanish", AppLanguage.SPANISH, dark = true, withCreate = true)

    /** The account on the page of options: nobody signed in, and what signing in is for. */
    @Test
    fun phone_account_signed_out_spanish() = captureWithSettings(
        "phone_47_account_signed_out_spanish",
        accountState(AccountState.SignedOut)
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_account_signed_out_dark() = captureWithSettings(
        "phone_48_account_signed_out_dark",
        accountState(AccountState.SignedOut)
    )

    /** Someone signed in, with a name and an address as long as they come. */
    @Test
    fun phone_account_signed_in_spanish() = captureWithSettings(
        "phone_49_account_signed_in_spanish",
        accountState(LONG_ACCOUNT)
    )

    @Test
    @Config(qualifiers = "+night")
    fun phone_account_signed_in_dark() = captureWithSettings(
        "phone_50_account_signed_in_dark",
        accountState(AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com"))
    )

    /** The worst case: a small phone, a large font, and nobody signed in. */
    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_account_large_font() =
        captureAccountOptions("phone_51_small_phone_account_large_font", accountState(AccountState.SignedOut), 1.5f)

    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_account_signed_in_large_font() =
        captureAccountOptions("phone_52_small_phone_account_signed_in_large_font", accountState(LONG_ACCOUNT), 1.5f)

    /** The button's longest label, which is the Portuguese one. */
    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_account_portuguese_large_font() = captureAccountOptions(
        "phone_54_small_phone_account_portuguese_large_font",
        accountState(AccountState.SignedOut, AppLanguage.PORTUGUESE),
        1.5f
    )

    /** The largest type the system offers: the button's label takes a second line. */
    @Test
    @Config(qualifiers = SMALL_PHONE)
    fun small_phone_account_largest_font() =
        captureAccountOptions("phone_55_small_phone_account_largest_font", accountState(AccountState.SignedOut), 2f)

    /** The account, opened: a name and an address as long as they come, in full. */
    @Test
    fun phone_account_sheet_spanish() =
        captureAccountSheet("phone_56_account_sheet_spanish", LONG_ACCOUNT, dark = false)

    /** A deletion that failed, said under the row that was touched. */
    @Test
    @Config(qualifiers = "+night")
    fun phone_account_sheet_delete_failed_dark() = captureAccountSheet(
        "phone_57_account_sheet_delete_failed_dark",
        AccountState.SignedIn("1", "Lorena Suárez", "lorena@example.com"),
        dark = true,
        failed = true
    )

    /** On a window tall enough to show the line that says what failed, which a small phone scrolls to. */
    @Test
    @Config(qualifiers = SMALL_PHONE_TALL)
    fun small_phone_account_sheet_large_font() = captureAccountSheet(
        "phone_58_small_phone_account_sheet_large_font",
        LONG_ACCOUNT,
        dark = false,
        failed = true,
        fontScale = 1.5f
    )

    /** Asked of whoever has just signed in: who they are, and what is sent of it. */
    @Test
    fun phone_profile_new_spanish() = captureProfile(
        "phone_59_profile_new_spanish",
        ProfileDraft("Lorena"),
        AppLanguage.SPANISH,
        dark = false
    )

    /** Filled in, in the dark theme. */
    @Test
    @Config(qualifiers = "+night")
    fun phone_profile_filled_dark() = captureProfile(
        "phone_60_profile_filled_dark",
        ProfileDraft("Lorena", PROFILE.birthDate),
        AppLanguage.SPANISH,
        dark = true
    )

    /** Corrected later, from the account: the same sheet, with what it holds. */
    @Test
    fun phone_profile_edit_spanish() = captureProfile(
        "phone_61_profile_edit_spanish",
        ProfileDraft("Lorena", PROFILE.birthDate),
        AppLanguage.SPANISH,
        dark = false,
        firstTime = false
    )

    /**
     * The worst case, on a window tall enough to show all of what scrolls: a small phone's
     * width, a large font, the longest words, a date that does not exist.
     */
    @Test
    @Config(qualifiers = SMALL_PHONE_TALL)
    fun small_phone_profile_portuguese_large_font() = captureProfile(
        "phone_62_small_phone_profile_portuguese_large_font",
        ProfileDraft("Maria Fernanda").apply {
            typeDay("31")
            typeMonth("2")
            typeYear("1990")
        },
        AppLanguage.PORTUGUESE,
        dark = false,
        fontScale = 1.5f
    )

    /** English, whose "Month" is the longest name a date field has. */
    @Test
    @Config(qualifiers = SMALL_PHONE_TALL)
    fun small_phone_profile_english_large_font() = captureProfile(
        "phone_64_small_phone_profile_english_large_font",
        ProfileDraft("Lorena"),
        AppLanguage.ENGLISH,
        dark = false,
        fontScale = 1.5f
    )

    @Test
    @Config(qualifiers = SMALL_PHONE_TALL)
    fun small_phone_profile_edit_failed_large_font() = captureProfile(
        "phone_63_small_phone_profile_edit_failed_large_font",
        ProfileDraft("Lorena", PROFILE.birthDate),
        AppLanguage.SPANISH,
        dark = false,
        firstTime = false,
        failed = true,
        fontScale = 1.5f
    )

    private fun captureProfile(
        name: String,
        draft: ProfileDraft,
        language: AppLanguage,
        dark: Boolean,
        firstTime: Boolean = true,
        failed: Boolean = false,
        fontScale: Float = 1f
    ) = captureRoboImage(filePath = path(name)) {
        WithFontScale(fontScale) {
            MagicEightBallPhoneTheme(dark = dark) {
                ProvideAppLanguage(language) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        ProfileContent(
                            draft = draft,
                            firstTime = firstTime,
                            today = LocalDate.of(2026, 11, 13),
                            busy = false,
                            trouble = if (failed) stringResource(R.string.profile_save_failed) else null,
                            onSave = { _, _ -> },
                            onSecondary = {},
                            onOpenPrivacyPolicy = {},
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }
    }

    /** The options page on its own, from its top, where the account is. */
    private fun captureAccountOptions(name: String, state: EightBallUiState, fontScale: Float) =
        captureRoboImage(filePath = path(name)) {
            WithFontScale(fontScale) {
                MagicEightBallPhoneTheme {
                    ProvideAppLanguage(state.settings.language) {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            SettingsContent(
                                state = state,
                                onLanguageSelected = {},
                                onPersonalitySelected = {},
                                onAiAnswersChange = {},
                                onVoiceQuestionsChange = {},
                                onAllowMicrophone = {},
                                onShareApp = {}
                            )
                        }
                    }
                }
            }
        }

    private fun captureAccountSheet(
        name: String,
        account: AccountState.SignedIn,
        dark: Boolean,
        failed: Boolean = false,
        fontScale: Float = 1f
    ) = captureRoboImage(filePath = path(name)) {
        WithFontScale(fontScale) {
            MagicEightBallPhoneTheme(dark = dark) {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        AccountSheetContent(
                            account = account,
                            profile = PROFILE,
                            failed = failed,
                            busy = false,
                            onEditProfile = {},
                            onSignOut = {},
                            onDelete = {},
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }
    }

    private fun accountState(account: AccountState, language: AppLanguage = AppLanguage.SPANISH) = EightBallUiState(
        settings = UserSettings(language = language),
        aiAnswersAvailable = true,
        config = AppConfig(accountEnabled = true),
        account = account
    )

    /** What is asked before an answer is reported. */
    @Test
    fun report_reasons_spanish() = captureRoboImage(filePath = path("phone_45_report_reasons_spanish")) {
        MagicEightBallPhoneTheme(dark = false) {
            ProvideAppLanguage(AppLanguage.SPANISH) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    ReportContent(onReason = {}, modifier = Modifier.padding(top = 24.dp))
                }
            }
        }
    }

    /** On, and the system is not letting the app notify: said as a warning, with the way to allow it. */
    @Test
    fun phone_settings_daily_verdict_blocked_spanish() =
        captureRoboImage(filePath = path("phone_39_settings_daily_verdict_blocked_spanish")) {
            MagicEightBallPhoneTheme(dark = false) {
                ProvideAppLanguage(AppLanguage.SPANISH) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        SettingsContent(
                            state = EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH)),
                            onLanguageSelected = {},
                            onPersonalitySelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {},
                            notificationsAllowed = false
                        )
                    }
                }
            }
        }

    @Test
    fun phone_history_with_remark_spanish() = capture(
        "phone_27_history_with_remark_spanish",
        EightBallUiState(
            settings = UserSettings(language = AppLanguage.SPANISH),
            history = listOf(
                HistoryEntry(2, "¿Me va a escribir?", "No, mapa roto.\nDiecinueve islas hundidas guarden ese secreto maldito.", NOW - 20_000L, fromAi = true),
                HistoryEntry(1, "", "Es cierto.", NOW - 12 * MINUTE, fromAi = false)
            )
        ),
        initialPage = HISTORY_PAGE
    )

    /** With the classic style the AI switch says why it is off, and with the day's answers spent, that. */
    @Test
    fun phone_settings_classic_and_spent() {
        captureWithSettings(
            "phone_28_settings_classic",
            EightBallUiState(
                settings = UserSettings(language = AppLanguage.SPANISH, personality = Magic8BallPersonality.NEUTRAL),
                aiAnswersAvailable = true
            )
        )
        captureWithSettings(
            "phone_29_settings_ai_spent",
            EightBallUiState(
                settings = UserSettings(language = AppLanguage.SPANISH),
                aiAnswersAvailable = true,
                aiAllowanceSpent = true
            )
        )
    }

    @Test
    fun phone_streak_spanish() = capture(
        "phone_30_streak_spanish",
        EightBallUiState(settings = UserSettings(language = AppLanguage.SPANISH), streakDays = 4)
    )

    @Test
    fun phone_portuguese() {
        captureStyles("phone_32_styles_portuguese", AppLanguage.PORTUGUESE, dark = true)
        captureWithSettings(
            "phone_33_settings_portuguese",
            EightBallUiState(settings = UserSettings(language = AppLanguage.PORTUGUESE), aiAnswersAvailable = true)
        )
        capture(
            "phone_34_ask_portuguese",
            EightBallUiState(settings = UserSettings(language = AppLanguage.PORTUGUESE), streakDays = 3)
        )
    }

    @Test
    fun phone_styles_spanish_dark() = captureStyles("phone_18_styles_spanish_dark", AppLanguage.SPANISH, dark = true)

    @Test
    fun phone_styles_english_light() = captureStyles("phone_19_styles_english_light", AppLanguage.ENGLISH, dark = false)

    @Test
    fun every_style_is_in_one_group_and_the_row_shows_the_chosen_one() {
        // Every character the app wrote: the user's own are no group of the app's.
        assertEquals(CustomCharacter.LOOKS.sortedBy { it.name }, StyleGroups.flatMap { it.styles }.sortedBy { it.name })
        Magic8BallPersonality.entries.forEach { style ->
            val row = stylesInRow(style)
            assertTrue(style in row)
            assertTrue(Magic8BallPersonality.NEUTRAL in row)
            // Whatever is set from afar: the plain style stays, and a hidden one is not offered.
            val steered = stylesInRow(
                style,
                featured = listOf(Magic8BallPersonality.PIRATE, Magic8BallPersonality.CAT),
                hidden = setOf(Magic8BallPersonality.CAT)
            )
            assertTrue(Magic8BallPersonality.NEUTRAL in steered && style in steered)
            assertTrue(style == Magic8BallPersonality.CAT || Magic8BallPersonality.CAT !in steered)
            assertEquals(row.size, row.distinct().size)
        }
    }

    @Test
    fun `a style taken out from afar leaves the sheet, unless it is the one in use`() {
        val hidden = setOf(Magic8BallPersonality.CAT, Magic8BallPersonality.DOG)

        val shown = styleGroups(Magic8BallPersonality.SARCASTIC, hidden).flatMap { it.styles }
        assertTrue(Magic8BallPersonality.CAT !in shown && Magic8BallPersonality.DOG !in shown)
        assertEquals(CustomCharacter.LOOKS.size - 2, shown.size)

        assertTrue(Magic8BallPersonality.CAT in styleGroups(Magic8BallPersonality.CAT, hidden).flatMap { it.styles })
    }

    /** The sheet of styles on its own: a sheet's window is not rendered on the JVM. */
    private fun captureStyles(name: String, language: AppLanguage, dark: Boolean, withCreate: Boolean = false) {
        captureRoboImage(filePath = path(name)) {
            MagicEightBallPhoneTheme(dark = dark) {
                ProvideAppLanguage(language) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        StylesContent(selected = Magic8BallPersonality.LOVESTRUCK, onSelected = {}, onClose = {},
                            onCreate = if (withCreate) ({}) else null
                        )
                    }
                }
            }
        }
    }

    private fun capture(
        name: String,
        state: EightBallUiState,
        voiceLevel: Float = 0f,
        fontScale: Float = 1f,
        initialPage: Int = BALL_PAGE
    ) {
        captureRoboImage(filePath = path(name)) {
            WithFontScale(fontScale) { Screen(state, voiceLevel, initialPage) }
        }
    }

    /** The options page in front, as it is after a swipe to the left. */
    private fun captureWithSettings(name: String, state: EightBallUiState) =
        capture(name, state.copy(phase = BallPhase.Idle), initialPage = OPTIONS_PAGE)

    @Composable
    private fun Screen(
        state: EightBallUiState,
        voiceLevel: Float,
        initialPage: Int = BALL_PAGE
    ) {
        MagicEightBallPhoneTheme {
            ProvideAppLanguage(state.settings.language) {
                EightBallContent(
                    state = state,
                    now = NOW,
                    voiceLevel = { voiceLevel },
                    // An animation that never ends would never come to rest for its picture.
                    still = true,
                    onAsk = {},
                    onHoldStart = {},
                    onHoldEnd = {},
                    onHoldCancel = {},
                    onToggleVoiceQuestion = {},
                    onDismiss = {},
                    onDeleteHistoryEntry = {},
                    onRestoreHistoryEntry = {},
                    onClearHistory = {},
                    options = {
                        SettingsContent(
                            state = state,
                            onLanguageSelected = {},
                            onPersonalitySelected = {},
                            onAiAnswersChange = {},
                            onVoiceQuestionsChange = {},
                            onAllowMicrophone = {},
                            onShareApp = {}
                        )
                    },
                    initialPage = initialPage
                )
            }
        }
    }

    @Composable
    private fun WithFontScale(fontScale: Float, content: @Composable () -> Unit) {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale),
            content = content
        )
    }

    private fun answered(answer: String, language: AppLanguage = AppLanguage.ENGLISH) = EightBallUiState(
        phase = BallPhase.Answered(id = 0, question = "", answer = AnswerText.Plain(answer)),
        settings = UserSettings(language = language)
    )

    private fun path(name: String) = "build/outputs/roborazzi/$name.png"
}

/** 1080 x 1920 px. */
private const val PHONE = "w432dp-h768dp-400dpi"

/** 720 x 1280 px, a small phone. */
private const val SMALL_PHONE = "w360dp-h640dp-xhdpi"

/** As wide as a small phone and as tall as it takes to see the whole of something that scrolls. */
private const val SMALL_PHONE_TALL = "w360dp-h1500dp-xhdpi"

/** 1200 x 1920 px, a 7-inch tablet. */
private const val TABLET = "w600dp-h960dp-xhdpi"

/** 1920 x 1200 px, the same tablet on its side. */
private const val TABLET_LANDSCAPE = "w960dp-h600dp-land-night-xhdpi"

private const val NOW = 1_800_000_000_000L
private const val MINUTE = 60_000L

private val HISTORY = listOf(
    HistoryEntry(3, "Should I order pizza tonight?", "Yes. Screenshot this.", NOW - 20_000L, fromAi = true),
    HistoryEntry(2, "", "Don't count on it.", NOW - 12 * MINUTE, fromAi = false),
    HistoryEntry(1, "Will I finish my book this month?", null, NOW - 26 * 60 * MINUTE, fromAi = false)
)

private val ANSWERED = EightBallUiState(
    phase = BallPhase.Answered(
        id = 0,
        question = "Should I order pizza tonight?",
        answer = AnswerText.Plain("Yes. Screenshot this.")
    ),
    aiAnswersAvailable = true
)

private const val LONG_READING = "Cuatro tazas vacías y un mensaje a medio escribir no son un plan, corazón. Guarda el teléfono en el cajón de las bufandas, cena algo caliente y mañana, con la cabeza fresca, decides si de verdad vale tu desvelo."

private val PROFILE = Profile("Lorena", LocalDate.of(1990, 3, 12))

private val LONG_ACCOUNT = AccountState.SignedIn(
    id = "1",
    name = "María Fernanda de los Ángeles Rodríguez-Echeverría",
    email = "maria.fernanda.rodriguez.echeverria@correo-electronico.example.com"
)
