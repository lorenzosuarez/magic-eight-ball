package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
 * @param onOpen receives the entry that was tapped, to show it in the ball again.
 */
@Composable
internal fun HistoryContent(
    entries: List<HistoryEntry>,
    now: Long,
    onDelete: (HistoryEntry) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (HistoryEntry) -> Unit = {}
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 0.dp, end = 4.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The page's name is in the bar above, with the dots.
            Spacer(Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                TextButton(onClick = { confirmClear = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.history_clear_all))
                }
            }
        }

        if (entries.isEmpty()) {
            EmptyHistory(Modifier.weight(1f))
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                    HistoryRow(
                        entry = entry,
                        now = now,
                        onDelete = onDelete,
                        onOpen = onOpen,
                        first = index == 0,
                        last = index == entries.lastIndex,
                        modifier = Modifier.animateItem()
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.history_footer),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

/** One answer: what the ball said, what was asked, how long ago and who wrote it. */
@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    now: Long,
    onDelete: (HistoryEntry) -> Unit,
    onOpen: (HistoryEntry) -> Unit,
    first: Boolean,
    last: Boolean,
    modifier: Modifier = Modifier
) {
    // The rows are one card, as on the options page: the first and the last round its corners.
    val top = if (first) CARD_CORNER else 0.dp
    val bottom = if (last) CARD_CORNER else 0.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // The whole row brings the answer back; its delete button keeps its own touch.
            .clickable(onClickLabel = stringResource(R.string.history_open)) { onOpen(entry) }
            .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // What the ball said and, when the character added something, that on a line of its own.
            val said = entry.answer ?: stringResource(R.string.answer_fallback)
            Text(
                text = said.substringBefore('\n'),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            said.substringAfter('\n', "").takeIf { it.isNotBlank() }?.let { remark ->
                Text(
                    text = remark,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.question.isNotEmpty()) {
                Text(
                    text = entry.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // When, who answered when that is known, and that the AI wrote it when it did.
            Text(
                text = listOfNotNull(
                    timeAgo(entry.askedAtMillis, now),
                    entry.style?.let { stringResource(it.labelRes()) },
                    if (entry.fromAi) stringResource(R.string.source_ai) else null
                ).joinToString(" \u00b7 "),
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
    if (!last) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
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

/**
 * Nothing asked yet: the ball in line, with its window empty, and under it what will be here.
 * It sits a little above the middle, where the eye rests, and scrolls if the text is too large.
 */
@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(EMPTY_ABOVE))
        Icon(
            painter = painterResource(R.drawable.ic_empty_recents),
            // The words under it say everything it does.
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = EMPTY_ART_ALPHA)
        )
        Text(
            text = stringResource(R.string.history_empty_title),
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.history_empty),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(EMPTY_BELOW))
    }
}

private const val EMPTY_ABOVE = 2f
private const val EMPTY_BELOW = 3f
private const val EMPTY_ART_ALPHA = 0.6f

private val CARD_CORNER = 16.dp
