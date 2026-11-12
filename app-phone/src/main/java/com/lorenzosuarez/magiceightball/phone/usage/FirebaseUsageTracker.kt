package com.lorenzosuarez.magiceightball.phone.usage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.example.magiceightball.core.domain.gateway.UsageEvent
import com.example.magiceightball.core.domain.gateway.UsageTracker
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [UsageTracker] over Google Analytics for Firebase.
 *
 * Nothing is measured until the user's consent allows it: collection is off in the manifest and
 * is switched on here only by [UsageConsent]. The choice is read from where the consent message
 * leaves it and followed as it changes, so withdrawing it under "Ad privacy choices" stops the
 * measuring as well. The measurements are never offered to advertising: the consent types that
 * would allow it are always denied, and neither the advertising ID nor the device's own ID is
 * collected for them.
 *
 * Without Firebase in the build this does nothing at all.
 */
@Singleton
class FirebaseUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context
) : UsageTracker {

    private val analytics: FirebaseAnalytics? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAnalytics.getInstance(context) else null

    private val choices: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // Held here: the preferences keep only a weak reference to their listeners.
    private val onChoiceChanged = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key.startsWith(UsageConsent.KEY_PREFIX)) applyConsent()
    }

    init {
        choices.registerOnSharedPreferenceChangeListener(onChoiceChanged)
        applyConsent()
    }

    override fun track(event: UsageEvent) {
        val analytics = analytics ?: return
        val params = Bundle().apply { event.params.forEach { (key, value) -> putString(key, value) } }
        // Dropped by the SDK while collection is off.
        analytics.logEvent(event.name, params)
    }

    private fun applyConsent() {
        val analytics = analytics ?: return
        val allowed = UsageConsent.allows(
            gdprApplies = if (choices.contains(UsageConsent.KEY_GDPR_APPLIES)) {
                choices.getInt(UsageConsent.KEY_GDPR_APPLIES, 0)
            } else {
                null
            },
            purposeConsents = choices.getString(UsageConsent.KEY_PURPOSE_CONSENTS, null)
        )
        analytics.setConsent(
            mapOf(
                ConsentType.ANALYTICS_STORAGE to if (allowed) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                ConsentType.AD_STORAGE to ConsentStatus.DENIED,
                ConsentType.AD_USER_DATA to ConsentStatus.DENIED,
                ConsentType.AD_PERSONALIZATION to ConsentStatus.DENIED
            )
        )
        analytics.setAnalyticsCollectionEnabled(allowed)
    }
}

/**
 * Whether the user's privacy choice allows measuring how the app is used.
 *
 * The consent message (Google's User Messaging Platform) leaves the choice in the app's default
 * preferences under the keys of the IAB's Transparency and Consent Framework; this reads them.
 */
internal object UsageConsent {
    const val KEY_PREFIX = "IABTCF_"
    const val KEY_GDPR_APPLIES = "IABTCF_gdprApplies"
    const val KEY_PURPOSE_CONSENTS = "IABTCF_PurposeConsents"

    /** Storing on the device, measuring content, and improving the product: what this measuring is. */
    private val PURPOSES = listOf(1, 8, 10)

    /**
     * @param gdprApplies 1 where the user must be asked, 0 where not, null while nobody knows:
     * the message has not been answered, or was never shown.
     * @param purposeConsents one character per purpose, in order, '1' for those consented to.
     *
     * Until it is known whether the user must be asked, nothing is measured: the cautious answer.
     */
    fun allows(gdprApplies: Int?, purposeConsents: String?): Boolean = when (gdprApplies) {
        0 -> true
        1 -> PURPOSES.all { purposeConsents?.getOrNull(it - 1) == '1' }
        else -> false
    }
}
