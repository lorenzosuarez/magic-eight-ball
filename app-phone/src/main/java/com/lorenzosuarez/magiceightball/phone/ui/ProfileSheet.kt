package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.common.BrandPalette
import com.example.magiceightball.core.domain.model.AskerFacts
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import com.lorenzosuarez.magiceightball.phone.account.Profile
import com.lorenzosuarez.magiceightball.phone.account.ProfileOutcome
import com.lorenzosuarez.magiceightball.phone.account.ProfileRules
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * What the account brings up by itself, whichever page is in front: the profile it is not kept
 * without, asked for as soon as someone has signed in, and the word that there is no account
 * for someone whose date of birth is that of a minor.
 */
@Composable
internal fun AccountPrompts(
    state: EightBallUiState,
    onSaveProfile: suspend (String, LocalDate) -> ProfileOutcome,
    onDeclineProfile: suspend () -> AccountOutcome,
    onOpenPrivacyPolicy: () -> Unit,
    onAgeNoticeSeen: () -> Unit
) {
    // Open from the moment a profile is needed until the sheet has closed itself, so that it
    // closes as a sheet does and not by vanishing with the account. Asked again whenever it is
    // needed and not being asked: a sheet that closed does not end the need.
    var asking by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.needsProfile, asking) { if (state.needsProfile && !asking) asking = true }
    if (asking) {
        ProfileSheet(
            profile = null,
            // Google's name for the account, down to its first word: a start, to be corrected.
            suggestedName = (state.account as? AccountState.SignedIn)?.name?.trim()?.substringBefore(' ').orEmpty(),
            // Gone once it is answered or there is nobody to ask: not while the profile is only
            // unheard of, which a lost connection makes it for a while.
            gone = state.account !is AccountState.SignedIn || state.profile is ProfileState.Present || !state.offersAccount,
            onSave = onSaveProfile,
            onDecline = onDeclineProfile,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
            onClose = { asking = false }
        )
    } else if (state.ageNotice) {
        // The dialog is a window of its own: its texts are read here, in the app's language.
        val title = stringResource(R.string.age_notice_title)
        // Said to be deleted only when it was: an account that could not be reached is still there.
        val text = stringResource(
            if (state.account is AccountState.SignedIn) R.string.age_notice_text_pending else R.string.age_notice_text
        )
        val understood = stringResource(R.string.action_understood)
        AlertDialog(
            onDismissRequest = onAgeNoticeSeen,
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = { TextButton(onClick = onAgeNoticeSeen) { Text(understood) } }
        )
    }
}

/**
 * The sheet that asks who the user is, or lets them correct it.
 *
 * Asked for the first time ([profile] is null) it is not put away by a swipe, a touch outside
 * it or the back key: an account is kept with a profile or not at all, so the only ways out
 * are its two buttons, and "not now" says what it means. Over a profile that exists, it closes
 * as any sheet does and nothing changes.
 *
 * @param gone whether what the sheet was open over is no longer there: the account, or the
 * need for a profile. The sheet then closes.
 * @param onDecline "not now"; null over a profile that exists, where there is only cancelling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileSheet(
    profile: Profile?,
    suggestedName: String,
    gone: Boolean,
    onSave: suspend (String, LocalDate) -> ProfileOutcome,
    onDecline: (suspend () -> AccountOutcome)?,
    onOpenPrivacyPolicy: () -> Unit,
    onClose: () -> Unit
) {
    val firstTime = profile == null
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val draft = rememberSaveable(saver = ProfileDraft.Saver) {
        ProfileDraft(profile?.firstName ?: ProfileRules.nameOrNull(suggestedName).orEmpty(), profile?.birthDate)
    }
    var busy by remember { mutableStateOf(false) }
    var trouble by rememberSaveable { mutableStateOf<ProfileTrouble?>(null) }
    // Up once the sheet is leaving by its own doing: until then, asked for the first time, it stays.
    var leaving by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !firstTime || leaving || it != SheetValue.Hidden }
    )
    fun leave() {
        leaving = true
        scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() }
    }
    LaunchedEffect(gone) { if (gone) leave() }

    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    ModalBottomSheet(
        onDismissRequest = { if (!firstTime) onClose() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetGround,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        // No handle on a sheet that a pull does not move.
        dragHandle = if (firstTime) null else ({ androidx.compose.material3.BottomSheetDefaults.DragHandle() }),
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = !firstTime)
    ) {
        InAppLanguage(language) {
            ProfileContent(
                draft = draft,
                firstTime = firstTime,
                today = today,
                busy = busy,
                trouble = trouble?.let { stringResource(it.message) },
                onSave = { name, date ->
                    if (!busy) {
                        scope.launch {
                            busy = true
                            trouble = null
                            val outcome = try {
                                onSave(name, date)
                            } finally {
                                busy = false
                            }
                            // An under-age date takes the account away, which closes the sheet.
                            when (outcome) {
                                ProfileOutcome.SAVED -> leave()
                                ProfileOutcome.FAILED -> trouble = ProfileTrouble.NOT_SAVED
                                ProfileOutcome.REFUSED -> trouble = ProfileTrouble.REFUSED
                                ProfileOutcome.UNDER_AGE -> Unit
                            }
                        }
                    }
                },
                onSecondary = {
                    if (onDecline == null) {
                        leave()
                    } else if (!busy) {
                        scope.launch {
                            busy = true
                            trouble = null
                            val outcome = try {
                                onDecline()
                            } finally {
                                busy = false
                            }
                            // Gone, the account closes the sheet. Still there, the user is told
                            // and can try again; backing out of Google's own question needs no telling.
                            if (outcome != AccountOutcome.DONE && outcome != AccountOutcome.CANCELLED) {
                                trouble = ProfileTrouble.ACCOUNT_KEPT
                            }
                        }
                    }
                },
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                modifier = if (firstTime) Modifier.padding(top = 24.dp) else Modifier
            )
        }
    }
}

/** What can stand between the sheet's buttons and what they do, and how each is said. */
internal enum class ProfileTrouble(val message: Int) {
    NOT_SAVED(R.string.profile_save_failed),

    /** The server's clock and the device's disagree about the date: it is the date to look at. */
    REFUSED(R.string.profile_date_invalid),

    /** Going on without an account could not delete the one just made. */
    ACCOUNT_KEPT(R.string.account_delete_failed)
}

/** What is being typed into the profile, until it is saved or given up. */
@Stable
internal class ProfileDraft(name: String = "", birthDate: LocalDate? = null) {
    var name by mutableStateOf(AskerFacts.cut(name))
        private set
    var day by mutableStateOf(birthDate?.dayOfMonth?.toString().orEmpty())
        private set
    var month by mutableStateOf(birthDate?.monthValue?.toString().orEmpty())
        private set
    var year by mutableStateOf(birthDate?.year?.toString().orEmpty())
        private set

    fun typeName(typed: String) {
        name = AskerFacts.cut(typed)
    }

    fun typeDay(typed: String) {
        day = typed.filter(Char::isDigit).take(2)
    }

    fun typeMonth(typed: String) {
        month = typed.filter(Char::isDigit).take(2)
    }

    fun typeYear(typed: String) {
        year = typed.filter(Char::isDigit).take(YEAR_DIGITS)
    }

    /** Whether all three numbers of the date are in: only then is a date that does not exist worth saying. */
    val dateTyped: Boolean get() = day.isNotEmpty() && month.isNotEmpty() && year.length == YEAR_DIGITS

    fun date(today: LocalDate): LocalDate? = ProfileRules.dateOrNull(day, month, year, today)

    val nameToSave: String? get() = ProfileRules.nameOrNull(name)

    companion object {
        private const val YEAR_DIGITS = 4

        val Saver = listSaver<ProfileDraft, String>(
            save = { listOf(it.name, it.day, it.month, it.year) },
            restore = { (name, day, month, year) ->
                ProfileDraft(name).apply {
                    typeDay(day)
                    typeMonth(month)
                    typeYear(year)
                }
            }
        )
    }
}

/**
 * What [ProfileSheet] shows: the two things asked, what is sent of them to the AI service and
 * what never is, and the button that is the user's agreement to it. A date that cannot be one
 * is said under the date; a save that failed, by the button.
 *
 * @param trouble what the last touch of a button could not do, if anything.
 * @param onSecondary "not now" the first time, and cancelling over a profile that exists.
 */
@Composable
internal fun ProfileContent(
    draft: ProfileDraft,
    firstTime: Boolean,
    today: LocalDate,
    busy: Boolean,
    trouble: String?,
    onSave: (String, LocalDate) -> Unit,
    onSecondary: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = draft.nameToSave
    val date = draft.date(today)
    val badDate = draft.dateTyped && date == null
    val scroll = rememberScrollState()
    // What a button could not do is said by the buttons: brought into sight if it is not.
    LaunchedEffect(trouble) { if (trouble != null) scroll.animateScrollTo(scroll.maxValue) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(if (firstTime) R.string.profile_title_new else R.string.profile_title_edit),
            modifier = Modifier.padding(horizontal = 32.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.profile_hint),
            modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // The fields share the cards' edge, so that what is typed in them starts where every
        // other line on the sheet does.
        OutlinedTextField(
            value = draft.name,
            onValueChange = draft::typeName,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            enabled = !busy,
            singleLine = true,
            label = { Text(stringResource(R.string.profile_name)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            shape = ButtonShape
        )
        // One date, typed as its three numbers, each under its own name: no calendar to page
        // back through for decades, and no doubt about which number is the month.
        Text(
            text = stringResource(R.string.profile_birth_date),
            modifier = Modifier.padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateField(draft.day, draft::typeDay, R.string.profile_day, !busy, badDate, Modifier.weight(1f))
            DateField(draft.month, draft::typeMonth, R.string.profile_month, !busy, badDate, Modifier.weight(1.4f))
            DateField(draft.year, draft::typeYear, R.string.profile_year, !busy, badDate, Modifier.weight(1.2f), last = true)
        }
        if (badDate) {
            Trouble(stringResource(R.string.profile_date_invalid), Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp))
        } else if (date != null) {
            // The date read back in words, whatever it is: a slip of the finger in the year
            // is seen here, before the button, and not after.
            Text(
                text = date.spelledOut(),
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Before the button, and in full: what agreeing to it means.
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.sheetCard)
        ) {
            Text(
                text = stringResource(R.string.profile_sent_title),
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
                style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(if (firstTime) R.string.profile_sent_text else R.string.profile_sent_text_edit),
                modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 14.dp),
                // Several lines to be read in full: broken so that none ends on a word alone.
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RowDivider()
            // The whole of it, for whoever wants it: a row that opens something, as on the options page.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onOpenPrivacyPolicy)
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_privacy_policy),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trouble != null) {
            Trouble(trouble, Modifier.padding(start = 32.dp, top = 12.dp, end = 32.dp))
        }
        Button(
            onClick = { if (name != null && date != null) onSave(name, date) },
            modifier = Modifier
                .padding(start = 16.dp, top = if (trouble != null) 8.dp else 16.dp, end = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = !busy && name != null && date != null,
            shape = ButtonShape,
            // The brand's deep accent in both themes: white on the dark theme's lighter one is too faint to read.
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(BrandPalette.ACCENT_DEEP),
                contentColor = Color(BrandPalette.ON_ACCENT)
            )
        ) {
            BusyRing(busy)
            Text(
                text = stringResource(if (firstTime) R.string.profile_accept else R.string.action_save),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        // The other way out says what it does: the first time, going on with no account.
        TextButton(
            onClick = onSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
            enabled = !busy,
            shape = ButtonShape
        ) {
            Text(
                text = stringResource(if (firstTime) R.string.profile_not_now else R.string.action_cancel),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** What stands in the way, said where it happened and heard by whoever cannot see it. */
@Composable
private fun Trouble(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Heading),
        color = MaterialTheme.colorScheme.error
    )
}

/** One of the date's three numbers. */
@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    enabled: Boolean,
    wrong: Boolean,
    modifier: Modifier = Modifier,
    last: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = wrong,
        shape = ButtonShape,
        singleLine = true,
        label = { Text(stringResource(label), maxLines = 1) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (last) ImeAction.Done else ImeAction.Next
        )
    )
}
