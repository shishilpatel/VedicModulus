package com.example.billing.service

import android.app.Activity
import android.content.Context
import com.example.billing.AppleStoreKitManager
import com.example.billing.TargetBillingPlatform
import com.example.billing.UserBillingStatus
import com.example.billing.VedicPaidPlan
import kotlinx.coroutines.flow.StateFlow

/**
 * Apple StoreKit 2 Billing Service implementation for iOS platform.
 */
class AppleStoreKitBillingService(context: Context) : IBillingService {

    private val manager = AppleStoreKitManager.getInstance(context)

    override val billingStatus: StateFlow<UserBillingStatus> = manager.billingStatus
    override val plans: StateFlow<List<VedicPaidPlan>> = manager.plans
    override val isProcessing: StateFlow<Boolean> = manager.isProcessingPurchase
    override val lastMessage: StateFlow<String?> = manager.lastBillingMessage

    override fun launchPurchaseFlow(activity: Activity?, plan: VedicPaidPlan) {
        manager.launchAppleStoreKitPurchaseFlow(plan)
    }

    override fun restorePurchases() {
        manager.restoreApplePurchases()
    }

    override fun openSubscriptionsPortal(context: Context) {
        manager.openAppleSubscriptionsManagement()
    }

    override fun getPlatform(): TargetBillingPlatform {
        return TargetBillingPlatform.APPLE_STOREKIT
    }
}
