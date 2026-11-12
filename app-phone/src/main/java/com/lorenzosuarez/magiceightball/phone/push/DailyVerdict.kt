package com.lorenzosuarez.magiceightball.phone.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.magiceightball.core.domain.model.AppLanguage
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.repository.StreakRepository
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase
import com.lorenzosuarez.magiceightball.phone.MainActivity
import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.config.AppConfigSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * The day's verdict: once a day, a line in the voice of the style the user has chosen, as a
 * notification. It is a reason to come back that sounds like the ball and not like an app asking
 * to be opened, and touching it opens the ball with that verdict on it.
 *
 * It is made on the device, from the answers built into the app: nothing is sent or fetched for
 * it. It stays quiet on a day the ball has already been asked, since nobody needs calling back
 * to where they have just been.
 */
internal object DailyVerdict {
    /** Carries the verdict from the notification to the activity it opens. */
    const val EXTRA_VERDICT = "daily_verdict"

    private const val WORK_NAME = "daily_verdict"
    private const val NOTIFICATION_ID = 8
    private const val KEY_HOUR = "hour"
    private const val NO_HOUR = -1

    /** Has the verdict made every day at about [hour] o'clock, by the device's own clock. */
    fun schedule(context: Context, hour: Int, now: LocalDateTime = LocalDateTime.now()) {
        val request = PeriodicWorkRequestBuilder<DailyVerdictWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour, now).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        val kept = context.getSharedPreferences(WORK_NAME, Context.MODE_PRIVATE)
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, policyFor(kept.getInt(KEY_HOUR, NO_HOUR), hour), request)
        kept.edit().putInt(KEY_HOUR, hour).apply()
    }

    /**
     * This is called every time the app starts, and the app is started by the very job that
     * gives the verdict: scheduling it afresh there would cancel it as it begins, every day.
     * So a job already set for [hour] is left alone, and only a change of hour starts another.
     */
    internal fun policyFor(scheduledHour: Int, hour: Int): ExistingPeriodicWorkPolicy =
        if (scheduledHour == hour) ExistingPeriodicWorkPolicy.KEEP else ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        // Turned on again, it is scheduled anew.
        context.getSharedPreferences(WORK_NAME, Context.MODE_PRIVATE).edit().remove(KEY_HOUR).apply()
    }

    /** How long from [now] until it is next [hour] o'clock: later today, or tomorrow if that has passed. */
    fun delayUntil(hour: Int, now: LocalDateTime): Duration {
        val today = now.toLocalDate().atTime(hour.coerceIn(0, 23), 0)
        val next = if (today.isAfter(now)) today else today.plusDays(1)
        return Duration.between(now, next)
    }

    /** Whether there is a verdict to give on [today], counted in days since 1970. */
    fun isDue(
        enabledByUser: Boolean,
        enabledFromAfar: Boolean,
        mayNotify: Boolean,
        lastAskedDay: Long?,
        today: Long
    ): Boolean = enabledByUser && enabledFromAfar && mayNotify && lastAskedDay != today

    fun show(context: Context, language: AppLanguage, verdict: String) {
        // In the app's own language, which may not be the device's.
        val localized = context.createConfigurationContext(
            Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language.code)) }
        )
        val open = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_VERDICT, verdict),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(localized.getString(R.string.daily_verdict_title))
            .setContentText(verdict)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            // One at a time: today's takes the place of yesterday's, if that was never touched.
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // The permission was taken away between the check and the call: nothing to show.
        }
    }
}

/** Makes the day's verdict, when one is due: see [DailyVerdict]. */
class DailyVerdictWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun userSettings(): UserSettingsRepository
        fun streaks(): StreakRepository
        fun sendMessage(): SendMessageUseCase
        fun appConfig(): AppConfigSource
    }

    override suspend fun doWork(): Result {
        val app = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val settings = withTimeoutOrNull(TIMEOUT_MS) { app.userSettings().settings.first() } ?: return Result.success()
        val streak = withTimeoutOrNull(TIMEOUT_MS) { app.streaks().streak.first() }
        val due = DailyVerdict.isDue(
            enabledByUser = settings.dailyVerdictEnabled,
            enabledFromAfar = app.appConfig().config.value.dailyVerdictEnabled,
            mayNotify = Notifications.mayNotify(applicationContext),
            lastAskedDay = streak?.lastDay,
            today = LocalDate.now().toEpochDay()
        )
        if (!due) return Result.success()

        // From the answers built into the app: a verdict nobody asked for spends no call to the AI service.
        val result = withTimeoutOrNull(TIMEOUT_MS) {
            app.sendMessage()(
                trigger = TRIGGER,
                languageCode = settings.language.code,
                personality = settings.personality,
                useAi = false
            )
        }
        (result as? Magic8BallResult.Success)?.message?.takeIf { it.isNotBlank() }?.let { verdict ->
            DailyVerdict.show(applicationContext, settings.language, verdict)
        }
        return Result.success()
    }

    private companion object {
        const val TRIGGER = "daily"
        const val TIMEOUT_MS = 5_000L
    }
}
