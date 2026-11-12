package com.example.magiceightball.feature.chat.components

import org.junit.Assert.assertEquals
import org.junit.Test

class SoftHyphensTest {

    @Test
    fun `a long word may be cut where a syllable starts, in each of the app's languages`() {
        assertEquals("Sí, sorprendente-mente.", marked("Sí, sorprendentemente."))
        assertEquals("Ne-ce-sa-ria-mente.", marked("Necesariamente."))
        assertEquals("Sim, de-fi-ni-ti-va-mente.", marked("Sim, definitivamente."))
        assertEquals("Isso é au-tossa-bo-ta-gem.", marked("Isso é autossabotagem."))
        assertEquals("Abso-lu-tely, unquestio-nably.", marked("Absolutely, unquestionably."))
    }

    @Test
    fun `letters that are written as one sound, or belong to the next syllable together, stay together`() {
        assertEquals("cachorro", marked("cachorro"))
        assertEquals("Ella sonríe", marked("Ella sonríe"))
        assertEquals("orgulho", marked("orgulho"))
        assertEquals("sobre", marked("sobre"))
    }

    @Test
    fun `no word is cut within two letters of either of its ends`() {
        assertEquals("Sí. Qué pena.", marked("Sí. Qué pena."))
        assertEquals("amor", marked("amor"))
        assertEquals("ideal", marked("ideal"))
    }

    /** The text with its marks made visible. */
    private fun marked(text: String): String = text.withSoftHyphens().replace('\u00AD', '-')
}
