package com.lorenzosuarez.magiceightball.phone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
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
