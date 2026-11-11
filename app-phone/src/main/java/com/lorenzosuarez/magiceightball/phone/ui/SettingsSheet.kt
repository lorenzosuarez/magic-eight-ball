package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.magiceightball.core.domain.model.UserSettings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.VoiceStatus

/**
 * The options, as the page to the right of the ball: every choice takes effect at once. Grouped
 * by what they are about, each row on a card of its own. It scrolls when the font is large or
 * the window is short.
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
    onSpokenAnswersChange: (Boolean) -> Unit = {},
    onPartyNamesChange: (List<String>) -> Unit = {}
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Brand(poweredByAi = state.usesAiAnswers)

        SectionTitle(stringResource(R.string.options_answers))
        OptionCard {
            ChipChoice(
                title = stringResource(R.string.settings_style),
                options = Magic8BallPersonality.entries,
                selected = state.settings.personality,
                label = { it.labelRes() },
                onSelected = onPersonalitySelected
            )
        }
        OptionCard {
            SwitchRow(
                title = stringResource(R.string.settings_ai),
                supporting = stringResource(
                    when {
                        !state.aiAnswersAvailable -> R.string.settings_ai_unavailable
                        state.settings.aiAnswersEnabled -> R.string.settings_ai_on
                        else -> R.string.settings_ai_off
                    }
                ),
                checked = state.usesAiAnswers,
                enabled = state.aiAnswersAvailable,
                onCheckedChange = onAiAnswersChange
            )
        }
        OptionCard {
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

        SectionTitle(stringResource(R.string.options_asking))
        OptionCard {
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
        }
        OptionCard {
            ActionRow(
                title = stringResource(R.string.settings_party),
                supporting = if (state.settings.isParty) {
                    state.settings.partyNames.joinToString(", ")
                } else {
                    stringResource(R.string.settings_party_off)
                },
                icon = R.drawable.ic_group,
                onClick = { editingParty = true }
            )
        }

        SectionTitle(stringResource(R.string.options_general))
        OptionCard {
            Choice(
                title = stringResource(R.string.settings_language),
                options = AppLanguage.entries,
                selected = state.settings.language,
                label = { it.labelRes() },
                onSelected = onLanguageSelected
            )
        }
        OptionCard {
            ActionRow(
                title = stringResource(R.string.settings_share_app),
                supporting = stringResource(R.string.settings_share_app_hint),
                icon = R.drawable.ic_share,
                onClick = onShareApp
            )
        }
        if (onShowAdPrivacyOptions != null) {
            OptionCard {
                ActionRow(
                    title = stringResource(R.string.settings_ad_privacy),
                    supporting = stringResource(R.string.settings_ad_privacy_hint),
                    icon = R.drawable.ic_settings,
                    onClick = onShowAdPrivacyOptions
                )
            }
        }

        Text(
            text = stringResource(state.privacyNoteRes()),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** What the app is, said plainly at the top of its options: the name, that an AI answers, the version. */
@Composable
private fun Brand(poweredByAi: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.brand_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = listOfNotNull(
                // Left out while the answers are the bundled ones, rather than said of answers no AI wrote.
                stringResource(R.string.brand_tagline).takeIf { poweredByAi },
                stringResource(R.string.footer_version, BuildConfig.VERSION_NAME)
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** One option on a card of its own: a touch of the text's colour over the backdrop, so it suits both themes. */
@Composable
private fun OptionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = CARD_ALPHA))
    ) {
        content()
    }
}


/** A titled choice of one among a few, as a row of connected buttons. [label] gives a string resource. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Choice(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> Int,
    onSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(label(option)))
                }
            }
        }
    }
}

/** One of many: chips that wrap onto as many lines as they need, where a row of segments would not fit. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipChoice(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> Int,
    onSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(stringResource(label(option))) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
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
    onCheckedChange: (Boolean) -> Unit
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled, Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/** Who is playing: names typed in one field, since that is how a group is dictated. Saving none ends the party. */
@Composable
private fun PartyDialog(names: List<String>, onSave: (List<String>) -> Unit, onDismiss: () -> Unit) {
    var typed by rememberSaveable { mutableStateOf(names.joinToString(", ")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_party)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.party_dialog_hint)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Text(
                    text = stringResource(R.string.party_dialog_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(UserSettings.parsePartyNames(typed)) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

/** Something to do rather than to set: the whole row is the button. */
@Composable
private fun ActionRow(title: String, supporting: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled = true, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Voice questions while the microphone permission is missing: the way to grant it. */
@Composable
private fun MicrophoneRow(onAllowMicrophone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(
            title = stringResource(R.string.settings_voice),
            supporting = stringResource(R.string.settings_voice_denied),
            enabled = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(16.dp))
        FilledTonalButton(onClick = onAllowMicrophone, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.settings_voice_allow))
        }
    }
}

@Composable
private fun SettingText(title: String, supporting: String, enabled: Boolean, modifier: Modifier = Modifier) {
    val alpha = if (enabled) 1f else DISABLED_ALPHA
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
    }
}

/** The privacy note that is true for what is in use right now. */
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
}

@StringRes
private fun Magic8BallPersonality.labelRes(): Int = when (this) {
    Magic8BallPersonality.SARCASTIC -> R.string.style_sarcastic
    Magic8BallPersonality.MYSTICAL -> R.string.style_mystical
    Magic8BallPersonality.NEUTRAL -> R.string.style_neutral
    Magic8BallPersonality.GRANDMA -> R.string.style_grandma
    Magic8BallPersonality.PIRATE -> R.string.style_pirate
    Magic8BallPersonality.PHILOSOPHER -> R.string.style_philosopher
    Magic8BallPersonality.COACH -> R.string.style_coach
    Magic8BallPersonality.TOXIC_EX -> R.string.style_toxic_ex
}

/** Material's emphasis for disabled content. */
private const val DISABLED_ALPHA = 0.38f
private const val CARD_ALPHA = 0.06f
