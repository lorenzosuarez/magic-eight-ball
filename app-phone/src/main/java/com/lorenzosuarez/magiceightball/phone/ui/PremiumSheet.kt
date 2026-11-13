package com.lorenzosuarez.magiceightball.phone.ui

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magiceightball.core.common.BrandPalette
import com.lorenzosuarez.magiceightball.phone.R
import kotlinx.coroutines.launch

/**
 * What is shown to someone who wants to make a character and has not paid for it, on the day
 * that is asked for: what paying brings, the way to it, and the way back. It is never shown
 * while nothing is sold.
 *
 * @param price what it costs, as the store words it for this user, with how often after a
 * slash when it is paid again and again ("R$ 14,90/mês"); null until the store has said.
 * @param terms how it is paid, in a sentence: once, or again every so often and how to stop.
 * Nothing can be bought until both are said: without either the button names no price and
 * does not answer.
 * @param onUpgrade the user wants it: what starts the purchase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumSheet(onUpgrade: () -> Unit, onClose: () -> Unit, price: String? = null, terms: String? = null) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
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
            PremiumContent(
                price = price,
                terms = terms,
                onUpgrade = onUpgrade,
                onNotNow = { scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() } }
            )
        }
    }
}

/**
 * What [PremiumSheet] shows: what is offered in a line, the three things it comes to as a list
 * that plainly is not buttons, how it is paid and what stays free, said before the button, and
 * under the button the way back.
 */
@Composable
internal fun PremiumContent(
    price: String?,
    onUpgrade: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    terms: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.premium_title),
            modifier = Modifier.padding(horizontal = 32.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.premium_hint),
            modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Paragraph),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // The tick grows with the type, so it stays in the middle of the first line of its point.
        val (tickInset, tickSize) = with(LocalDensity.current) { 2.sp.toDp() to 20.sp.toDp() }
        // A list, ticked: these say, and do nothing. Not the rows of the other sheets, which are touched.
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(R.string.premium_point_count, R.string.premium_point_voice, R.string.premium_point_card).forEach { point ->
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
            text = stringResource(R.string.premium_free_note),
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
        Button(
            onClick = onUpgrade,
            enabled = offer != null,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(BrandPalette.ACCENT_DEEP),
                contentColor = Color(BrandPalette.ON_ACCENT)
            )
        ) {
            Text(
                text = if (offer == null) {
                    stringResource(R.string.premium_upgrade)
                } else {
                    // The price is one thing: it goes to a second line whole, or stays on the first.
                    stringResource(R.string.premium_upgrade_priced, offer.replace(' ', ' '))
                },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        TextButton(
            onClick = onNotNow,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
            shape = ButtonShape
        ) {
            Text(stringResource(R.string.action_not_now), textAlign = TextAlign.Center)
        }
    }
}
