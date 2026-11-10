package com.example.magiceightball

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.ChatScreen

/**
 * Root of the app: the theme, the time on top, and the single screen.
 *
 * There is only one destination, so no navigation host is needed; leaving the app is the
 * system's swipe-to-dismiss on the activity.
 */
@Composable
fun WearApp() {
    MagicEightBallTheme {
        AppScaffold {
            ChatScreen()
        }
    }
}
