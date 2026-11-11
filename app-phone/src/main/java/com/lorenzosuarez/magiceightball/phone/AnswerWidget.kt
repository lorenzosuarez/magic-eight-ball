package com.lorenzosuarez.magiceightball.phone

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The ball on the home screen. It shows the "8" until it is touched; a touch then puts an
 * answer on the die, and the next touch another, without opening the app.
 *
 * Its answers are always the bundled ones, in the saved language and style: a widget is
 * touched idly and often, and none of those touches should spend a call to the AI service.
 */
class AnswerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, views(context, answer = null)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_ASK) return

        val pending = goAsync()
        val app = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        scope.launch {
            try {
                // A broadcast has a few seconds to live: no answer by then leaves the ball as it was.
                val answer = withTimeoutOrNull(ANSWER_TIMEOUT_MS) {
                    val settings = app.userSettings().settings.first()
                    val result = app.sendMessage()(
                        trigger = ASK_TRIGGER,
                        languageCode = settings.language.code,
                        personality = settings.personality,
                        useAi = false
                    )
                    (result as? Magic8BallResult.Success)?.message?.takeIf { it.isNotBlank() }
                }
                if (answer != null) {
                    val manager = AppWidgetManager.getInstance(context)
                    val ids = manager.getAppWidgetIds(ComponentName(context, AnswerWidget::class.java))
                    ids.forEach { manager.updateAppWidget(it, views(context, answer)) }
                }
            } finally {
                pending.finish()
            }
        }
    }

    /** The widget with [answer] on its die, or at rest when there is none. A touch asks either way. */
    private fun views(context: Context, answer: String?): RemoteViews {
        val ask = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AnswerWidget::class.java).setAction(ACTION_ASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return RemoteViews(context.packageName, R.layout.widget_answer).apply {
            setOnClickPendingIntent(R.id.widget_root, ask)
            setImageViewResource(R.id.widget_face, if (answer == null) R.drawable.widget_eight else R.drawable.widget_die)
            setViewVisibility(R.id.widget_text, if (answer == null) View.GONE else View.VISIBLE)
            setTextViewText(R.id.widget_text, answer.orEmpty())
            setContentDescription(
                R.id.widget_root,
                answer?.let { context.getString(R.string.ball_answer_description, it) }
                    ?: context.getString(R.string.widget_description)
            )
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun sendMessage(): SendMessageUseCase
        fun userSettings(): UserSettingsRepository
    }

    private companion object {
        const val ACTION_ASK = "com.lorenzosuarez.magiceightball.phone.widget.ASK"
        const val ASK_TRIGGER = "widget"
        const val ANSWER_TIMEOUT_MS = 4_000L
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
