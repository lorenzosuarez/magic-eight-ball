package com.lorenzosuarez.magiceightball.phone

import com.lorenzosuarez.magiceightball.phone.push.DailyVerdict
import android.content.Intent
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lorenzosuarez.magiceightball.phone.ads.AdsController
import com.lorenzosuarez.magiceightball.phone.ui.EightBallScreen
import com.lorenzosuarez.magiceightball.phone.ui.MagicEightBallPhoneTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: EightBallViewModel by viewModels()

    @Inject
    lateinit var ads: AdsController

    // Portrait is a product decision for phones only: R.bool.portrait_only is false from 600dp
    // of width up, so tablets and foldables keep rotating freely, which is what the check asks for.
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        // The app follows the system's light or dark setting, and so do the bars' icons.
        val bars = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)

        // Set both ways: a requested orientation outlives the activity being recreated, so a
        // foldable locked while folded would otherwise stay locked once unfolded.
        requestedOrientation = if (resources.getBoolean(R.bool.portrait_only)) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        setContent {
            val adPrivacyOptionsRequired by ads.privacyOptionsRequired.collectAsStateWithLifecycle()
            val rewardedReady by ads.rewardedReady.collectAsStateWithLifecycle()
            MagicEightBallPhoneTheme {
                EightBallScreen(
                    viewModel = viewModel,
                    onAnswerDismissed = { ads.onAnswerDismissed(this) },
                    onShowAdPrivacyOptions = if (adPrivacyOptionsRequired) {
                        { ads.showPrivacyOptions(this) }
                    } else {
                        null
                    },
                    // Offered only while there is an ad to watch.
                    onGetMoreAiAnswers = if (rewardedReady) {
                        { ads.showRewarded(this, viewModel::onAiAnswersEarned) }
                    } else {
                        null
                    }
                )
            }
        }
        ads.start(this)
        holdFirstFrameUntilSettingsAreRead()
        showDailyVerdictFrom(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showDailyVerdictFrom(intent)
    }

    /** Opened from the day's verdict: the ball shows it, once, however often the activity is recreated. */
    private fun showDailyVerdictFrom(intent: Intent?) {
        val verdict = intent?.getStringExtra(DailyVerdict.EXTRA_VERDICT) ?: return
        intent.removeExtra(DailyVerdict.EXTRA_VERDICT)
        viewModel.showDailyVerdict(verdict)
    }

    /**
     * Keeps the launch screen up for the few milliseconds it takes to read the saved choices,
     * so the first frame the user sees is already in their language and with their settings.
     */
    private fun holdFirstFrameUntilSettingsAreRead() {
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewModel.state.value == null) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            }
        )
    }
}
