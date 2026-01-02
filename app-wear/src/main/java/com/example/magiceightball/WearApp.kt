package com.example.magiceightball

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.magiceightball.core.designsystem.theme.MagicEightBallTheme
import com.example.magiceightball.feature.chat.ChatScreen

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    MagicEightBallTheme {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "chat"
        ) {
            composable("chat") {
                ChatScreen()
            }
        }
    }
}
