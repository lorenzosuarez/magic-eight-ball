package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.Stable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.SnackbarDuration
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
 * The options, as the page to the right of the ball: every choice takes effect at once. Who
 * answers is not among them: that is chosen on the ball's page, where the answer is.
 *
 * @param onAskPremium what is sold was asked for: see [CharacterFlows].
 *
 * The page itself is a way in: who the account is, and then the subjects in groups, a row to
 * each with its mark. A subject opens in the page's own place, with the way back at the top,
 * and there are its switches; [nav] says which one is open, for whoever draws the top of the page.
 * It scrolls when the font is large or the window is short.
 */
@Composable
internal fun SettingsContent(
    state: EightBallUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
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
    onAskPremium: () -> Unit = {},
    nav: OptionsNav = rememberOptionsNav()
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
    val signedIn = state.account is AccountState.SignedIn
    // The classic style never asks the AI service, and neither does one of the user's own while
    // it is not theirs to use: what is said of the AI says so rather than promise it.
    val classic = state.answersAsClassic
    val waiting = classic && state.settings.personality == Magic8BallPersonality.CUSTOM
    val aiNote = when {
        // Not "for now": on a device given an under-age date of birth it is for good.
        state.underAge -> stringResource(R.string.settings_ai_unavailable_here)
        !state.aiAnswersAvailable -> stringResource(R.string.settings_ai_unavailable)
        waiting -> stringResource(R.string.settings_ai_custom_waiting)
        classic -> stringResource(R.string.settings_ai_classic)
        state.usesAiAnswers && state.aiAllowanceSpent -> stringResource(R.string.settings_ai_spent)
        else -> null
    }
    // The one thing here that is not as the user set it: today's are used up.
    val aiSpent = state.usesAiAnswers && !classic && state.aiAllowanceSpent
    val voiceDenied = state.voice == VoiceStatus.PermissionDenied
    // On, and yet nothing would arrive: the system is not letting the app notify.
    val dailyBlocked = state.settings.dailyVerdictEnabled && !notificationsAllowed
    // One at a time: a subject comes in from the side it lies on, and goes back there.
    AnimatedContent(
        targetState = nav.section,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            val forward = targetState != null
            (slideInHorizontally(tween(SECTION_MS)) { if (forward) it / 5 else -it / 5 } + fadeIn(tween(SECTION_MS))) togetherWith
                (slideOutHorizontally(tween(SECTION_MS)) { if (forward) -it / 5 else it / 5 } + fadeOut(tween(SECTION_MS / 2)))
        },
        label = "options"
    ) { section ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            when (section) {
                null -> {
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
                    // What is sold, said once on the page and only to whoever does not have it. It is kept
                    // with an account: whoever has none is first shown the way to one.
                    if (state.offersPremium) {
                        Section(stringResource(R.string.options_premium)) {
                            ActionRow(
                                title = stringResource(R.string.premium_row_title),
                                supporting = stringResource(R.string.premium_row_hint),
                                onClick = onAskPremium,
                                icon = R.drawable.ic_crown
                            )
                        }
                    }
                    Section(stringResource(R.string.options_answers)) {
                        ActionRow(
                            title = stringResource(R.string.settings_ai),
                            // What stands in its way, when something does; else what it is.
                            supporting = aiNote ?: stringResource(R.string.settings_ai_on),
                            onClick = { nav.section = OptionsSection.ANSWERS },
                            icon = R.drawable.ic_sparkles,
                            alert = aiSpent
                        )
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.options_voice),
                            supporting = when (state.voice) {
                                VoiceStatus.Unsupported -> stringResource(R.string.settings_voice_unsupported)
                                VoiceStatus.PermissionDenied -> stringResource(R.string.settings_voice_denied)
                                else -> stringResource(R.string.options_voice_hint)
                            },
                            onClick = { nav.section = OptionsSection.VOICE },
                            icon = R.drawable.ic_mic,
                            alert = voiceDenied
                        )
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.settings_party),
                            supporting = if (state.settings.isParty) {
                                state.settings.partyNames.joinToString(", ")
                            } else {
                                stringResource(R.string.settings_party_off)
                            },
                            onClick = { editingParty = true },
                            icon = R.drawable.ic_party
                        )
                    }
                    Section(stringResource(R.string.options_general)) {
                        // Offered only while the developer has it on: a switch that does nothing is a lie.
                        if (state.config.dailyVerdictEnabled) {
                            ActionRow(
                                title = stringResource(R.string.options_notifications),
                                supporting = stringResource(
                                    if (dailyBlocked) R.string.settings_daily_blocked else R.string.settings_daily_on
                                ),
                                onClick = { nav.section = OptionsSection.NOTIFICATIONS },
                                icon = R.drawable.ic_bell,
                                alert = dailyBlocked
                            )
                            RowDivider()
                        }
                        ActionRow(
                            title = stringResource(R.string.settings_language),
                            supporting = stringResource(state.settings.language.labelRes()),
                            onClick = { nav.section = OptionsSection.LANGUAGE },
                            icon = R.drawable.ic_language
                        )
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.settings_share_app),
                            supporting = stringResource(R.string.settings_share_app_hint),
                            onClick = onShareApp,
                            icon = R.drawable.ic_share
                        )
                    }
                    Section(stringResource(R.string.options_about)) {
                        if (onShowAdPrivacyOptions != null) {
                            // The same choices also decide whether the app's use is measured, so whoever has
                            // paid to see no ads still has the way back to them: under a name without ads in it.
                            ActionRow(
                                title = stringResource(
                                    if (state.premium) R.string.settings_privacy_choices else R.string.settings_ad_privacy
                                ),
                                supporting = stringResource(
                                    if (state.premium) R.string.settings_privacy_choices_hint else R.string.settings_ad_privacy_hint
                                ),
                                onClick = onShowAdPrivacyOptions,
                                icon = R.drawable.ic_shield
                            )
                            RowDivider()
                        }
                        ActionRow(
                            title = stringResource(R.string.settings_privacy_policy),
                            supporting = stringResource(R.string.settings_privacy_policy_hint),
                            onClick = onOpenPrivacyPolicy,
                            icon = R.drawable.ic_terms
                        )
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.options_licenses),
                            supporting = stringResource(R.string.options_licenses_hint),
                            onClick = { nav.section = OptionsSection.LICENSES },
                            icon = R.drawable.ic_licenses
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

                OptionsSection.ANSWERS -> Section(null) {
                    SwitchRow(
                        title = stringResource(R.string.settings_ai),
                        supporting = stringResource(R.string.settings_ai_on),
                        note = aiNote,
                        checked = state.usesAiAnswers && !classic,
                        enabled = state.aiAnswersAvailable && !classic,
                        onCheckedChange = onAiAnswersChange,
                        alert = aiSpent
                    )
                    // The day's AI answers are used up and there is an ad to watch for more: the user's
                    // choice. Not offered to whoever has paid to see no ads.
                    if (aiSpent && !state.premium && onGetMoreAiAnswers != null) {
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
                }

                OptionsSection.VOICE -> Section(null) {
                    // Refused, the switch is still the switch and still off: what stands in its way is
                    // said under it, and the way out is a row of its own, as for notifications.
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
                        onCheckedChange = { if (voiceDenied) onAllowMicrophone() else onVoiceQuestionsChange(it) },
                        alert = voiceDenied
                    )
                    if (voiceDenied) {
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.settings_voice_allow_microphone),
                            supporting = stringResource(R.string.settings_voice_allow_hint),
                            onClick = onAllowMicrophone
                        )
                    }
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_spoken),
                        supporting = stringResource(R.string.settings_spoken_on),
                        checked = state.settings.spokenAnswersEnabled,
                        enabled = true,
                        onCheckedChange = onSpokenAnswersChange
                    )
                }

                OptionsSection.NOTIFICATIONS -> Section(null) {
                    SwitchRow(
                        title = stringResource(R.string.settings_daily),
                        supporting = stringResource(R.string.settings_daily_on),
                        note = if (dailyBlocked) stringResource(R.string.settings_daily_blocked) else null,
                        checked = state.settings.dailyVerdictEnabled,
                        enabled = true,
                        onCheckedChange = onDailyVerdictChange,
                        alert = dailyBlocked
                    )
                    if (dailyBlocked) {
                        RowDivider()
                        ActionRow(
                            title = stringResource(R.string.settings_daily_allow),
                            supporting = stringResource(R.string.settings_daily_allow_hint),
                            onClick = onAllowNotifications
                        )
                    }
                }

                OptionsSection.LANGUAGE -> {
                    // What was just touched is chosen in the frame of the touch, and follows what is saved.
                    var touched by remember(state.settings.language) { mutableStateOf(state.settings.language) }
                    Section(null, modifier = Modifier.selectableGroup()) {
                        AppLanguage.entries.forEachIndexed { index, language ->
                            if (index > 0) RowDivider()
                            CheckRow(
                                title = stringResource(language.labelRes()),
                                chosen = language == touched,
                                onClick = {
                                    touched = language
                                    onLanguageSelected(language)
                                }
                            )
                        }
                    }
                }

                OptionsSection.LICENSES -> Licenses()
            }
        }
    }
}

/** A subject of the options that has a page of its own, and what the page is called. */
internal enum class OptionsSection(@StringRes val title: Int) {
    ANSWERS(R.string.settings_ai),
    VOICE(R.string.options_voice),
    NOTIFICATIONS(R.string.options_notifications),
    LANGUAGE(R.string.settings_language),
    LICENSES(R.string.options_licenses)
}

/** Which subject of the options is open; none is the page itself. */
@Stable
internal class OptionsNav(section: OptionsSection? = null) {
    var section by mutableStateOf(section)
}

/** Kept through a turn of the screen. */
@Composable
internal fun rememberOptionsNav(): OptionsNav = rememberSaveable(
    saver = Saver<OptionsNav, String>({ it.section?.name ?: "" }, { name -> OptionsNav(OptionsSection.entries.find { it.name == name }) })
) { OptionsNav() }

/** What the app uses that other people made, and on what terms. */
@Composable
private fun Licenses() {
    val resources = LocalContext.current.resources
    val lucide = remember { resources.openRawResource(R.raw.license_lucide).bufferedReader().use { it.readText().trim() } }
    Section(null) {
        License(name = "Lucide", what = stringResource(R.string.license_icons), terms = lucide)
        RowDivider()
        License(
            name = "Google Sans",
            what = stringResource(R.string.license_font),
            terms = "Copyright 2025 The Google Sans Project Authors.\nSIL Open Font License, Version 1.1. https://openfontlicense.org"
        )
    }
}

@Composable
private fun License(name: String, what: String, terms: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SettingText(title = name, supporting = what, enabled = true)
        Text(
            text = terms,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A group of rows: its name, when it has one, and under it its rows, each on a tile of its own.
 * Where one row ends and the next begins is said by a [RowDivider] between them: what lies
 * between two of those is one tile, however many things it is made of. See [groupedShape].
 *
 * @param ground what the tiles are filled with: the page's card, or a sheet's on a sheet.
 */
@Composable
internal fun Section(
    title: String?,
    modifier: Modifier = Modifier,
    ground: Color = MaterialTheme.colorScheme.pageCard,
    rows: @Composable () -> Unit
) {
    if (title != null) {
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Spacer(Modifier.height(8.dp))
    }
    // Where each tile begins and ends, top to bottom: worked out as the rows are laid out, and drawn behind them.
    var tiles by remember { mutableStateOf(emptyList<IntRange>()) }
    Layout(
        content = rows,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GROUP_CORNER))
            .drawBehind {
                val seam = CornerRadius(GROUP_SEAM.toPx())
                tiles.forEach { tile ->
                    drawRoundRect(
                        color = ground,
                        topLeft = Offset(0f, tile.first.toFloat()),
                        size = Size(size.width, (tile.last - tile.first).toFloat()),
                        cornerRadius = seam
                    )
                }
            }
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        val found = mutableListOf<IntRange>()
        var y = 0
        var from = 0
        placeables.forEachIndexed { index, placeable ->
            if (measurables[index].layoutId == SEAM) {
                if (y > from) found += from..y
                y += placeable.height
                from = y
            } else {
                y += placeable.height
            }
        }
        if (y > from) found += from..y
        if (found != tiles) tiles = found
        layout(constraints.maxWidth, y) {
            var top = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, top)
                top += placeable.height
            }
        }
    }
}

/** Where one row of a group ends and the next begins: the hair between two tiles. */
@Composable
internal fun RowDivider() {
    Spacer(Modifier.layoutId(SEAM).fillMaxWidth().height(GROUP_GAP))
}

/** What marks a [RowDivider] among the things a [Section] lays out. */
private const val SEAM = "seam"

/** How long a subject takes to come in, and the page to come back. */
private const val SECTION_MS = 220

/** One of several, as a row of its own: its name, and a mark at its end when it is the one. */
@Composable
private fun CheckRow(title: String, chosen: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = chosen, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = GROUP_ROW_HEIGHT)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (chosen) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
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
 *   says; a row that does its work on the touch has none, and neither has one with a mark at
 *   its start, which is a way in by the look of it.
 * @param icon the mark at its start, on a row that names a subject.
 * @param alert whether the line under its name says that something is wrong.
 */
@Composable
internal fun ActionRow(
    title: String,
    supporting: String,
    onClick: () -> Unit,
    danger: Boolean = false,
    opens: Boolean = true,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    alert: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp).size(24.dp),
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SettingText(title, supporting, enabled = true, modifier = Modifier.weight(1f), danger = danger, wrong = alert)
        if (opens && icon == null) {
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
    danger: Boolean = false,
    wrong: Boolean = false
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
            // On a row that only leads somewhere, this line is where what is wrong there is said.
            color = if (wrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
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

