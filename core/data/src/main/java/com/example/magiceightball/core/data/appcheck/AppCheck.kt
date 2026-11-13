package com.example.magiceightball.core.data.appcheck

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Has the app prove to Firebase that it is itself.
 *
 * The AI service is asked through Firebase with no key in the app, so anyone could ask in the
 * app's name and at the developer's cost. With App Check each request carries a proof that it
 * comes from this app as it was published, and Firebase turns away a request without one. How
 * the proof is made depends on the build: see [appCheckProviderFactory].
 */
@Singleton
class AppCheck @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiAnswers: AiAnswersAvailability,
    private val userSettings: UserSettingsRepository
) {
    /** Lives as long as the app's process does. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * To be called once, as the app starts and before anything is asked of Firebase. Built
     * without `google-services.json` there is no Firebase to prove anything to, and it does
     * nothing.
     */
    fun install() {
        if (FirebaseApp.getApps(context).isEmpty()) return
        // Nothing here may keep the app from opening: on a device where the proof cannot be set
        // up, a request goes without one, is turned away, and the answer is a bundled one.
        try {
            val appCheck = Firebase.appCheck
            appCheck.installAppCheckProviderFactory(appCheckProviderFactory())
            // Firebase keeps a proof for as long as it holds and hands the same one back until
            // then, so asking for it again costs nothing.
            proofWanted(
                onScreen = ProcessLifecycleOwner.get().lifecycle.currentStateFlow
                    .map { it.isAtLeast(Lifecycle.State.STARTED) },
                aiAvailable = aiAnswers.isAvailable,
                userAllowsAi = userSettings.settings.map { it.aiAnswersEnabled }
            )
                .onEach { appCheck.getAppCheckToken(false) }
                .catch { e -> Log.w(TAG, "Could not make the proof ahead of the first answer", e) }
                .launchIn(scope)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set up App Check", e)
        }
    }

    private companion object {
        const val TAG = "AppCheck"
    }
}

/**
 * Tells when to make the proof ahead of the first answer, which has not the few seconds Google
 * Play can take over it: each time the app comes on screen while both the developer and the user
 * have AI answers on.
 *
 * Not whenever the app's process starts: a widget, a tile, a notification or the day's verdict
 * start it too, with nobody there to ask anything, and Google Play makes only so many proofs a
 * day for the whole app.
 */
internal fun proofWanted(
    onScreen: Flow<Boolean>,
    aiAvailable: Flow<Boolean>,
    userAllowsAi: Flow<Boolean>
): Flow<Unit> =
    combine(onScreen, aiAvailable, userAllowsAi) { shown, available, allowed -> shown && available && allowed }
        .distinctUntilChanged()
        .filter { it }
        .map { }
