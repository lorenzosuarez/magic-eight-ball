package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.common.BrandPalette
import com.lorenzosuarez.magiceightball.phone.R
import kotlinx.coroutines.launch

/**
 * What is shown to someone who wants to make a character and has not paid for it, on the day
 * that is asked for: what paying brings, the way to it, and the way back. It is never shown
 * while nothing is sold.
 *
 * @param price what it costs, as the store words it for this user; null until the store has
 * said, and then the button names no price.
 * @param onUpgrade the user wants it: what starts the purchase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumSheet(onUpgrade: () -> Unit, onClose: () -> Unit, price: String? = null) {
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
                onUpgrade = onUpgrade,
                onNotNow = { scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() } }
            )
        }
    }
}

/**
 * What [PremiumSheet] shows: what is offered in a line, the three things it comes to as rows of
 * one card, the button, the way back, and what stays free, so that nobody thinks the app they
 * had has been taken from them.
 */
@Composable
internal fun PremiumContent(
    price: String?,
    onUpgrade: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
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
        // One card of rows, as on the options page: these say, and do nothing.
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            listOf(R.string.premium_point_count, R.string.premium_point_voice, R.string.premium_point_card)
                .forEachIndexed { index, point ->
                    if (index > 0) RowDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(point),
                            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading)
                        )
                    }
                }
        }
        Button(
            onClick = onUpgrade,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp).fillMaxWidth().heightIn(min = 48.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(BrandPalette.ACCENT_DEEP),
                contentColor = Color(BrandPalette.ON_ACCENT)
            )
        ) {
            Text(
                text = if (price == null) {
                    stringResource(R.string.premium_upgrade)
                } else {
                    stringResource(R.string.premium_upgrade_priced, price)
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
        Text(
            text = stringResource(R.string.premium_free_note),
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall.copy(lineBreak = LineBreak.Heading),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
