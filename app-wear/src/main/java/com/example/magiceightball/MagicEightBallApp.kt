package com.example.magiceightball

import android.app.Application
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MagicEightBallApp : Application() {

    @Inject
    lateinit var geminiSettings: GeminiSettingsSource

    override fun onCreate() {
        super.onCreate()
        // Fetch the remote Gemini switch early so the first shake already honours it.
        geminiSettings.refresh()
    }
}
