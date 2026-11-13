package com.lorenzosuarez.magiceightball.phone.ui

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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        ReportReason.entries.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onReason(reason) }
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(reason.labelRes()), style = MaterialTheme.typography.bodyLarge)
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
