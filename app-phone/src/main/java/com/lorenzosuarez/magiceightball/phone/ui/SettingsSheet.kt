package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.model.UserSettings
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.VoiceStatus

/**
 * The options, as the page to the right of the ball: every choice takes effect at once.
 *
 * Text and switches, nothing else: one card per subject under a quiet label, its rows separated
 * by hairlines. It scrolls when the font is large or the window is short.
 */
@Composable
internal fun SettingsContent(
    state: EightBallUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit,
    onAiAnswersChange: (Boolean) -> Unit,
    onVoiceQuestionsChange: (Boolean) -> Unit,
    onAllowMicrophone: () -> Unit,
    onShareApp: () -> Unit,
    modifier: Modifier = Modifier,
    onShowAdPrivacyOptions: (() -> Unit)? = null,
    onGetMoreAiAnswers: (() -> Unit)? = null,
    onSpokenAnswersChange: (Boolean) -> Unit = {},
    onPartyNamesChange: (List<String>) -> Unit = {},
    onDailyVerdictChange: (Boolean) -> Unit = {}
) {
    var editingParty by rememberSaveable { mutableStateOf(false) }
    if (editingParty) {
        PartyDialog(
            names = state.settings.partyNames,
            onSave = {
                onPartyNamesChange(it)
                editingParty = false
            },
            onDismiss = { editingParty = false }
        )
    }
    var browsingStyles by rememberSaveable { mutableStateOf(false) }
    if (browsingStyles) {
        StylesSheet(
            selected = state.settings.personality,
            hidden = state.config.hiddenStyles,
            onSelected = onPersonalitySelected,
            onClose = { browsingStyles = false }
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        Section(stringResource(R.string.options_answers)) {
            StyleRow(
                selected = state.settings.personality,
                onSelected = onPersonalitySelected,
                onSeeAll = { browsingStyles = true },
                featured = state.config.featuredStyles,
                hidden = state.config.hiddenStyles
            )
            RowDivider()
            // The classic style never asks the AI service: the switch says so rather than promise it.
            val classic = state.settings.personality == Magic8BallPersonality.NEUTRAL
            SwitchRow(
                title = stringResource(R.string.settings_ai),
                supporting = stringResource(
                    when {
                        !state.aiAnswersAvailable -> R.string.settings_ai_unavailable
                        classic -> R.string.settings_ai_classic
                        !state.settings.aiAnswersEnabled -> R.string.settings_ai_off
                        state.aiAllowanceSpent -> R.string.settings_ai_spent
                        else -> R.string.settings_ai_on
                    }
                ),
                checked = state.usesAiAnswers && !classic,
                enabled = state.aiAnswersAvailable && !classic,
                onCheckedChange = onAiAnswersChange,
                // The one thing on this page that is not as the user set it: today's are used up.
                alert = state.usesAiAnswers && !classic && state.aiAllowanceSpent
            )
            // The day's AI answers are used up and there is an ad to watch for more: the user's choice.
            if (state.usesAiAnswers && !classic && state.aiAllowanceSpent && onGetMoreAiAnswers != null) {
                RowDivider()
                ActionRow(
                    title = stringResource(R.string.settings_ai_more),
                    supporting = stringResource(R.string.settings_ai_more_hint),
                    onClick = onGetMoreAiAnswers
                )
            }
            RowDivider()
            SwitchRow(
                title = stringResource(R.string.settings_spoken),
                supporting = stringResource(
                    if (state.settings.spokenAnswersEnabled) R.string.settings_spoken_on else R.string.settings_spoken_off
                ),
                checked = state.settings.spokenAnswersEnabled,
                enabled = true,
                onCheckedChange = onSpokenAnswersChange
            )
        }

        Section(stringResource(R.string.options_asking)) {
            if (state.voice == VoiceStatus.PermissionDenied) {
                MicrophoneRow(onAllowMicrophone)
            } else {
                SwitchRow(
                    title = stringResource(R.string.settings_voice),
                    supporting = stringResource(
                        when (state.voice) {
                            VoiceStatus.Unsupported -> R.string.settings_voice_unsupported
                            VoiceStatus.Ready -> R.string.settings_voice_on
                            else -> R.string.settings_voice_off
                        }
                    ),
                    checked = state.voice.isReady,
                    enabled = state.voice != VoiceStatus.Unsupported,
                    onCheckedChange = onVoiceQuestionsChange
                )
            }
            RowDivider()
            ActionRow(
                title = stringResource(R.string.settings_party),
                supporting = if (state.settings.isParty) {
                    state.settings.partyNames.joinToString(", ")
                } else {
                    stringResource(R.string.settings_party_off)
                },
                onClick = { editingParty = true }
            )
        }

        Section(stringResource(R.string.options_general)) {
            // Offered only while the developer has it on: a switch that does nothing is a lie.
            if (state.config.dailyVerdictEnabled) {
                SwitchRow(
                    title = stringResource(R.string.settings_daily),
                    supporting = stringResource(
                        if (state.settings.dailyVerdictEnabled) R.string.settings_daily_on else R.string.settings_daily_off
                    ),
                    checked = state.settings.dailyVerdictEnabled,
                    enabled = true,
                    onCheckedChange = onDailyVerdictChange
                )
                RowDivider()
            }
            ChoiceRow(
                title = stringResource(R.string.settings_language),
                options = AppLanguage.entries,
                selected = state.settings.language,
                label = { it.labelRes() },
                onSelected = onLanguageSelected
            )
            RowDivider()
            ActionRow(
                title = stringResource(R.string.settings_share_app),
                supporting = stringResource(R.string.settings_share_app_hint),
                onClick = onShareApp
            )
            if (onShowAdPrivacyOptions != null) {
                RowDivider()
                ActionRow(
                    title = stringResource(R.string.settings_ad_privacy),
                    supporting = stringResource(R.string.settings_ad_privacy_hint),
                    onClick = onShowAdPrivacyOptions
                )
            }
        }

        Text(
            text = stringResource(state.privacyNoteRes()),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.brand_name) + " · " +
                stringResource(R.string.footer_version, BuildConfig.VERSION_NAME),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** A subject: its name, and under it one card that holds everything about it. */
@Composable
private fun Section(title: String, rows: @Composable () -> Unit) {
    Text(
        text = title,
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        rows()
    }
}

/** The hairline between two rows of a card, starting where their text does. */
@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = ROW_TEXT_START),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * One of several, as a row: what is being chosen, and under it the choices as tiles that wrap
 * onto as many lines as they need. The chosen one is filled with the text's own colour.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> Int,
    onSelected: (T) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val chosen = option == selected
                Box(
                    modifier = Modifier
                        .clip(ButtonShape)
                        .background(
                            if (chosen) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA)
                            }
                        )
                        .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelected(option) })
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(label(option)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (chosen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** A setting that is on or off. The whole row is the switch, for touch and for TalkBack. */
@Composable
private fun SwitchRow(
    title: String,
    supporting: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    alert: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled, Modifier.weight(1f), alert)
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/** Something to do rather than to set: the whole row is the button. */
@Composable
private fun ActionRow(title: String, supporting: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled = true, modifier = Modifier.weight(1f))
    }
}

/** Voice questions while the microphone permission is missing: the way to grant it. */
@Composable
private fun MicrophoneRow(onAllowMicrophone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(
            title = stringResource(R.string.settings_voice),
            supporting = stringResource(R.string.settings_voice_denied),
            enabled = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        FilledTonalButton(
            onClick = onAllowMicrophone,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = ButtonShape
        ) {
            Text(stringResource(R.string.settings_voice_allow))
        }
    }
}

@Composable
private fun SettingText(
    title: String,
    supporting: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    alert: Boolean = false
) {
    val alpha = if (enabled) 1f else DISABLED_ALPHA
    // What explains the setting is quiet; what says something is wrong is not.
    val quiet = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = quiet.copy(alpha = alpha)
        )
    }
}

/** What is said about the user's data depends on what the app is doing with it right now. */
@StringRes
private fun EightBallUiState.privacyNoteRes(): Int = when {
    voice.isReady && usesAiAnswers -> R.string.privacy_voice_and_ai
    voice.isReady -> R.string.privacy_voice_only
    usesAiAnswers -> R.string.privacy_ai_only
    else -> R.string.privacy_none
}

@StringRes
private fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.ENGLISH -> R.string.language_english
    AppLanguage.SPANISH -> R.string.language_spanish
    AppLanguage.PORTUGUESE -> R.string.language_portuguese
}

/** Cards and the buttons on them: the two corner sizes the whole app uses. */
internal val CardShape = RoundedCornerShape(16.dp)
internal val ButtonShape = RoundedCornerShape(10.dp)

/** Where a row's text starts, and so where the hairline under it does. */
private val ROW_TEXT_START = 16.dp

/** Material's emphasis for disabled content. */
private const val DISABLED_ALPHA = 0.38f

/** A choice that is not the chosen one: a touch of the text's colour, so it suits both themes. */
internal const val TILE_ALPHA = 0.08f
