package com.example.billing

import android.app.Activity
import android.content.Context
import com.example.billing.service.BillingServiceFactory
import com.example.billing.service.IBillingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TargetBillingPlatform(val displayName: String) {
    GOOGLE_PLAY("Google Play Billing (Android)"),
    APPLE_STOREKIT("Apple StoreKit 2 (iOS / App Store)")
}

/**
 * Cross-Platform Unified Billing Service Layer facade.
 * Implements IBillingService to abstract Google Play Billing API (Android)
 * and Apple StoreKit 2 API (iOS) into a unified interface across both ecosystems.
 */
class UnifiedBillingManager private constructor(private val context: Context) : IBillingService {

    private val _activePlatform = MutableStateFlow(TargetBillingPlatform.GOOGLE_PLAY)
    val activePlatform: StateFlow<TargetBillingPlatform> = _activePlatform.asStateFlow()

    private val playService: IBillingService by lazy {
        BillingServiceFactory.getBillingService(context, TargetBillingPlatform.GOOGLE_PLAY)
    }

    private val appleService: IBillingService by lazy {
        BillingServiceFactory.getBillingService(context, TargetBillingPlatform.APPLE_STOREKIT)
    }

    private val currentService: IBillingService
        get() = if (_activePlatform.value == TargetBillingPlatform.GOOGLE_PLAY) playService else appleService

    override val billingStatus: StateFlow<UserBillingStatus>
        get() = currentService.billingStatus

    override val plans: StateFlow<List<VedicPaidPlan>>
        get() = currentService.plans

    override val isProcessing: StateFlow<Boolean>
        get() = currentService.isProcessing

    override val lastMessage: StateFlow<String?>
        get() = currentService.lastMessage

    val playBillingStatus: StateFlow<UserBillingStatus>
        get() = playService.billingStatus

    val appleBillingStatus: StateFlow<UserBillingStatus>
        get() = appleService.billingStatus

    fun setTargetPlatform(platform: TargetBillingPlatform) {
        _activePlatform.value = platform
    }

    override fun launchPurchaseFlow(activity: Activity?, plan: VedicPaidPlan) {
        currentService.launchPurchaseFlow(activity, plan)
    }

    override fun restorePurchases() {
        currentService.restorePurchases()
    }

    override fun openSubscriptionsPortal(context: Context) {
        currentService.openSubscriptionsPortal(context)
    }

    fun openSubscriptionManagement() {
        openSubscriptionsPortal(context)
    }

    override fun getPlatform(): TargetBillingPlatform {
        return _activePlatform.value
    }

    companion object {
        @Volatile
        private var INSTANCE: UnifiedBillingManager? = null

        fun getInstance(context: Context): UnifiedBillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UnifiedBillingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
