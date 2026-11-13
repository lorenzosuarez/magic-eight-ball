package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.common.BrandPalette
import com.example.magiceightball.core.domain.model.AskerFacts
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.account.CharacterSave
import kotlinx.coroutines.launch

/**
 * The sheet where a character of the user's own is made, or corrected: its name, how it answers
 * in their words, and the colour and the accessory it borrows from the app's characters, with
 * the card it will be shared on drawn as they pick. Over a character that exists it is also
 * where that character is deleted, after being asked once more.
 *
 * @param character the one being corrected; null for a new one.
 * @param onDelete deletes [character]; null for a new one, which has nothing to delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CharacterEditorSheet(
    character: CustomCharacter?,
    onSave: suspend (String, String, Magic8BallPersonality, Magic8BallPersonality) -> CharacterSave,
    onDelete: (suspend () -> Boolean)?,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val draft = rememberSaveable(saver = CharacterDraft.Saver) { CharacterDraft(character) }
    var busy by remember { mutableStateOf(false) }
    var trouble by rememberSaveable { mutableStateOf<CharacterTrouble?>(null) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    fun leave() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() }
    }
    // One thing at a time: a second touch while the first is still being answered does nothing.
    fun attempt(work: suspend () -> CharacterTrouble?) {
        if (busy) return
        scope.launch {
            busy = true
            trouble = null
            val failed = try {
                work()
            } finally {
                busy = false
            }
            if (failed == null) leave() else trouble = failed
        }
    }

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
            CharacterEditorContent(
                draft = draft,
                isNew = character == null,
                busy = busy,
                trouble = trouble?.let { stringResource(it.message) },
                onSave = { name, description ->
                    attempt {
                        when (onSave(name, description, draft.colour, draft.emblem)) {
                            is CharacterSave.Saved -> null
                            CharacterSave.Full -> CharacterTrouble.FULL
                            CharacterSave.Changed -> CharacterTrouble.CHANGED
                            CharacterSave.Refused -> CharacterTrouble.REFUSED
                            CharacterSave.Failed -> CharacterTrouble.NOT_SAVED
                        }
                    }
                },
                onCancel = ::leave,
                onDelete = if (onDelete == null) null else ({ confirmingDelete = true })
            )
        }
    }

    if (confirmingDelete && character != null && onDelete != null) {
        // The dialog is a window of its own too: its texts are read here, in the app's language.
        val title = stringResource(R.string.character_delete_title, character.name)
        val text = stringResource(R.string.character_delete_text)
        val confirm = stringResource(R.string.action_delete)
        val cancel = stringResource(R.string.action_cancel)
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingDelete = false
                        attempt { if (onDelete()) null else CharacterTrouble.NOT_DELETED }
                    }
                ) {
                    Text(confirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text(cancel) } }
        )
    }
}

/** What can stand between the editor's buttons and what they do, and how each is said. */
internal enum class CharacterTrouble(val message: Int) {
    NOT_SAVED(R.string.profile_save_failed),
    REFUSED(R.string.character_refused),
    FULL(R.string.character_full),

    /** Changed or deleted on another device since it was opened here. */
    CHANGED(R.string.character_changed),
    NOT_DELETED(R.string.character_delete_failed)
}

/** What is being typed and picked for a character, until it is saved or given up. */
@Stable
internal class CharacterDraft(character: CustomCharacter? = null) {
    var name by mutableStateOf(AskerFacts.cut(character?.name.orEmpty(), CustomCharacter.MAX_NAME_LENGTH))
        private set
    var description by mutableStateOf(AskerFacts.cut(character?.description.orEmpty(), CustomCharacter.MAX_DESCRIPTION_LENGTH))
        private set
    var colour by mutableStateOf(character?.colour ?: FIRST_COLOUR)
    var emblem by mutableStateOf(character?.emblem ?: Magic8BallPersonality.NEUTRAL)

    /** A name is one line: the key for a new one moves on instead. */
    fun typeName(typed: String) {
        name = AskerFacts.cut(typed.replace('\n', ' '), CustomCharacter.MAX_NAME_LENGTH)
    }

    fun typeDescription(typed: String) {
        description = AskerFacts.cut(typed, CustomCharacter.MAX_DESCRIPTION_LENGTH)
    }

    val nameToSave: String? get() = CustomCharacter.nameOrNull(name)
    val descriptionToSave: String? get() = CustomCharacter.descriptionOrNull(description)

    /** How much of the room for a description is taken, counted as it is cut: by character. */
    val descriptionLength: Int get() = description.codePointCount(0, description.length)

    companion object {
        /** What a new character's card is before a colour is picked: the app's own first. */
        private val FIRST_COLOUR = Magic8BallPersonality.SARCASTIC

        val Saver = listSaver<CharacterDraft, String>(
            save = { listOf(it.name, it.description, it.colour.name, it.emblem.name) },
            restore = { (name, description, colour, emblem) ->
                CharacterDraft().apply {
                    typeName(name)
                    typeDescription(description)
                    this.colour = CustomCharacter.LOOKS.find { it.name == colour } ?: FIRST_COLOUR
                    this.emblem = CustomCharacter.LOOKS.find { it.name == emblem } ?: Magic8BallPersonality.NEUTRAL
                }
            }
        )
    }
}

/**
 * What [CharacterEditorSheet] shows. From the top: the card as it will be shared, which follows
 * every pick; the two things typed; the two things picked; what is sent of them, said before the
 * button that saves; and the ways out.
 *
 * @param trouble what the last touch of a button could not do, if anything.
 * @param onDelete asks to delete the character; null for a new one.
 */
@Composable
internal fun CharacterEditorContent(
    draft: CharacterDraft,
    isNew: Boolean,
    busy: Boolean,
    trouble: String?,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val name = draft.nameToSave
    val description = draft.descriptionToSave
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
            text = stringResource(if (isNew) R.string.character_new_title else R.string.character_edit_title),
            modifier = Modifier.padding(horizontal = 32.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.character_hint),
            modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CharacterPreview(draft)
        OutlinedTextField(
            value = draft.name,
            onValueChange = draft::typeName,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp).fillMaxWidth(),
            enabled = !busy,
            singleLine = true,
            label = { Text(stringResource(R.string.character_name)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            shape = ButtonShape
        )
        OutlinedTextField(
            value = draft.description,
            onValueChange = draft::typeDescription,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp).fillMaxWidth(),
            enabled = !busy,
            minLines = DESCRIPTION_LINES,
            label = { Text(stringResource(R.string.character_description)) },
            placeholder = { Text(stringResource(R.string.character_description_example)) },
            // How much room is left, where the typing is.
            supportingText = {
                Text(
                    text = "${draft.descriptionLength}/${CustomCharacter.MAX_DESCRIPTION_LENGTH}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = ButtonShape
        )
        PickerLabel(R.string.character_colour)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            CustomCharacter.LOOKS.forEach { look ->
                val chosen = look == draft.colour
                val label = stringResource(look.labelRes())
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .selectable(selected = chosen, enabled = !busy, role = Role.RadioButton) { draft.colour = look }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center
                ) {
                    // The colour, and round it the ring that says it is the one.
                    Box(
                        modifier = Modifier
                            .size(if (chosen) 44.dp else 36.dp)
                            .then(
                                if (chosen) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(if (chosen) 32.dp else 36.dp)
                                .clip(CircleShape)
                                .background(shareColor(look))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                    }
                }
            }
        }
        PickerLabel(R.string.character_emblem)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nothing first, then what each of the app's characters wears: the plain one wears nothing.
            (listOf(Magic8BallPersonality.NEUTRAL) + (CustomCharacter.LOOKS - Magic8BallPersonality.NEUTRAL)).forEach { look ->
                val chosen = look == draft.emblem
                val label = stringResource(if (look == Magic8BallPersonality.NEUTRAL) R.string.character_no_emblem else look.labelRes())
                Box(
                    modifier = Modifier
                        .size(EMBLEM_TILE)
                        .clip(ButtonShape)
                        // On the colour picked, which is light in both themes: the ball is drawn in ink.
                        .background(shareColor(draft.colour))
                        .then(
                            if (chosen) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, ButtonShape) else Modifier
                        )
                        .selectable(selected = chosen, enabled = !busy, role = Role.RadioButton) { draft.emblem = look }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center
                ) {
                    CharacterBall(emblem = look, size = EMBLEM_TILE - 8.dp)
                }
            }
        }
        // Before the button, and in full: what saving it sends, and when.
        Text(
            text = stringResource(R.string.character_sent),
            modifier = Modifier.padding(start = 32.dp, top = 20.dp, end = 32.dp),
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (trouble != null) {
            Text(
                text = trouble,
                modifier = Modifier
                    .padding(start = 32.dp, top = 12.dp, end = 32.dp)
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Heading),
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = { if (name != null && description != null) onSave(name, description) },
            modifier = Modifier
                .padding(start = 16.dp, top = if (trouble != null) 8.dp else 16.dp, end = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = !busy && name != null && description != null,
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(BrandPalette.ACCENT_DEEP),
                contentColor = Color(BrandPalette.ON_ACCENT)
            )
        ) {
            Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
            enabled = !busy,
            shape = ButtonShape
        ) {
            Text(stringResource(R.string.action_cancel), textAlign = TextAlign.Center)
        }
        if (onDelete != null) {
            // Last, and in the colour of a warning: what cannot be taken back.
            TextButton(
                onClick = onDelete,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
                enabled = !busy,
                shape = ButtonShape,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.character_delete), textAlign = TextAlign.Center)
            }
        }
    }
}

/** The card as it will be shared, small: the colour, the ball with what it wears, and the name that signs. */
@Composable
private fun CharacterPreview(draft: CharacterDraft) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(CardShape)
            .background(shareColor(draft.colour))
            .padding(start = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CharacterBall(emblem = draft.emblem, size = PREVIEW_BALL)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // In the card's ink, which is the same in both themes: the card is always light.
            Text(
                text = "— " + (draft.nameToSave ?: stringResource(R.string.character_unnamed)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = ShareInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.character_preview),
                style = MaterialTheme.typography.bodySmall,
                color = ShareInk.copy(alpha = PREVIEW_NOTE_ALPHA)
            )
        }
    }
}

/** What the row of picks under it is for, named as the date is on the profile. */
@Composable
private fun PickerLabel(text: Int) {
    Text(
        text = stringResource(text),
        modifier = Modifier.padding(start = 32.dp, top = 16.dp, end = 32.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private const val DESCRIPTION_LINES = 3
private val PREVIEW_BALL = 104.dp
private val EMBLEM_TILE = 72.dp
private const val PREVIEW_NOTE_ALPHA = 0.7f
