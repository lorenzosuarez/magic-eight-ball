package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.account.AccountOutcome
import com.lorenzosuarez.magiceightball.phone.account.AccountState
import com.lorenzosuarez.magiceightball.phone.account.Bought
import com.lorenzosuarez.magiceightball.phone.account.CharacterSave
import com.lorenzosuarez.magiceightball.phone.account.ProfileState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * What a page asks of [CharacterFlows]. The pages only say what is wanted; everything that
 * follows from it is the flows' own.
 */
@Stable
internal class FlowsAsked(browsing: Boolean = false) {
    /** The characters, to choose among: their sheet is up for as long as this is so. */
    var browsing by mutableStateOf(browsing)

    /** What is sold, asked for from a page: taken up at once, and then no longer asked. */
    var premium by mutableStateOf(false)
}

/** What is asked of the flows, kept through a turn of the screen as far as it is a sheet that is up. */
@Composable
internal fun rememberFlowsAsked(): FlowsAsked =
    rememberSaveable(saver = Saver<FlowsAsked, Boolean>({ it.browsing }, { FlowsAsked(it) })) { FlowsAsked() }

/**
 * Choosing who answers, making and correcting characters of one's own, and buying what is sold:
 * the sheets and the notices these lead through. They are kept together because each leads to
 * the others, and apart from any page, so that the ball's page and the options can both ask for
 * them and nothing is said twice.
 */
@Composable
internal fun CharacterFlows(
    state: EightBallUiState,
    asked: FlowsAsked,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit,
    onSignIn: suspend () -> AccountOutcome = { AccountOutcome.FAILED },
    onChooseCharacter: (CustomCharacter) -> Unit = {},
    onSaveCharacter: suspend (CustomCharacter?, String, String, Magic8BallPersonality, Magic8BallPersonality) -> CharacterSave =
        { _, _, _, _, _ -> CharacterSave.Failed },
    onDeleteCharacter: suspend (String) -> Boolean = { false },
    onBuyPremium: suspend () -> Bought = { Bought.FAILED }
) {
    // Which character of the user's own is open in the editor: its slot, or [NEW] for one being made.
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by rememberSaveable { mutableStateOf<CharactersNotice?>(null) }
    // What is sold is being shown, and what for: null while it is not.
    var paying by rememberSaveable { mutableStateOf<PremiumFor?>(null) }
    val scope = rememberCoroutineScope()
    val signedIn = state.account is AccountState.SignedIn
    val snackbars = LocalSnackbars.current
    val premiumMessage = stringResource(R.string.account_premium)
    // What is sold was asked for with nobody signed in, and they signed in for it: it is shown
    // once the account is whole and known not to hold it, and forgotten if they sign out first.
    var wantsPremium by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(signedIn) { if (!signedIn) wantsPremium = false }
    LaunchedEffect(wantsPremium, signedIn, state.profile, state.premiumKnown) {
        if (wantsPremium && signedIn && state.profile is ProfileState.Present && state.premiumKnown) {
            wantsPremium = false
            if (state.offersPremium && paying == null && editing == null) paying = PremiumFor.EVERYTHING
        }
    }
    // What is sold, asked for from a page: taken up once. It is kept with an account, so whoever
    // has none is first shown the way to one.
    LaunchedEffect(asked.premium) {
        if (asked.premium) {
            asked.premium = false
            if (signedIn) paying = PremiumFor.EVERYTHING else notice = CharactersNotice.PREMIUM_ACCOUNT
        }
    }
    // Whether what is kept with the account, its profile and its characters, has been heard of.
    val accountKnown = state.profile != ProfileState.Unknown && state.characters != null
    // What "Create" does once that is known: said here once, for a touch now or one that waited.
    val create = {
        val kept = state.characters
        when {
            !state.canMakeCharacters || kept == null -> notice = CharactersNotice.UNKNOWN
            kept.size >= CustomCharacter.MAX_COUNT -> notice = CharactersNotice.FULL
            // Last: nobody is asked to pay for what they could not have made anyway.
            state.mustPayToCreate -> paying = PremiumFor.CHARACTERS
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
            if (editing == null && paying == null && state.profile != ProfileState.Missing) createNow()
        }
    }
    if (asked.browsing) {
        StylesSheet(
            selected = state.settings.personality,
            hidden = state.config.hiddenStyles,
            onSelected = onPersonalitySelected,
            onClose = {
                asked.browsing = false
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
    paying?.let { wanted ->
        // Bought, here or wherever a payment that was pending came through: on to what they
        // wanted, which for whoever only asked what is sold is the page they were on.
        val goOn = {
            // Once, though it may be told both by the purchase and by the account.
            if (paying != null) {
                paying = null
                if (wanted == PremiumFor.CHARACTERS) {
                    editing = NEW
                } else {
                    // Back on the page with nothing opened for them: they are told it is theirs.
                    scope.launch {
                        snackbars?.currentSnackbarData?.dismiss()
                        snackbars?.showSnackbar(premiumMessage, duration = SnackbarDuration.Short)
                    }
                }
            }
        }
        // No longer sold, or not yet known to be, as when the app has just come back: the sheet
        // goes, and nothing opens that was not paid for. Whoever wants to make one asks again.
        LaunchedEffect(state.config.premiumEnabled) { if (!state.config.premiumEnabled) paying = null }
        PremiumSheet(
            offer = state.offer,
            wanted = wanted,
            held = state.premium,
            onBuy = onBuyPremium,
            onBought = goOn,
            onClose = { paying = null }
        )
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
        // What stands in the way is that nobody is signed in: the way there is offered.
        val signsIn = what == CharactersNotice.ACCOUNT || what == CharactersNotice.PREMIUM_ACCOUNT
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                if (signsIn) {
                    TextButton(
                        onClick = {
                            notice = null
                            scope.launch {
                                when (onSignIn()) {
                                    // What they signed in for is not forgotten on the way.
                                    AccountOutcome.DONE -> if (what == CharactersNotice.PREMIUM_ACCOUNT) wantsPremium = true
                                    // Backing out of Google's own question needs no telling.
                                    AccountOutcome.CANCELLED -> Unit
                                    AccountOutcome.NO_GOOGLE_ACCOUNT -> notice = CharactersNotice.NO_GOOGLE
                                    else -> notice = CharactersNotice.NO_SIGN_IN
                                }
                            }
                        }
                    ) { Text(go) }
                } else {
                    TextButton(onClick = { notice = null }) { Text(understood) }
                }
            },
            dismissButton = if (signsIn) {
                { TextButton(onClick = { notice = null }) { Text(later) } }
            } else null
        )
    }
}

/** What making a character, or buying what is sold, can run into before its sheet opens, and how each is said. */
internal enum class CharactersNotice(@StringRes val title: Int, @StringRes val text: Int) {
    /** Nobody is signed in: the way to an account. */
    ACCOUNT(R.string.characters_account_title, R.string.characters_account_text),

    /** Signed in, and what is kept with the account has not been heard of. */
    UNKNOWN(R.string.characters_unknown_title, R.string.characters_unknown_text),

    /** Every slot is taken. */
    FULL(R.string.characters_full_title, R.string.characters_full_text),

    /** The way to an account was taken, and led nowhere. */
    NO_SIGN_IN(R.string.characters_no_sign_in_title, R.string.characters_unknown_text),

    /** It led nowhere because the device has no Google account to sign in with. */
    NO_GOOGLE(R.string.characters_no_sign_in_title, R.string.characters_no_google_text),

    /** What is sold was asked for with nobody signed in: it is kept with an account. */
    PREMIUM_ACCOUNT(R.string.premium_account_title, R.string.premium_account_text)
}

/** What [CharacterFlows] calls the editor open on nobody yet. */
private const val NEW = ""

/** How long a touch on "Create" waits for an account that is on its way before saying it has not come. */
private const val ACCOUNT_PATIENCE_MS = 6_000L

/** Before the characters of the account have been heard of once: how many there are is not known. */
private const val UNHEARD_OF = -1
