package com.lorenzosuarez.magiceightball.phone.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialTest {

    @Test
    fun `a name is shown by its first letter, made a capital`() {
        assertEquals("F", initialOf("ferchis.mtz"))
        assertEquals("Ñ", initialOf("  ñandú"))
        assertEquals("4", initialOf("404_renzo"))
    }

    @Test
    fun `a name that opens on an emoji or a sign is shown by its first letter, never half an emoji`() {
        assertEquals("V", initialOf("✨valen✨"))
        assertEquals("J", initialOf("🔥 Juan"))
        assertEquals("L", initialOf("_luu"))
    }

    @Test
    fun `a name with no letter is shown by what it opens with, whole`() {
        assertEquals("🔥", initialOf("🔥🔥"))
        assertEquals("", initialOf("   "))
    }
}
