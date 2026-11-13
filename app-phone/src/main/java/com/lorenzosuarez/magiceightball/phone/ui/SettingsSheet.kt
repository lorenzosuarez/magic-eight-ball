package com.lorenzosuarez.magiceightball.phone.ui

import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
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
    onDetailedAnswersChange: (Boolean) -> Unit = {},
    onPartyNamesChange: (List<String>) -> Unit = {},
    onDailyVerdictChange: (Boolean) -> Unit = {},
    notificationsAllowed: Boolean = true,
    onAllowNotifications: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onSignIn: suspend () -> AccountOutcome = { AccountOutcome.FAILED },
    onSignOut: () -> Unit = {},
    onDeleteAccount: suspend () -> AccountOutcome = { AccountOutcome.FAILED }
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
        // First on the page where it is offered: who the answers are for comes before how they are given.
        if (state.offersAccount) {
            AccountSection(
                account = state.account,
                onSignIn = onSignIn,
                onSignOut = onSignOut,
                onDeleteAccount = onDeleteAccount
            )
        }
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
                supporting = stringResource(R.string.settings_ai_on),
                note = when {
                    !state.aiAnswersAvailable -> stringResource(R.string.settings_ai_unavailable)
                    classic -> stringResource(R.string.settings_ai_classic)
                    state.usesAiAnswers && state.aiAllowanceSpent -> stringResource(R.string.settings_ai_spent)
                    else -> null
                },
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
            // How much the ball says: only what the AI writes can be longer.
            SwitchRow(
                title = stringResource(R.string.settings_detailed),
                supporting = stringResource(R.string.settings_detailed_on),
                // Dimmed, it says why.
                note = if (!state.usesAiAnswers || classic) stringResource(R.string.settings_detailed_needs_ai) else null,
                checked = state.settings.detailedAnswers && state.usesAiAnswers && !classic,
                enabled = state.usesAiAnswers && !classic,
                onCheckedChange = onDetailedAnswersChange
            )
            RowDivider()
            SwitchRow(
                title = stringResource(R.string.settings_spoken),
                supporting = stringResource(R.string.settings_spoken_on),
                checked = state.settings.spokenAnswersEnabled,
                enabled = true,
                onCheckedChange = onSpokenAnswersChange
            )
        }

        Section(stringResource(R.string.options_asking)) {
            // Refused, the switch is still the switch and still off: what stands in its way is
            // said under it, and the way out is a row of its own, as for notifications.
            val denied = state.voice == VoiceStatus.PermissionDenied
            SwitchRow(
                title = stringResource(R.string.settings_voice),
                supporting = stringResource(R.string.settings_voice_on),
                note = when (state.voice) {
                    VoiceStatus.Unsupported -> stringResource(R.string.settings_voice_unsupported)
                    VoiceStatus.PermissionDenied -> stringResource(R.string.settings_voice_denied)
                    else -> null
                },
                checked = state.voice.isReady,
                enabled = state.voice != VoiceStatus.Unsupported,
                onCheckedChange = { if (denied) onAllowMicrophone() else onVoiceQuestionsChange(it) },
                alert = denied
            )
            if (denied) {
                RowDivider()
                ActionRow(
                    title = stringResource(R.string.settings_voice_allow_microphone),
                    supporting = stringResource(R.string.settings_voice_allow_hint),
                    onClick = onAllowMicrophone
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
                // On, and yet nothing would arrive: the system is not letting the app notify.
                val blocked = state.settings.dailyVerdictEnabled && !notificationsAllowed
                SwitchRow(
                    title = stringResource(R.string.settings_daily),
                    supporting = stringResource(R.string.settings_daily_on),
                    note = if (blocked) stringResource(R.string.settings_daily_blocked) else null,
                    checked = state.settings.dailyVerdictEnabled,
                    enabled = true,
                    onCheckedChange = onDailyVerdictChange,
                    alert = blocked
                )
                if (blocked) {
                    RowDivider()
                    ActionRow(
                        title = stringResource(R.string.settings_daily_allow),
                        supporting = stringResource(R.string.settings_daily_allow_hint),
                        onClick = onAllowNotifications
                    )
                }
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
            RowDivider()
            ActionRow(
                title = stringResource(R.string.settings_privacy_policy),
                supporting = stringResource(R.string.settings_privacy_policy_hint),
                onClick = onOpenPrivacyPolicy
            )
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
        // What the app is built on: a name, so always in English.
        Text(
            text = stringResource(R.string.footer_powered_by),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
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
                        .heightIn(min = 48.dp)
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
    alert: Boolean = false,
    note: String? = null
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
        // By the title, wherever the row ends: a third line under it does not move the switch.
        verticalAlignment = Alignment.Top
    ) {
        SettingText(title, supporting, enabled, Modifier.weight(1f), alert, note)
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(top = 6.dp),
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * The account. With nobody signed in: what signing in is for, and the one button that does it,
 * which is also how an account is made. With someone signed in: who it is, and the two ways
 * out. Whatever fails is said here, where it was tried, until the next try.
 */
@Composable
private fun AccountSection(
    account: AccountState,
    onSignIn: suspend () -> AccountOutcome,
    onSignOut: () -> Unit,
    onDeleteAccount: suspend () -> AccountOutcome
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var failure by rememberSaveable { mutableStateOf<AccountOutcome?>(null) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    // One thing at a time: a second touch while the first is still being answered does nothing.
    fun attempt(work: suspend () -> AccountOutcome) {
        if (busy) return
        scope.launch {
            busy = true
            failure = null
            try {
                failure = work().takeIf { it == AccountOutcome.FAILED || it == AccountOutcome.NO_GOOGLE_ACCOUNT }
            } finally {
                busy = false
            }
        }
    }

    Section(stringResource(R.string.options_account)) {
        if (account is AccountState.SignedIn) {
            AccountIdentity(account)
            if (failure != null) AccountFailure(stringResource(R.string.account_delete_failed))
            RowDivider()
            ActionRow(
                title = stringResource(R.string.account_sign_out),
                supporting = stringResource(R.string.account_sign_out_hint),
                onClick = onSignOut
            )
            RowDivider()
            ActionRow(
                title = stringResource(R.string.account_delete),
                supporting = stringResource(R.string.account_delete_hint),
                onClick = { confirmingDelete = true },
                danger = true
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingText(
                    title = stringResource(R.string.account_title),
                    supporting = stringResource(R.string.account_hint),
                    enabled = true
                )
                GoogleButton(enabled = !busy, onClick = { attempt(onSignIn) })
            }
            when (failure) {
                AccountOutcome.NO_GOOGLE_ACCOUNT -> AccountFailure(stringResource(R.string.account_no_google))
                null -> Unit
                else -> AccountFailure(stringResource(R.string.account_sign_in_failed))
            }
        }
    }

    if (confirmingDelete) {
        // The dialog is a window of its own, which starts again from the device's language:
        // its texts are read here, in the app's.
        val title = stringResource(R.string.account_delete_title)
        val text = stringResource(R.string.account_delete_text)
        val confirm = stringResource(R.string.account_delete)
        val cancel = stringResource(R.string.action_cancel)
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        attempt(onDeleteAccount)
                    }
                ) {
                    Text(confirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text(cancel) } }
        )
    }
}

/** Who is signed in: a letter for a face, the name, and the address that tells one account from another. */
@Composable
private fun AccountIdentity(account: AccountState.SignedIn) {
    val name = account.name.ifBlank { account.email }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = BAR_DISC_ALPHA))
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Under a name, the address; when the account gave no name, the address is the name.
            if (account.name.isNotBlank() && account.email.isNotBlank()) {
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** What did not work, said in the colour of a warning and heard by whoever cannot see it. */
@Composable
private fun AccountFailure(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

/**
 * The button that signs in with Google, drawn as Google asks for it to be: its mark in its own
 * colours and its own words, on the neutral ground Google names for a light screen and the dark
 * one, outlined, for a dark screen.
 */
@Composable
private fun GoogleButton(enabled: Boolean, onClick: () -> Unit) {
    val dark = MaterialTheme.colorScheme.onSurface.luminance() > 0.5f
    val ground = if (dark) GoogleDarkGround else GoogleLightGround
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(ground)
            .then(if (dark) Modifier.border(1.dp, GoogleDarkOutline, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else BUSY_ALPHA)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Text(
            text = stringResource(R.string.account_continue_google),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (dark) GoogleDarkInk else GoogleLightInk
        )
    }
}

/** Something to do rather than to set: the whole row is the button. */
@Composable
private fun ActionRow(title: String, supporting: String, onClick: () -> Unit, danger: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled = true, modifier = Modifier.weight(1f), danger = danger)
        // What tells a row that opens something from one that only says something.
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingText(
    title: String,
    supporting: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
    note: String? = null,
    danger: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            // Always in full: a row that cannot be changed just now is still to be read, and
            // its switch and the line under it say that it cannot. What cannot be taken back
            // is named in the colour of a warning.
            color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        // What the setting does: said once, and the same whichever way the switch is.
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // What stands in its way just now, when something does: a line of its own, so that the
        // line above never changes. Quiet when it only explains, not when something is wrong.
        if (note != null) {
            Text(
                text = note,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** What is said about the user's data depends on what the app is doing with it right now. */
@StringRes
private fun EightBallUiState.privacyNoteRes(): Int {
    // The Classic style never asks the AI service, whatever the switch says.
    val asksAi = usesAiAnswers && settings.personality != Magic8BallPersonality.NEUTRAL
    return when {
        voice.isReady && asksAi -> R.string.privacy_voice_and_ai
        voice.isReady -> R.string.privacy_voice_only
        asksAi -> R.string.privacy_ai_only
        else -> R.string.privacy_none
    }
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

/** The grounds, outline and inks Google sets for its sign-in button. */
private val GoogleLightGround = Color(0xFFF2F2F2)
private val GoogleLightInk = Color(0xFF1F1F1F)
private val GoogleDarkGround = Color(0xFF131314)
private val GoogleDarkOutline = Color(0xFF8E918F)
private val GoogleDarkInk = Color(0xFFE3E3E3)

/** How faint the button is while the system is still answering the last touch on it. */
private const val BUSY_ALPHA = 0.6f
