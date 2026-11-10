package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallEffect
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.EightBallViewModel
import com.lorenzosuarez.magiceightball.phone.HistoryEntry
import com.lorenzosuarez.magiceightball.phone.R

@Composable
fun EightBallScreen(viewModel: EightBallViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // The accelerometer is only listened to while the screen is in front of the user.
    LifecycleResumeEffect(viewModel) {
        viewModel.setShakeDetectionEnabled(true)
        onPauseOrDispose { viewModel.setShakeDetectionEnabled(false) }
    }

    val view = LocalView.current
    LaunchedEffect(viewModel, view) {
        viewModel.effects.collect { effect ->
            val feedback = when (effect) {
                EightBallEffect.AskStarted -> HapticFeedbackConstantsCompat.CONTEXT_CLICK
                EightBallEffect.AnswerRevealed -> HapticFeedbackConstantsCompat.CONFIRM
            }
            ViewCompat.performHapticFeedback(view, feedback)
        }
    }

    val focusManager = LocalFocusManager.current
    EightBallContent(
        state = state,
        question = viewModel.question,
        onQuestionChange = viewModel::onQuestionChange,
        onAsk = {
            // Also hides the keyboard, so the ball and its answer are in view.
            focusManager.clearFocus()
            viewModel.ask()
        },
        onPersonalitySelected = viewModel::setPersonality
    )
}

/** The stateless screen: everything [EightBallScreen] shows, driven by plain values. */
@Composable
internal fun EightBallContent(
    state: EightBallUiState,
    question: String,
    onQuestionChange: (String) -> Unit,
    onAsk: () -> Unit,
    onPersonalitySelected: (Magic8BallPersonality) -> Unit
) {
    // safeDrawing covers the system bars, display cutouts and the keyboard, which is all the
    // edge-to-edge window needs.
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            // One centred column of a comfortable width: the same layout on phones, tablets
            // and unfolded foldables. It scrolls when the font is large or the window is short.
            Column(
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.question_label)) },
                    placeholder = { Text(stringResource(R.string.question_placeholder)) },
                    supportingText = { Text(stringResource(R.string.question_supporting)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(onSend = { onAsk() })
                )
                EightBall(
                    phase = state.phase,
                    onClick = onAsk,
                    modifier = Modifier
                        .widthIn(max = BALL_MAX_SIZE)
                        .fillMaxWidth()
                )
                AnswerPanel(state.phase)
                PersonalitySelector(
                    selected = state.personality,
                    onSelected = onPersonalitySelected
                )
                if (state.history.isNotEmpty()) {
                    HistoryCard(state.history)
                }
            }
        }
    }
}

/**
 * The words under the ball: a hint, then the answer fading and settling in. A polite live
 * region, so TalkBack reads the answer when it arrives without the user looking for it.
 */
@Composable
private fun AnswerPanel(phase: BallPhase) {
    AnimatedContent(
        targetState = phase,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        transitionSpec = {
            val enter = fadeIn(tween(durationMillis = 350)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.85f
            )
            enter togetherWith fadeOut(tween(durationMillis = 120))
        },
        contentAlignment = Alignment.Center,
        label = "answer"
    ) { target ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (target) {
                BallPhase.Idle -> Hint(stringResource(R.string.hint_idle))
                BallPhase.Thinking -> Hint(stringResource(R.string.hint_thinking))
                is BallPhase.Answered -> {
                    Text(
                        text = target.entry.answer.text(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    if (target.entry.question.isNotEmpty()) {
                        Text(
                            text = target.entry.question,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalitySelector(
    selected: Magic8BallPersonality,
    onSelected: (Magic8BallPersonality) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.personality_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Wraps onto a second line when the chips do not fit side by side (large fonts).
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Magic8BallPersonality.entries.forEach { personality ->
                FilterChip(
                    selected = personality == selected,
                    onClick = { onSelected(personality) },
                    label = { Text(personality.label()) },
                    modifier = Modifier.semantics { role = Role.RadioButton }
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(history: List<HistoryEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            history.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider()
                // Read as one item by TalkBack: the question, then its answer.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {}
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (entry.question.isNotEmpty()) {
                        Text(
                            text = entry.question,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.history_no_question),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = entry.answer.text(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerText.text(): String = when (this) {
    is AnswerText.Plain -> value
    AnswerText.Fallback -> stringResource(R.string.answer_fallback)
}

@Composable
private fun Magic8BallPersonality.label(): String = stringResource(
    when (this) {
        Magic8BallPersonality.SARCASTIC -> R.string.personality_sarcastic
        Magic8BallPersonality.MYSTICAL -> R.string.personality_mystical
    }
)

private val CONTENT_MAX_WIDTH = 560.dp
private val BALL_MAX_SIZE = 260.dp

@PreviewLightDark
@Preview(name = "Tablet", widthDp = 840, heightDp = 1200)
@Preview(name = "Large font", fontScale = 2f)
@Composable
private fun EightBallContentPreview() {
    val answered = HistoryEntry(
        id = 1,
        question = "Will it work out?",
        answer = AnswerText.Plain("Don't bet on it.")
    )
    MagicEightBallPhoneTheme {
        EightBallContent(
            state = EightBallUiState(
                phase = BallPhase.Answered(answered),
                history = listOf(answered, HistoryEntry(id = 0, question = "", answer = AnswerText.Fallback))
            ),
            question = "",
            onQuestionChange = {},
            onAsk = {},
            onPersonalitySelected = {}
        )
    }
}
