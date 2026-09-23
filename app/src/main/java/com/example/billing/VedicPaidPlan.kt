package com.example.billing

/**
 * Represents the tier and duration of an Ad-Free Plan available via Google Play Store.
 */
enum class PlanTier {
    MONTHLY,
    ANNUAL,
    ASTROLOGER_PRO_MONTHLY,
    ASTROLOGER_PRO_ANNUAL,
    ASTROLOGER_QUERY,
    ASTROLOGER_REPORT,
    LIFETIME
}

/**
 * Metadata and pricing for plans offered through Google Play Billing.
 */
data class VedicPaidPlan(
    val planId: String,
    val tier: PlanTier,
    val title: String,
    val subtitle: String,
    val formattedPrice: String,
    val billingCycleText: String,
    val isSubscription: Boolean,
    val isPopular: Boolean = false,
    val savingsBadge: String? = null,
    val trialDays: Int = 0,
    val trialBadge: String? = null,
    val benefits: List<String>
) {
    companion object {
        const val PRODUCT_MONTHLY = "vedic_ad_free_monthly"
        const val PRODUCT_ANNUAL = "vedic_ad_free_annual"
        const val PRODUCT_ASTROLOGER_PRO_MONTHLY = "vedic_astrologer_pro_monthly"
        const val PRODUCT_ASTROLOGER_PRO_ANNUAL = "vedic_astrologer_pro_annual"
        const val PRODUCT_ASTROLOGER_QUERY = "vedic_astrologer_query_199"
        const val PRODUCT_ASTROLOGER_REPORT = "vedic_astrologer_report_499"
        const val PRODUCT_LIFETIME = "vedic_ad_free_lifetime"

        val DEFAULT_PLANS = listOf(
            VedicPaidPlan(
                planId = PRODUCT_MONTHLY,
                tier = PlanTier.MONTHLY,
                title = "Monthly Sadhak Pass",
                subtitle = "100% Ad-Free • Wear OS Sync",
                formattedPrice = "₹49",
                billingCycleText = "/ month",
                isSubscription = true,
                isPopular = false,
                savingsBadge = null,
                trialDays = 7,
                trialBadge = "7 Days Free",
                benefits = listOf(
                    "7 Days 100% Free Trial",
                    "Ad-Free Cosmic Predictions",
                    "Full Wear OS Watch Sync",
                    "Cancel anytime in Play Store"
                )
            ),
            VedicPaidPlan(
                planId = PRODUCT_ANNUAL,
                tier = PlanTier.ANNUAL,
                title = "Annual Rishi Pass",
                subtitle = "Save ₹89 • Best Value",
                formattedPrice = "₹499",
                billingCycleText = "/ year",
                isSubscription = true,
                isPopular = true,
                savingsBadge = "SAVE 15%",
                trialDays = 14,
                trialBadge = "14 Days Free",
                benefits = listOf(
                    "14 Days 100% Free Trial",
                    "All Monthly Features Included",
                    "Save ₹89 vs Monthly Renewal",
                    "PDF Kundali Chart Exports"
                )
            ),
            VedicPaidPlan(
                planId = PRODUCT_ASTROLOGER_PRO_MONTHLY,
                tier = PlanTier.ASTROLOGER_PRO_MONTHLY,
                title = "Astrologer Monthly Pro",
                subtitle = "Verified Scholar Workspace",
                formattedPrice = "₹199",
                billingCycleText = "/ month",
                isSubscription = true,
                isPopular = false,
                savingsBadge = null,
                trialDays = 7,
                trialBadge = "7 Days Free",
                benefits = listOf(
                    "7 Days Free Trial",
                    "Verified Scholar Badge & Listing",
                    "Manage Client Folders & Notes",
                    "Receive 1-on-1 Consultation Queries"
                )
            ),
            VedicPaidPlan(
                planId = PRODUCT_ASTROLOGER_PRO_ANNUAL,
                tier = PlanTier.ASTROLOGER_PRO_ANNUAL,
                title = "Astrologer Annual Pro Pass",
                subtitle = "Featured Scholar Suite • Save 79%",
                formattedPrice = "₹499",
                billingCycleText = "/ year",
                isSubscription = true,
                isPopular = false,
                savingsBadge = "SAVE 79%",
                trialDays = 14,
                trialBadge = "14 Days Free",
                benefits = listOf(
                    "14 Days Free Trial",
                    "Priority Placement on Jyotish Tab",
                    "Unlimited Client Storage",
                    "Lower Consultation Commission"
                )
            ),
            VedicPaidPlan(
                planId = PRODUCT_ASTROLOGER_QUERY,
                tier = PlanTier.ASTROLOGER_QUERY,
                title = "Ask an Astrologer Query",
                subtitle = "Direct personalized written query answered by verified Vedic scholar",
                formattedPrice = "₹199",
                billingCycleText = "per question",
                isSubscription = false,
                isPopular = false,
                savingsBadge = "1-ON-1 CONSULTATION",
                trialDays = 0,
                trialBadge = null,
                benefits = listOf(
                    "1 Specific Question (Career, Marriage, Health)",
                    "Verified Vedic Astrologer Analysis",
                    "Customized Remedies & Mantra Recommendations",
                    "Written Response Delivered in App within 24 Hrs"
                )
            ),
            VedicPaidPlan(
                planId = PRODUCT_ASTROLOGER_REPORT,
                tier = PlanTier.ASTROLOGER_REPORT,
                title = "Detailed Astrologer Report",
                subtitle = "30-page verified Kundali, Dasha & Vastu Audit PDF report",
                formattedPrice = "₹499",
                billingCycleText = "one-time report",
                isSubscription = false,
                isPopular = false,
                savingsBadge = "FULL LIFE AUDIT",
                trialDays = 0,
                trialBadge = null,
                benefits = listOf(
                    "Comprehensive 30-Page Custom PDF Report",
                    "Hand-Verified Astrologer Chart Analysis",
                    "10-Year Dasha & Transit Forecast",
                    "Complete Room-by-Room Vastu Audit Remedies"
                )
            )
        )
    }
}

/**
 * Encapsulates the user's active billing status.
 */
data class UserBillingStatus(
    val isAdFree: Boolean = false,
    val planId: String? = null,
    val orderId: String? = null,
    val purchaseToken: String? = null,
    val purchaseTimeMs: Long = 0L,
    val isAutoRenewing: Boolean = true,
    val planTier: PlanTier? = null,
    val isTrialActive: Boolean = false,
    val trialDaysRemaining: Int = 0
)
