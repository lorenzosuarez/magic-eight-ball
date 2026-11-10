package com.example.magiceightball.feature.chat

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.VerticalPageIndicator
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.components.BallColors
import com.example.magiceightball.feature.chat.components.DesignedMotion
import com.example.magiceightball.feature.chat.components.PredictionTriangle
import com.example.magiceightball.feature.chat.components.PredictionTriangleAnimator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    // Nothing is drawn until the saved choices are known, so no default flashes by first.
    val state = viewModel.state.collectAsStateWithLifecycle().value ?: return
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val context = LocalContext.current
    val microphoneRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onMicrophonePermissionResult
    )

    // Shakes only count while the prediction page is settled on screen and the app is resumed.
    val isPredictionPageSettled = pagerState.settledPage == PAGE_PREDICTION
    LifecycleResumeEffect(viewModel, isPredictionPageSettled) {
        viewModel.onMicrophonePermissionChecked(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
        viewModel.setShakeDetectionEnabled(isPredictionPageSettled)
        onPauseOrDispose { viewModel.setShakeDetectionEnabled(false) }
    }

    // The voice level is smoothed here and only ever read while drawing the rings.
    val voiceLevel = remember { Animatable(0f) }
    LaunchedEffect(viewModel) {
        withContext(DesignedMotion) {
            viewModel.voiceLevel.collectLatest { level ->
                voiceLevel.animateTo(level, spring(stiffness = Spring.StiffnessMedium))
            }
        }
    }

    ChatEffects(
        effects = viewModel.effects,
        onRequestMicrophonePermission = { microphoneRequest.launch(Manifest.permission.RECORD_AUDIO) }
    )

    CompositionLocalProvider(LocalAppLanguage provides state.settings.language) {
        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    PAGE_PREDICTION -> ChatScreenContent(
                        state = state,
                        voiceLevel = { voiceLevel.value },
                        onHoldStart = viewModel::onHoldStart,
                        onHoldEnd = viewModel::onHoldEnd,
                        onHoldCancel = viewModel::onHoldCancel
                    )
                    PAGE_LANGUAGE -> LanguageSelectionScreen(
                        selectedLanguage = state.settings.language,
                        onLanguageSelected = viewModel::setLanguage
                    )
                    PAGE_PERSONALITY -> PersonalitySelectionScreen(
                        selectedPersonality = state.settings.personality,
                        onPersonalitySelected = viewModel::setPersonality
                    )
                    PAGE_AI_ANSWERS -> AiAnswersSelectionScreen(
                        isEnabled = state.settings.aiAnswersEnabled,
                        isAvailable = state.aiAnswersAvailable,
                        onEnabledChange = viewModel::setAiAnswersEnabled
                    )
                }
            }
            // Dots at the side say there is more below the ball. They step aside while a
            // question is in progress, when there is only the ball.
            if (state.machineState is ChatStateMachine.Idle) {
                VerticalPageIndicator(
                    pagerState = pagerState,
                    selectedColor = BallColors.OnSurface,
                    unselectedColor = BallColors.OnSurface.copy(alpha = PAGE_DOT_OFF_ALPHA)
                )
            }
        }
    }
}

/**
 * Acts on each [ChatEffect] while the screen is at least started: a haptic for what the ball
 * does, and the system's dialog when the microphone has to be asked for.
 */
@Composable
private fun ChatEffects(effects: Flow<ChatEffect>, onRequestMicrophonePermission: () -> Unit) {
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnRequestMicrophonePermission by rememberUpdatedState(onRequestMicrophonePermission)

    LaunchedEffect(effects, lifecycleOwner, view) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collect { effect ->
                val haptic = when (effect) {
                    ChatEffect.RequestMicrophonePermission -> {
                        currentOnRequestMicrophonePermission()
                        null
                    }
                    ChatEffect.ShakeDetected -> HapticFeedbackConstants.GESTURE_START
                    ChatEffect.ListeningStarted -> HapticFeedbackConstants.LONG_PRESS
                    ChatEffect.ListeningStopped -> HapticFeedbackConstants.GESTURE_END
                    ChatEffect.AnswerRevealed -> HapticFeedbackConstants.CONFIRM
                }
                haptic?.let(view::performHapticFeedback)
            }
        }
    }
}

/**
 * The ball, which is the whole screen: its "8" (at rest, and while it listens) or its window
 * with the die (thinking / answer) sliding over it. Where the app may listen, holding the
 * screen asks a question out loud for as long as the hold lasts.
 *
 * @param voiceLevel how loud the voice is while listening, from 0 to 1; read while drawing only.
 */
@Composable
fun ChatScreenContent(
    state: ChatState,
    modifier: Modifier = Modifier,
    voiceLevel: () -> Float = { 0f },
    onHoldStart: () -> Unit = {},
    onHoldEnd: () -> Unit = {},
    onHoldCancel: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BallColors.Ball)
            .holdToAsk(state.canAskByVoice, onHoldStart, onHoldEnd, onHoldCancel)
    ) {
        AnimatedContent(
            targetState = state.machineState,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState.showsTheEight) {
                    ContentTransform(EnterFromStart, ExitToEnd, sizeTransform = null)
                } else {
                    ContentTransform(EnterFromEnd, ExitToStart, sizeTransform = null)
                }
            },
            contentAlignment = Alignment.Center,
            // Rest and listening share one slot, and so do thinking and answer: within a slot
            // nothing slides, things change in place.
            contentKey = { it.showsTheEight },
            label = "ChatScreenTransition"
        ) { machine ->
            when (machine) {
                ChatStateMachine.Idle, is ChatStateMachine.Listening -> ChatScreenIdle(
                    usesAiAnswers = state.usesAiAnswers,
                    canAskByVoice = state.canAskByVoice,
                    listening = machine as? ChatStateMachine.Listening,
                    voiceLevel = voiceLevel
                )
                // One call site for thinking and answer: two would be two different composables,
                // and the die would be rebuilt (losing its motion and cross-fade) on the answer.
                ChatStateMachine.Running, is ChatStateMachine.Completed ->
                    Prediction(answer = (machine as? ChatStateMachine.Completed)?.answer)
            }
        }
    }
}

private val ChatStateMachine.showsTheEight: Boolean
    get() = this is ChatStateMachine.Idle || this is ChatStateMachine.Listening

/**
 * Calls [onHoldStart] once a finger has stayed down for [HOLD_DELAY_MS], then [onHoldEnd] when
 * it lifts or [onHoldCancel] when the gesture is taken away, as a swipe to another page does.
 * Nothing is consumed, so the pager keeps its swipes.
 */
private fun Modifier.holdToAsk(
    enabled: Boolean,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit
): Modifier = composed {
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnHoldEnd by rememberUpdatedState(onHoldEnd)
    val currentOnHoldCancel by rememberUpdatedState(onHoldCancel)

    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            val stillDown = try {
                withTimeout(HOLD_DELAY_MS) { waitForUpOrCancellation() }
                false
            } catch (_: PointerEventTimeoutCancellationException) {
                true
            }
            if (stillDown) {
                currentOnHoldStart()
                if (waitForUpOrCancellation() != null) currentOnHoldEnd() else currentOnHoldCancel()
            }
        }
    }
}

/**
 * The die in its window. A null [answer] means the ball is still thinking.
 */
@Composable
private fun Prediction(answer: AnswerText?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PredictionWindow(Modifier.fillMaxSize(WINDOW_SCREEN_FRACTION)) {
            PredictionTriangleAnimator(isThinking = answer == null) {
                PredictionTriangle(
                    text = when (answer) {
                        null -> ""
                        is AnswerText.Plain -> answer.value
                        AnswerText.Fallback -> localizedString(Strings.AnswerFallback)
                    },
                    isLoading = answer == null
                )
            }
        }
    }
}

/**
 * The round window of the ball. It takes the largest circle that fits and gives [content] the
 * box of the triangular die, all in proportion to that circle so it scales with the screen.
 */
@Composable
private fun PredictionWindow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        // The die comes close enough for its corners to leave the window, which cuts them off.
        // The two borders, one against the other, are drawn over it, so the die is only ever
        // seen inside them.
        modifier = modifier
            .clip(CircleShape)
            .drawWithCache {
                val window = Brush.radialGradient(listOf(BallColors.WindowCenter, BallColors.WindowEdge))
                val rim = Stroke(width = WindowRimWidth.toPx())
                val rimRadius = (size.minDimension - rim.width) / 2f
                val innerRim = Stroke(width = WindowInnerRimWidth.toPx())
                val innerRimRadius = rimRadius - rim.width / 2f - innerRim.width / 2f
                onDrawWithContent {
                    drawCircle(window)
                    drawContent()
                    drawCircle(BallColors.WindowRimInner, radius = innerRimRadius, style = innerRim)
                    drawCircle(BallColors.WindowRim, radius = rimRadius, style = rim)
                }
            }
    ) { measurables, constraints ->
        val diameter = min(constraints.maxWidth, constraints.maxHeight)
            .takeIf { it != Constraints.Infinity }
            ?: WindowFallbackDiameter.roundToPx()
        val dieWidth = (diameter * DIE_WIDTH_FRACTION).roundToInt()
        val dieHeight = (diameter * DIE_HEIGHT_FRACTION).roundToInt()
        val die = measurables.single().measure(Constraints.fixed(dieWidth, dieHeight))

        layout(diameter, diameter) {
            die.place(
                x = (diameter - dieWidth) / 2,
                y = (diameter - dieHeight) / 2
            )
        }
    }
}

private const val PAGE_PREDICTION = 0
private const val PAGE_LANGUAGE = 1
private const val PAGE_PERSONALITY = 2
private const val PAGE_AI_ANSWERS = 3
private const val PAGE_COUNT = 4
private const val PAGE_DOT_OFF_ALPHA = 0.35f

private const val TRANSITION_MILLIS = 300

/** A press shorter than this is not a hold: it leaves room for taps and swipes. */
private const val HOLD_DELAY_MS = 250L
private val EnterFromEnd =
    slideInHorizontally(tween(TRANSITION_MILLIS)) { it } + fadeIn(tween(TRANSITION_MILLIS))
private val EnterFromStart =
    slideInHorizontally(tween(TRANSITION_MILLIS)) { -it } + fadeIn(tween(TRANSITION_MILLIS))
private val ExitToEnd =
    slideOutHorizontally(tween(TRANSITION_MILLIS)) { it } + fadeOut(tween(TRANSITION_MILLIS))
private val ExitToStart =
    slideOutHorizontally(tween(TRANSITION_MILLIS)) { -it } + fadeOut(tween(TRANSITION_MILLIS))

/** Share of the screen taken by the window (160dp on a 192dp screen). */
private const val WINDOW_SCREEN_FRACTION = 0.83f

/**
 * Die box relative to the window diameter. Nearly as wide as the window: the risen die fills
 * most of it and its top corners go out of sight, so there is always die under the answer.
 */
private const val DIE_WIDTH_FRACTION = 0.96f
private const val DIE_HEIGHT_FRACTION = 0.86f

private val WindowFallbackDiameter = 160.dp
private val WindowRimWidth = 1.dp
private val WindowInnerRimWidth = 2.5.dp

@Preview(name = "ChatScreen - Wear Round Completed", showBackground = true, widthDp = 192, heightDp = 192)
@Composable
private fun ChatScreenPreviewWearRoundCompleted() {
    ChatScreenPreview(ChatStateMachine.Completed(AnswerText.Plain("Outlook good, but uncertain.")))
}

@Preview(name = "ChatScreen - Wear Round Idle", showBackground = true, widthDp = 192, heightDp = 192)
@Composable
private fun ChatScreenPreviewWearRoundIdle() {
    ChatScreenPreview(ChatStateMachine.Idle)
}

@Preview(name = "ChatScreen - Wear Round Running", showBackground = true, widthDp = 192, heightDp = 192)
@Composable
private fun ChatScreenPreviewWearRoundRunning() {
    ChatScreenPreview(ChatStateMachine.Running)
}

@Preview(name = "ChatScreen - Wear Large Round Fallback", showBackground = true, widthDp = 240, heightDp = 240)
@Composable
private fun ChatScreenPreviewWearLargeRoundFallback() {
    ChatScreenPreview(
        ChatStateMachine.Completed(AnswerText.Fallback),
        screenSizeDp = 240
    )
}

@Composable
private fun ChatScreenPreview(machineState: ChatStateMachine, screenSizeDp: Int = 192) {
    MagicEightBallTheme {
        ChatScreenContent(
            state = ChatState(machineState = machineState),
            modifier = Modifier.size(screenSizeDp.dp)
        )
    }
}
