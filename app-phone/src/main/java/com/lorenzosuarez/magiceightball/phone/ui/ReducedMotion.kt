package com.lorenzosuarez.magiceightball.phone.ui

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect

/**
 * Whether the user asked the system to remove animations (Accessibility, or the developer
 * option that sets the animator duration scale to zero). Compose already ends its animations
 * at once then; this lets the ball also skip the movement that would otherwise never stop.
 * Read again whenever the app comes back to the foreground.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    fun read(): Boolean =
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    var reduced by remember(resolver) { mutableStateOf(read()) }
    LifecycleStartEffect(resolver) {
        reduced = read()
        onStopOrDispose { }
    }
    return reduced
}
