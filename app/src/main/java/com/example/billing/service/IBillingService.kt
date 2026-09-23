package com.example.billing.service

import android.app.Activity
import android.content.Context
import com.example.billing.TargetBillingPlatform
import com.example.billing.UserBillingStatus
import com.example.billing.VedicPaidPlan
import kotlinx.coroutines.flow.StateFlow

/**
 * Service Layer Interface for Cross-Platform Billing.
 * Abstracts Google Play Billing API (Android) and Apple StoreKit 2 API (iOS)
 * into a single unified contract for the subscription & pay-per-use model.
 */
interface IBillingService {
    val billingStatus: StateFlow<UserBillingStatus>
    val plans: StateFlow<List<VedicPaidPlan>>
    val isProcessing: StateFlow<Boolean>
    val lastMessage: StateFlow<String?>

    fun launchPurchaseFlow(activity: Activity?, plan: VedicPaidPlan)
    fun restorePurchases()
    fun openSubscriptionsPortal(context: Context)
    fun getPlatform(): TargetBillingPlatform
}
