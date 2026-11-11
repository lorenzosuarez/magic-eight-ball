package com.example.magiceightball.feature.chat

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
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
import com.example.magiceightball.feature.chat.components.GlassOverDie
import com.example.magiceightball.feature.chat.components.PredictionTriangle
import com.example.magiceightball.feature.chat.components.PredictionTriangleAnimator
import com.example.magiceightball.feature.chat.components.RISEN_DROP
import kotlinx.coroutines.delay
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
    // Once the system no longer shows its dialog, a hold opens the app's page in the system
    // settings instead: on a watch there is no shorter way to the permission.
    var microphoneAskedAtMillis by remember { mutableStateOf(0L) }
    val microphoneRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMicrophonePermissionResult(granted)
        // A refusal that comes back at once is the system's, not the user's.
        val answeredBySystem =
            SystemClock.elapsedRealtime() - microphoneAskedAtMillis < DIALOG_SKIPPED_MILLIS
        if (!granted && answeredBySystem) context.openAppSettings()
    }

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

    var pageDotsShown by remember { mutableStateOf(true) }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            pageDotsShown = true
        } else {
            delay(PAGE_DOTS_LINGER_MILLIS)
            pageDotsShown = false
        }
    }

    // The app arrives from the launch screen rather than replacing it at a stroke.
    val intro = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        withContext(DesignedMotion) { intro.animateTo(1f, tween(INTRO_MILLIS)) }
    }

    ChatEffects(
        effects = viewModel.effects,
        onRequestMicrophonePermission = {
            microphoneAskedAtMillis = SystemClock.elapsedRealtime()
            microphoneRequest.launch(Manifest.permission.RECORD_AUDIO)
        }
    )

    CompositionLocalProvider(LocalAppLanguage provides state.settings.language) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = intro.value }
        ) {
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
            // Dots at the side say there is more below the ball. They show when the app opens
            // and while the pages move, then leave the screen to its content; and never
            // while a question is in progress, when there is only the ball.
            AnimatedVisibility(
                visible = pageDotsShown && state.machineState is ChatStateMachine.Idle,
                enter = fadeIn(tween(PAGE_DOTS_FADE_MILLIS)),
                exit = fadeOut(tween(PAGE_DOTS_FADE_MILLIS))
            ) {
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
                    ChatEffect.NothingHeard -> HapticFeedbackConstants.REJECT
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

/**
 * Waits for every finger to lift, wherever it has wandered. False when another gesture, the
 * pager's swipe for instance, has taken the touch for itself.
 */
private suspend fun AwaitPointerEventScope.awaitLift(): Boolean {
    while (true) {
        val event = awaitPointerEvent()
        if (event.changes.any { it.isConsumed }) return false
        if (event.changes.none { it.pressed }) return true
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

    val askByVoice = localizedString(Strings.AskByVoice)

    // For those who cannot hold: a long click starts listening, and the question ends by itself
    // when they stop talking.
    semantics {
        if (enabled) {
            onLongClick(label = askByVoice) {
                currentOnHoldStart()
                true
            }
        }
    }.pointerInput(enabled) {
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
                // Whatever ends the gesture ends the hold: a lift asks, and anything else
                // (a swipe taken by the pager, the gesture being torn down) cancels.
                var lifted = false
                try {
                    lifted = awaitLift()
                } finally {
                    if (lifted) currentOnHoldEnd() else currentOnHoldCancel()
                }
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
        modifier = Modifier
            .fillMaxSize()
            // The window sits in the ball: a soft shadow on the ball around it.
            .drawWithCache {
                val windowRadius = size.minDimension / 2f * WINDOW_SCREEN_FRACTION
                val reach = windowRadius * (1f + RECESS_REACH)
                val recess = Brush.radialGradient(
                    0f to Color.Black.copy(alpha = RECESS_ALPHA),
                    1f / (1f + RECESS_REACH) to Color.Black.copy(alpha = RECESS_ALPHA),
                    1f to Color.Transparent,
                    radius = reach
                )
                onDrawBehind { drawCircle(recess, radius = reach) }
            },
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
                    isLoading = answer == null,
                    glass = GlassOverRisenDie
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
                // The inner border as a bevel: in shadow at the top, a little light at the bottom.
                val bezel = Brush.verticalGradient(listOf(BallColors.BezelTop, BallColors.BezelBottom))
                // The liquid over the die: clear in the middle, darker towards the glass's edge.
                val glassRadius = innerRimRadius - innerRim.width / 2f
                val depth = Brush.radialGradient(
                    0f to Color.Transparent,
                    DEPTH_CLEAR to Color.Transparent,
                    1f to Color.Black.copy(alpha = DEPTH_ALPHA),
                    radius = glassRadius
                )
                onDrawWithContent {
                    drawCircle(window)
                    drawContent()
                    drawCircle(depth, radius = glassRadius)
                    drawCircle(bezel, radius = innerRimRadius, style = innerRim)
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

/** How long the page dots stay once the pages have stopped moving, and how they come and go. */
private const val PAGE_DOTS_LINGER_MILLIS = 1_800L
private const val PAGE_DOTS_FADE_MILLIS = 250
private const val INTRO_MILLIS = 420

/** No one reads the system's permission dialog and answers it in less than this. */
private const val DIALOG_SKIPPED_MILLIS = 1_000L

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

/** Share of the screen taken by the window (142dp on a 192dp screen): the rest is the ball. */
private const val WINDOW_SCREEN_FRACTION = 0.74f

/** The shadow around the window, as a share of its radius, and how dark it starts. */
private const val RECESS_REACH = 0.14f
private const val RECESS_ALPHA = 0.38f

/** Where the liquid starts to darken, as a share of the glass's radius, and how dark it gets. */
private const val DEPTH_CLEAR = 0.55f
private const val DEPTH_ALPHA = 0.6f

/**
 * Die box relative to the window diameter. Nearly as wide as the window: the risen die fills
 * most of it and its top corners go out of sight, so there is always die under the answer.
 */
private const val DIE_WIDTH_FRACTION = 0.96f
private const val DIE_HEIGHT_FRACTION = 0.86f

/** The glass's diameter relative to the window's: what the borders leave (5dp of 71dp a side). */
private const val GLASS_WINDOW_FRACTION = 0.92f

/**
 * The glass as the risen die has it over itself: the die's box is centred in the window and
 * then dropped by [RISEN_DROP] of its height, and the glass is the window less its two borders.
 */
private val GlassOverRisenDie = GlassOverDie(
    centerY = 0.5f - RISEN_DROP,
    radius = GLASS_WINDOW_FRACTION / 2f / DIE_HEIGHT_FRACTION
)

private val WindowFallbackDiameter = 142.dp
private val WindowRimWidth = 1.dp
private val WindowInnerRimWidth = 4.dp

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

/** The app's page in the system settings, where a refused permission can still be granted. */
private fun Context.openAppSettings() {
    val settings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(settings)
    } catch (_: ActivityNotFoundException) {
        // A watch without that page: the shake still answers.
    }
}
