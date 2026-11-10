package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.lorenzosuarez.magiceightball.phone.R
import java.text.DateFormat
import java.util.Date

/**
 * The history page: the latest answers, newest first. An entry goes away with its delete
 * button; "Clear all", always in sight at the top, asks first.
 *
 * @param now the current time, for "5 min ago".
 * @param onDelete receives the entry to delete; the caller offers to undo it.
 */
@Composable
internal fun HistoryContent(
    entries: List<HistoryEntry>,
    now: Long,
    onDelete: (HistoryEntry) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (entries.isNotEmpty()) {
                TextButton(onClick = { confirmClear = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.history_clear_all))
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(entries, key = { it.id }) { entry ->
                HistoryRow(entry, now, onDelete, Modifier.animateItem())
            }
            item {
                Text(
                    text = stringResource(
                        if (entries.isEmpty()) R.string.history_empty else R.string.history_footer
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmClear) {
        // The dialog is a window of its own, which starts again from the device's language:
        // its texts are read here, in the app's.
        val title = stringResource(R.string.history_clear_title)
        val text = stringResource(R.string.history_clear_text)
        val confirm = stringResource(R.string.history_clear_all)
        val cancel = stringResource(R.string.action_cancel)
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onClearAll()
                    }
                ) {
                    Text(confirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(cancel) }
            }
        )
    }
}

/** One answer: what the ball said, what was asked, how long ago and who wrote it. */
@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    now: Long,
    onDelete: (HistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.answer ?: stringResource(R.string.answer_fallback),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.question.isNotEmpty()) {
                Text(
                    text = entry.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    R.string.history_entry_meta,
                    timeAgo(entry.askedAtMillis, now),
                    stringResource(if (entry.fromAi) R.string.source_ai else R.string.source_classic)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AppIconButton(
            icon = R.drawable.ic_delete,
            description = stringResource(R.string.history_delete),
            onClick = { onDelete(entry) },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "Just now", "5 min ago", "2 h ago", "3 d ago", then the date, in the app's language. */
@Composable
private fun timeAgo(thenMillis: Long, nowMillis: Long): String {
    val minutes = ((nowMillis - thenMillis) / MILLIS_PER_MINUTE).coerceAtLeast(0L).toInt()
    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < MINUTES_PER_HOUR -> stringResource(R.string.time_minutes_ago, minutes)
        minutes < MINUTES_PER_DAY -> stringResource(R.string.time_hours_ago, minutes / MINUTES_PER_HOUR)
        minutes < MINUTES_PER_DAY * DAYS_SHOWN_AS_AGO ->
            stringResource(R.string.time_days_ago, minutes / MINUTES_PER_DAY)
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM, LocalConfiguration.current.locales[0])
            .format(Date(thenMillis))
    }
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 60 * 24

/** Older than this, an entry shows its date. */
private const val DAYS_SHOWN_AS_AGO = 7
