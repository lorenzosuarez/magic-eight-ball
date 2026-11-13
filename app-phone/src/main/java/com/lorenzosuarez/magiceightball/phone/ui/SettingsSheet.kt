package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.runtime.mutableIntStateOf
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.CharacterSave
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.lorenzosuarez.magiceightball.phone.account.ProfileOutcome
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import com.lorenzosuarez.magiceightball.phone.account.Bought
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
    onDeleteAccount: suspend () -> AccountOutcome = { AccountOutcome.FAILED },
    onSaveProfile: suspend (String, LocalDate) -> ProfileOutcome = { _, _ -> ProfileOutcome.FAILED },
    onChooseCharacter: (CustomCharacter) -> Unit = {},
    onSaveCharacter: suspend (CustomCharacter?, String, String, Magic8BallPersonality, Magic8BallPersonality) -> CharacterSave =
        { _, _, _, _, _ -> CharacterSave.Failed },
    onDeleteCharacter: suspend (String) -> Boolean = { false },
    onBuyPremium: suspend () -> Bought = { Bought.FAILED }
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
    // Which character of the user's own is open in the editor: its slot, or [NEW] for one being made.
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by rememberSaveable { mutableStateOf<CharactersNotice?>(null) }
    var paying by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val signedIn = state.account is AccountState.SignedIn
    // Whether what is kept with the account, its profile and its characters, has been heard of.
    val accountKnown = state.profile != ProfileState.Unknown && state.characters != null
    // What "Create" does once that is known: said here once, for a touch now or one that waited.
    val create = {
        val kept = state.characters
        when {
            !state.canMakeCharacters || kept == null -> notice = CharactersNotice.UNKNOWN
            kept.size >= CustomCharacter.MAX_COUNT -> notice = CharactersNotice.FULL
            // Last: nobody is asked to pay for what they could not have made anyway.
            state.mustPayToCreate -> paying = true
            else -> editing = NEW
        }
    }
    // "Create" touched while the account was still on its way: it goes on by itself when it
    // arrives, and says that it could not only once it has plainly not come.
    var waitingToCreate by remember { mutableStateOf(false) }
    // The places kept for their characters while those are on their way: as many as there were
    // when last heard of, one when one of them is the character chosen, and none when there is
    // no reason to think there are any. Kept for a while only: after that it is a connection
    // that is not there, which a touch on "Create" says in words.
    val yoursOnTheirWay = state.offersAccount && signedIn && state.characters == null
    // Of this account: what another one had says nothing of it.
    var lastHeardOf by rememberSaveable((state.account as? AccountState.SignedIn)?.id) { mutableIntStateOf(UNHEARD_OF) }
    LaunchedEffect(state.characters) { state.characters?.let { lastHeardOf = it.size } }
    var patient by remember(yoursOnTheirWay) { mutableStateOf(true) }
    LaunchedEffect(yoursOnTheirWay) {
        if (yoursOnTheirWay) {
            delay(ACCOUNT_PATIENCE_MS)
            patient = false
        }
    }
    val yoursPlaces = when {
        !yoursOnTheirWay || !patient -> 0
        lastHeardOf != UNHEARD_OF -> lastHeardOf
        state.settings.customCharacter != null -> 1
        else -> 0
    }
    // As things stand when the wait ends, not as they stood when it began.
    val createNow by rememberUpdatedState(create)
    LaunchedEffect(waitingToCreate, accountKnown) {
        if (waitingToCreate) {
            if (!accountKnown) delay(ACCOUNT_PATIENCE_MS)
            waitingToCreate = false
            // Unless something else was opened meanwhile, or the profile is being asked for:
            // what waited does not land on top of that.
            if (editing == null && !paying && state.profile != ProfileState.Missing) createNow()
        }
    }
    if (browsingStyles) {
        StylesSheet(
            selected = state.settings.personality,
            hidden = state.config.hiddenStyles,
            onSelected = onPersonalitySelected,
            onClose = {
                browsingStyles = false
                // A wait that outlived the sheet it was asked on would open something unasked.
                waitingToCreate = false
            },
            // Offered wherever an account is: whoever has none yet is shown the way to one.
            onCreate = if (state.offersAccount) {
                {
                    when {
                        !signedIn -> notice = CharactersNotice.ACCOUNT
                        accountKnown -> create()
                        else -> waitingToCreate = true
                    }
                }
            } else null,
            yours = state.characters.orEmpty().takeIf { state.offersAccount && signedIn }.orEmpty(),
            yoursPlaces = yoursPlaces,
            creating = waitingToCreate,
            chosenOfYours = state.settings.customCharacter?.id,
            onYoursSelected = onChooseCharacter,
            onEditYours = { editing = it.id }
        )
    }
    if (paying) {
        // Bought, here or wherever a payment that was pending came through: on to what they wanted.
        val goOn = {
            paying = false
            editing = NEW
        }
        LaunchedEffect(state.premium) { if (state.premium) goOn() }
        // No longer sold, or not yet known to be, as when the app has just come back: the sheet
        // goes, and nothing opens that was not paid for. Whoever wants to make one asks again.
        LaunchedEffect(state.config.premiumEnabled) { if (!state.config.premiumEnabled) paying = false }
        PremiumSheet(offer = state.offer, onBuy = onBuyPremium, onBought = goOn, onClose = { paying = false })
    }
    // The character the editor was opened on: kept while those of the account are for a moment
    // not known, so that what is being typed is not lost to a connection that blinked.
    var opened by remember { mutableStateOf<CustomCharacter?>(null) }
    editing?.let { slot ->
        val kept = state.characters
        val character = kept?.find { it.id == slot } ?: opened?.takeIf { it.id == slot && kept == null }
        if (slot == NEW || character != null) {
            // As it was when the editor opened: what is saved goes over that, or not at all.
            if (opened?.id != slot) opened = character
            val original = opened
            CharacterEditorSheet(
                character = original,
                onSave = { name, description, colour, emblem -> onSaveCharacter(original, name, description, colour, emblem) },
                onDelete = original?.let { { onDeleteCharacter(it.id) } },
                onClose = {
                    editing = null
                    opened = null
                }
            )
        } else if (kept != null) {
            // Known to be gone, from here or from another device: there is nothing to correct.
            LaunchedEffect(slot) {
                editing = null
                opened = null
            }
        }
    }
    notice?.let { what ->
        // The dialog is a window of its own: its texts are read here, in the app's language.
        val title = stringResource(what.title)
        val text = stringResource(what.text)
        val go = stringResource(R.string.account_continue_google)
        val later = stringResource(R.string.action_not_now)
        val understood = stringResource(R.string.action_understood)
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                if (what == CharactersNotice.ACCOUNT) {
                    TextButton(
                        onClick = {
                            notice = null
                            scope.launch {
                                val outcome = onSignIn()
                                // Backing out of Google's own question needs no telling.
                                if (outcome != AccountOutcome.DONE && outcome != AccountOutcome.CANCELLED) {
                                    notice = CharactersNotice.NO_SIGN_IN
                                }
                            }
                        }
                    ) { Text(go) }
                } else {
                    TextButton(onClick = { notice = null }) { Text(understood) }
                }
            },
            dismissButton = if (what == CharactersNotice.ACCOUNT) {
                { TextButton(onClick = { notice = null }) { Text(later) } }
            } else null
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
                onDeleteAccount = onDeleteAccount,
                profile = (state.profile as? ProfileState.Present)?.profile,
                onSaveProfile = onSaveProfile,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                premium = state.premium,
                // Only of someone signed in: signed out there is no profile to wait for, and
                // the wait would be over before it began.
                loading = signedIn && state.profile == ProfileState.Unknown
            )
        }
        Section(stringResource(R.string.options_answers)) {
            StyleRow(
                selected = state.settings.personality,
                onSelected = onPersonalitySelected,
                onSeeAll = { browsingStyles = true },
                featured = state.config.featuredStyles,
                hidden = state.config.hiddenStyles,
                custom = state.settings.customCharacter
            )
            RowDivider()
            // The classic style never asks the AI service, and neither does one of the user's own
            // while it is not theirs to use: the switch says so rather than promise it.
            val classic = state.answersAsClassic
            val waiting = classic && state.settings.personality == Magic8BallPersonality.CUSTOM
            SwitchRow(
                title = stringResource(R.string.settings_ai),
                supporting = stringResource(R.string.settings_ai_on),
                note = when {
                    // Not "for now": on a device given an under-age date of birth it is for good.
                    state.underAge -> stringResource(R.string.settings_ai_unavailable_here)
                    !state.aiAnswersAvailable -> stringResource(R.string.settings_ai_unavailable)
                    waiting -> stringResource(R.string.settings_ai_custom_waiting)
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
            // With a profile, what the AI service is told of it is said here too.
            text = stringResource(state.privacyNoteRes()) +
                if (state.sendsProfile) " " + stringResource(R.string.privacy_account) else "",
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
internal fun Section(title: String, rows: @Composable () -> Unit) {
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
internal fun RowDivider() {
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
    // As on the cards of the characters: what was just touched is chosen in the frame of the
    // touch, and follows what is saved whenever that changes.
    var touched by remember(selected) { mutableStateOf(selected) }
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
                val chosen = option == touched
                // As on the cards of the characters, right above: the choice passes from one
                // tile to another, and nothing of either moves.
                val ground by animateColorAsState(
                    targetValue = if (chosen) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA)
                    },
                    animationSpec = choiceTween(),
                    label = "tile ground"
                )
                val ink by animateColorAsState(
                    targetValue = if (chosen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                    animationSpec = choiceTween(),
                    label = "tile ink"
                )
                Box(
                    modifier = Modifier
                        .clip(ButtonShape)
                        .background(ground)
                        .selectable(
                            selected = chosen,
                            role = Role.RadioButton,
                            onClick = {
                                touched = option
                                onSelected(option)
                            }
                        )
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(label(option)),
                        style = MaterialTheme.typography.labelLarge,
                        // One weight, chosen or not, as on the cards: a tile that changed its
                        // weight would change its width, and push the others.
                        fontWeight = FontWeight.SemiBold,
                        color = ink
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
 * Something to do rather than to set: the whole row is the button.
 *
 * @param opens whether the touch opens something, which is what the arrow at the row's end
 *   says; a row that does its work on the touch has none.
 */
@Composable
internal fun ActionRow(
    title: String,
    supporting: String,
    onClick: () -> Unit,
    danger: Boolean = false,
    opens: Boolean = true,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingText(title, supporting, enabled = true, modifier = Modifier.weight(1f), danger = danger)
        if (opens) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun SettingText(
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
            // Broken as a heading is: no word left alone on the last line.
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
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

/** What making a character can run into before the editor opens, and how each is said. */
internal enum class CharactersNotice(@StringRes val title: Int, @StringRes val text: Int) {
    /** Nobody is signed in: the way to an account. */
    ACCOUNT(R.string.characters_account_title, R.string.characters_account_text),

    /** Signed in, and what is kept with the account has not been heard of. */
    UNKNOWN(R.string.characters_unknown_title, R.string.characters_unknown_text),

    /** Every slot is taken. */
    FULL(R.string.characters_full_title, R.string.characters_full_text),

    /** The way to an account was taken, and led nowhere. */
    NO_SIGN_IN(R.string.characters_no_sign_in_title, R.string.characters_unknown_text)
}

/** What [SettingsContent] calls the editor open on nobody yet. */
private const val NEW = ""

/** Whether answers are being made for whoever is signed in: the AI service is asked, and they have a profile. */
private val EightBallUiState.sendsProfile: Boolean
    get() = offersAccount && profile is ProfileState.Present && usesAiAnswers && !answersAsClassic

/** What is said about the user's data depends on what the app is doing with it right now. */
@StringRes
private fun EightBallUiState.privacyNoteRes(): Int {
    // The Classic style never asks the AI service, whatever the switch says.
    val asksAi = usesAiAnswers && !answersAsClassic
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

/** How long a touch on "Create" waits for an account that is on its way before saying it has not come. */
private const val ACCOUNT_PATIENCE_MS = 6_000L

/** Before the characters of the account have been heard of once: how many there are is not known. */
private const val UNHEARD_OF = -1
