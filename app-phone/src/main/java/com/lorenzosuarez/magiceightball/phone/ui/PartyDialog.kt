package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.UserSettings
import com.lorenzosuarez.magiceightball.phone.R

/**
 * The party as it is being put together: the names so far and what is in the field.
 *
 * Kept apart from the dialog that shows it so that what it does can be tried without one.
 */
@Stable
internal class PartyDraft(names: List<String>, typed: String = "") {
    var players by mutableStateOf(names)
        private set
    var typed by mutableStateOf(typed)
        private set

    /** Whether there is room for no more: the field then has nothing left to do. */
    val full: Boolean get() = players.size >= UserSettings.MAX_PARTY

    /** Whether what is in the field would add anyone. */
    val canAdd: Boolean get() = !full && typed.isNotBlank()

    /** Saving needs a party; a name still in the field counts, since nobody means to leave it out. */
    val canSave: Boolean get() = toSave().size >= UserSettings.MIN_PARTY

    /** What is being typed. A comma, a semicolon or a new line ends a name, as when a list is pasted. */
    fun type(value: String) {
        typed = value
        if (value.any { it == ',' || it == ';' || it == '\n' }) add()
    }

    /** Takes what is in the field as one more name, or several, and leaves the field empty. */
    fun add() {
        players = toSave()
        typed = ""
    }

    fun remove(name: String) {
        players = players - name
    }

    /**
     * The names with what is in the field: tidied, none twice, never more than a party holds, and
     * each with its capital, whatever the keyboard made of it.
     */
    fun toSave(): List<String> = UserSettings.parsePartyNames((players + typed).joinToString(","))
        .map { name -> name.replaceFirstChar { it.titlecase() } }

    companion object {
        val Saver = listSaver<PartyDraft, String>(
            save = { listOf(it.typed) + it.players },
            restore = { PartyDraft(names = it.drop(1), typed = it.first()) }
        )
    }
}

/**
 * Who is playing, one name at a time: a name is typed and added with the keyboard's own key, so
 * a group goes in without leaving the keyboard, and each name then stands on its own where it
 * can be seen and taken off with a touch.
 *
 * While a party is on, "Turn off" ends it in one touch, and touching outside changes nothing.
 */
@Composable
internal fun PartyDialog(names: List<String>, onSave: (List<String>) -> Unit, onDismiss: () -> Unit) {
    val draft = rememberSaveable(saver = PartyDraft.Saver) { PartyDraft(names) }
    // The dialog is a window of its own, which starts again from the device's language: every
    // text in it is read in the app's.
    val language = rememberAppLanguage()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { InAppLanguage(language) { Text(stringResource(R.string.settings_party)) } },
        // Open on the keyboard: there is nothing to do here but type a name.
        text = { InAppLanguage(language) { PartyFields(draft, focusOnOpen = true) } },
        confirmButton = {
            InAppLanguage(language) {
                TextButton(onClick = { onSave(draft.toSave()) }, enabled = draft.canSave) {
                    Text(stringResource(R.string.action_save))
                }
            }
        },
        dismissButton = {
            InAppLanguage(language) {
                if (names.isEmpty()) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                } else {
                    TextButton(onClick = { onSave(emptyList()) }) { Text(stringResource(R.string.party_turn_off)) }
                }
            }
        }
    )
}

/** What the dialog holds: what the game is, the field, the players so far and how many are still needed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PartyFields(draft: PartyDraft, focusOnOpen: Boolean, modifier: Modifier = Modifier) {
    val field = remember { FocusRequester() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.party_dialog_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = draft.typed,
            onValueChange = draft::type,
            modifier = Modifier.fillMaxWidth().focusRequester(field),
            enabled = !draft.full,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.party_name_hint)) },
            trailingIcon = {
                TextButton(onClick = draft::add, enabled = draft.canAdd) {
                    Text(stringResource(R.string.action_add))
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            // Adding keeps the keyboard up, ready for the next name.
            keyboardActions = KeyboardActions(onDone = { draft.add() })
        )
        // Asked for from here, where the field already exists, and not for a party that is full.
        LaunchedEffect(Unit) { if (focusOnOpen && !draft.full) field.requestFocus() }
        if (draft.players.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                draft.players.forEach { name ->
                    PlayerChip(name = name, onRemove = { draft.remove(name) })
                }
            }
        }
        // What is still missing, or how many are in: said again as it changes.
        Text(
            text = if (draft.players.size < UserSettings.MIN_PARTY) {
                stringResource(R.string.party_need_more)
            } else {
                stringResource(R.string.party_count, draft.players.size, UserSettings.MAX_PARTY)
            },
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A player: the name and, after it, the cross that says a touch takes it off. */
@Composable
private fun PlayerChip(name: String, onRemove: () -> Unit) {
    val remove = stringResource(R.string.party_remove, name)
    Row(
        modifier = Modifier
            .clip(ButtonShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TILE_ALPHA))
            .clickable(onClickLabel = remove, role = Role.Button, onClick = onRemove)
            .heightIn(min = 40.dp)
            .padding(start = 14.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
