package com.lorenzosuarez.magiceightball.phone.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallEffect
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.EightBallViewModel
import com.lorenzosuarez.magiceightball.phone.Notice
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.history.HistoryEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The screen wired to its [viewModel] and to the platform: the microphone permission, the
 * lifecycle, haptics, sharing and the settings sheet.
 */
@Composable
fun EightBallScreen(viewModel: EightBallViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The permission asked for from the settings sheet differs in one way: when the system no
    // longer shows its dialog, the user is taken to the app's page in the system settings.
    var askedFromSettings by remember { mutableStateOf(false) }
    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMicrophonePermissionResult(granted)
        if (!granted && askedFromSettings && !context.mayShowMicrophoneDialog()) {
            context.openAppSettings()
        }
        askedFromSettings = false
    }

    // The accelerometer and the microphone are only used while the screen is in front of the user.
    LifecycleResumeEffect(viewModel, context) {
        viewModel.onMicrophonePermissionChecked(context.hasMicrophonePermission())
        viewModel.setResumed(true)
        onPauseOrDispose { viewModel.setResumed(false) }
    }

    val view = LocalView.current
    LaunchedEffect(viewModel, view) {
        viewModel.effects.collect { effect ->
            val feedback = when (effect) {
                EightBallEffect.RequestMicrophonePermission -> {
                    permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
                    null
                }
                EightBallEffect.ListeningStarted -> HapticFeedbackConstantsCompat.LONG_PRESS
                EightBallEffect.ListeningStopped -> HapticFeedbackConstantsCompat.GESTURE_END
                EightBallEffect.AskStarted -> HapticFeedbackConstantsCompat.CONTEXT_CLICK
                EightBallEffect.AnswerRevealed -> HapticFeedbackConstantsCompat.CONFIRM
            }
            feedback?.let { ViewCompat.performHapticFeedback(view, it) }
        }
    }

    // The voice level is smoothed here and only ever read while drawing the ball.
    val voiceLevel = remember { Animatable(0f) }
    LaunchedEffect(viewModel) {
        viewModel.voiceLevel.collectLatest { level ->
            voiceLevel.animateTo(level, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    // For "5 min ago" in the history; a minute is all the precision it shows.
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(CLOCK_TICK_MS)
            value = System.currentTimeMillis()
        }
    }

    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Nothing but the background until the saved choices are known.
        val current = state ?: return@Box
        ProvideAppLanguage(current.settings.language) {
            EightBallContent(
                state = current,
                now = now,
                voiceLevel = { voiceLevel.value },
                reducedMotion = rememberReducedMotion(),
                onAsk = viewModel::ask,
                onHoldStart = viewModel::onHoldStart,
                onHoldEnd = viewModel::onHoldEnd,
                onHoldCancel = viewModel::onHoldCancel,
                onToggleVoiceQuestion = viewModel::toggleVoiceQuestion,
                onAskAgain = viewModel::askAgain,
                onDismiss = viewModel::dismiss,
                onShare = context::shareText,
                onDeleteHistoryEntry = viewModel::deleteHistoryEntry,
                onClearHistory = viewModel::clearHistory,
                onOpenSettings = { settingsOpen = true }
            )
            if (settingsOpen) {
                SettingsSheet(
                    state = current,
                    onLanguageSelected = viewModel::setLanguage,
                    onPersonalitySelected = viewModel::setPersonality,
                    onAiAnswersChange = viewModel::setAiAnswersEnabled,
                    onVoiceQuestionsChange = viewModel::setVoiceQuestionsEnabled,
                    onAllowMicrophone = {
                        askedFromSettings = true
                        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onDismiss = { settingsOpen = false }
                )
            }
        }
    }
}

/**
 * The stateless screen. One stage at a time, each saying what to do in the same place:
 *
 * - at rest, the ball with "hold and ask" above it and "Recents" peeking from the bottom;
 * - listening, rings around the ball and the words as they are heard, large, under it;
 * - thinking, the ball spinning over the question;
 * - the answer, large, with "ask again" and "share", until it is closed or replaced.
 *
 * The ball is the one main action throughout. The history sheet is only there at rest, so
 * nothing competes with a question in progress or with its answer.
 *
 * @param now the current time, for the history's "5 min ago".
 * @param voiceLevel read while drawing only; see [EightBall].
 * @param onShare receives the text to hand to another app.
 */
@OptIn(ExperimentalMaterial3Api::class)
// The scaffold's padding is the sheet's peek, and the stage is meant to run under the sheet.
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun EightBallContent(
    state: EightBallUiState,
    now: Long,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    onAskAgain: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onDeleteHistoryEntry: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val atRest = state.phase is BallPhase.Idle
    // The sheet can be hidden, but only by the app: a swipe down stops at the peek.
    val sheetState = rememberStandardBottomSheetState(
        initialValue = if (atRest) SheetValue.PartiallyExpanded else SheetValue.Hidden,
        confirmValueChange = { it != SheetValue.Hidden },
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)
    val sheetExpanded = sheetState.targetValue == SheetValue.Expanded
    val scope = rememberCoroutineScope()

    // A question, however it was started, takes the history away until the ball is at rest again.
    LaunchedEffect(atRest) {
        if (atRest) sheetState.partialExpand() else sheetState.hide()
    }
    BackHandler(enabled = sheetExpanded) {
        scope.launch { sheetState.partialExpand() }
    }
    // Back closes a question in progress or an answer, like the close button.
    BackHandler(enabled = !atRest, onBack = onDismiss)

    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // On a short window the sheet shows less of itself rather than taking the ball's place.
    val peek = minOf(HISTORY_PEEK, LocalConfiguration.current.screenHeightDp.dp * HISTORY_PEEK_MAX_FRACTION)

    BottomSheetScaffold(
        sheetContent = {
            HistoryContent(
                entries = state.history,
                now = now,
                canExpand = !sheetExpanded,
                onExpand = { scope.launch { sheetState.expand() } },
                onShare = onShare,
                onDelete = onDeleteHistoryEntry,
                onClearAll = onClearHistory,
                modifier = Modifier.heightIn(min = peek + navigationBar)
            )
        },
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = peek + navigationBar,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Stage(
            state = state,
            historyPeek = peek,
            voiceLevel = voiceLevel,
            reducedMotion = reducedMotion,
            onAsk = onAsk,
            onHoldStart = onHoldStart,
            onHoldEnd = onHoldEnd,
            onHoldCancel = onHoldCancel,
            onToggleVoiceQuestion = onToggleVoiceQuestion,
            onAskAgain = onAskAgain,
            onDismiss = onDismiss,
            onShare = onShare,
            onOpenSettings = onOpenSettings
        )
    }
}

/** Everything but the history: the bar, what to do, the ball and what it heard or said. */
@Composable
private fun Stage(
    state: EightBallUiState,
    historyPeek: Dp,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    onAskAgain: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val atRest = state.phase is BallPhase.Idle
    val busy = state.phase is BallPhase.Listening || state.phase is BallPhase.Thinking

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // The ball keeps its size and its place through every stage: only what is written
        // above and under it changes.
        val headlineHeight = with(LocalDensity.current) {
            (MaterialTheme.typography.headlineSmall.lineHeight.toDp() +
                MaterialTheme.typography.bodyLarge.lineHeight.toDp()) * HEADLINE_LINES_EACH
        }
        // With a large font on a small window it shrinks, so the history at rest never covers it.
        val roomForBall = maxHeight - TOP_BAR_HEIGHT - headlineHeight - BALL_GAP - historyPeek - BALL_GAP / 2
        val ballSize = minOf(
            minOf(maxWidth * BALL_WIDTH_FRACTION, maxHeight * BALL_HEIGHT_FRACTION, BALL_MAX_SIZE),
            roomForBall
        ).coerceAtLeast(BALL_MIN_SIZE)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TOP_BAR_HEIGHT)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = !atRest, enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(
                                if (busy) R.string.action_cancel else R.string.action_close
                            ),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(visible = !busy, enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Headline(
                state = state,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = TEXT_MAX_WIDTH)
                    .fillMaxWidth()
                    .heightIn(min = headlineHeight)
            )
            Spacer(Modifier.height(BALL_GAP))

            Ball(
                state = state,
                voiceLevel = voiceLevel,
                reducedMotion = reducedMotion,
                onAsk = onAsk,
                onHoldStart = onHoldStart,
                onHoldEnd = onHoldEnd,
                onHoldCancel = onHoldCancel,
                onToggleVoiceQuestion = onToggleVoiceQuestion,
                modifier = Modifier.size(ballSize)
            )
            Spacer(Modifier.height(BALL_GAP))

            Outcome(
                phase = state.phase,
                onAskAgain = onAskAgain,
                onShare = onShare,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp)
                    .widthIn(max = TEXT_MAX_WIDTH)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * The ball, which takes the press that asks: a tap asks, a hold listens for as long as it
 * lasts. For assistive technologies it is a button that asks, it offers "ask by voice" as an
 * action of its own since a press cannot be held with TalkBack or a switch, and it announces
 * its answer when it arrives.
 */
@Composable
private fun Ball(
    state: EightBallUiState,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phase = state.phase
    val answer = (phase as? BallPhase.Answered)?.answer?.text()

    val description = if (answer != null) {
        stringResource(R.string.ball_answer_description, answer)
    } else {
        stringResource(R.string.ball_description)
    }
    val busyDescription = when (phase) {
        is BallPhase.Listening -> stringResource(R.string.ball_state_listening)
        is BallPhase.Thinking -> stringResource(R.string.ball_state_thinking)
        else -> null
    }
    val listening = phase is BallPhase.Listening
    val clickLabel = stringResource(if (listening) R.string.action_finish_question else R.string.action_ask)
    val voiceLabel = stringResource(R.string.action_ask_by_voice)
    val offersVoice = state.voice.isReady && (phase is BallPhase.Idle || phase is BallPhase.Answered)

    // A tap while the app listens ends the question; any other tap asks.
    val tap = if (listening) onToggleVoiceQuestion else onAsk

    var pressed by remember { mutableStateOf(false) }
    val currentTap by rememberUpdatedState(tap)
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnHoldEnd by rememberUpdatedState(onHoldEnd)
    val currentOnHoldCancel by rememberUpdatedState(onHoldCancel)

    EightBall(
        mode = when (phase) {
            BallPhase.Idle -> BallMode.Idle
            is BallPhase.Listening -> BallMode.Listening
            is BallPhase.Thinking -> BallMode.Thinking
            is BallPhase.Answered -> BallMode.Answered
        },
        answer = answer,
        pressed = pressed,
        voiceLevel = voiceLevel,
        reducedMotion = reducedMotion,
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    try {
                        // True when lifted in time, false when the gesture was taken away,
                        // null when the finger is still down after the delay.
                        val lifted = try {
                            withTimeout(HOLD_DELAY_MS) { waitForUpOrCancellation() != null }
                        } catch (_: PointerEventTimeoutCancellationException) {
                            null
                        }
                        when (lifted) {
                            true -> currentTap()
                            false -> Unit
                            null -> {
                                currentOnHoldStart()
                                if (waitForUpOrCancellation() != null) currentOnHoldEnd() else currentOnHoldCancel()
                            }
                        }
                    } finally {
                        pressed = false
                    }
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = description
                busyDescription?.let { stateDescription = it }
                liveRegion = LiveRegionMode.Polite
                onClick(label = clickLabel) {
                    tap()
                    true
                }
                if (offersVoice) {
                    customActions = listOf(
                        CustomAccessibilityAction(voiceLabel) {
                            onToggleVoiceQuestion()
                            true
                        }
                    )
                }
            }
            .onKeyEvent { event ->
                val asks = event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.Spacebar)
                if (asks) tap()
                asks
            }
            .focusable()
    )
}

/**
 * What to do right now, above the ball: a title and one supporting line. Only a change of
 * stage is animated.
 */
@Composable
private fun Headline(state: EightBallUiState, modifier: Modifier = Modifier) {
    val headline = state.headline()
    AnimatedContent(
        targetState = headline,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.BottomCenter,
        label = "headline"
    ) { shown ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = shown.title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
            if (shown.supporting.isNotEmpty()) {
                Text(
                    text = shown.supporting,
                    modifier = Modifier.semantics {
                        if (shown.isNotice) liveRegion = LiveRegionMode.Polite
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = if (shown.isNotice) NOTICE_MAX_LINES else HEADLINE_LINES_EACH,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class HeadlineContent(val title: String, val supporting: String, val isNotice: Boolean = false)

@Composable
private fun EightBallUiState.headline(): HeadlineContent = when (val phase = phase) {
    BallPhase.Idle -> HeadlineContent(
        title = stringResource(if (voice.isReady) R.string.idle_title_voice else R.string.idle_title_tap),
        supporting = when (notice) {
            Notice.MicrophoneDenied -> stringResource(R.string.notice_microphone_denied)
            Notice.VoiceUnsupported -> stringResource(R.string.notice_voice_unsupported)
            null -> stringResource(if (voice.isReady) R.string.idle_hint_voice else R.string.idle_hint_tap)
        },
        isNotice = notice != null
    )
    is BallPhase.Listening -> HeadlineContent(
        title = stringResource(R.string.listening_title),
        supporting = stringResource(
            if (phase.handsFree) R.string.listening_hint_hands_free else R.string.listening_hint_hold
        )
    )
    is BallPhase.Thinking -> HeadlineContent(
        title = stringResource(R.string.thinking_title),
        supporting = stringResource(R.string.thinking_hint)
    )
    is BallPhase.Answered -> HeadlineContent(
        title = stringResource(R.string.answer_title),
        supporting = phase.question.quoted()
    )
}

/**
 * Under the ball: the words as they are heard, the question while the ball thinks, then the
 * answer with what can be done with it. Nothing at rest, where the history peeks instead.
 * The words being heard just update; every other change is animated.
 */
@Composable
private fun Outcome(
    phase: BallPhase,
    onAskAgain: () -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = phase,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.TopCenter,
        contentKey = { (it as? BallPhase.Answered)?.id ?: it::class },
        label = "outcome"
    ) { shown ->
        when (shown) {
            BallPhase.Idle -> Spacer(Modifier.fillMaxWidth())
            is BallPhase.Listening -> Text(
                text = shown.transcript,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = TRANSCRIPT_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
            is BallPhase.Thinking -> Text(
                text = shown.question.quoted(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = TRANSCRIPT_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
            is BallPhase.Answered -> Answer(shown, onAskAgain, onShare)
        }
    }
}

/** The answer, large, who wrote it, and the two things to do next. */
@Composable
private fun Answer(
    answered: BallPhase.Answered,
    onAskAgain: () -> Unit,
    onShare: (String) -> Unit
) {
    val answer = answered.answer.text()
    val fromAi = (answered.answer as? AnswerText.Plain)?.fromAi == true
    val shareText = shareText(answered.question, answer)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = answer,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(if (fromAi) R.string.source_ai_answer else R.string.source_classic_answer),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAskAgain,
            modifier = Modifier
                .widthIn(max = ACTION_MAX_WIDTH)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(stringResource(R.string.action_ask_again), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = { onShare(shareText) }, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(painter = painterResource(R.drawable.ic_share), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_share))
        }
    }
}

@Composable
private fun String.quoted(): String = if (isEmpty()) "" else stringResource(R.string.question_quoted, this)

@Composable
private fun AnswerText.text(): String = when (this) {
    is AnswerText.Plain -> value
    AnswerText.Fallback -> stringResource(R.string.answer_fallback)
}

/** Hands [text] to whichever app the user picks. */
private fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    try {
        startActivity(Intent.createChooser(send, null))
    } catch (_: ActivityNotFoundException) {
        // No app to share with: nothing to do.
    }
}

private fun Context.hasMicrophonePermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

/** False once the system has stopped showing the permission dialog for this app. */
private fun Context.mayShowMicrophoneDialog(): Boolean =
    findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) ?: true

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val StageEnter = fadeIn(tween(durationMillis = 220, delayMillis = 60)) +
    slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 4 }

/** A press shorter than this is a tap; from here on it is a hold, and the app listens. */
private const val HOLD_DELAY_MS = 180L

private const val CLOCK_TICK_MS = 30_000L

private const val BALL_WIDTH_FRACTION = 0.6f
private const val BALL_HEIGHT_FRACTION = 0.34f
private val BALL_MAX_SIZE = 360.dp
private val BALL_MIN_SIZE = 96.dp
private val BALL_GAP = 48.dp
private val TOP_BAR_HEIGHT = 56.dp
private val TEXT_MAX_WIDTH = 520.dp
private val ACTION_MAX_WIDTH = 360.dp
private const val HEADLINE_LINES_EACH = 2
private const val TITLE_MAX_LINES = 3
private const val NOTICE_MAX_LINES = 3
private const val TRANSCRIPT_MAX_LINES = 4

/** How much of the history sheet shows at rest: its handle, its title and the latest answer. */
private val HISTORY_PEEK = 212.dp
private const val HISTORY_PEEK_MAX_FRACTION = 0.25f

@PreviewLightDark
@Preview(name = "Tablet", widthDp = 840, heightDp = 1200)
@Preview(name = "Large font", fontScale = 2f)
@Composable
private fun EightBallContentPreview() {
    MagicEightBallPhoneTheme {
        EightBallContent(
            state = EightBallUiState(
                phase = BallPhase.Answered(
                    id = 0,
                    question = "Will it work out?",
                    answer = AnswerText.Plain("Don't bet on it.")
                ),
                history = listOf(HistoryEntry(1, "Will it work out?", "Don't bet on it.", 0L, fromAi = true))
            ),
            now = 0L,
            voiceLevel = { 0f },
            reducedMotion = false,
            onAsk = {},
            onHoldStart = {},
            onHoldEnd = {},
            onHoldCancel = {},
            onToggleVoiceQuestion = {},
            onAskAgain = {},
            onDismiss = {},
            onShare = {},
            onDeleteHistoryEntry = {},
            onClearHistory = {},
            onOpenSettings = {}
        )
    }
}
