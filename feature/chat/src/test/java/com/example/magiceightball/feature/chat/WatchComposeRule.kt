package com.example.magiceightball.feature.chat

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.robolectric.Shadows.shadowOf

/**
 * [compose], ready to show the watch's screens on the JVM. The compose rule starts an empty
 * activity, which only exists in tests: it is declared to Robolectric before the rule runs,
 * rather than in a manifest that would ship with the app.
 */
internal fun onWatch(compose: ComposeContentTestRule): RuleChain = RuleChain
    .outerRule(
        object : ExternalResource() {
            override fun before() {
                val context = ApplicationProvider.getApplicationContext<Application>()
                shadowOf(context.packageManager).addOrUpdateActivity(
                    ActivityInfo().apply {
                        packageName = context.packageName
                        name = ComponentActivity::class.java.name
                        // No title bar: the content is the whole screen, as on the watch.
                        theme = android.R.style.Theme_Material_NoActionBar
                    }
                )
            }
        }
    )
    .around(compose)
