package com.lorenzosuarez.magiceightball.phone

import android.app.Application
import com.example.magiceightball.core.data.appcheck.AppCheck
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.lorenzosuarez.magiceightball.phone.push.Notifications
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.lorenzosuarez.magiceightball.phone.config.AppConfigSource
import com.lorenzosuarez.magiceightball.phone.push.DailyVerdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MagicEightBallPhoneApp : Application() {

    @Inject
    lateinit var appCheck: AppCheck

    @Inject
    lateinit var geminiSettings: GeminiSettingsSource

    @Inject
    lateinit var userSettings: UserSettingsRepository

    @Inject
    lateinit var appConfig: AppConfigSource

    /** Lives as long as the app's process does. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Before anything is asked of Firebase: what is asked carries the proof that it is this app.
        appCheck.install()
        // Fetch the remote Gemini switch early so the first answer already honours it.
        geminiSettings.refresh()
        Notifications.createChannel(this)
        keepDailyVerdictScheduled()
    }

    /**
     * The day's verdict is scheduled while both the user and the developer have it on, at the
     * hour set from afar, and taken off the schedule when either turns it off.
     */
    private fun keepDailyVerdictScheduled() {
        combine(userSettings.settings, appConfig.config) { settings, config ->
            if (settings.dailyVerdictEnabled && config.dailyVerdictEnabled) config.dailyVerdictHour else null
        }
            .distinctUntilChanged()
            .onEach { hour -> if (hour != null) DailyVerdict.schedule(this, hour) else DailyVerdict.cancel(this) }
            .launchIn(scope)
    }
}
