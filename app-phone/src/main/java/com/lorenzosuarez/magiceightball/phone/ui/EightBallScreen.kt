package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import com.example.magiceightball.core.domain.gateway.ReportReason
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.res.pluralStringResource
import androidx.core.app.NotificationManagerCompat
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.layout.Layout
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.PointerInputChange
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.HistoryEntry
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.lorenzosuarez.magiceightball.phone.AnswerText
import com.lorenzosuarez.magiceightball.phone.BallPhase
import com.lorenzosuarez.magiceightball.phone.EightBallEffect
import com.lorenzosuarez.magiceightball.phone.EightBallUiState
import com.lorenzosuarez.magiceightball.phone.EightBallViewModel
import com.lorenzosuarez.magiceightball.phone.Notice
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import com.lorenzosuarez.magiceightball.phone.push.NotificationsSetup
import android.os.Build
import com.lorenzosuarez.magiceightball.phone.push.Notifications
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
 * @param onGetMoreAiAnswers shows the ad the user may watch for more AI answers today; null
 * while there is none to watch.
 */
@Composable
fun EightBallScreen(
    viewModel: EightBallViewModel,
    onAnswerDismissed: () -> Unit = {},
    onShowAdPrivacyOptions: (() -> Unit)? = null,
    onGetMoreAiAnswers: (() -> Unit)? = null
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
    val notificationsRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onNotificationsAnswered
    )
    val askForMicrophone = { fromSettings: Boolean ->
        askedFromSettings = fromSettings
        askedAtMillis = SystemClock.elapsedRealtime()
        permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
    }

    // The accelerometer and the microphone are only used while the screen is in front of the user.
    // Whether the system lets the app notify: looked at again each time the user comes back,
    // since it is changed in the system's settings.
    var notificationsAllowed by remember { mutableStateOf(true) }
    LifecycleResumeEffect(viewModel, context) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
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

    // The app arrives rather than appears: the launch screen shows the ball's "8" on the window's
    // plain colour, and the ball is drawn around it while everything else fades in.
    val intro = remember { Animatable(0f) }
    val ready = state != null
    DesignedMotionEffect(ready) {
        if (ready) intro.animateTo(1f, tween(INTRO_MS))
    }
    // The link says it was shared from the app, so that installs that come of it can be counted.
    val playStoreUrl = PLAY_STORE_URL + context.packageName + SHARE_REFERRER
    val shareAnswerText = stringResource(R.string.share_answer_text, playStoreUrl)

    Box(modifier = Modifier.fillMaxSize()) {
        // Nothing but the window's own background until the saved choices are known.
        val current = state ?: return@Box
        ProvideAppLanguage(current.settings.language) {
            // Said once, as the die comes up with it; never again for the same answer.
            val voice = rememberAnswerVoice()
            val answered = current.phase as? BallPhase.Answered
            // The answer and, after a breath, what the character adds to it.
            val answerToSay = answered?.answer?.let { listOfNotNull(it.text(), it.remark()).joinToString(". ") }
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
            NotificationsSetup(
                language = current.settings.language,
                atRest = current.phase is BallPhase.Idle,
                bestStreak = current.streakBest,
                askAfterDays = current.config.notificationsAskAfterDays,
                onAnswered = viewModel::onNotificationsAnswered
            )
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
                onShowHistoryEntry = viewModel::showAgain,
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
                        onDetailedAnswersChange = {
                            tick(it.toggleFeedback())
                            viewModel.setDetailedAnswers(it)
                        },
                        onSpokenAnswersChange = {
                            tick(it.toggleFeedback())
                            viewModel.setSpokenAnswersEnabled(it)
                        },
                        onPartyNamesChange = viewModel::setPartyNames,
                        notificationsAllowed = notificationsAllowed,
                        // The system's own page for this app's notifications: it works whether or
                        // not the permission can still be asked for.
                        onAllowNotifications = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        onDailyVerdictChange = { on ->
                            tick(on.toggleFeedback())
                            viewModel.setDailyVerdictEnabled(on)
                            // Turned on by hand, it is asked for at once: the wish has just been stated.
                            if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !Notifications.mayNotify(context)
                            ) {
                                Notifications.markAsked(context)
                                notificationsRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onShareApp = {
                            context.shareText(shareAppText)
                            viewModel.onAppShared()
                        },
                        onOpenPrivacyPolicy = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl(current.settings.language)))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } catch (_: ActivityNotFoundException) {
                                // No browser on the device: there is nothing to open it with.
                            }
                        },
                        onSignIn = { viewModel.signIn(context) },
                        onSignOut = viewModel::signOut,
                        onDeleteAccount = { viewModel.deleteAccount(context) },
                        onShowAdPrivacyOptions = onShowAdPrivacyOptions,
                        onGetMoreAiAnswers = onGetMoreAiAnswers,
                    )
                },
                onAllowMicrophone = { askForMicrophone(true) },
                onAskWho = viewModel::askWho,
                onShareAnswer = { picture ->
                    tick(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                    context.shareAnswer(picture, shareAnswerText)
                    viewModel.onAnswerShared()
                },
                onReportAnswer = { reason ->
                    tick(HapticFeedbackConstantsCompat.CONFIRM)
                    viewModel.reportAnswer(reason)
                },
                onRateAnswer = { liked ->
                    tick(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                    viewModel.rateAnswer(liked)
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
    onShowHistoryEntry: (HistoryEntry) -> Unit = {},
    onAllowMicrophone: () -> Unit = {},
    onShareAnswer: (ImageBitmap) -> Unit = {},
    onReportAnswer: (ReportReason) -> Unit = {},
    onRateAnswer: (Boolean) -> Unit = {},
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
    val reportedMessage = stringResource(R.string.report_done)
    // Reporting asks one thing first, what is wrong, and the answer to that sends it.
    var reporting by rememberSaveable { mutableStateOf(false) }
    if (reporting && state.phase is BallPhase.Answered) {
        ReportSheet(
            onReason = { reason ->
                reporting = false
                onReportAnswer(reason)
                scope.launch {
                    snackbars.currentSnackbarData?.dismiss()
                    snackbars.showSnackbar(reportedMessage, duration = SnackbarDuration.Short)
                }
            },
            onClose = { reporting = false }
        )
    }
    val deleteWithUndo: (HistoryEntry) -> Unit = { entry ->
        onDeleteHistoryEntry(entry.id)
        scope.launch {
            // One at a time: the latest deletion is the one that can be undone.
            snackbars.currentSnackbarData?.dismiss()
            val result = snackbars.showSnackbar(deletedMessage, undoLabel, duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) onRestoreHistoryEntry(entry)
        }
    }

    // A question, however it was started, brings the ball to the front; closing its answer
    // goes back to the page it was started from, so one opened from Recents closes onto Recents.
    var cameFrom by remember { mutableIntStateOf(BALL_PAGE) }
    LaunchedEffect(atRest) {
        if (!atRest) {
            cameFrom = pagerState.currentPage
            pagerState.animateScrollToPage(BALL_PAGE)
        } else if (cameFrom != BALL_PAGE) {
            val page = cameFrom
            cameFrom = BALL_PAGE
            pagerState.animateScrollToPage(page)
        }
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
                    remark = answered.answer.remark(),
                    styleLabel = answered.style?.let { stringResource(it.labelRes()) },
                    personality = answered.style ?: Magic8BallPersonality.NEUTRAL
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = atRest
        ) { page ->
            val pageName = stringResource(
                when (page) {
                    HISTORY_PAGE -> R.string.history_title
                    OPTIONS_PAGE -> R.string.settings_title
                    else -> R.string.brand_name
                }
            )
            // The ball floats on the backdrop; its neighbours are plain pages, edge to edge,
            // so that what is listed on them is read against one flat colour.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Coming into view, a page says its name to whoever cannot see it slide in.
                    .semantics { paneTitle = pageName }
                    .then(
                        if (page == BALL_PAGE) {
                            Modifier
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                        }
                    )
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // What stands at the top of a page belongs to it and travels with it: a name is
                // already on its way in while the page is, rather than appearing once it has arrived.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TOP_BAR_HEIGHT)
                        .padding(horizontal = 16.dp)
                ) {
                    when (page) {
                        BALL_PAGE -> BallBar(
                            state = state,
                            busy = busy,
                            onDismiss = onDismiss,
                            onSpeakAnswer = onSpeakAnswer,
                            onReport = { reporting = true },
                            onOpenHistory = { scope.launch { pagerState.animateScrollToPage(HISTORY_PAGE) } },
                            onOpenOptions = { scope.launch { pagerState.animateScrollToPage(OPTIONS_PAGE) } },
                            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = intro() }
                        )
                        else -> {
                          // The way back to the ball, at the side the ball lies on.
                          val toBall = { scope.launch { pagerState.animateScrollToPage(BALL_PAGE) }; Unit }
                          BarIconButton(
                              icon = if (page == HISTORY_PAGE) R.drawable.ic_arrow_forward else R.drawable.ic_arrow_back,
                              description = stringResource(R.string.action_back_to_ball),
                              onClick = toBall,
                              modifier = Modifier.align(if (page == HISTORY_PAGE) Alignment.CenterEnd else Alignment.CenterStart)
                          )
                          Text(
                            text = stringResource(
                                if (page == HISTORY_PAGE) R.string.history_title else R.string.settings_title
                            ),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = PAGE_TITLE_TOP)
                                .semantics { heading() },
                            // The bar has one line's room over the dots: the name grows with the
                            // system's font only as far as that room goes.
                            style = MaterialTheme.typography.titleMedium.let {
                                val fit = (PAGE_TITLE_MAX_SCALE / LocalDensity.current.fontScale).coerceAtMost(1f)
                                it.copy(fontSize = it.fontSize * fit, lineHeight = it.lineHeight * fit)
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (page == OPTIONS_PAGE) {
                        // What the page of options has to say once it is done is said by the
                        // screen, as everything else that has just been done is.
                        CompositionLocalProvider(LocalSnackbars provides snackbars) { options() }
                    } else if (page == HISTORY_PAGE) {
                        HistoryContent(
                            entries = state.history,
                            now = now,
                            onDelete = deleteWithUndo,
                            onClearAll = onClearHistory,
                            onOpen = onShowHistoryEntry,
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
                            onRateAnswer = onRateAnswer,
                            onAskWho = onAskWho
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbars,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        )

        // Over every page, and the one thing up here that stays put while they slide under it:
        // where the user is. It gives way to a question, which has nowhere else to go.
        if (atRest) {
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = intro() }
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxWidth()
                    .height(TOP_BAR_HEIGHT)
            ) {
                PageDots(
                    position = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = PAGE_DOTS_TOP)
                )
            }
        }
    }
}

/**
 * The top of the ball's page. At rest, the way to each neighbour at the side it lies on; with a
 * question or an answer, the way out of it and, for an answer, the ways to hear it and to report it.
 */
@Composable
private fun BallBar(
    state: EightBallUiState,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSpeakAnswer: () -> Unit,
    onReport: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (state.phase is BallPhase.Idle) {
            BarButton(
                icon = R.drawable.ic_history,
                description = stringResource(R.string.history_title),
                onClick = onOpenHistory,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            BarButton(
                icon = R.drawable.ic_settings,
                description = stringResource(R.string.settings_title),
                onClick = onOpenOptions,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        } else {
            BarButton(
                icon = R.drawable.ic_close,
                description = stringResource(if (busy) R.string.action_cancel else R.string.action_close),
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            // With an answer on the die, what is done about the answer itself from up here: hear
            // it and, since nobody read what the AI wrote before it was shown, report it.
            val answered = state.phase as? BallPhase.Answered
            if (answered != null) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if ((answered.answer as? AnswerText.Plain)?.fromAi == true) {
                        // Once reported it stays marked, and a second press does nothing.
                        BarIconButton(
                            icon = if (answered.reported) R.drawable.ic_flag_filled else R.drawable.ic_flag,
                            description = stringResource(
                                if (answered.reported) R.string.report_state_done else R.string.report_label
                            ),
                            onClick = onReport,
                            enabled = !answered.reported,
                            tint = if (answered.reported) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    BarButton(
                        icon = R.drawable.ic_volume,
                        description = stringResource(R.string.action_read_aloud),
                        onClick = onSpeakAnswer
                    )
                }
            }
        }
    }
}

@Composable
private fun BarButton(icon: Int, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BarIconButton(icon = icon, description = description, onClick = onClick, modifier = modifier)
}

/**
 * One dot per page and, over them, a brighter one for the page in front. It never jumps: as a
 * page is dragged the bright dot stretches towards the next one and then lets go of the one it
 * left, so the eye follows it across. Decoration: the pages announce themselves.
 *
 * @param position the page in front, with the fraction of the way to the next: read while
 * drawing, so a drag moves the dot without composing anything again.
 */
@Composable
private fun PageDots(position: () -> Float, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    val step = PAGE_DOT_SIZE + PAGE_DOT_GAP
    Canvas(modifier = modifier.size(width = step * (PAGE_COUNT - 1) + PAGE_DOT_SIZE, height = PAGE_DOT_SIZE)) {
        val dot = PAGE_DOT_SIZE.toPx()
        val stride = step.toPx()
        repeat(PAGE_COUNT) { page ->
            drawCircle(color, radius = dot / 2, center = Offset(page * stride + dot / 2, dot / 2), alpha = DOT_OFF_ALPHA)
        }
        val at = position().coerceIn(0f, PAGE_COUNT - 1f)
        val from = at.toInt().coerceAtMost(PAGE_COUNT - 2)
        val way = at - from
        // The first half of the way the leading edge runs ahead; the second half the trailing edge catches up.
        val start = (from + ((way - 0.5f) * 2).coerceAtLeast(0f)) * stride
        val end = (from + (way * 2).coerceAtMost(1f)) * stride + dot
        drawRoundRect(
            color = color,
            topLeft = Offset(start, 0f),
            size = Size(end - start, dot),
            cornerRadius = CornerRadius(dot / 2)
        )
    }
}

/**
 * The ball's page, in three bands that each do one thing. At the top, how things stand; in the
 * middle the ball, alone at rest, with the question over it and what came of it under it once
 * there is one; at the bottom edge, under the thumb, what can be done now.
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
    onRateAnswer: (Boolean) -> Unit,
    onAskWho: (String) -> Unit
) {
    // The ball has weight while it waits and while it thinks; with an answer on it, it holds still to be read.
    val swaying = !still && state.config.ballSwayEnabled &&
        (state.phase is BallPhase.Idle || state.phase is BallPhase.Thinking)
    val sway = rememberBallSway(active = swaying, ticks = state.phase is BallPhase.Idle)
    val swayStrength by animateFloatAsState(if (swaying) 1f else 0f, tween(durationMillis = 200), label = "sway")
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val ballSize = minOf(
            minOf(maxWidth * BALL_WIDTH_FRACTION, BALL_MAX_SIZE),
            // Room for what stands over and under the ball, counted once: when what is written
            // under it needs more than its share, the ball gives way upwards rather than shrink.
            maxHeight - (BALL_GAP * 2 + TITLE_ROOM + CAPTION_HEIGHT + ACTIONS_HEIGHT)
        ).coerceAtLeast(BALL_MIN_SIZE)
        // The die's text does not follow the system's font size: when it comes out small, the
        // answer is written under the ball as well.
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val answerText = (state.phase as? BallPhase.Answered)?.answer?.text()
        val answerOnDie = remember(answerText, ballSize, density) {
            answerText?.let { answerTextSizeOnBall(it, ballSize, density, measurer) }
        }
        // Found out while laying out: the ball has given way to what there is to read so far
        // that its die no longer reads. Once that is so for an answer, it stays so.
        val answerId = (state.phase as? BallPhase.Answered)?.id
        var dieTooSmall by remember(answerId) { mutableStateOf(false) }
        val answerSmall = dieTooSmall || (answerOnDie != null && answerOnDie < ANSWER_READABLE)
        // On a dark screen a black ball has no outline of its own: a light behind it gives it one.
        val glow = if (MaterialTheme.colorScheme.onSurface.luminance() > 0.5f) BallGlow else Color.Transparent
        val band = Modifier
            .graphicsLayer { alpha = intro() }
            .padding(horizontal = 32.dp)
            .widthIn(max = TEXT_MAX_WIDTH)
            .fillMaxWidth()

        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Box(modifier = Modifier.graphicsLayer { alpha = intro() }.padding(top = 8.dp)) { Status(state) }
                Title(state = state, modifier = band)
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
                            // A little behind the hand that moves the phone: see [rememberBallSway].
                            val reach = BALL_SWAY.toPx() * swayStrength
                            translationX = sway.x * reach
                            translationY = sway.y * reach
                        }
                        .drawBehind {
                            if (glow.alpha > 0f) {
                                val reach = size.minDimension / 2f * BALL_GLOW_REACH
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        0f to glow,
                                        0.5f to glow,
                                        1f to Color.Transparent,
                                        center = center,
                                        radius = reach
                                    ),
                                    radius = reach
                                )
                            }
                        }
                )
                Caption(state = state, answerSmall = answerSmall, modifier = band)
                Actions(
                    state = state,
                    onAllowMicrophone = onAllowMicrophone,
                    onShareAnswer = onShareAnswer,
                    onRateAnswer = onRateAnswer,
                    onAskWho = onAskWho,
                    modifier = band.padding(top = 24.dp, bottom = 16.dp).heightIn(min = 48.dp)
                )
            }
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
            val status = measurables[0].measure(loose)
            val title = measurables[1].measure(loose)
            val ball = measurables[2].measure(loose)
            val actions = measurables[4].measure(loose)
            val breath = BALL_BREATHING.roundToPx()
            // The room the ball is given is the size it grows to with an answer on it, in every
            // state, so that what stands around it does not move when it grows.
            val full = ball.height * (1f + ANSWER_GROWTH)
            val smallest = minOf(BALL_MIN_SEEN.toPx(), full)
            // What is written under the ball takes what it needs, short of pushing the ball under
            // its smallest: past that it scrolls where it is, and never runs into what is below it.
            val around = title.height + actions.height + breath * 2
            val caption = measurables[3].measure(
                loose.copy(maxHeight = (height - around - smallest.roundToInt()).coerceAtLeast(0))
            )
            // The ball gives way to what there is to read: as large as the room left allows.
            val seen = (height - around - caption.height).toFloat().coerceIn(smallest, full)
            val fit = seen / full
            if (!dieTooSmall && answerOnDie != null && answerOnDie * fit < ANSWER_READABLE) dieTooSmall = true
            // In the middle between the bar above and the bottom edge, which is a little higher
            // than the middle of this page; it moves from there only to make room.
            val highest = (title.height + breath).toFloat()
            val middle = (height - seen) / 2f - TOP_BAR_HEIGHT.toPx() / 2f
            val lowest = height - actions.height - caption.height - breath - seen
            // With more to read than fits under a ball in the middle, what room there is goes
            // half over the question and half under the signature, not all to one end.
            val top = if (lowest < middle) {
                highest + (lowest - highest).coerceAtLeast(0f) / 2f
            } else maxOf(middle, highest)
            layout(width, height) {
                status.place((width - status.width) / 2, 0)
                title.place((width - title.width) / 2, (top - breath - title.height).roundToInt())
                ball.placeWithLayer(
                    x = (width - ball.width) / 2,
                    y = (top + seen / 2f - ball.height / 2f).roundToInt()
                ) {
                    scaleX = fit
                    scaleY = fit
                }
                caption.place((width - caption.width) / 2, (top + seen + breath).roundToInt())
                actions.place((width - actions.width) / 2, height - actions.height)
            }
        }
    }
}

/** How things stand, said once and out of the way: for now, a run of days. */
@Composable
private fun Status(state: EightBallUiState) {
    val shown = state.phase is BallPhase.Idle && state.notice == null && !state.settings.isParty &&
        state.streakDays >= state.config.streakMinDaysShown
    if (shown) {
        // Said, not shown as a button: nothing is drawn around it, since a touch does nothing.
        Text(
            text = pluralStringResource(R.plurals.streak_days, state.streakDays, state.streakDays),
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
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

    // An answer takes a moment to rise into view. A tap in that moment was meant for what was
    // there before it, and must not ask again over an answer nobody has read yet.
    val answerId = (phase as? BallPhase.Answered)?.id
    var readable by remember(answerId) { mutableStateOf(answerId == null) }
    LaunchedEffect(answerId) {
        if (answerId != null) {
            delay(ANSWER_SETTLE_MS)
            readable = true
        }
    }

    // A tap while the app listens ends the question; any other tap asks.
    val tap = when {
        listening -> onToggleVoiceQuestion
        !readable -> NoTap
        else -> onAsk
    }

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
                    val down = awaitFirstDown()
                    pressed = true
                    try {
                        when (awaitTapOrHold(down)) {
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
 * Above the ball, one line that says where things stand: that the app is listening or
 * thinking and, with an answer, the question it answers. At rest, nothing.
 */
@Composable
private fun Title(state: EightBallUiState, modifier: Modifier = Modifier) {
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
                is BallPhase.Answered -> QuietText(phase.question.quoted(), maxLines = 3)
                // At rest the ball stands alone: how to ask is said at the bottom edge.
                else -> if (phase !is BallPhase.Idle || turnName != null) Text(
                    text = when {
                        phase is BallPhase.Listening -> stringResource(R.string.hint_listening)
                        phase is BallPhase.Thinking -> stringResource(R.string.hint_thinking)
                        // At a party the line says whose turn it is.
                        else -> stringResource(R.string.party_turn, turnName.orEmpty())
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
 * them. At rest it is empty; under an answer, who said it and, when the AI service wrote it,
 * a quiet note that says so.
 *
 * The text on the die is as large as the die allows and cannot follow the system's font size,
 * so with a large font the answer is written here as well.
 */
@Composable
private fun Caption(state: EightBallUiState, answerSmall: Boolean, modifier: Modifier = Modifier) {
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    val captionScroll = rememberScrollState()

    AnimatedContent(
        targetState = state.phase,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.TopCenter,
        contentKey = { (it as? BallPhase.Answered)?.id ?: it::class },
        label = "caption"
    ) { phase ->
        when (phase) {
            BallPhase.Idle -> Spacer(Modifier.fillMaxWidth())
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
                // Where there is more to read than room for it, it scrolls in its own place, and
                // what is still below fades out rather than being cut through a line.
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        if (captionScroll.canScrollForward) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Black,
                                    1f to Color.Transparent,
                                    startY = size.height - CAPTION_FADE.toPx(),
                                    endY = size.height
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
                    .verticalScroll(captionScroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (largeFont || answerSmall) {
                    // The answer itself: heavier than what is said about it, and a little apart.
                    Text(
                        text = phase.answer.text(),
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                // The character's comeback to the question, said under its answer and more quietly:
                // the ball has the word, and this is an aside.
                phase.answer.remark()?.let { remark ->
                    Text(
                        text = remark,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodyLarge,
                        // The character's own words, not a note about them: in full ink, apart
                        // from the question over the ball, which is the user's and is quiet.
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = REMARK_MAX_LINES,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Who said it, signed as on the picture that is shared: an answer is only funny
                // when it is known whose it is.
                val fromAi = (phase.answer as? AnswerText.Plain)?.fromAi == true
                phase.style?.let { style ->
                    // On the same line, and more quietly: that the AI wrote it.
                    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
                    val small = MaterialTheme.typography.labelMedium
                    val ai = stringResource(R.string.source_ai)
                    Text(
                        text = buildAnnotatedString {
                            append("\u2014 " + stringResource(style.labelRes()))
                            if (fromAi) {
                                withStyle(
                                    SpanStyle(
                                        color = quiet,
                                        fontSize = small.fontSize,
                                        fontFamily = small.fontFamily,
                                        fontStyle = FontStyle.Normal,
                                        letterSpacing = small.letterSpacing
                                    )
                                ) { append("  \u00b7  $ai") }
                            }
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                // An answer kept before styles were has nobody to sign it: it is still said who wrote it.
                if (fromAi && phase.style == null) {
                    Text(
                        text = stringResource(R.string.answer_from_ai),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AI_NOTE_ALPHA),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * At the bottom edge, where the thumb is: what can be done now. At rest, how to ask, and
 * whatever stands in the way of asking by voice with the way out of it; with an answer, what
 * can be done with it. It is always here, so the hand does not have to look for it.
 */
@Composable
private fun Actions(
    state: EightBallUiState,
    onAllowMicrophone: () -> Unit,
    onShareAnswer: () -> Unit,
    onRateAnswer: (Boolean) -> Unit,
    onAskWho: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notice = state.notice
    val whoQuestion = stringResource(R.string.party_who)
    AnimatedContent(
        targetState = state.phase,
        modifier = modifier,
        transitionSpec = { StageEnter togetherWith fadeOut(tween(durationMillis = 120)) },
        contentAlignment = Alignment.BottomCenter,
        contentKey = { (it as? BallPhase.Answered)?.id ?: it::class },
        label = "actions"
    ) { phase ->
        when (phase) {
            BallPhase.Idle -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // How to ask: at a party too, where the phone goes to someone who has never held it.
                Text(
                    text = stringResource(if (state.voice.isReady) R.string.hint_hold else R.string.hint_tap),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                // A refusal is said in the colour of a warning.
                if (notice != null) {
                    QuietText(
                        text = stringResource(
                            when (notice) {
                                Notice.MicrophoneDenied -> R.string.notice_microphone_denied
                                Notice.VoiceUnsupported -> R.string.notice_voice_unsupported
                            }
                        ),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        color = if (notice == Notice.MicrophoneDenied) {
                            MaterialTheme.colorScheme.error
                        } else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // The way out of a refusal, right where it is explained.
                if (notice == Notice.MicrophoneDenied) {
                    FilledTonalButton(
                        onClick = onAllowMicrophone,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = ButtonShape
                    ) {
                        Text(stringResource(R.string.settings_voice_allow_microphone), fontWeight = FontWeight.SemiBold)
                    }
                }
                // At a party, the other thing the ball can be asked: to pick one of the players.
                if (notice == null && state.settings.isParty) {
                    FilledTonalButton(
                        onClick = { onAskWho(whoQuestion) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = ButtonShape
                    ) {
                        Text(whoQuestion, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            is BallPhase.Answered -> AnswerActions(
                liked = phase.liked,
                onShare = onShareAnswer,
                onRate = onRateAnswer,
                modifier = Modifier.fillMaxWidth()
            )
            else -> Spacer(Modifier.fillMaxWidth())
        }
    }
}

/**
 * What can be done with an answer, in one row and with nothing drawn around it: share it,
 * which is the main thing and the only one with a name, and say whether it was a good one, with
 * two quiet thumbs.
 */
@Composable
private fun AnswerActions(
    liked: Boolean?,
    onShare: () -> Unit,
    onRate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onShare,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_share), fontWeight = FontWeight.SemiBold)
        }
        RateButton(
            good = true,
            chosen = liked,
            label = stringResource(R.string.rate_good),
            onClick = { onRate(true) }
        )
        RateButton(
            good = false,
            chosen = liked,
            label = stringResource(R.string.rate_bad),
            onClick = { onRate(false) }
        )
    }
}

/** A thumb: filled in the accent once chosen. */
@Composable
private fun RateButton(good: Boolean, chosen: Boolean?, label: String, onClick: () -> Unit) {
    val selected = chosen == good
    val icon = when {
        good && selected -> R.drawable.ic_thumb_up_filled
        good -> R.drawable.ic_thumb_up
        selected -> R.drawable.ic_thumb_down_filled
        else -> R.drawable.ic_thumb_down
    }
    // The one not chosen keeps its full contrast: it can still be chosen instead.
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .toggleable(value = selected, role = Role.RadioButton, onValueChange = { onClick() })
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
    }
}


@Composable
private fun QuietText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = CAPTION_MAX_LINES,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun String.quoted(): String = if (isEmpty()) "" else stringResource(R.string.question_quoted, this)

private fun AnswerText.remark(): String? = (this as? AnswerText.Plain)?.remark

@Composable
private fun AnswerText.text(): String = when (this) {
    is AnswerText.Plain -> value
    AnswerText.Fallback -> stringResource(R.string.answer_fallback)
}

/**
 * Waits for every finger to lift, inside the ball or not: a thumb drifts while its owner talks.
 * False when another gesture, a scroll for instance, has taken the touch for itself.
 */
/**
 * What a press on the ball turns out to be: true for a tap, lifted in time; false when it was
 * taken away, or slid off as the start of a swipe to another page; null when the finger is
 * still down, and still where it came down, after the system's own long-press time. That time
 * follows the user's accessibility setting for it, so a slow, deliberate tap stays a tap.
 */
private suspend fun AwaitPointerEventScope.awaitTapOrHold(down: PointerInputChange): Boolean? = try {
    withTimeout(viewConfiguration.longPressTimeoutMillis) {
        var outcome: Boolean? = null
        while (outcome == null) {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
            outcome = when {
                change == null || change.isConsumed -> false
                !change.pressed -> true
                (change.position - down.position).getDistance() > viewConfiguration.touchSlop -> false
                else -> null
            }
        }
        outcome
    }
} catch (_: PointerEventTimeoutCancellationException) {
    null
}

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

/** What Google Play passes on to the app's install figures: that this install came from a share. */
/** The app's own site, where the policy is published in each language the app speaks. */
private const val SITE_URL = "https://magic-ai-8-ball.web.app"

/** The policy in the language the app is set to. */
internal fun privacyPolicyUrl(language: AppLanguage): String = SITE_URL + when (language) {
    AppLanguage.ENGLISH -> "/privacy"
    AppLanguage.SPANISH -> "/es/privacy"
    AppLanguage.PORTUGUESE -> "/pt/privacy"
}
private const val SHARE_REFERRER = "&referrer=utm_source%3Dshare%26utm_medium%3Dapp"

/** How long an answer takes to rise into view: a tap sooner than that does not ask again. */
private const val ANSWER_SETTLE_MS = 700L
private val NoTap: () -> Unit = {}

private const val CLOCK_TICK_MS = 30_000L

/** The answer is said as its text appears on the die, not before. */
private const val SPOKEN_ANSWER_DELAY_MS = 450L

private const val THINKING_TICKS = 5
private const val THINKING_TICK_MS = 180L
private const val THINKING_TICK_SLOWDOWN_MS = 70L

/** No one reads the system's permission dialog and answers it in less than this. */
private const val DIALOG_SKIPPED_MS = 1_000L

/** The arrival from the launch screen. */
private const val INTRO_MS = 520

internal const val HISTORY_PAGE = 0
internal const val BALL_PAGE = 1
internal const val OPTIONS_PAGE = 2
private const val PAGE_COUNT = 3
private const val DOT_OFF_ALPHA = 0.48f

private const val BALL_WIDTH_FRACTION = 0.58f
private val BALL_MAX_SIZE = 320.dp
private val BALL_MIN_SIZE = 160.dp

/** Wide enough for the ball to grow into when it shows an answer. */
private val BALL_GAP = 56.dp
/** The air between the ball and what stands over and under it. */
private val BALL_BREATHING = 24.dp
/** How much of the text under the ball fades out at the bottom when there is more of it below. */
private val CAPTION_FADE = 32.dp
/** As small as the ball is ever seen, however much there is to read under it. */
private val BALL_MIN_SEEN = 112.dp
/** As far as the ball is ever left behind: enough to be felt as weight, never enough to look loose. */
private val BALL_SWAY = 12.dp

private val TOP_BAR_HEIGHT = 72.dp

/** Where a page's name and the dots stand in the bar: one over the other, with the bar's middle between them. */
private val PAGE_TITLE_TOP = 15.dp
private val PAGE_DOTS_TOP = 51.dp
private val PAGE_DOT_SIZE = 6.dp
private val PAGE_DOT_GAP = 8.dp
private val TEXT_MAX_WIDTH = 480.dp
/** Room kept under the ball for what came of a question, and under that for what can be done. */
private val CAPTION_HEIGHT = 104.dp
/** Room kept over the ball for the one or two lines that say where things stand. */
private val TITLE_ROOM = 48.dp
/** Under this size on the die, the answer is written under the ball as well. */
private val ANSWER_READABLE = 12.dp
/** The light behind the ball on a dark screen, and how far past the ball it reaches. */
private val BallGlow = Color(0xFF3550C8).copy(alpha = 0.3f)
private const val BALL_GLOW_REACH = 1.8f
/** As far as a page's name follows the system's font size. */
private const val PAGE_TITLE_MAX_SCALE = 1.3f
private val ACTIONS_HEIGHT = 80.dp
/** Small text on the coloured end of the backdrop: enough ink to pass 4.5:1 in both themes. */
private const val CAPTION_MAX_LINES = 3
/** Room for the few sentences of an answer that says more; one line of comeback takes what it needs. */
private const val REMARK_MAX_LINES = 9

/** How faint the note under an answer the AI wrote is. */
private const val AI_NOTE_ALPHA = 1f

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
