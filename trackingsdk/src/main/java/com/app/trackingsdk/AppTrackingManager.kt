package com.app.trackingsdk

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import com.adjust.sdk.Adjust
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.revenuecat.purchases.CustomerInfo


object AppTrackingManager {
    private lateinit var userId: String
    private var isDebug: Boolean = false
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var firebaseAnalytics: FirebaseAnalytics? = null

    // Use lazy initialization for each manager
    private val adjustManager by lazy { AdjustManager }
    private val oneSignalInitializer by lazy { OneSignalInitializer }
    private val revenueCatManager by lazy { RevenueCatManager }

    private var isInitialized = false

    // Initialization function to set up the necessary SDKs once
    fun init(context: Context, isDebug: Boolean, userId: String) {
        if (isInitialized) return // Avoid re-initializing

        this.userId = userId
        this.isDebug = isDebug

        // Firebase setup
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(context).apply {
            setUserId(userId)
        }

        remoteConfig = FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(if (isDebug) 0 else 3600)
                    .build()
            )
        }

        // Fetch and activate remote config, then initialize other SDKs
        fetchRemoteConfigValues(context)

        isInitialized = true
    }

    private fun fetchRemoteConfigValues(context: Context) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val adjustKey = remoteConfig.getString("adjust_sdk_key").takeIf { it.isNotBlank() }
                val oneSignalKey = remoteConfig.getString("onesignal_sdk_key").takeIf { it.isNotBlank() }
                val revenueCatKey = remoteConfig.getString("revenuecat_sdk_key").takeIf { it.isNotBlank() }

                if (adjustKey != null && oneSignalKey != null && revenueCatKey != null) {
                    initializeSDKs(context, adjustKey, oneSignalKey, revenueCatKey)
                } else {
                    Log.e("AppTrackingManager", "SDK keys are missing from remote config")
                }
            }
        }
    }

    private fun initializeSDKs(context: Context, adjustKey: String, oneSignalKey: String, revenueCatKey: String) {
        adjustManager.initialize(context, adjustKey, isDebug, userId)
        oneSignalInitializer.initialize(context, oneSignalKey)
        revenueCatManager.initialize(context, revenueCatKey, userId)
    }

    // Example of a function using initialized SDKs
    fun checkIsSubscribed(context: Context, entitlementId: String, onResult: (Boolean) -> Unit) {
        revenueCatManager.isUserSubscribed(context, entitlementId) { isSubscribed ->
            onResult(isSubscribed)
        }
    }

    fun presentPayWall(
        userId: String,
        context: Context,
        caller: ActivityResultCaller,
        adjustSubscriptionEventToken: String,
        onResult: (String, IapObject?) -> Unit
    ) {
        // Present RevenueCat paywall
        RevenueCatManager.presentPayWall(caller) { result, customerInfo ->
            when (result) {
                "successfully purchased" -> {
                    // Initialize BillingManager to capture orderId and purchaseToken
                    BillingManager().initialize(context) { orderId, purchaseToken ->
                        // Create IapObject with retrieved orderId and purchaseToken
                        val iapObject = customerInfo?.let {
                            createIapObjectFromCustomerInfo(it, userId = userId, orderId = orderId, purchaseToken = purchaseToken)
                        }

                        // Track subscription event with Adjust
                        trackSubscriptionEvent(adjustSubscriptionEventToken, iapObject, context)

                        // Callback to handle successful purchase with IapObject
                        onResult("successfully purchased", iapObject)
                    }
                }
                "canceled" -> onResult("canceled", null)
                else -> onResult("error", null)
            }
        }
    }

    fun trackSubscriptionEvent(
        adjustSubscriptionEventToken: String,
        iapObject: IapObject?,
        context: Context
    ) {
        iapObject?.let {
            val adjustEvent = AdjustManager.getParamsToAdjust(adjustSubscriptionEventToken, it, context)
            Adjust.trackEvent(adjustEvent)
        } ?: Log.w("AppTrackingManager", "IapObject is null, cannot track subscription event")
    }






    private fun initializeOtherSDKs(
        adjustKey: String,
        oneSignalKey: String,
        revenueCatKey: String,
        isDebug: Boolean,
        context: Context,
        userId: String
    ) {
        AdjustManager.initialize(context, adjustKey, isDebug, userId)
        OneSignalInitializer.initialize(context, oneSignalKey)
        RevenueCatManager.initialize(context, revenueCatKey, userId)
    }

    fun createIapObjectFromCustomerInfo(
        customerInfo: CustomerInfo,
        userId: String,
        orderId: String?,
        purchaseToken: String?
    ): IapObject {
        val activeSubscriptions = customerInfo.activeSubscriptions
        val inAppProductId = activeSubscriptions.firstOrNull() ?: ""  // Use first active subscription if available
        val transactionId = customerInfo.nonSubscriptionTransactions.firstOrNull()?.transactionIdentifier

        return IapObject(
            value = inAppProductId,
            transactionId = transactionId,
            originalTransactionId = customerInfo.originalAppUserId,
            purchaseToken = purchaseToken,
            orderId = orderId,
            userId = userId
        )
    }


}