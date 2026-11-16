package com.lorenzosuarez.magiceightball.phone.ui

import android.app.Application
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MotionRemovedTest {

    private val resolver = ApplicationProvider.getApplicationContext<Application>().contentResolver

    @Test
    fun `motion is removed only for whoever turned the device's animations off`() {
        assertFalse(resolver.motionRemoved())

        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        assertTrue(resolver.motionRemoved())

        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f)
        assertFalse(resolver.motionRemoved())
    }
}
