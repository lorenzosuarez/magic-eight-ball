package com.lorenzosuarez.magiceightball.phone.ui

import android.view.inputmethod.InputConnectionWrapper
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
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
            shape = FieldShape
        )
        // One date, typed as its three numbers, each under its own name: no calendar to page
        // back through for decades, and no doubt about which number is the month.
        Text(
            text = stringResource(R.string.profile_birth_date),
            modifier = Modifier.padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // And typed in one go: each number hands the cursor on to the next, and takes it back.
        val (day, month, year) = remember { List(3) { DateWay() } }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateField(draft::day, draft::typeDay, R.string.profile_day, LAST_DAY, !busy, badDate, day, before = null, next = month, Modifier.weight(1f))
            DateField(draft::month, draft::typeMonth, R.string.profile_month, LAST_MONTH, !busy, badDate, month, before = day, next = year, Modifier.weight(1.4f))
            DateField(draft::year, draft::typeYear, R.string.profile_year, LAST_YEAR, !busy, badDate, year, before = month, next = null, Modifier.weight(1.2f))
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
        Section(
            title = null,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
            ground = MaterialTheme.colorScheme.sheetCard
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
        PrimaryButton(
            text = stringResource(if (firstTime) R.string.profile_accept else R.string.action_save),
            onClick = { if (name != null && date != null) onSave(name, date) },
            modifier = Modifier.padding(start = 16.dp, top = if (trouble != null) 8.dp else 16.dp, end = 16.dp).fillMaxWidth(),
            enabled = name != null && date != null,
            busy = busy
        )
        // The other way out says what it does: the first time, going on with no account.
        QuietButton(
            text = stringResource(if (firstTime) R.string.profile_not_now else R.string.action_cancel),
            onClick = onSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp).fillMaxWidth(),
            enabled = !busy
        )
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

/**
 * One of the date's three numbers. A date is typed in one go: the cursor goes on to the next
 * number as soon as this one is whole (see [typedWhole]), and deleting in it once it is empty
 * goes back to the one before, as in the boxes a code is typed into.
 *
 * Come back into with something in it, the number is selected whole, so that what is typed
 * takes its place with nothing to delete first.
 *
 * @param value what the draft holds of it, read again after every key: the draft keeps the
 * digits alone, and no more of them than the number has.
 * @param most the largest the number can be.
 * @param way the way into it, and [before] and [next] the ways into the numbers beside it:
 * null where there is none.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DateField(
    value: () -> String,
    onValueChange: (String) -> Unit,
    label: Int,
    most: Int,
    enabled: Boolean,
    wrong: Boolean,
    way: DateWay,
    before: DateWay?,
    next: DateWay?,
    modifier: Modifier = Modifier
) {
    // What the field knows beyond the digits the draft keeps: where the cursor is among them,
    // and which of them are selected.
    var held by remember { mutableStateOf(TextFieldValue(value(), TextRange(value().length))) }
    // What the number was when the focus arrived, for as long as the field has it.
    var entered by remember { mutableStateOf<String?>(null) }
    // Once the focus has arrived, and not as it does: the touch that brings it puts the cursor
    // down under the finger after the field has heard of it, and would undo this. And only if
    // nothing has been typed in the meantime: a digit typed that fast would be selected, and
    // the one after it would take its place.
    LaunchedEffect(entered) {
        if (entered != null) {
            val end = value().length
            if (entered == value()) {
                held = held.copy(selection = if (way.byDeleting) TextRange(end) else TextRange(0, end))
            }
            way.byDeleting = false
        }
    }
    // Up from the key that leaves the number whole until the cursor is handed on, which is once
    // the field shows what was typed and not from under the key: a field that loses the focus
    // with something in it selected says once more what it last showed, and the number that
    // was typed over would come back.
    var whole by remember { mutableStateOf(false) }
    LaunchedEffect(whole) {
        if (whole) {
            next?.focus?.requestFocus()
            whole = false
        }
    }
    // With nothing left here to delete, deleting goes back to the number before, and says
    // whether it did. Asked by whichever hears of it: the key, or the keyboard's own request,
    // which may still come once the cursor has gone and is then nobody's.
    // It is one and the same for as long as the field stands, and reads the number through what
    // is kept current: a keyboard listened to by something new at every key starts again at
    // every key.
    val holds by rememberUpdatedState(value)
    val goesBack: () -> Boolean = remember(before) {
        {
            if (before == null || entered == null || holds().isNotEmpty()) {
                false
            } else {
                before.byDeleting = true
                before.focus.requestFocus()
                true
            }
        }
    }
    // A keyboard on the screen does not always send the key that deletes: with nothing before
    // the cursor, some ask all the same for what is before it to be deleted, which no field
    // hears of. It is listened for where the keyboard says it.
    val listened = remember(goesBack) {
        PlatformTextInputInterceptor { request, session ->
            session.startInputMethod { attributes ->
                object : InputConnectionWrapper(request.createInputConnection(attributes), false) {
                    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean =
                        (beforeLength > 0 && goesBack()) || super.deleteSurroundingText(beforeLength, afterLength)

                    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean =
                        (beforeLength > 0 && goesBack()) || super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                }
            }
        }
    }
    InterceptPlatformTextInput(listened) {
        OutlinedTextField(
            value = held.copy(text = value()),
            onValueChange = { typed ->
                val was = held.copy(text = value())
                onValueChange(typed.text)
                held = typed
                if (typedWhole(was, value(), most)) whole = true
            },
            modifier = modifier
                .focusRequester(way.focus)
                .onFocusChanged {
                    // Gone from before it was seen to arrive, as when a key held down deletes on
                    // through it: how it was come into is not kept for the next time.
                    if (!it.isFocused) way.byDeleting = false
                    entered = if (it.isFocused) value() else null
                }
                // The key itself, from a keyboard with keys or from one on the screen that sends it.
                .onPreviewKeyEvent { pressed ->
                    pressed.type == KeyEventType.KeyDown && pressed.key == Key.Backspace && goesBack()
                },
            enabled = enabled,
            isError = wrong,
            shape = FieldShape,
            singleLine = true,
            label = { Text(stringResource(label), maxLines = 1) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (next == null) ImeAction.Done else ImeAction.Next
            ),
            // No handle to carry the cursor by: among two digits, or four, a touch puts it where
            // it is wanted, and the handle would hang under a number that is selected whole.
            colors = OutlinedTextFieldDefaults.colors(
                selectionColors = TextSelectionColors(
                    handleColor = Color.Transparent,
                    backgroundColor = LocalTextSelectionColors.current.backgroundColor
                )
            )
        )
    }
}

/** The way into one of the date's numbers, for the numbers beside it. */
private class DateWay {
    val focus = FocusRequester()

    /**
     * Up from the moment deleting goes back into the number until it has the focus: it then takes
     * the cursor at its end, for the deleting to go on, where any other way in selects it whole.
     */
    var byDeleting = false
}

/**
 * Whether what was just typed leaves one of the date's numbers whole, so that the next is typed
 * without reaching for it: it has as many digits as the largest it can be, or no digit put after
 * it would keep it within that. A day goes on at its second digit, or at a first one from 4 to
 * 9; a month at its second, or at a first one from 2 to 9.
 *
 * Only a digit typed at the end of the number does it. One put in before another is a
 * correction, and whoever is correcting stays where they are; so does whoever deletes, moves the
 * cursor, or types into a number that already has all its digits.
 *
 * @param before the field as it was: what it held, and where in it the typing went.
 * @param typed what it holds now, digits alone.
 * @param most the largest the number can be.
 */
internal fun typedWhole(before: TextFieldValue, typed: String, most: Int): Boolean {
    val atEnd = before.selection.max == before.text.length && typed.length > before.selection.min
    val number = typed.toIntOrNull()
    return atEnd && typed != before.text && number != null &&
        (typed.length == most.toString().length || number * 10 > most)
}

/** The largest each of the date's numbers can be, which is what says when one is typed whole. */
private const val LAST_DAY = 31
private const val LAST_MONTH = 12
private const val LAST_YEAR = 9999
