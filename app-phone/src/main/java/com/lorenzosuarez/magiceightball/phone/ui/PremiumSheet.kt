package com.lorenzosuarez.magiceightball.phone.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magiceightball.core.common.BrandPalette
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.account.Bought
import com.lorenzosuarez.magiceightball.phone.account.Offer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * What is shown to someone who wants what is sold and has not paid for it: what paying brings,
 * what it costs and how it is paid, the way to it, and the way back. It is never shown while
 * nothing is sold.
 *
 * @param wanted what brought them here, which is said first.
 * @param held whether it has become theirs meanwhile, here or wherever a payment that was
 * pending came through: the sheet then goes as if it had just been bought.
 * @param offer what it costs, as the store has it; null while the store has not said, and then
 * nothing can be bought.
 * @param onBuy the user wants it: shows Google Play's own screen, and tells how that ended.
 * @param onBought it is theirs: what they came for can go on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumSheet(
    offer: Offer?,
    onBuy: suspend () -> Bought,
    onBought: () -> Unit,
    onClose: () -> Unit,
    wanted: PremiumFor = PremiumFor.CHARACTERS,
    held: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    var busy by remember { mutableStateOf(false) }
    // How the last try ended, when that is worth saying: kept through a turn of the screen.
    var ended by rememberSaveable { mutableStateOf<Bought?>(null) }
    // A price that is slow to come is not for ever on its way: after a while it is said to be missing.
    var priceLate by remember { mutableStateOf(false) }
    LaunchedEffect(offer == null) {
        priceLate = false
        if (offer == null) {
            delay(PRICE_LATE_MS)
            priceLate = true
        }
    }
    // Theirs now: the sheet goes down as it does when closed, and then what they came for goes on.
    LaunchedEffect(held) {
        if (held) {
            sheetState.hide()
            onBought()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetGround,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        InAppLanguage(language) {
            PremiumContent(
                price = offer?.price,
                wanted = wanted,
                terms = stringResource(R.string.premium_terms),
                busy = busy,
                ended = ended,
                priceLate = priceLate,
                onUpgrade = {
                    // One thing at a time: a second touch while the first is being answered does nothing.
                    if (!busy) {
                        // Before anything is started: two touches in one frame are still one.
                        busy = true
                        scope.launch {
                            ended = null
                            val outcome = try {
                                onBuy()
                            } finally {
                                busy = false
                            }
                            when (outcome) {
                                Bought.DONE -> {
                                    sheetState.hide()
                                    onBought()
                                }
                                // Backing out of Google Play's own screen needs no telling.
                                Bought.CANCELLED -> Unit
                                else -> ended = outcome
                            }
                        }
                    }
                },
                onNotNow = { scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() } }
            )
        }
    }
}

/**
 * What [PremiumSheet] shows: what is offered in a line, what it comes to as a list that plainly
 * is not buttons, what stays free and how it is paid, said before the button, and under the
 * button the way back.
 *
 * After a try that did not end with it bought, the buttons are those that still lead somewhere:
 * a payment Google Play is waiting for is only to be understood, one made and not yet checked
 * is to be checked again, and one held by another account cannot be bought here at all.
 *
 * @param price what it costs, as the store words it; null until the store has said.
 * @param terms how it is paid, in a sentence. Nothing can be bought until both are said:
 * without either the button names no price and does not answer.
 * @param busy while a purchase is being made or checked: nothing here answers meanwhile.
 * @param ended how the last try ended, when there is something to say of it.
 * @param priceLate whether the store has been asked its price for long enough to say so.
 * @param wanted what brought them here: its title, and the order of what paying brings.
 */
@Composable
internal fun PremiumContent(
    price: String?,
    onUpgrade: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    terms: String? = null,
    busy: Boolean = false,
    ended: Bought? = null,
    priceLate: Boolean = false,
    wanted: PremiumFor = PremiumFor.CHARACTERS
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(wanted.title),
            // Without a line under it, the title keeps the list at the same distance by itself.
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp, bottom = if (wanted.hint == null) 16.dp else 0.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
            fontWeight = FontWeight.SemiBold
        )
        if (wanted.hint != null) {
            Text(
                text = stringResource(wanted.hint),
                modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp, bottom = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // The tick grows with the type, so it stays in the middle of the first line of its point.
        val (tickInset, tickSize) = with(LocalDensity.current) { 2.sp.toDp() to 20.sp.toDp() }
        // A list, ticked: these say, and do nothing. Not the rows of the other sheets, which are touched.
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.sheetCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            wanted.points.forEach { point ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.padding(top = tickInset).size(tickSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(point),
                        style = MaterialTheme.typography.bodyLarge.copy(lineBreak = LineBreak.Heading)
                    )
                }
            }
        }
        // Before the button: what saying no costs, which is nothing.
        Text(
            text = stringResource(wanted.note),
            modifier = Modifier.padding(start = 32.dp, top = 16.dp, end = 32.dp),
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // And how it is paid, right over the button and as plain as the rest: it is not small print.
        if (terms != null) {
            Text(
                text = terms,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph)
            )
        }
        // A price is an offer only with its terms beside it.
        val offer = price?.takeIf { terms != null }
        // What is going on, how the last try ended, or why the button does not answer: heard by
        // whoever cannot see it.
        val said = when {
            busy -> R.string.premium_busy
            ended != null -> ended.message
            offer != null -> null
            priceLate -> R.string.premium_price_failed
            else -> R.string.premium_no_price
        }
        if (said != null) {
            // Nothing went wrong with a payment that is on its way: it is news, not a warning.
            val news = !busy && ended == Bought.PENDING
            val wrong = !busy && !news && (ended != null || priceLate)
            Text(
                text = stringResource(said),
                modifier = Modifier
                    .padding(start = 32.dp, top = 12.dp, end = 32.dp)
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
                fontWeight = if (news) FontWeight.Medium else null,
                color = when {
                    wrong -> MaterialTheme.colorScheme.error
                    news -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        // What has been bought before by another account is not sold here again: no button for it.
        val held = !busy && ended == Bought.TAKEN
        val waiting = !busy && ended == Bought.PENDING
        val unchecked = !busy && ended == Bought.UNCONFIRMED
        if (!held) {
            Button(
                // A payment on its way is only to be understood: the button closes.
                onClick = if (waiting) onNotNow else onUpgrade,
                modifier = Modifier
                    .padding(start = 16.dp, top = if (said != null) 8.dp else 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !busy && (waiting || unchecked || offer != null),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(BrandPalette.ACCENT_DEEP),
                    contentColor = Color(BrandPalette.ON_ACCENT)
                )
            ) {
                BusyRing(busy)
                Text(
                    text = when {
                        waiting -> stringResource(R.string.action_understood)
                        // Paid for already: what is left is to check it, and the price is not asked again.
                        unchecked -> stringResource(R.string.premium_check_again)
                        offer == null -> stringResource(R.string.premium_upgrade)
                        // The price is one thing: it goes to a second line whole, or stays on the first.
                        else -> stringResource(R.string.premium_upgrade_priced, offer.replace(' ', Typography.nbsp))
                    },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (!waiting) {
            TextButton(
                onClick = onNotNow,
                modifier = Modifier
                    .padding(start = 16.dp, top = if (held) 8.dp else 4.dp, end = 16.dp)
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !busy,
                shape = ButtonShape
            ) {
                Text(
                    text = stringResource(if (held) R.string.action_close else R.string.action_not_now),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** How long the store is waited for before its price is said to be missing. */
private const val PRICE_LATE_MS = 8_000L

/**
 * What brought someone to what is sold, and so what the sheet says first: the character they
 * were about to make, or all of it, for whoever asked from the page of options. Either way all
 * that paying brings is said, in four points.
 */
internal enum class PremiumFor(
    @StringRes val title: Int,
    @StringRes val hint: Int?,
    val points: List<Int>,
    /** What saying no costs, which is nothing they have now. */
    @StringRes val note: Int
) {
    CHARACTERS(
        title = R.string.premium_title,
        hint = R.string.premium_hint,
        note = R.string.premium_free_note,
        points = listOf(
            R.string.premium_point_count,
            R.string.premium_point_voice,
            R.string.premium_point_ai,
            R.string.premium_point_no_ads
        )
    ),
    EVERYTHING(
        title = R.string.premium_all_title,
        hint = null,
        note = R.string.premium_all_note,
        points = listOf(
            R.string.premium_point_no_ads,
            R.string.premium_point_ai,
            R.string.premium_point_characters,
            // Here no title has said whose tone: the point says it itself.
            R.string.premium_point_tone
        )
    )
}

/** What is said of a try that did not end with it bought. */
private val Bought.message: Int
    get() = when (this) {
        Bought.PENDING -> R.string.premium_pending
        Bought.TAKEN -> R.string.premium_taken
        Bought.UNCONFIRMED -> R.string.premium_unconfirmed
        else -> R.string.premium_failed
    }
