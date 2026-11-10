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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
 * The stateless screen: the ball, one short text under it, and nothing else that is not needed
 * right now.
 *
 * - at rest, a single hint and "Recents" peeking from the bottom;
 * - listening, rings around the ball and the words as they are heard;
 * - thinking, the ball shaken, over the question;
 * - the answer on the ball, with "ask again" and "share", until it is closed or replaced.
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
    val peek = HISTORY_PEEK

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
                // Pulled up, the sheet stops short of the top: the screen behind it stays in sight.
                modifier = Modifier.heightIn(
                    min = peek + navigationBar,
                    max = LocalConfiguration.current.screenHeightDp.dp * HISTORY_MAX_HEIGHT_FRACTION
                )
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

/**
 * Everything but the history: the ball, as large as the window allows and always in the same
 * place, and one short text under it. The answer itself is on the ball.
 */
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
            // Kept free in every stage, so the ball does not move when the history comes and goes.
            .padding(bottom = historyPeek)
    ) {
        val ballSize = minOf(
            minOf(maxWidth * BALL_WIDTH_FRACTION, BALL_MAX_SIZE),
            maxHeight - TOP_BAR_HEIGHT - BALL_GAP - CAPTION_HEIGHT
        ).coerceAtLeast(BALL_MIN_SIZE)

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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = TOP_BAR_HEIGHT),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Caption(
                state = state,
                onAskAgain = onAskAgain,
                onShare = onShare,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = TEXT_MAX_WIDTH)
                    .fillMaxWidth()
                    .heightIn(min = CAPTION_HEIGHT)
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
 * The one text under the ball: what to do at rest, the words as they are heard, the question
 * while the ball thinks, and with the answer, the question again over what can be done next.
 * The words being heard just update; every other change is animated.
 *
 * The text on the die is as large as the die allows and cannot follow the system's font size,
 * so with a large font the answer is written here instead of the question.
 */
@Composable
private fun Caption(
    state: EightBallUiState,
    onAskAgain: () -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notice = state.notice
    val voiceReady = state.voice.isReady
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE

    AnimatedContent(
        targetState = state.phase,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.TopCenter,
        contentKey = { (it as? BallPhase.Answered)?.id ?: it::class },
        label = "caption"
    ) { phase ->
        when (phase) {
            BallPhase.Idle -> CaptionText(
                text = when (notice) {
                    Notice.MicrophoneDenied -> stringResource(R.string.notice_microphone_denied)
                    Notice.VoiceUnsupported -> stringResource(R.string.notice_voice_unsupported)
                    null -> stringResource(if (voiceReady) R.string.hint_voice else R.string.hint_tap)
                },
                modifier = Modifier.semantics { if (notice != null) liveRegion = LiveRegionMode.Polite }
            )
            is BallPhase.Listening -> if (phase.transcript.isEmpty()) {
                CaptionText(
                    stringResource(
                        if (phase.handsFree) R.string.hint_listening_hands_free else R.string.hint_listening
                    )
                )
            } else {
                Text(
                    text = phase.transcript,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = CAPTION_MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }
            is BallPhase.Thinking -> CaptionText(phase.question.quoted())
            is BallPhase.Answered -> {
                val answer = phase.answer.text()
                val shareText = shareText(phase.question, answer)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        largeFont -> Text(
                            text = answer,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        phase.question.isNotEmpty() -> CaptionText(phase.question.quoted(), maxLines = 2)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(onClick = onAskAgain, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.action_ask_again))
                        }
                        FilledTonalIconButton(onClick = { onShare(shareText) }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = stringResource(R.string.action_share),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionText(text: String, modifier: Modifier = Modifier, maxLines: Int = CAPTION_MAX_LINES) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
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

private const val BALL_WIDTH_FRACTION = 0.8f
private val BALL_MAX_SIZE = 440.dp
private val BALL_MIN_SIZE = 96.dp
private val BALL_GAP = 40.dp
private val TOP_BAR_HEIGHT = 56.dp
private val TEXT_MAX_WIDTH = 480.dp
private const val CAPTION_MAX_LINES = 3

/** Room kept under the ball, so it stays put whatever is written there. */
private val CAPTION_HEIGHT = 112.dp

/** From this font scale up, the answer is also written under the ball. */
private const val LARGE_FONT_SCALE = 1.3f

/** How much of the history sheet shows at rest: its handle and its title. */
private val HISTORY_PEEK = 92.dp
private const val HISTORY_MAX_HEIGHT_FRACTION = 0.86f

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
