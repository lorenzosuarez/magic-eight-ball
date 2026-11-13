package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import kotlin.math.ceil
import kotlinx.coroutines.launch

/**
 * The account on the page of options. With nobody signed in: what signing in is for, and the
 * one button that does it, which is also how an account is made. With someone signed in: who it
 * is, as one row that opens [AccountSheet]; the ways out are kept there, out of the way of
 * everything else on the page.
 */
@Composable
internal fun AccountSection(
    account: AccountState,
    onSignIn: suspend () -> AccountOutcome,
    onSignOut: () -> Unit,
    onDeleteAccount: suspend () -> AccountOutcome
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var failure by rememberSaveable { mutableStateOf<AccountOutcome?>(null) }
    var managing by rememberSaveable { mutableStateOf(false) }
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
            AccountIdentity(
                account = account,
                onClick = {
                    failure = null
                    managing = true
                }
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
                null -> Unit
                AccountOutcome.NO_GOOGLE_ACCOUNT -> AccountFailure(
                    stringResource(R.string.account_no_google),
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
                else -> AccountFailure(
                    stringResource(R.string.account_sign_in_failed),
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }

    // Open only over an account: once it is signed out of or gone, there is nothing to show.
    if (managing && account is AccountState.SignedIn) {
        AccountSheet(
            account = account,
            failed = failure != null,
            busy = busy,
            onSignOut = {
                managing = false
                onSignOut()
            },
            onDelete = { attempt(onDeleteAccount) },
            onClose = { managing = false }
        )
    }
    val signedIn = account is AccountState.SignedIn
    LaunchedEffect(signedIn) { if (!signedIn) managing = false }
}

/**
 * Who is signed in, as the row that opens the account: a letter for a face, the name, and the
 * address that tells one account from another.
 */
@Composable
private fun AccountIdentity(account: AccountState.SignedIn, onClick: () -> Unit) {
    val name = account.name.ifBlank { account.email }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
        // One line each here, where the row has to stay a row: both are in full on the sheet.
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
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * The account, opened: whose it is and the two ways out of it. Signing out is one touch and can
 * be taken back by signing in again; deleting cannot, so it asks first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSheet(
    account: AccountState.SignedIn,
    failed: Boolean,
    busy: Boolean,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        InAppLanguage(language) {
            AccountSheetContent(
                account = account,
                failed = failed,
                busy = busy,
                onSignOut = { scope.launch { sheetState.hide() }.invokeOnCompletion { onSignOut() } },
                onDelete = { confirmingDelete = true }
            )
        }
    }

    if (confirmingDelete) {
        // The dialog is a window of its own too: its texts are read here, in the app's language.
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
                        onDelete()
                    }
                ) {
                    Text(confirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text(cancel) } }
        )
    }
}

/**
 * What [AccountSheet] shows: the name and the address in full, however long, and under them one
 * card with the two ways out, the one that cannot be taken back last and in the colour of a
 * warning. A deletion that failed is said under the row that was touched.
 */
@Composable
internal fun AccountSheetContent(
    account: AccountState.SignedIn,
    failed: Boolean,
    busy: Boolean,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = account.name.ifBlank { account.email }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 32.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (account.name.isNotBlank() && account.email.isNotBlank()) {
            Text(
                text = account.email,
                modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // One card of rows, as on the options page.
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .alpha(if (busy) BUSY_ALPHA else 1f)
        ) {
            ActionRow(
                title = stringResource(R.string.account_sign_out),
                supporting = stringResource(R.string.account_sign_out_hint),
                onClick = onSignOut,
                opens = false,
                enabled = !busy
            )
            RowDivider()
            ActionRow(
                title = stringResource(R.string.account_delete),
                supporting = stringResource(R.string.account_delete_hint),
                onClick = onDelete,
                danger = true,
                opens = false,
                enabled = !busy
            )
        }
        if (failed) {
            AccountFailure(
                stringResource(R.string.account_delete_failed),
                Modifier.padding(start = 32.dp, top = 12.dp, end = 32.dp)
            )
        }
    }
}

/** What did not work, said in the colour of a warning and heard by whoever cannot see it. */
@Composable
private fun AccountFailure(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

/**
 * The button that signs in with Google, in the colours, words and spacing Google sets for one
 * drawn by an app: white with an outline on a light screen, dark with an outline on a dark one.
 * A label too long for one line takes a second, and the button grows to hold it.
 */
@Composable
private fun GoogleButton(enabled: Boolean, onClick: () -> Unit) {
    val dark = MaterialTheme.colorScheme.onSurface.luminance() > 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else BUSY_ALPHA)
            .heightIn(min = 48.dp)
            .clip(GoogleButtonShape)
            .background(if (dark) GoogleDarkGround else GoogleLightGround)
            .border(1.dp, if (dark) GoogleDarkOutline else GoogleLightOutline, GoogleButtonShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        // A label on two lines says it is as wide as the room it was given, which would leave
        // the mark alone at the button's edge: it is held to its longest line instead, so that
        // mark and words stay together in the middle.
        var longestLine by remember { mutableStateOf<Int?>(null) }
        Text(
            text = stringResource(R.string.account_continue_google),
            modifier = Modifier.layout { measurable, constraints ->
                val label = measurable.measure(constraints)
                val width = longestLine?.coerceAtMost(label.width) ?: label.width
                layout(width, label.height) { label.place((width - label.width) / 2, 0) }
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (dark) GoogleDarkInk else GoogleLightInk,
            textAlign = TextAlign.Center,
            onTextLayout = { result ->
                longestLine = (0 until result.lineCount).maxOf { line ->
                    ceil(result.getLineRight(line) - result.getLineLeft(line)).toInt()
                }
            }
        )
    }
}

/** The grounds, outlines and inks Google sets for its sign-in button, light and dark. */
private val GoogleLightGround = Color(0xFFFFFFFF)
private val GoogleLightOutline = Color(0xFF747775)
private val GoogleLightInk = Color(0xFF1F1F1F)
private val GoogleDarkGround = Color(0xFF131314)
private val GoogleDarkOutline = Color(0xFF8E918F)
private val GoogleDarkInk = Color(0xFFE3E3E3)

/** A pill at one line's height, and still round-cornered when the label takes two. */
private val GoogleButtonShape = RoundedCornerShape(24.dp)

/** How faint a control is while the system is still answering the last touch on it. */
private const val BUSY_ALPHA = 0.6f
