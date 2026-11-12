package com.lorenzosuarez.magiceightball.phone

import android.app.Application
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.lorenzosuarez.magiceightball.phone.push.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MagicEightBallPhoneApp : Application() {

    @Inject
    lateinit var geminiSettings: GeminiSettingsSource

    override fun onCreate() {
        super.onCreate()
        // Fetch the remote Gemini switch early so the first answer already honours it.
        geminiSettings.refresh()
        Notifications.createChannel(this)
    }
}
