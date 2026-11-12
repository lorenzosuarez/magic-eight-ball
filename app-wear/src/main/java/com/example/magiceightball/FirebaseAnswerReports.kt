package com.example.magiceightball

import android.content.Context
import com.example.magiceightball.core.domain.gateway.AnswerReports
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reported answers are sent to the developer as non-fatal reports of the crash reporting
 * service, which the app already uses: there is no server of the app's own to send them to.
 * Each one is the answer as it was written, with its style and language, as on the phone.
 */
@Singleton
class FirebaseAnswerReports @Inject constructor(
    @ApplicationContext private val context: Context
) : AnswerReports {

    override fun report(answer: String, remark: String?, style: Magic8BallPersonality?, languageCode: String) {
        // A build without the service has nowhere to send it.
        if (FirebaseApp.getApps(context).isEmpty()) return
        val said = listOfNotNull(answer, remark).joinToString(" | ").take(MAX_LENGTH)
        FirebaseCrashlytics.getInstance().apply {
            recordException(ReportedAnswer("$languageCode/${style?.name ?: "unknown"}: $said"))
            // Not left for the next time the app starts.
            sendUnsentReports()
        }
    }

    /** Not an error of the app: the shape the service gives to something worth a look. */
    private class ReportedAnswer(message: String) : Exception(message) {
        // Every report is the same event, wherever in the app it was sent from.
        override fun fillInStackTrace(): Throwable = this
    }

    private companion object {
        const val MAX_LENGTH = 300
    }
}
