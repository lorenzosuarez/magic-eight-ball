package com.example.magiceightball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.tiles.TileService
import com.example.magiceightball.feature.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    /** Whether the watch has dimmed to its always-on screen, on which the app stays visible. */
    private var isAmbient by mutableStateOf(false)

    private val ambientObserver = AmbientLifecycleObserver(
        this,
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
            }

            override fun onExitAmbient() {
                isAmbient = false
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)

        // The splash stays until the saved language and choices are read (a few milliseconds),
        // so the first frame is already in the user's language.
        splashScreen.setKeepOnScreenCondition { chatViewModel.state.value == null }

        setContent {
            WearApp(chatViewModel, isAmbient)
        }
    }

    override fun onStop() {
        super.onStop()
        // The tile speaks the language chosen in the app: whatever was changed here, it is
        // drawn again once the app is left, rather than at its next touch.
        TileService.getUpdater(this).requestUpdate(AnswerTileService::class.java)
    }
}
