package com.example.magiceightball.feature.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.components.PredictionTriangle
import com.example.magiceightball.feature.chat.components.PredictionTriangleAnimator
import com.example.magiceightball.core.designsystem.R as DesignR

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    MagicEightBallTheme {
        val pagerState = androidx.wear.compose.foundation.pager.rememberPagerState(pageCount = { 3 })

        androidx.wear.compose.foundation.pager.VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ChatScreenContent(state)
                1 -> LanguageSelectionScreen(
                    selectedLanguage = state.language,
                    onLanguageSelected = { viewModel.setLanguage(it) }
                )
                2 -> PersonalitySelectionScreen(
                    selectedPersonality = state.personality,
                    language = state.language,
                    onPersonalitySelected = { viewModel.setPersonality(it) }
                )
            }
        }
    }
}

@Composable
fun ChatScreenContent(state: ChatState) {
    AnimatedContent(
        targetState = state.machineState,
        transitionSpec = {
            if (targetState is ChatStateMachine.Idle) {
                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) + fadeIn(animationSpec = tween(300))) togetherWith (slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                ) + fadeOut(animationSpec = tween(300)))
            } else {
                (slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) + fadeIn(animationSpec = tween(300))) togetherWith (slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                ) + fadeOut(animationSpec = tween(300)))
            }
        },
        contentKey = { it is ChatStateMachine.Idle },
        label = "ChatScreenTransition"
    ) { machine ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (machine) {
                is ChatStateMachine.Idle -> ChatScreenIdleWrapper(titleRes = state.titleRes)
                else -> ChatScreenPredictionWrapper(machine)
            }
        }
    }
}

@Composable
fun ChatScreenIdleWrapper(titleRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = DesignR.drawable.ic_eight_ball_background),
                contentScale = ContentScale.Crop
            ),
        contentAlignment = Alignment.Center
    ) {
        ChatScreenIdle(titleRes = titleRes)
    }
}

@Composable
fun ChatScreenPredictionWrapper(machineState: ChatStateMachine) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = DesignR.drawable.ic_eight_ball_background),
                contentScale = ContentScale.Crop
            ),
        contentAlignment = Alignment.Center
    ) {
        PredictionTriangleAnimator(
            machineState = machineState,
            content = { modifier, isLoading ->
                PredictionCircle(modifier = modifier, contentOffsetY = 14.dp, innerWidth = 150.dp, innerHeight = 120.dp) { innerModifier ->
                    PredictionTriangle(
                        text = if (machineState is ChatStateMachine.Completed) machineState.answer else "",
                        modifier = innerModifier,
                        isLoading = isLoading
                    )
                }
            }
        )
    }
}

@Composable
fun PredictionCircle(
    modifier: Modifier = Modifier,
    outerSize: Dp = 160.dp,
    innerWidth: Dp = 120.dp,
    innerHeight: Dp = 120.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.Gray,
    backgroundColor: Color = Color.Black,
    contentOffsetY: Dp = 0.dp,
    content: @Composable (Modifier) -> Unit
) {
    Box(
        modifier = modifier
            .size(outerSize)
            .background(color = backgroundColor, shape = CircleShape)
            .border(BorderStroke(borderWidth, borderColor), shape = CircleShape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        content(Modifier.size(width = innerWidth, height = innerHeight).offset(y = contentOffsetY))
    }
}

@Preview(
    name = "ChatScreen - Wear Round Completed",
    showBackground = true,
    widthDp = 320,
    heightDp = 320
)
@Composable
fun ChatScreenPreviewWearRoundCompleted() {
    MagicEightBallTheme {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            ChatScreenContent(
                state = ChatState(
                    machineState = ChatStateMachine.Completed("Outlook good, but uncertain."),
                    titleRes = R.string.title_main
                )
            )
        }
    }
}

@Preview(
    name = "ChatScreen - Wear Round Idle",
    showBackground = true,
    widthDp = 320,
    heightDp = 320
)
@Composable
fun ChatScreenPreviewWearRoundIdle() {
    MagicEightBallTheme {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            ChatScreenContent(
                state = ChatState(
                    machineState = ChatStateMachine.Idle,
                    titleRes = R.string.title_main
                )
            )
        }
    }
}

@Preview(
    name = "ChatScreen - Wear Round Running",
    showBackground = true,
    widthDp = 320,
    heightDp = 320
)
@Composable
fun ChatScreenPreviewWearRoundRunning() {
    MagicEightBallTheme {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            ChatScreenContent(
                state = ChatState(
                    machineState = ChatStateMachine.Running,
                    titleRes = R.string.title_main
                )
            )
        }
    }
}
