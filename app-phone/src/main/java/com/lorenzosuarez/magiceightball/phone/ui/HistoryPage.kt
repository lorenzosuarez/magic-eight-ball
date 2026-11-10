package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.history.HistoryEntry
import java.text.DateFormat
import java.util.Date

/**
 * The history page: the latest answers, newest first.
 *
 * @param now the current time, for "5 min ago".
 * @param onShare receives the text to share for an entry.
 */
@Composable
internal fun HistoryContent(
    entries: List<HistoryEntry>,
    now: Long,
    onShare: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.history_title),
            modifier = Modifier
                .padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 12.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 24.dp
            )
        ) {
            items(entries, key = { it.id }) { entry ->
                HistoryRow(entry, now, onShare, onDelete, Modifier.animateItem())
            }
            item {
                // Where the list ends: what it is, and the way to empty it.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (entries.isEmpty()) R.string.history_empty else R.string.history_footer
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = { confirmClear = true }) {
                            Text(stringResource(R.string.history_clear_all))
                        }
                    }
                }
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

/** One answer: what was asked, what the ball said, how long ago and who wrote it. */
@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    now: Long,
    onShare: (String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val answer = entry.answer ?: stringResource(R.string.answer_fallback)
    val shareText = shareText(entry.question, answer)
    // Read here and not in the menu, which is a window of its own in the device's language.
    val shareLabel = stringResource(R.string.action_share)
    val deleteLabel = stringResource(R.string.history_delete)
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 10.dp, end = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = answer,
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
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = stringResource(R.string.history_more),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(shareLabel) },
                    onClick = {
                        menuOpen = false
                        onShare(shareText)
                    }
                )
                DropdownMenuItem(
                    text = { Text(deleteLabel) },
                    onClick = {
                        menuOpen = false
                        onDelete(entry.id)
                    }
                )
            }
        }
    }
}

/** What goes to another app when an answer is shared. */
@Composable
internal fun shareText(question: String, answer: String): String =
    if (question.isEmpty()) {
        stringResource(R.string.share_answer, answer)
    } else {
        stringResource(R.string.share_question_and_answer, question, answer)
    }

/** "Just now", "5 min ago", "2 h ago", "3 d ago", then the date, in the app's language. */
@Composable
private fun timeAgo(thenMillis: Long, nowMillis: Long): String {
    val minutes = ((nowMillis - thenMillis) / 60_000L).coerceAtLeast(0L).toInt()
    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_minutes_ago, minutes)
        minutes < 60 * 24 -> stringResource(R.string.time_hours_ago, minutes / 60)
        minutes < 60 * 24 * 7 -> stringResource(R.string.time_days_ago, minutes / (60 * 24))
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM, LocalConfiguration.current.locales[0])
            .format(Date(thenMillis))
    }
}
