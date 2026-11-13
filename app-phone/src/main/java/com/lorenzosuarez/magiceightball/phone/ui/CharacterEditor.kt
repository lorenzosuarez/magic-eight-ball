package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var description by mutableStateOf(paragraph(character?.description.orEmpty()))
        private set
    var colour by mutableStateOf(character?.colour ?: FIRST_COLOUR)
    var emblem by mutableStateOf(character?.emblem ?: Magic8BallPersonality.NEUTRAL)

    /** A name is one line: the key for a new one moves on instead. */
    fun typeName(typed: String) {
        name = AskerFacts.cut(typed.replace('\n', ' '), CustomCharacter.MAX_NAME_LENGTH)
    }

    /** A description is one paragraph: the key for a new line says it is done instead. */
    fun typeDescription(typed: String) {
        description = paragraph(typed)
    }

    val nameToSave: String? get() = CustomCharacter.nameOrNull(name)
    val descriptionToSave: String? get() = CustomCharacter.descriptionOrNull(description)

    /** How much of the room for a description is taken, counted as it is cut: by character. */
    val descriptionLength: Int get() = description.codePointCount(0, description.length)
    val nameLength: Int get() = name.codePointCount(0, name.length)

    companion object {
        /** What a new character's card is before a colour is picked: the app's own first. */
        private val FIRST_COLOUR = Magic8BallPersonality.SARCASTIC

        /** Whatever breaks a line: the pair of them that ends one on some systems is one break. */
        private val LINE_BREAK = Regex("\\r\\n|[\\n\\u000B\\u000C\\r\\u0085\\u2028\\u2029]")

        /** [text] as the one paragraph a description is: a line break in it, pasted or kept from before, becomes a space and is counted as one. */
        private fun paragraph(text: String) =
            AskerFacts.cut(text.replace(LINE_BREAK, " "), CustomCharacter.MAX_DESCRIPTION_LENGTH)

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
    val focus = LocalFocusManager.current
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
            supportingText = { Counter(draft.nameLength, CustomCharacter.MAX_NAME_LENGTH) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            shape = ButtonShape
        )
        // On a keyboard with keys, in the description: the key that would break the line is
        // done, as on the screen's, and the key that would type a gap goes on to the picks.
        val keys = Modifier.onPreviewKeyEvent { pressed ->
            when {
                pressed.type != KeyEventType.KeyDown -> false
                pressed.key == Key.Enter || pressed.key == Key.NumPadEnter -> {
                    focus.clearFocus()
                    true
                }
                pressed.key == Key.Tab -> {
                    focus.moveFocus(if (pressed.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
                    true
                }
                else -> false
            }
        }
        // Named over the field and not inside it, so that the example of what to write shows
        // for as long as nothing is written.
        PickerLabel(stringResource(R.string.character_description), top = 4.dp)
        OutlinedTextField(
            value = draft.description,
            onValueChange = draft::typeDescription,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().then(keys),
            enabled = !busy,
            minLines = DESCRIPTION_MIN_LINES,
            // No taller than this: more is scrolled inside it, not the whole sheet with it.
            maxLines = DESCRIPTION_MAX_LINES,
            placeholder = { Text(stringResource(R.string.character_description_example)) },
            supportingText = { Counter(draft.descriptionLength, CustomCharacter.MAX_DESCRIPTION_LENGTH) },
            // One paragraph, and the last thing typed: the key that would break the line says done
            // instead, and done leaves the field, which takes the keyboard from over the picks.
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            shape = ButtonShape
        )
        PickerLabel(stringResource(R.string.character_colour), top = 4.dp)
        ColourRow(chosen = draft.colour, enabled = !busy, onChosen = { draft.colour = it })
        // What is worn has a name, said here: the drawings are small, and some need it.
        val wears = stringResource(if (draft.emblem == Magic8BallPersonality.NEUTRAL) R.string.character_no_emblem else draft.emblem.labelRes())
        PickerLabel(stringResource(R.string.character_emblem) + " · " + wears)
        EmblemRow(chosen = draft.emblem, colour = draft.colour, enabled = !busy, onChosen = { draft.emblem = it })
        // Before the button, and in full: where saving it keeps it, and where it is sent.
        Text(
            text = stringResource(R.string.character_sent),
            modifier = Modifier.padding(start = 32.dp, top = 20.dp, end = 32.dp),
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val ready = name != null && description != null
        // Why the button waits, said while it does: a button that will not be touched says nothing.
        val waiting = stringResource(R.string.character_incomplete).takeIf { !ready && trouble == null }
        val said = trouble ?: waiting
        // Its room is kept when there is nothing to say, so that the buttons under it stay where
        // they are as the last letter makes the draft whole: unseen then, and unheard.
        Text(
            text = said ?: stringResource(R.string.character_incomplete),
            modifier = Modifier
                .padding(start = 32.dp, top = 12.dp, end = 32.dp)
                .fillMaxWidth()
                .alpha(if (said == null) 0f else 1f)
                .then(
                    if (said == null) {
                        Modifier.clearAndSetSemantics {}
                    } else {
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    }
                ),
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Heading),
            color = if (trouble != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = { if (name != null && description != null) onSave(name, description) },
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = !busy && ready,
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(BrandPalette.ACCENT_DEEP),
                contentColor = Color(BrandPalette.ON_ACCENT)
            )
        ) {
            BusyRing(busy)
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

/** How much of the room is taken, at the end of the line under a field: in the colour of a warning once it is all taken. */
@Composable
private fun Counter(length: Int, most: Int) {
    Text(
        text = "$length/$most",
        modifier = Modifier.fillMaxWidth(),
        color = if (length >= most) MaterialTheme.colorScheme.error else Color.Unspecified,
        textAlign = TextAlign.End
    )
}

/**
 * The card as it will be shared, small: the colour, the ball with what it wears, and the name
 * that signs it, in the hand the card signs in.
 */
@Composable
private fun CharacterPreview(draft: CharacterDraft) {
    // A ball that wears nothing sits in the middle; one that does leaves the room over it.
    val lift by animateDpAsState(
        targetValue = if (draft.emblem == Magic8BallPersonality.NEUTRAL) PREVIEW_BARE_LIFT else 0.dp,
        // In step with the cross-fade of what it wears: neither is still going when the other is done.
        animationSpec = tween(CHOICE_MS),
        label = "preview ball"
    )
    // The card follows a pick as the mark of the pick arrives: one colour passes into the next.
    val ground by animateColorAsState(shareColor(draft.colour), tween(CHOICE_MS), label = "preview colour")
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(CardShape)
            .background(ground)
            .padding(start = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // So does what the ball wears. What was picked comes over what was worn, which only then
        // goes: fading both at once would let the card show through the ball between the two.
        AnimatedContent(
            targetState = draft.emblem,
            // Moved where it is drawn, not where it is measured: the card keeps its height as the ball rises.
            modifier = Modifier.offset { IntOffset(0, -lift.roundToPx()) },
            transitionSpec = { fadeIn(tween(CHOICE_MS / 2)) togetherWith fadeOut(tween(CHOICE_MS / 2, delayMillis = CHOICE_MS / 2)) },
            label = "preview emblem"
        ) { worn ->
            CharacterBall(emblem = worn, size = PREVIEW_BALL)
        }
        // Clear of the card's edges when a long name and large type make it four lines.
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // In the card's ink, which is the same in both themes: the card is always light.
            val named = draft.nameToSave
            Text(
                text = "— " + (named ?: stringResource(R.string.character_unnamed)),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp,
                    lineBreak = LineBreak.Heading
                ),
                // Fainter until there is a name: it is not one yet.
                color = ShareInk.copy(alpha = if (named == null) UNNAMED_ALPHA else 1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.character_preview),
                style = MaterialTheme.typography.bodySmall,
                color = ShareInk
            )
        }
    }
}

/**
 * The colours to pick among: those of the app's characters, as dots in a row that opens on the
 * one picked and always shows half of the next, which is what says there are more.
 */
@Composable
private fun ColourRow(chosen: Magic8BallPersonality, enabled: Boolean, onChosen: (Magic8BallPersonality) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val whole = ((maxWidth - PICK_EDGE) / (SWATCH + SWATCH_LEAST_GAP) - 0.5f).toInt().coerceAtLeast(1)
        val step = (maxWidth - PICK_EDGE - SWATCH / 2) / whole
        val at = COLOURS.indexOf(chosen).coerceAtLeast(0)
        val start = with(LocalDensity.current) { (step * (at - 1).coerceAtLeast(0)).roundToPx() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState(start))
                .padding(start = PICK_EDGE)
                .selectableGroup()
        ) {
            OnCardColours {
                COLOURS.forEach { look ->
                    val picked = look == chosen
                    val label = stringResource(look.labelRes())
                    val touch = remember { MutableInteractionSource() }
                    val mark by animateFloatAsState(if (picked) 1f else 0f, tween(CHOICE_MS), label = "colour picked")
                    // The touch takes the dot and the gap after it: the dots stay where the edge is.
                    // The light of the touch is the dot's alone: to the eye, it is what was touched.
                    Box(
                        modifier = Modifier
                            .size(width = step, height = 48.dp)
                            .selectable(selected = picked, interactionSource = touch, indication = null, enabled = enabled, role = Role.RadioButton) { onChosen(look) }
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // The same size picked or not: picked, it is a ring with the colour inside it.
                        // The ring comes as the colour draws in from it, and goes as it fills out again.
                        Box(
                            modifier = Modifier
                                .size(SWATCH)
                                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = mark), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                Modifier
                                    .size(SWATCH - 12.dp * mark)
                                    .clip(CircleShape)
                                    .background(shareColor(look))
                                    .indication(touch, PressHighlight)
                                    // The palest of them would be lost on a pale sheet without an edge.
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = SWATCH_EDGE_ALPHA), CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * What the ball can wear: nothing, or what one of the app's characters wears, drawn on the
 * colour picked, which is light in both themes. The ones that read best come first, the row
 * opens on the one picked, and half of the next always shows.
 */
@Composable
private fun EmblemRow(
    chosen: Magic8BallPersonality,
    colour: Magic8BallPersonality,
    enabled: Boolean,
    onChosen: (Magic8BallPersonality) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val whole = if (maxWidth >= WIDE_ROW) 4 else 3
        val tile = (maxWidth - PICK_EDGE - EMBLEM_GAP * whole) / (whole + 0.5f)
        val at = EMBLEMS.indexOf(chosen).coerceAtLeast(0)
        val start = with(LocalDensity.current) { ((tile + EMBLEM_GAP) * (at - 1).coerceAtLeast(0)).roundToPx() }
        // The tiles take the colour picked as the card over them does, and with it.
        val ground by animateColorAsState(shareColor(colour), tween(CHOICE_MS), label = "emblem ground")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState(start))
                .padding(horizontal = PICK_EDGE)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(EMBLEM_GAP)
        ) {
            OnCardColours {
                EMBLEMS.forEach { look ->
                    val picked = look == chosen
                    val label = stringResource(if (look == Magic8BallPersonality.NEUTRAL) R.string.character_no_emblem else look.labelRes())
                    val touch = remember { MutableInteractionSource() }
                    val mark by animateFloatAsState(if (picked) 1f else 0f, tween(CHOICE_MS), label = "emblem picked")
                    // Picked: a ring at the tile's own edge, in the text's colour, and the tile a little
                    // inside it. A ring drawn on the tile would be lost on the lighter colours. The
                    // ring comes as the tile draws in from it, and goes as the tile fills out again.
                    Box(
                        modifier = Modifier
                            .size(tile)
                            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = mark), EmblemRing)
                            .clip(EmblemRing)
                            .selectable(selected = picked, interactionSource = touch, indication = null, enabled = enabled, role = Role.RadioButton) { onChosen(look) }
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center
                    ) {
                        val inside = tile - 10.dp * mark
                        // The light of the touch is drawn here, over the tile's colour: behind it, it would not show.
                        Box(
                            modifier = Modifier.size(inside).clip(ButtonShape).background(ground).indication(touch, PressHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            CharacterBall(emblem = look, size = inside)
                        }
                    }
                }
            }
        }
    }
}

/**
 * What is picked among is drawn on the card's colours, which are light in both themes: the
 * light of a touch on them is the card's ink, where the sheet's own would not show in the dark.
 */
@Composable
private fun OnCardColours(content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalContentColor provides ShareInk, content = content)

/** What the row of picks, or the field, under it is for: named as the date is on the profile. */
@Composable
private fun PickerLabel(text: String, top: androidx.compose.ui.unit.Dp = 16.dp) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 32.dp, top = top, end = 32.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private const val DESCRIPTION_MIN_LINES = 3
private const val DESCRIPTION_MAX_LINES = 5
private val PREVIEW_BALL = 104.dp
private val PREVIEW_BARE_LIFT = 10.dp
private const val UNNAMED_ALPHA = 0.5f

/** Where a row of picks starts, as the cards and the fields do. */
private val PICK_EDGE = 16.dp
private val SWATCH = 40.dp
private val SWATCH_LEAST_GAP = 12.dp
private const val SWATCH_EDGE_ALPHA = 0.24f
private val EMBLEM_GAP = 8.dp
private val WIDE_ROW = 400.dp

/** Round the picked tile: the tile's own corners, a little wider for standing outside it. */
private val EmblemRing = RoundedCornerShape(13.dp)

/**
 * The colours offered: those of the app's characters, but one of two that are told apart only
 * side by side.
 */
private val COLOURS = CustomCharacter.LOOKS - Magic8BallPersonality.PHILOSOPHER

/** Nothing first, then what each of the app's characters wears: those that read best at this size before the rest. */
private val EMBLEMS = listOf(
    Magic8BallPersonality.NEUTRAL,
    Magic8BallPersonality.CAT,
    Magic8BallPersonality.DRAMA,
    Magic8BallPersonality.PIRATE,
    Magic8BallPersonality.DETECTIVE,
    Magic8BallPersonality.VILLAIN,
    Magic8BallPersonality.ZOOMER,
    Magic8BallPersonality.ROBOT,
    Magic8BallPersonality.COACH,
    Magic8BallPersonality.POET,
    Magic8BallPersonality.THERAPIST,
    Magic8BallPersonality.CONSPIRACY,
    Magic8BallPersonality.DOG,
    Magic8BallPersonality.LOVESTRUCK,
    Magic8BallPersonality.ASTRO,
    Magic8BallPersonality.MYSTICAL,
    Magic8BallPersonality.PHILOSOPHER,
    Magic8BallPersonality.SARCASTIC,
    Magic8BallPersonality.GRANDMA
)
