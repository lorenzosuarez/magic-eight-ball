package com.lorenzosuarez.magiceightball.phone.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.magiceightball.core.domain.model.AppLanguage
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.lorenzosuarez.magiceightball.phone.MainActivity
import com.lorenzosuarez.magiceightball.phone.R

/**
 * Messages the developer sends to the app's users from Firebase Cloud Messaging: something new
 * in the app, a reason to come back.
 *
 * A message is sent to a topic, not to a person: every copy of the app follows [TOPIC_ALL] and
 * the topic of its language, so a message can be written in the language it will be read in.
 * Nothing about the user goes with that: a topic is a name the copy of the app listens to.
 */
internal object Notifications {
    const val CHANNEL_ID = "news"
    const val TOPIC_ALL = "all"

    /** The topic followed by the copies of the app set to [language]: "lang_es" for Spanish. */
    fun topicOf(language: AppLanguage): String = "lang_${language.code}"

    /** The channel the user sees in the system's settings. Safe to call at every start. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notifications_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    /** Follows the topics of [language] and stops following those of the other languages. */
    fun followTopics(context: Context, language: AppLanguage) {
        if (FirebaseApp.getApps(context).isEmpty()) return
        val messaging = FirebaseMessaging.getInstance()
        messaging.subscribeToTopic(TOPIC_ALL)
        AppLanguage.entries.forEach { other ->
            if (other == language) {
                messaging.subscribeToTopic(topicOf(other))
            } else {
                messaging.unsubscribeFromTopic(topicOf(other))
            }
        }
    }

    fun mayNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Shows a message that arrived while the app was open: the system shows the others itself. */
    fun show(context: Context, title: String?, body: String?) {
        if (body.isNullOrBlank() || !mayNotify(context)) return
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title?.takeIf { it.isNotBlank() } ?: context.getString(R.string.brand_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(body.hashCode(), notification)
        } catch (_: SecurityException) {
            // The permission was taken away between the check and the call: nothing to show.
        }
    }

    /**
     * Whether this is the moment to ask for notifications: on a device that asks at all, only
     * once, and only of someone who has come back for [askAfterDays] days, so the question is
     * put to people who already like the app. Zero days means the question is never put.
     */
    fun shouldAsk(sdk: Int, granted: Boolean, alreadyAsked: Boolean, bestStreak: Int, askAfterDays: Int): Boolean =
        sdk >= Build.VERSION_CODES.TIRAMISU && !granted && !alreadyAsked &&
            askAfterDays > 0 && bestStreak >= askAfterDays

    private const val PREFERENCES = "notifications"
    private const val KEY_ASKED = "asked"

    fun wasAsked(context: Context): Boolean =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(KEY_ASKED, false)

    fun markAsked(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(KEY_ASKED, true).apply()
    }
}

/**
 * Asks for notifications when [Notifications.shouldAsk] says so, and follows the topics of the
 * app's [language]. Put where the ball is at rest: [atRest] keeps the question away from someone
 * in the middle of asking or reading.
 *
 * @param onAnswered called with the user's answer to the system's question.
 */
@Composable
internal fun NotificationsSetup(
    language: AppLanguage,
    atRest: Boolean,
    bestStreak: Int,
    askAfterDays: Int,
    onAnswered: (granted: Boolean) -> Unit
) {
    val context = LocalContext.current
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onAnswered)

    LaunchedEffect(language) { Notifications.followTopics(context, language) }
    LaunchedEffect(atRest, bestStreak, askAfterDays) {
        val ask = atRest && Notifications.shouldAsk(
            sdk = Build.VERSION.SDK_INT,
            granted = Notifications.mayNotify(context),
            alreadyAsked = Notifications.wasAsked(context),
            bestStreak = bestStreak,
            askAfterDays = askAfterDays
        )
        if (ask && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Marked before asking: whatever comes of it, the question is never put twice.
            Notifications.markAsked(context)
            request.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
