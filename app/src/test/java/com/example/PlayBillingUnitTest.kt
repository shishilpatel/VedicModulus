package com.example

import com.example.billing.PlanTier
import com.example.billing.VedicPaidPlan
import org.junit.Assert.*
import org.junit.Test

class PlayBillingUnitTest {

    @Test
    fun defaultPlans_containMonthlyAnnualAndLifetime() {
        val plans = VedicPaidPlan.DEFAULT_PLANS
        assertEquals(3, plans.size)

        val monthly = plans.find { it.tier == PlanTier.MONTHLY }
        assertNotNull(monthly)
        assertEquals(VedicPaidPlan.PRODUCT_MONTHLY, monthly?.planId)
        assertTrue(monthly?.isSubscription == true)

        val annual = plans.find { it.tier == PlanTier.ANNUAL }
        assertNotNull(annual)
        assertEquals(VedicPaidPlan.PRODUCT_ANNUAL, annual?.planId)
        assertTrue(annual?.isPopular == true)
        assertTrue(annual?.isSubscription == true)

        val lifetime = plans.find { it.tier == PlanTier.LIFETIME }
        assertNotNull(lifetime)
        assertEquals(VedicPaidPlan.PRODUCT_LIFETIME, lifetime?.planId)
        assertFalse(lifetime?.isSubscription == true)
    }

    @Test
    fun plans_haveBenefitsList() {
        for (plan in VedicPaidPlan.DEFAULT_PLANS) {
            assertTrue("Plan ${plan.title} should have benefits", plan.benefits.isNotEmpty())
            assertTrue(plan.formattedPrice.isNotBlank())
        }
    }
}
