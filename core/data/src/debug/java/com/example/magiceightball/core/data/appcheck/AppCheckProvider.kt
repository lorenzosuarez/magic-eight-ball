package com.example.magiceightball.core.data.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * A debug build is not the app Google Play knows, so it cannot be vouched for by it. It proves
 * itself with a token of its own instead, which it writes to the log when the app starts (a line
 * that begins "Firebase App Check debug token:") and which counts only once the developer has
 * added it to the project in the Firebase console. This file is compiled into debug builds only.
 */
internal fun appCheckProviderFactory(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
