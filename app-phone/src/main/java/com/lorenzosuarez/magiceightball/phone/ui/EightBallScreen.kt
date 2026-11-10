package com.lorenzosuarez.magiceightball.phone.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.collectLatest

/**
 * The screen wired to its [viewModel] and to the platform: the microphone permission, the
 * lifecycle, haptics and the settings sheet.
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
                voiceLevel = { voiceLevel.value },
                reducedMotion = rememberReducedMotion(),
                onAsk = viewModel::ask,
                onHoldStart = viewModel::onHoldStart,
                onHoldEnd = viewModel::onHoldEnd,
                onHoldCancel = viewModel::onHoldCancel,
                onToggleVoiceQuestion = viewModel::toggleVoiceQuestion,
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
 * The stateless screen: the ball in the middle of the window, one quiet line under it and the
 * settings button in the corner. The whole screen takes the press that asks.
 *
 * @param voiceLevel read while drawing only; see [EightBall].
 */
@Composable
internal fun EightBallContent(
    state: EightBallUiState,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val currentOnAsk by rememberUpdatedState(onAsk)
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnHoldEnd by rememberUpdatedState(onHoldEnd)
    val currentOnHoldCancel by rememberUpdatedState(onHoldCancel)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                            true -> currentOnAsk()
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
    ) {
        // The ball sits in the exact centre whenever the caption's full height fits under it;
        // on a short window or with a large font it moves up, and only then shrinks, to make room.
        val safeDrawing = WindowInsets.safeDrawing
        val captionLineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        val (roomAbove, roomBelow) = with(LocalDensity.current) {
            val above = safeDrawing.getTop(this).toDp() + SCREEN_MARGIN
            val below = CAPTION_GAP + captionLineHeight.toDp() * CAPTION_MAX_LINES +
                safeDrawing.getBottom(this).toDp() + SCREEN_MARGIN
            above to below
        }
        val ballSize = minOf(
            minOf(maxWidth * BALL_WIDTH_FRACTION, maxHeight * BALL_HEIGHT_FRACTION, BALL_MAX_SIZE),
            maxHeight - roomAbove - roomBelow
        ).coerceAtLeast(BALL_MIN_SIZE)
        val ballTop = ((maxHeight - ballSize) / 2)
            .coerceAtMost(maxHeight - roomBelow - ballSize)
            .coerceAtLeast(roomAbove)

        Ball(
            state = state,
            pressed = pressed,
            voiceLevel = voiceLevel,
            reducedMotion = reducedMotion,
            onAsk = onAsk,
            onToggleVoiceQuestion = onToggleVoiceQuestion,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = ballTop)
                .size(ballSize)
        )

        Caption(
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = ballTop + ballSize + CAPTION_GAP)
                .windowInsetsPadding(safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 32.dp)
                .widthIn(max = CAPTION_MAX_WIDTH)
                .fillMaxWidth()
        )

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The ball with what assistive technologies need from it: it is a button that asks, it offers
 * "ask by voice" as an action of its own since a press cannot be held with TalkBack or a
 * switch, and it announces its answer when it arrives.
 */
@Composable
private fun Ball(
    state: EightBallUiState,
    pressed: Boolean,
    voiceLevel: () -> Float,
    reducedMotion: Boolean,
    onAsk: () -> Unit,
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
            .semantics {
                role = Role.Button
                contentDescription = description
                busyDescription?.let { stateDescription = it }
                liveRegion = LiveRegionMode.Polite
                onClick(label = clickLabel) {
                    if (listening) onToggleVoiceQuestion() else onAsk()
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
                if (asks) onAsk()
                asks
            }
            .focusable()
    )
}

/**
 * The one line under the ball: a hint at rest, the words as they are heard, then the question
 * that was asked. Only a change of kind is animated; the words being heard just update.
 *
 * The text on the die is as large as the die allows and cannot follow the system's font size,
 * so with a large font the answer is written here instead of the question.
 */
@Composable
private fun Caption(state: EightBallUiState, modifier: Modifier = Modifier) {
    val caption = state.caption()
    AnimatedContent(
        targetState = caption,
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(tween(durationMillis = 220, delayMillis = 60)) +
                slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 4 }
            enter togetherWith fadeOut(tween(durationMillis = 120))
        },
        contentAlignment = Alignment.TopCenter,
        contentKey = { it.kind },
        label = "caption"
    ) { shown ->
        Text(
            text = shown.text,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    if (shown.kind == CaptionKind.Notice) liveRegion = LiveRegionMode.Polite
                },
            style = MaterialTheme.typography.bodyLarge,
            color = when (shown.kind) {
                CaptionKind.Transcript, CaptionKind.Question, CaptionKind.Answer ->
                    MaterialTheme.colorScheme.onSurface
                CaptionKind.Hint, CaptionKind.Notice -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = CAPTION_MAX_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private enum class CaptionKind { Hint, Notice, Transcript, Question, Answer }

private data class CaptionContent(val kind: CaptionKind, val text: String)

@Composable
private fun EightBallUiState.caption(): CaptionContent {
    val question = when (val phase = phase) {
        is BallPhase.Thinking -> phase.question
        is BallPhase.Answered -> phase.question
        else -> ""
    }
    val answerForLargeFont = (phase as? BallPhase.Answered)?.answer?.text()
        ?.takeIf { LocalDensity.current.fontScale >= LARGE_FONT_SCALE }
    return when {
        answerForLargeFont != null -> CaptionContent(CaptionKind.Answer, answerForLargeFont)
        phase is BallPhase.Listening && phase.transcript.isNotEmpty() ->
            CaptionContent(CaptionKind.Transcript, phase.transcript)
        phase is BallPhase.Listening ->
            CaptionContent(CaptionKind.Transcript, stringResource(R.string.hint_listening))
        question.isNotEmpty() -> CaptionContent(CaptionKind.Question, question)
        notice != null -> CaptionContent(
            CaptionKind.Notice,
            stringResource(
                when (notice) {
                    Notice.MicrophoneDenied -> R.string.notice_microphone_denied
                    Notice.VoiceUnsupported -> R.string.notice_voice_unsupported
                }
            )
        )
        else -> CaptionContent(
            CaptionKind.Hint,
            stringResource(if (voice.isReady) R.string.hint_voice else R.string.hint_tap)
        )
    }
}

@Composable
private fun AnswerText.text(): String = when (this) {
    is AnswerText.Plain -> value
    AnswerText.Fallback -> stringResource(R.string.answer_fallback)
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

/** A press shorter than this is a tap; from here on it is a hold, and the app listens. */
private const val HOLD_DELAY_MS = 180L

private const val BALL_WIDTH_FRACTION = 0.76f
private const val BALL_HEIGHT_FRACTION = 0.46f
private val BALL_MAX_SIZE = 420.dp
private val BALL_MIN_SIZE = 96.dp
private val SCREEN_MARGIN = 16.dp
private val CAPTION_GAP = 44.dp
private val CAPTION_MAX_WIDTH = 440.dp
private const val CAPTION_MAX_LINES = 3

/** From this font scale up, the answer is also given in the caption. */
private const val LARGE_FONT_SCALE = 1.3f

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
                )
            ),
            voiceLevel = { 0f },
            reducedMotion = false,
            onAsk = {},
            onHoldStart = {},
            onHoldEnd = {},
            onHoldCancel = {},
            onToggleVoiceQuestion = {},
            onOpenSettings = {}
        )
    }
}
