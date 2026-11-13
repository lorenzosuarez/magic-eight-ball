package com.lorenzosuarez.magiceightball.phone.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * What whoever is using the app has paid for. One thing is meant to be paid for one day:
 * making characters of one's own. Nothing is sold yet, so nothing here asks anyone for anything.
 *
 * When something is sold, what was bought is to be heard from Google Play and checked on a
 * server before it counts: an app that takes its own word for it can be told anything.
 */
interface Entitlements {
    /** Whether whoever is using the app has what is sold. */
    val premium: StateFlow<Boolean>

    /** While nothing is sold: everyone has everything. */
    object Everyone : Entitlements {
        override val premium: StateFlow<Boolean> = MutableStateFlow(true)
    }
}
