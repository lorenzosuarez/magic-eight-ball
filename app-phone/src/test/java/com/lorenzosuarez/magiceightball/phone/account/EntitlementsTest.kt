package com.lorenzosuarez.magiceightball.phone.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntitlementsTest {

    @Test
    fun `a grant missing from a copy that never heard from the server takes Premium from nobody`() {
        assertNull(heldBy(exists = false, fromCache = true, lasting = false))
    }

    @Test
    fun `nothing is held once the server says there is no grant`() {
        assertEquals(Held.NOTHING, heldBy(exists = false, fromCache = false, lasting = false))
    }

    @Test
    fun `a grant is believed from the copy as from the server, a subscription by its end`() {
        for (fromCache in listOf(true, false)) {
            assertEquals(Held.SUBSCRIBED, heldBy(exists = true, fromCache = fromCache, lasting = true))
            assertEquals(Held.FOR_GOOD, heldBy(exists = true, fromCache = fromCache, lasting = false))
        }
    }
}
