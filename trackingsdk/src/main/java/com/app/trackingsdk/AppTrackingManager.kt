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


class AppTrackingManager {
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var firebaseAnalytics: FirebaseAnalytics? = null


    fun init(context: Context, isDebug: Boolean, userId: String) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        // Optional: Set user properties or analytics configurations here
        firebaseAnalytics?.setUserId(userId)

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        fetchRemoteConfigValues(isDebug, userId, context)
    }

    private fun fetchRemoteConfigValues(isDebug: Boolean, userId: String, context: Context) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val adjustKey = remoteConfig.getString("adjust_sdk_key").takeIf { it.isNotBlank() }
                    val oneSignalKey = remoteConfig.getString("onesignal_sdk_key").takeIf { it.isNotBlank() }
                    val revenueCatKey = remoteConfig.getString("revenuecat_sdk_key").takeIf { it.isNotBlank() }

                    if (adjustKey != null && oneSignalKey != null && revenueCatKey != null) {
                        initializeOtherSDKs(adjustKey, oneSignalKey, revenueCatKey, isDebug, context, userId)
                    } else {
                        // Handle missing or empty configuration keys here
                        Log.e("TrackingSdkManager", "SDK keys are missing from remote config")
                    }
                }
            }
    }

    fun trackSubscriptionEvent(
        adjustSubscriptionEventToken: String,
        iapObject: IapObject?,
        context: Context
    ) {
        val adjustEvent = AdjustManager().getParamsToAdjust(adjustSubscriptionEventToken, iapObject, context)
        Adjust.trackEvent(adjustEvent)
    }


    fun checkIsSubscribed(context: Context, entiltmentId :String, onResult: (Boolean) -> Unit) {
        RevenueCatManager().isUserSubscribed(context, entitlementId = entiltmentId) { isSubscribed ->
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
        RevenueCatManager().presentPayWall(caller) { result, customerInfo ->
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



    private fun initializeOtherSDKs(
        adjustKey: String,
        oneSignalKey: String,
        revenueCatKey: String,
        isDebug: Boolean,
        context: Context,
        userId: String
    ) {
        AdjustManager().initialize(context, adjustKey, isDebug, userId)
        OneSignalInitializer().initialize(context, oneSignalKey)
        RevenueCatManager().initialize(context, revenueCatKey, userId)
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