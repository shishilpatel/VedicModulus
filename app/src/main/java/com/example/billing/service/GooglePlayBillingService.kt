package com.example.billing.service

import android.app.Activity
import android.content.Context
import com.example.billing.PlayBillingManager
import com.example.billing.TargetBillingPlatform
import com.example.billing.UserBillingStatus
import com.example.billing.VedicPaidPlan
import kotlinx.coroutines.flow.StateFlow

/**
 * Google Play Billing Service implementation for Android platform.
 */
class GooglePlayBillingService(context: Context) : IBillingService {

    private val manager = PlayBillingManager.getInstance(context)

    override val billingStatus: StateFlow<UserBillingStatus> = manager.billingStatus
    override val plans: StateFlow<List<VedicPaidPlan>> = manager.plans
    override val isProcessing: StateFlow<Boolean> = manager.isProcessingPurchase
    override val lastMessage: StateFlow<String?> = manager.lastBillingMessage

    override fun launchPurchaseFlow(activity: Activity?, plan: VedicPaidPlan) {
        if (activity != null) {
            manager.launchBillingFlow(activity, plan)
        } else {
            manager.simulateGooglePlayPurchase(plan)
        }
    }

    override fun restorePurchases() {
        manager.queryActivePurchases()
    }

    override fun openSubscriptionsPortal(context: Context) {
        manager.openPlayStoreSubscriptionManagement(context)
    }

    override fun getPlatform(): TargetBillingPlatform {
        return TargetBillingPlatform.GOOGLE_PLAY
    }
}
