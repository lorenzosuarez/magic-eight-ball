package com.example.magiceightball.core.data.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * A release build is vouched for by Google Play: that it is the app as it was published, on a
 * device that can be trusted. This file is compiled into release builds only.
 */
internal fun appCheckProviderFactory(): AppCheckProviderFactory =
    PlayIntegrityAppCheckProviderFactory.getInstance()
