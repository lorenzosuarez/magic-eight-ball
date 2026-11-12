package com.lorenzosuarez.magiceightball.phone.usage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageConsentTest {

    @Test
    fun `until it is known whether the user must be asked, nothing is measured`() {
        assertFalse(UsageConsent.allows(gdprApplies = null, purposeConsents = null))
        assertFalse(UsageConsent.allows(gdprApplies = null, purposeConsents = "1111111111"))
    }

    @Test
    fun `where the user need not be asked, measuring is allowed`() {
        assertTrue(UsageConsent.allows(gdprApplies = 0, purposeConsents = null))
    }

    @Test
    fun `where the user must be asked, every purpose of the measuring has to be consented to`() {
        assertTrue(UsageConsent.allows(gdprApplies = 1, purposeConsents = "1000000101"))
        assertTrue(UsageConsent.allows(gdprApplies = 1, purposeConsents = "11111111111"))

        // Storage refused, measuring refused, improving refused, in turn.
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = "0000000101"))
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = "1000000001"))
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = "1000000100"))
    }

    @Test
    fun `a refusal, an empty choice or a choice cut short allows nothing`() {
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = null))
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = ""))
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = "1"))
        assertFalse(UsageConsent.allows(gdprApplies = 1, purposeConsents = "0000000000"))
    }
}
