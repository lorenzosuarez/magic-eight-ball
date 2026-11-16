package com.lorenzosuarez.magiceightball.phone.ui

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/**
 * Motion at the speed it was designed for, whatever the system's animation scale is.
 *
 * The ball is the app: how it listens, thinks and answers is told by how it moves, and with
 * the system's animations turned off it would only jump from one picture to the next. The same
 * goes for getting around: a page that slides in says where it came from, and one that just
 * appears does not. This is a product decision. The activity composes everything in it (see
 * MainActivity), so the whole app moves as designed; what is launched here by hand does too,
 * wherever it is composed.
 *
 * What moves without telling anything is another matter: see [motionRemoved].
 */
internal object DesignedMotion : MotionDurationScale {
    override val scaleFactor: Float get() = 1f
}

/** A [LaunchedEffect] whose animations run in [DesignedMotion]. */
@Composable
internal fun DesignedMotionEffect(vararg keys: Any?, block: suspend CoroutineScope.() -> Unit) {
    LaunchedEffect(*keys) { withContext(DesignedMotion, block) }
}

/** A value that follows [target] with [spec], in [DesignedMotion]. */
@Composable
internal fun designedFloatAsState(target: Float, spec: AnimationSpec<Float>): State<Float> {
    val value = remember { Animatable(target) }
    DesignedMotionEffect(target, spec) { value.animateTo(target, spec) }
    return value.asState()
}

/**
 * Whether whoever uses this device asked it to remove animations. The ball's sway tells nothing,
 * it only goes on, so it is left out for them; what says where the app is still moves.
 */
@Composable
internal fun motionRemoved(): Boolean {
    val resolver = LocalContext.current.contentResolver
    // ponytail: read when the page is composed; one changed while the app is open is taken at its next start.
    return remember(resolver) { resolver.motionRemoved() }
}

internal fun ContentResolver.motionRemoved(): Boolean =
    Settings.Global.getFloat(this, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
