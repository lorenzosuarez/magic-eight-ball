package com.lorenzosuarez.magiceightball.phone.ui

import com.lorenzosuarez.magiceightball.phone.R
import com.lorenzosuarez.magiceightball.phone.VoiceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceSummaryTest {

    @Test
    fun `the voice row says which of its two switches are on`() {
        assertEquals(R.string.options_voice_both, voiceSummary(VoiceStatus.Ready, spoken = true))
        assertEquals(R.string.options_voice_questions, voiceSummary(VoiceStatus.Ready, spoken = false))
        assertEquals(R.string.options_voice_answers, voiceSummary(VoiceStatus.Off, spoken = true))
        assertEquals(R.string.options_voice_off, voiceSummary(VoiceStatus.Off, spoken = false))
    }

    @Test
    fun `what stands in the way of asking by voice is said before anything else`() {
        assertEquals(R.string.settings_voice_denied, voiceSummary(VoiceStatus.PermissionDenied, spoken = true))
        assertEquals(R.string.settings_voice_unsupported, voiceSummary(VoiceStatus.Unsupported, spoken = true))
    }
}
