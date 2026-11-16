package com.lorenzosuarez.magiceightball.phone.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShopTest {

    private val monthly = Plan("monthly", null, "base", listOf(Phase("0,99 €", "P1M", renews = true)))

    @Test
    fun `a period is read as the store writes it, in weeks, months or years`() {
        assertEquals(BillingPeriod(1, BillingPeriod.Unit.WEEK), BillingPeriod.of("P1W"))
        assertEquals(BillingPeriod(4, BillingPeriod.Unit.WEEK), BillingPeriod.of("P4W"))
        assertEquals(BillingPeriod(1, BillingPeriod.Unit.MONTH), BillingPeriod.of("P1M"))
        assertEquals(BillingPeriod(6, BillingPeriod.Unit.MONTH), BillingPeriod.of("P6M"))
        assertEquals(BillingPeriod(1, BillingPeriod.Unit.YEAR), BillingPeriod.of("P1Y"))
    }

    @Test
    fun `a period that cannot be said plainly is none`() {
        listOf("", "monthly", "P3D", "P1Y6M", "P1M1D", "P0M", "PT1H").forEach { assertNull(it, BillingPeriod.of(it)) }
    }

    @Test
    fun `the base plan is sold at its own price and period`() {
        assertEquals(monthly to Offer("0,99 €", BillingPeriod(1, BillingPeriod.Unit.MONTH)), listOf(monthly).sold())
    }

    @Test
    fun `a trial or a discount listed before it is passed over, and so is another base plan`() {
        val trial = Plan("monthly", "trial", "trial", listOf(Phase("Free", "P1W", renews = false), monthly.phases.single()))
        val yearly = Plan("yearly", null, "year", listOf(Phase("9,99 €", "P1Y", renews = true)))
        assertEquals("base", listOf(trial, yearly, monthly).sold()?.first?.token)
        assertNull(listOf(trial, yearly).sold())
    }

    @Test
    fun `nothing is sold on terms the sheet does not say`() {
        // A plan that ends by itself, one that changes price on the way, and one with no period to name.
        assertNull(listOf(monthly.copy(phases = listOf(Phase("0,99 €", "P1M", renews = false)))).sold())
        assertNull(listOf(monthly.copy(phases = monthly.phases + monthly.phases)).sold())
        assertNull(listOf(monthly.copy(phases = listOf(Phase("0,99 €", "P3D", renews = true)))).sold())
        assertNull(emptyList<Plan>().sold())
    }

    @Test
    fun `the page of the subscription is the one Google Play gives for it`() {
        assertEquals(
            "https://play.google.com/store/account/subscriptions?sku=premium_subscription&package=com.example",
            subscriptionPage("com.example")
        )
    }
}
