package com.example.magiceightball

import android.app.Application
import com.example.magiceightball.core.data.appcheck.AppCheck
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MagicEightBallApp : Application() {

    @Inject
    lateinit var appCheck: AppCheck

    @Inject
    lateinit var geminiSettings: GeminiSettingsSource

    override fun onCreate() {
        super.onCreate()
        // Before anything is asked of Firebase: what is asked carries the proof that it is this app.
        appCheck.install()
        // Fetch the remote Gemini switch early so the first shake already honours it.
        geminiSettings.refresh()
    }
}
