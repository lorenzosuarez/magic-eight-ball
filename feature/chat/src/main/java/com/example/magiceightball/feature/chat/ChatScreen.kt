package com.example.magiceightball.feature.chat

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.components.BallColors
import com.example.magiceightball.feature.chat.components.PredictionTriangle
import com.example.magiceightball.feature.chat.components.PredictionTriangleAnimator
import kotlinx.coroutines.flow.Flow
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    // Nothing is drawn until the saved choices are known, so no default flashes by first.
    val state = viewModel.state.collectAsStateWithLifecycle().value ?: return
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    // Shakes only count while the prediction page is settled on screen and the app is resumed.
    val isPredictionPageSettled = pagerState.settledPage == PAGE_PREDICTION
    LifecycleResumeEffect(viewModel, isPredictionPageSettled) {
        viewModel.setShakeDetectionEnabled(isPredictionPageSettled)
        onPauseOrDispose { viewModel.setShakeDetectionEnabled(false) }
    }

    ChatHaptics(viewModel.effects)

    CompositionLocalProvider(LocalAppLanguage provides state.settings.language) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                PAGE_PREDICTION -> ChatScreenContent(state)
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
    }
}

/**
 * Plays a haptic for each [ChatEffect], while the screen is at least started.
 */
@Composable
private fun ChatHaptics(effects: Flow<ChatEffect>) {
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(effects, lifecycleOwner, view) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collect { effect ->
                view.performHapticFeedback(
                    when (effect) {
                        ChatEffect.ShakeDetected -> HapticFeedbackConstants.GESTURE_START
                        ChatEffect.AnswerRevealed -> HapticFeedbackConstants.CONFIRM
                    }
                )
            }
        }
    }
}

/**
 * The ball, which is the whole screen: its "8" (idle) or its window with the die (thinking /
 * answer) sliding over it. Only the foreground takes part in the transition.
 */
@Composable
fun ChatScreenContent(state: ChatState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BallColors.Ball)
    ) {
        AnimatedContent(
            targetState = state.machineState,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState is ChatStateMachine.Idle) {
                    ContentTransform(EnterFromStart, ExitToEnd, sizeTransform = null)
                } else {
                    ContentTransform(EnterFromEnd, ExitToStart, sizeTransform = null)
                }
            },
            contentAlignment = Alignment.Center,
            // Thinking and answer share one slot: the die stays put and animates in place.
            contentKey = { it is ChatStateMachine.Idle },
            label = "ChatScreenTransition"
        ) { machine ->
            when (machine) {
                ChatStateMachine.Idle -> ChatScreenIdle(usesAiAnswers = state.usesAiAnswers)
                // One call site for thinking and answer: two would be two different composables,
                // and the die would be rebuilt (losing its motion and cross-fade) on the answer.
                ChatStateMachine.Running, is ChatStateMachine.Completed ->
                    Prediction(answer = (machine as? ChatStateMachine.Completed)?.answer)
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
        // The die comes close enough for its corners to leave the window, which cuts them off;
        // the two borders are drawn over it.
        modifier = modifier
            .clip(CircleShape)
            .drawWithCache {
                val window = Brush.radialGradient(listOf(BallColors.WindowCenter, BallColors.WindowEdge))
                val rim = Stroke(width = WindowRimWidth.toPx())
                val rimRadius = (size.minDimension - rim.width) / 2f
                val innerRim = Stroke(width = WindowInnerRimWidth.toPx())
                val innerRimRadius = rimRadius - rim.width / 2f - WindowRimGap.toPx() - innerRim.width / 2f
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
                y = (diameter - dieHeight) / 2 + (diameter * DIE_DROP_FRACTION).roundToInt()
            )
        }
    }
}

private const val PAGE_PREDICTION = 0
private const val PAGE_LANGUAGE = 1
private const val PAGE_PERSONALITY = 2
private const val PAGE_AI_ANSWERS = 3
private const val PAGE_COUNT = 4

private const val TRANSITION_MILLIS = 300
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
private const val DIE_DROP_FRACTION = 0.07f

private val WindowFallbackDiameter = 160.dp
private val WindowRimWidth = 1.dp
private val WindowRimGap = 1.5.dp
private val WindowInnerRimWidth = 1.5.dp

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
