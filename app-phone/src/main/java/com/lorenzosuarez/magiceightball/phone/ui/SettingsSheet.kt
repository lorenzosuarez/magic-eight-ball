package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
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
 * The settings, in a modal bottom sheet that follows the back gesture. Every choice takes
 * effect, and is saved, as soon as it is made.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    state: EightBallUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit,
    onAiAnswersChange: (Boolean) -> Unit,
    onVoiceQuestionsChange: (Boolean) -> Unit,
    onAllowMicrophone: () -> Unit,
    onShareApp: () -> Unit,
    onDismiss: () -> Unit,
    onShowAdPrivacyOptions: (() -> Unit)? = null,
    onSpokenAnswersChange: (Boolean) -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        // The sheet is a window of its own, which starts again from the device's language.
        ProvideAppLanguage(state.settings.language) {
            SettingsContent(
                state = state,
                onLanguageSelected = onLanguageSelected,
                onPersonalitySelected = onPersonalitySelected,
                onAiAnswersChange = onAiAnswersChange,
                onVoiceQuestionsChange = onVoiceQuestionsChange,
                onAllowMicrophone = onAllowMicrophone,
                onShareApp = onShareApp,
                onShowAdPrivacyOptions = onShowAdPrivacyOptions,
                onSpokenAnswersChange = onSpokenAnswersChange
            )
        }
    }
}

/** What the sheet holds. It scrolls when the font is large or the window is short. */
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
    onSpokenAnswersChange: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(20.dp))

        Choice(
            title = stringResource(R.string.settings_language),
            options = AppLanguage.entries,
            selected = state.settings.language,
            label = { it.labelRes() },
            onSelected = onLanguageSelected
        )
        Spacer(Modifier.height(20.dp))
        ChipChoice(
            title = stringResource(R.string.settings_style),
            options = Magic8BallPersonality.entries,
            selected = state.settings.personality,
            label = { it.labelRes() },
            onSelected = onPersonalitySelected
        )
        Spacer(Modifier.height(12.dp))

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
        SwitchRow(
            title = stringResource(R.string.settings_spoken),
            supporting = stringResource(
                if (state.settings.spokenAnswersEnabled) R.string.settings_spoken_on else R.string.settings_spoken_off
            ),
            checked = state.settings.spokenAnswersEnabled,
            enabled = true,
            onCheckedChange = onSpokenAnswersChange
        )

        ActionRow(
            title = stringResource(R.string.settings_share_app),
            supporting = stringResource(R.string.settings_share_app_hint),
            icon = R.drawable.ic_share,
            onClick = onShareApp
        )

        if (onShowAdPrivacyOptions != null) {
            ActionRow(
                title = stringResource(R.string.settings_ad_privacy),
                supporting = stringResource(R.string.settings_ad_privacy_hint),
                icon = R.drawable.ic_settings,
                onClick = onShowAdPrivacyOptions
            )
        }

        Text(
            text = stringResource(state.privacyNoteRes()),
            modifier = Modifier.padding(start = 24.dp, top = 12.dp, end = 24.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        modifier = Modifier.padding(horizontal = 24.dp),
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
        modifier = Modifier.padding(horizontal = 24.dp),
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
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled, Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/** Something to do rather than to set: the whole row is the button. */
@Composable
private fun ActionRow(title: String, supporting: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp),
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
            .padding(horizontal = 24.dp, vertical = 12.dp),
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
