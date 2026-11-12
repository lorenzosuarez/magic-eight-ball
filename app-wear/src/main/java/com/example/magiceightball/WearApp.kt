package com.example.magiceightball

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.ChatScreen
import com.example.magiceightball.feature.chat.ChatViewModel

/**
 * Root of the app: the theme, the time on top, and the single screen.
 *
 * There is only one destination, so no navigation host is needed; leaving the app is the
 * system's swipe-to-dismiss on the activity.
 *
 * @param isAmbient whether the watch has dimmed to its always-on screen.
 */
@Composable
fun WearApp(chatViewModel: ChatViewModel, isAmbient: Boolean) {
    MagicEightBallTheme {
        AppScaffold {
            ChatScreen(chatViewModel, isAmbient)
        }
    }
}
