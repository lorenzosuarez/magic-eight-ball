package com.lorenzosuarez.magiceightball.phone.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallEffect
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.EightBallViewModel
import com.lorenzosuarez.magiceightball.phone.Notice
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import com.lorenzosuarez.magiceightball.phone.R
import kotlinx.coroutines.delay
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The screen wired to its [viewModel] and to the platform: the microphone permission, the
 * lifecycle, haptics, sharing and the settings sheet.
 *
 * @param onAnswerDismissed called when the user closes an answer they have read.
 * @param onShowAdPrivacyOptions opens the ad privacy choices; null where none have to be offered.
 */
@Composable
fun EightBallScreen(
    viewModel: EightBallViewModel,
    onAnswerDismissed: () -> Unit = {},
    onShowAdPrivacyOptions: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The permission asked for from the settings sheet differs in one way: when the system no
    // longer shows its dialog, the user is taken to the app's page in the system settings.
    // The same goes for the "Allow" offered next to the explanation of a refusal.
    var askedFromSettings by remember { mutableStateOf(false) }
    var askedAtMillis by remember { mutableStateOf(0L) }
    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMicrophonePermissionResult(granted)
        // A refusal that comes back at once is the system's, not the user's: nobody who has
        // just said no in the dialog is sent to the settings.
        val answeredBySystem = SystemClock.elapsedRealtime() - askedAtMillis < DIALOG_SKIPPED_MS
        if (!granted && askedFromSettings && answeredBySystem && !context.mayShowMicrophoneDialog()) {
            context.openAppSettings()
        }
        askedFromSettings = false
    }
    val askForMicrophone = { fromSettings: Boolean ->
        askedFromSettings = fromSettings
        askedAtMillis = SystemClock.elapsedRealtime()
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
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
                    askForMicrophone(false)
                    null
                }
                EightBallEffect.ListeningStarted -> HapticFeedbackConstantsCompat.LONG_PRESS
                EightBallEffect.ListeningStopped -> HapticFeedbackConstantsCompat.GESTURE_END
                EightBallEffect.AskStarted -> HapticFeedbackConstantsCompat.CONTEXT_CLICK
                EightBallEffect.AnswerRevealed -> HapticFeedbackConstantsCompat.CONFIRM
                EightBallEffect.NoticeGiven -> HapticFeedbackConstantsCompat.REJECT
            }
            feedback?.let { ViewCompat.performHapticFeedback(view, it) }
        }
    }
    val tick = { feedback: Int -> ViewCompat.performHapticFeedback(view, feedback); Unit }

    // While the ball thinks, the die is felt coming up through the liquid: a few of the
    // faintest ticks, further and further apart, and then nothing until the answer lands.
    val thinking = state?.phase is BallPhase.Thinking
    LaunchedEffect(thinking, view) {
        if (!thinking) return@LaunchedEffect
        var pause = THINKING_TICK_MS
        repeat(THINKING_TICKS) {
            delay(pause)
            tick(HapticFeedbackConstantsCompat.TEXT_HANDLE_MOVE)
            pause += THINKING_TICK_SLOWDOWN_MS
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

    // The app arrives rather than appears: from the launch screen, which shows the ball on the
    // window's plain colour, the ball grows to its size while everything else fades in.
    val intro = remember { Animatable(0f) }
    val ready = state != null
    DesignedMotionEffect(ready) {
        if (ready) intro.animateTo(1f, tween(INTRO_MS))
    }
    val playStoreUrl = PLAY_STORE_URL + context.packageName
    val shareAnswerText = stringResource(R.string.share_answer_text, playStoreUrl)

    Box(modifier = Modifier.fillMaxSize()) {
        // Nothing but the window's own background until the saved choices are known.
        val current = state ?: return@Box
        ProvideAppLanguage(current.settings.language) {
            // Said once, as the die comes up with it; never again for the same answer.
            val voice = rememberAnswerVoice()
            val answered = current.phase as? BallPhase.Answered
            val answerToSay = answered?.answer?.text()
            val answerLocale = Locale.forLanguageTag(current.settings.language.code)
            val speaksAnswers = current.settings.spokenAnswersEnabled
            var saidAnswerId by rememberSaveable { mutableStateOf<Long?>(null) }
            LaunchedEffect(voice, answered?.id, speaksAnswers) {
                if (answerToSay == null) {
                    voice.hush()
                    return@LaunchedEffect
                }
                if (!speaksAnswers || answered.id == saidAnswerId) return@LaunchedEffect
                saidAnswerId = answered.id
                delay(SPOKEN_ANSWER_DELAY_MS)
                voice.say(answerToSay, answerLocale)
            }
            val shareAppText = stringResource(R.string.share_app_text, playStoreUrl)
            EightBallContent(
                state = current,
                now = now,
                intro = { intro.value },
                voiceLevel = { voiceLevel.value },
                still = false,
                onAsk = viewModel::ask,
                onHoldStart = viewModel::onHoldStart,
                onHoldEnd = viewModel::onHoldEnd,
                onHoldCancel = viewModel::onHoldCancel,
                onToggleVoiceQuestion = viewModel::toggleVoiceQuestion,
                onDismiss = {
                    // Closing an answer, as opposed to cancelling a question, is told apart:
                    // it is the one moment the app may use for something of its own.
                    val closesAnAnswer = current.phase is BallPhase.Answered
                    tick(HapticFeedbackConstantsCompat.GESTURE_END)
                    viewModel.dismiss()
                    if (closesAnAnswer) onAnswerDismissed()
                },
                onDeleteHistoryEntry = {
                    tick(HapticFeedbackConstantsCompat.CLOCK_TICK)
                    viewModel.deleteHistoryEntry(it)
                },
                onRestoreHistoryEntry = {
                    tick(HapticFeedbackConstantsCompat.CLOCK_TICK)
                    viewModel.restoreHistoryEntry(it)
                },
                onClearHistory = {
                    tick(HapticFeedbackConstantsCompat.CLOCK_TICK)
                    viewModel.clearHistory()
                },
                onSpeakAnswer = {
                    tick(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                    answerToSay?.let { voice.say(it, answerLocale) }
                },
                options = {
                    SettingsContent(
                        state = current,
                        onLanguageSelected = {
                            tick(HapticFeedbackConstantsCompat.SEGMENT_TICK)
                            viewModel.setLanguage(it)
                        },
                        onPersonalitySelected = {
                            tick(HapticFeedbackConstantsCompat.SEGMENT_TICK)
                            viewModel.setPersonality(it)
                        },
                        onAiAnswersChange = {
                            tick(it.toggleFeedback())
                            viewModel.setAiAnswersEnabled(it)
                        },
                        onVoiceQuestionsChange = {
                            tick(it.toggleFeedback())
                            viewModel.setVoiceQuestionsEnabled(it)
                        },
                        onAllowMicrophone = { askForMicrophone(true) },
                        onSpokenAnswersChange = {
                            tick(it.toggleFeedback())
                            viewModel.setSpokenAnswersEnabled(it)
                        },
                        onPartyNamesChange = viewModel::setPartyNames,
                        onShareApp = { context.shareText(shareAppText) },
                        onShowAdPrivacyOptions = onShowAdPrivacyOptions,
                    )
                },
                onAllowMicrophone = { askForMicrophone(true) },
                onAskWho = viewModel::askWho,
                onShareAnswer = { picture ->
                    tick(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                    context.shareAnswer(picture, shareAnswerText)
                }
            )
        }
    }
}

/**
 * The stateless screen, in two pages side by side with dots that tell which one is in front:
 * the ball, where the app opens, and to its left the history.
 *
 * On the ball's page everything has one place: what to do above the ball, the ball, and what
 * was heard under it. Holding the ball listens to a question for as long as the hold lasts; a
 * tap asks without one. While a question is in progress there is only that page, with
 * a way to cancel.
 *
 * @param now the current time, for the history's "5 min ago".
 * @param intro how far the app has arrived from the launch screen, from 0 to 1; read while
 * drawing only.
 * @param voiceLevel read while drawing only; see [EightBall].
 * @param onDeleteHistoryEntry deletes at once; the screen then offers to undo it, which calls
 * [onRestoreHistoryEntry].
 * @param initialPage the page in front at first: [BALL_PAGE] or [HISTORY_PAGE].
 */
@Composable
internal fun EightBallContent(
    state: EightBallUiState,
    now: Long,
    voiceLevel: () -> Float,
    still: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteHistoryEntry: (Long) -> Unit,
    onRestoreHistoryEntry: (HistoryEntry) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    onAllowMicrophone: () -> Unit = {},
    onShareAnswer: (ImageBitmap) -> Unit = {},
    onAskWho: (String) -> Unit = {},
    onSpeakAnswer: () -> Unit = {},
    options: @Composable () -> Unit = {},
    initialPage: Int = BALL_PAGE,
    intro: () -> Float = { 1f }
) {
    val atRest = state.phase is BallPhase.Idle
    val busy = state.phase is BallPhase.Listening || state.phase is BallPhase.Thinking
    val pagerState = rememberPagerState(initialPage = initialPage) { PAGE_COUNT }
    val onBallPage = pagerState.currentPage == BALL_PAGE
    val scope = rememberCoroutineScope()
    val shareLayer = rememberGraphicsLayer()

    // Deleting needs no confirmation because it can be taken back for a moment.
    val snackbars = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.history_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    val deleteWithUndo: (HistoryEntry) -> Unit = { entry ->
        onDeleteHistoryEntry(entry.id)
        scope.launch {
            // One at a time: the latest deletion is the one that can be undone.
            snackbars.currentSnackbarData?.dismiss()
            val result = snackbars.showSnackbar(deletedMessage, undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) onRestoreHistoryEntry(entry)
        }
    }

    // A question, however it was started, brings the ball to the front.
    LaunchedEffect(atRest) {
        if (!atRest) pagerState.animateScrollToPage(BALL_PAGE)
    }
    BackHandler(enabled = atRest && !onBallPage) {
        scope.launch { pagerState.animateScrollToPage(BALL_PAGE) }
    }
    // Back closes a question in progress or an answer, like the close button.
    BackHandler(enabled = !atRest, onBack = onDismiss)

    val backdrop = Backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            // Over the window's plain colour, which the launch screen leaves behind.
            .drawBehind { drawRect(backdrop, alpha = intro()) }
    ) {
        // Never seen here: the picture an answer is shared as, ready for when it is asked for.
        (state.phase as? BallPhase.Answered)?.let { answered ->
            key(answered.id) {
                ShareCard(
                    question = answered.question,
                    answer = answered.answer.text(),
                    layer = shareLayer,
                    fromAi = (answered.answer as? AnswerText.Plain)?.fromAi == true
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = TOP_BAR_HEIGHT),
            userScrollEnabled = atRest
        ) { page ->
            if (page == OPTIONS_PAGE) {
                options()
            } else if (page == HISTORY_PAGE) {
                HistoryContent(
                    entries = state.history,
                    now = now,
                    onDelete = deleteWithUndo,
                    onClearAll = onClearHistory,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Stage(
                    state = state,
                    intro = intro,
                    voiceLevel = voiceLevel,
                    still = still,
                    onAsk = onAsk,
                    onHoldStart = onHoldStart,
                    onHoldEnd = onHoldEnd,
                    onHoldCancel = onHoldCancel,
                    onToggleVoiceQuestion = onToggleVoiceQuestion,
                    onAllowMicrophone = onAllowMicrophone,
                    onShareAnswer = { scope.launch { onShareAnswer(shareLayer.toImageBitmap()) } },
                    onAskWho = onAskWho
                )
            }
        }

        SnackbarHost(
            hostState = snackbars,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        )

        // Over both pages: where the user is, and the ways out of here.
        Box(
            modifier = Modifier
                .graphicsLayer { alpha = intro() }
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxWidth()
                .height(TOP_BAR_HEIGHT)
                .padding(horizontal = 8.dp)
        ) {
            if (!atRest) {
                BarButton(
                    icon = R.drawable.ic_close,
                    description = stringResource(if (busy) R.string.action_cancel else R.string.action_close),
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                // With an answer on the die, the one thing left to do with it from up here: hear it.
                if (state.phase is BallPhase.Answered) {
                    BarButton(
                        icon = R.drawable.ic_volume,
                        description = stringResource(R.string.action_read_aloud),
                        onClick = onSpeakAnswer,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            } else {
                // From the ball, each neighbour is named at the side it lies on.
                if (onBallPage) {
                    LabeledBarButton(
                        icon = R.drawable.ic_history,
                        label = stringResource(R.string.history_title),
                        onClick = { scope.launch { pagerState.animateScrollToPage(HISTORY_PAGE) } },
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    LabeledBarButton(
                        icon = R.drawable.ic_settings,
                        label = stringResource(R.string.settings_title),
                        onClick = { scope.launch { pagerState.animateScrollToPage(OPTIONS_PAGE) } },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // A neighbour's own name stands over the dots, where the ball has none.
                    val title = when (pagerState.currentPage) {
                        HISTORY_PAGE -> R.string.history_title
                        OPTIONS_PAGE -> R.string.settings_title
                        else -> null
                    }
                    if (title != null) {
                        Text(
                            text = stringResource(title),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    PageDots(current = pagerState.currentPage)
                }
            }
        }
    }
}

/** A way to a neighbouring page: its icon with its name under it, so nobody has to guess what the icon means. */
@Composable
private fun LabeledBarButton(icon: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .widthIn(min = 64.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun BarButton(icon: Int, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconButton(icon = icon, description = description, onClick = onClick, modifier = modifier)
}

/** One dot per page, the one in front brighter. Decoration: the pages announce themselves. */
@Composable
private fun PageDots(current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(PAGE_COUNT) { page ->
            Box(
                Modifier
                    .size(7.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (page == current) 1f else DOT_OFF_ALPHA
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * The ball's page. From top to bottom, each in a place that never moves: what to do, the ball
 * and what was heard.
 */
@Composable
private fun Stage(
    state: EightBallUiState,
    intro: () -> Float,
    voiceLevel: () -> Float,
    still: Boolean,
    onAsk: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onHoldCancel: () -> Unit,
    onToggleVoiceQuestion: () -> Unit,
    onAllowMicrophone: () -> Unit,
    onShareAnswer: () -> Unit,
    onAskWho: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val ballSize = minOf(
            minOf(maxWidth * BALL_WIDTH_FRACTION, BALL_MAX_SIZE),
            // The column is centred, so the footer's room is taken from both ends.
            maxHeight - TITLE_HEIGHT - CAPTION_HEIGHT - BALL_GAP * 2 - FOOTER_HEIGHT * 2
        ).coerceAtLeast(BALL_MIN_SIZE)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title(
                state = state,
                modifier = Modifier
                    .graphicsLayer { alpha = intro() }
                    .padding(horizontal = 32.dp)
                    .widthIn(max = TEXT_MAX_WIDTH)
                    .fillMaxWidth()
                    .heightIn(min = TITLE_HEIGHT)
            )
            Spacer(Modifier.height(BALL_GAP))
            Ball(
                state = state,
                voiceLevel = voiceLevel,
                still = still,
                onAsk = onAsk,
                onHoldStart = onHoldStart,
                onHoldEnd = onHoldEnd,
                onHoldCancel = onHoldCancel,
                onToggleVoiceQuestion = onToggleVoiceQuestion,
                modifier = Modifier
                    .size(ballSize)
                    .graphicsLayer {
                        // From the size the launch screen draws it at, to its own.
                        val from = (LAUNCH_BALL_SIZE.toPx() / size.minDimension).coerceAtMost(1f)
                        val scale = from + (1f - from) * intro()
                        scaleX = scale
                        scaleY = scale
                    }
            )
            Spacer(Modifier.height(BALL_GAP))
            Caption(
                state = state,
                onAllowMicrophone = onAllowMicrophone,
                onShareAnswer = onShareAnswer,
                onAskWho = onAskWho,
                modifier = Modifier
                    .graphicsLayer { alpha = intro() }
                    .padding(horizontal = 32.dp)
                    .widthIn(max = TEXT_MAX_WIDTH)
                    .fillMaxWidth()
                    .heightIn(min = CAPTION_HEIGHT)
            )
        }

        // At rest only, and never under an explanation: the quietest thing on the screen.
        AnimatedVisibility(
            visible = state.phase is BallPhase.Idle && state.notice == null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .graphicsLayer { alpha = intro() },
            enter = fadeIn(tween(durationMillis = 400)),
            exit = fadeOut(tween(durationMillis = 120))
        ) {
            Footer(poweredByAi = state.usesAiAnswers)
        }
    }
}

/**
 * What the app runs on and which version it is, one under the other.
 *
 * @param poweredByAi false while the answers are the bundled ones: the line is then left out
 * rather than said of answers no AI wrote.
 */
@Composable
private fun Footer(poweredByAi: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.height(FOOTER_HEIGHT),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (poweredByAi) {
            Text(
                text = stringResource(R.string.footer_powered_by_ai).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = FOOTER_ALPHA)
            )
        }
        Text(
            text = stringResource(R.string.footer_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = FOOTER_VERSION_ALPHA)
        )
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
    still: Boolean,
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
        still = still,
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
                                // Ended by lifting, wherever the finger has wandered while
                                // talking; cancelled by anything else that ends the gesture.
                                var lifted = false
                                try {
                                    lifted = awaitLift()
                                } finally {
                                    if (lifted) currentOnHoldEnd() else currentOnHoldCancel()
                                }
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
 * Above the ball, one line that says where things stand: what to do at rest, that the app is
 * listening or thinking, and with an answer, the question it answers.
 */
@Composable
private fun Title(state: EightBallUiState, modifier: Modifier = Modifier) {
    val voiceReady = state.voice.isReady
    val turnName = state.turnName
    AnimatedContent(
        targetState = state.phase,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.BottomCenter,
        contentKey = { (it as? BallPhase.Answered)?.id ?: it::class },
        label = "title"
    ) { phase ->
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            when (phase) {
                is BallPhase.Answered -> QuietText(phase.question.quoted(), maxLines = 2)
                else -> Text(
                    text = when {
                        phase is BallPhase.Listening -> stringResource(R.string.hint_listening)
                        phase is BallPhase.Thinking -> stringResource(R.string.hint_thinking)
                        // At a party the line says whose turn it is; how to ask, they know by now.
                        turnName != null -> stringResource(R.string.party_turn, turnName)
                        else -> stringResource(if (voiceReady) R.string.hint_hold else R.string.hint_tap)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Under the ball, the user's own words: as they are heard, then while the ball thinks about
 * them. At rest it is empty, but for the one-off explanations about the microphone; under an
 * answer the AI service wrote, a quiet note says so.
 *
 * The text on the die is as large as the die allows and cannot follow the system's font size,
 * so with a large font the answer is written here as well.
 */
@Composable
private fun Caption(
    state: EightBallUiState,
    onAllowMicrophone: () -> Unit,
    onShareAnswer: () -> Unit,
    onAskWho: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notice = state.notice
    val whoQuestion = stringResource(R.string.party_who)
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
            BallPhase.Idle -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuietText(
                    text = when (notice) {
                        Notice.MicrophoneDenied -> stringResource(R.string.notice_microphone_denied)
                        Notice.VoiceUnsupported -> stringResource(R.string.notice_voice_unsupported)
                        null -> ""
                    },
                    modifier = Modifier.semantics { if (notice != null) liveRegion = LiveRegionMode.Polite }
                )
                // The way out of a refusal, right where it is explained.
                if (notice == Notice.MicrophoneDenied) {
                    TextButton(onClick = onAllowMicrophone, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.settings_voice_allow))
                    }
                }
                // At a party, the other thing the ball can be asked: to pick one of the players.
                if (notice == null && state.settings.isParty) {
                    TextButton(onClick = { onAskWho(whoQuestion) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(whoQuestion)
                    }
                }
            }
            // Until the first words arrive, this is where they will be written.
            is BallPhase.Listening -> if (phase.transcript.isEmpty()) {
                QuietText(stringResource(R.string.hint_speak))
            } else Text(
                text = phase.transcript,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = CAPTION_MAX_LINES,
                overflow = TextOverflow.Ellipsis
            )
            is BallPhase.Thinking -> QuietText(phase.question.quoted())
            is BallPhase.Answered -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (largeFont) {
                    Text(
                        text = phase.answer.text(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                // Said quietly, and only when it is true: who wrote this answer.
                if ((phase.answer as? AnswerText.Plain)?.fromAi == true) {
                    Text(
                        text = stringResource(R.string.answer_from_ai),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AI_NOTE_ALPHA),
                        textAlign = TextAlign.Center
                    )
                }
                TextButton(onClick = onShareAnswer, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_share))
                }
            }
        }
    }
}

@Composable
private fun QuietText(text: String, modifier: Modifier = Modifier, maxLines: Int = CAPTION_MAX_LINES) {
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

/**
 * Waits for every finger to lift, inside the ball or not: a thumb drifts while its owner talks.
 * False when another gesture, a scroll for instance, has taken the touch for itself.
 */
private suspend fun AwaitPointerEventScope.awaitLift(): Boolean {
    while (true) {
        val event = awaitPointerEvent()
        if (event.changes.any { it.isConsumed }) return false
        if (event.changes.none { it.pressed }) return true
    }
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

private fun Boolean.toggleFeedback(): Int =
    if (this) HapticFeedbackConstantsCompat.TOGGLE_ON else HapticFeedbackConstantsCompat.TOGGLE_OFF

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

/** Where the app lives in the store; the app's own id completes it. */
private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="

/** A press shorter than this is a tap; from here on it is a hold, and the app listens. */
private const val HOLD_DELAY_MS = 180L

private const val CLOCK_TICK_MS = 30_000L

/** The answer is said as its text appears on the die, not before. */
private const val SPOKEN_ANSWER_DELAY_MS = 450L

private const val THINKING_TICKS = 5
private const val THINKING_TICK_MS = 180L
private const val THINKING_TICK_SLOWDOWN_MS = 70L

/** No one reads the system's permission dialog and answers it in less than this. */
private const val DIALOG_SKIPPED_MS = 1_000L

/** The arrival from the launch screen, and the size that screen draws the ball at. */
private const val INTRO_MS = 520
private val LAUNCH_BALL_SIZE = 192.dp

internal const val HISTORY_PAGE = 0
internal const val BALL_PAGE = 1
internal const val OPTIONS_PAGE = 2
private const val PAGE_COUNT = 3
private const val DOT_OFF_ALPHA = 0.35f

private const val BALL_WIDTH_FRACTION = 0.58f
private val BALL_MAX_SIZE = 320.dp
private val BALL_MIN_SIZE = 96.dp

/** Wide enough for the ball to grow into when it shows an answer. */
private val BALL_GAP = 56.dp
private val TOP_BAR_HEIGHT = 72.dp
private val TEXT_MAX_WIDTH = 480.dp
private val TITLE_HEIGHT = 64.dp
private val CAPTION_HEIGHT = 104.dp
private val FOOTER_HEIGHT = 48.dp
private const val FOOTER_ALPHA = 0.42f
private const val FOOTER_VERSION_ALPHA = 0.28f
private const val CAPTION_MAX_LINES = 3

/** How faint the note under an answer the AI wrote is. */
private const val AI_NOTE_ALPHA = 0.7f

/** From this font scale up, the answer is also written under the ball. */
private const val LARGE_FONT_SCALE = 1.3f

@Preview
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
            still = false,
            onAsk = {},
            onHoldStart = {},
            onHoldEnd = {},
            onHoldCancel = {},
            onToggleVoiceQuestion = {},
            onDismiss = {},
            onDeleteHistoryEntry = {},
            onRestoreHistoryEntry = {},
            onClearHistory = {},
        )
    }
}
