package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.gateway.ReportReason
import com.lorenzosuarez.magiceightball.phone.R
import kotlinx.coroutines.launch

/**
 * What is asked before an answer is reported: what is wrong with it, as a few plain choices.
 * A touch on one sends the report, so reporting is two touches and never an accident; a touch
 * anywhere else, or back, leaves the answer as it was.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportSheet(onReason: (ReportReason) -> Unit, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // The sheet is a window of its own: the app's language is carried into it.
    val language = rememberAppLanguage()
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetGround,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        InAppLanguage(language) {
            ReportContent(
                onReason = { reason -> scope.launch { sheetState.hide() }.invokeOnCompletion { onReason(reason) } }
            )
        }
    }
}

/** What [ReportSheet] shows: the question and, under it, one row to a reason. */
@Composable
internal fun ReportContent(onReason: (ReportReason) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.report_title),
            modifier = Modifier.padding(horizontal = 32.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        // Said before the first touch: choosing is sending.
        Text(
            text = stringResource(R.string.report_hint),
            modifier = Modifier.padding(start = 32.dp, top = 2.dp, end = 32.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // One card of rows, as on the options page.
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.sheetCard)
        ) {
            ReportReason.entries.forEachIndexed { index, reason ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.hairline
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onReason(reason) }
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(reason.labelRes()), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun ReportReason.labelRes(): Int = when (this) {
    ReportReason.OFFENSIVE -> R.string.report_reason_offensive
    ReportReason.INAPPROPRIATE -> R.string.report_reason_inappropriate
    ReportReason.HARMFUL -> R.string.report_reason_harmful
    ReportReason.NONSENSE -> R.string.report_reason_nonsense
    ReportReason.OTHER -> R.string.report_reason_other
}
