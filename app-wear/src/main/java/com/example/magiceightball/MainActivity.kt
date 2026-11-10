package com.example.magiceightball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.magiceightball.feature.chat.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // The splash stays until the saved language and choices are read (a few milliseconds),
        // so the first frame is already in the user's language.
        splashScreen.setKeepOnScreenCondition { chatViewModel.state.value == null }

        setContent {
            WearApp(chatViewModel)
        }
    }
}
