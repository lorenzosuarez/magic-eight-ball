package com.lorenzosuarez.magiceightball.phone.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.lorenzosuarez.magiceightball.phone.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AdsController] over Google AdMob.
 *
 * Ads are off unless the Remote Config parameter [KEY_ENABLED] is true, so they can be switched
 * on, and off again, without a new release; until then this class asks for nothing and loads
 * nothing. When they are on, the user is first asked for consent where the law requires it
 * (through Google's User Messaging Platform), and no ad is requested before that allows it.
 *
 * Everything here runs on the main thread, as both SDKs require.
 */
@Singleton
class AdMobAdsController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : AdsController {

    private val consent: ConsentInformation by lazy { UserMessagingPlatform.getConsentInformation(context) }
    private val pacing = AdPacing()
    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var sdkStarted = false

    private val _privacyOptionsRequired = MutableStateFlow(false)
    override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    override fun start(activity: Activity) {
        if (!isEnabled()) return
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { prepare() }
            },
            // Offline, for instance: what the user chose last time still stands.
            { prepare() }
        )
        // Without waiting for the answer when the choice of an earlier run already allows ads.
        prepare()
    }

    override fun onAnswerDismissed(activity: Activity) {
        if (!isEnabled()) return
        val now = clock.millis()
        if (!pacing.answerClosed(now)) return
        val ad = interstitial
        if (ad == null) {
            load()
            return
        }
        interstitial = null
        pacing.adShown(now)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = load()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = load()
        }
        ad.show(activity)
    }

    override fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { prepare() }
    }

    /** Starts the ads SDK and loads the next ad, once and only if consent allows it. */
    private fun prepare() {
        _privacyOptionsRequired.value =
            consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (!consent.canRequestAds()) return
        if (!sdkStarted) {
            sdkStarted = true
            MobileAds.initialize(context)
        }
        load()
    }

    private fun load() {
        if (interstitial != null || loading || !consent.canRequestAds()) return
        loading = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading = false
                }

                // No ad this time: the app is no worse without one.
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                }
            }
        )
    }

    private fun isEnabled(): Boolean =
        FirebaseApp.getApps(context).isNotEmpty() && FirebaseRemoteConfig.getInstance().getBoolean(KEY_ENABLED)

    private companion object {
        const val KEY_ENABLED = "ads_enabled"
    }
}
