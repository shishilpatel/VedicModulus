package com.example.billing.service

import android.content.Context
import com.example.billing.TargetBillingPlatform

/**
 * Service Layer Factory providing the concrete IBillingService instance
 * for Google Play Billing (Android) or Apple StoreKit 2 (iOS).
 */
object BillingServiceFactory {

    @Volatile
    private var googlePlayService: IBillingService? = null

    @Volatile
    private var appleStoreKitService: IBillingService? = null

    fun getBillingService(context: Context, platform: TargetBillingPlatform): IBillingService {
        return when (platform) {
            TargetBillingPlatform.GOOGLE_PLAY -> {
                googlePlayService ?: synchronized(this) {
                    googlePlayService ?: GooglePlayBillingService(context.applicationContext).also { googlePlayService = it }
                }
            }
            TargetBillingPlatform.APPLE_STOREKIT -> {
                appleStoreKitService ?: synchronized(this) {
                    appleStoreKitService ?: AppleStoreKitBillingService(context.applicationContext).also { appleStoreKitService = it }
                }
            }
        }
    }
}
