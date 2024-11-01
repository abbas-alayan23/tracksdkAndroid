package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.app.trackingsdk.modules.AdjustModule
import com.app.trackingsdk.modules.FirebaseAnalyticsModule
import com.app.trackingsdk.modules.FirebaseModule
import com.app.trackingsdk.modules.OneSignalModule
import com.app.trackingsdk.modules.RemoteConfigModule
import com.app.trackingsdk.modules.RevenueCatModule
import com.app.trackingsdk.modules.SdkInitializerModule
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics

class TrackingSdk {
    private var analyticsModule: FirebaseAnalyticsModule? = null


    companion object {
        private const val LOG_TAG = "TrackingSdk"

    }

    fun initialize(
        context: Context,
        userId: String,
        packageName: String,
        firebaseApiKey: String,
        firebaseAppId: String,
        firebaseProjectId: String,
        firebaseDatabaseUrl: String,
        firebaseStorageBucket: String,
        firebaseMessagingSenderId: String,
        onComplete: () -> Unit
    ) {
        // Set Firebase options
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        // Initialize Firebase
        FirebaseModule.initialize(context, firebaseOptions)

        // Fetch and set configuration values
        RemoteConfigModule.initialize { configMap ->
            populateCoreModule(configMap)

            // Ensure all required keys are present before initializing SDKs
            if (areKeysPresentInCoreModule()) {
                SdkInitializerModule.initializeSDKs(context)
                logInitializationStatuses()
            } else {
                Log.e(LOG_TAG, "CoreModule is missing keys; SDK initialization skipped.")
            }
            onComplete()


            analyticsModule = FirebaseAnalyticsModule(context)


        }
    }

    private fun populateCoreModule(configMap: Map<String, String>) {
        configMap["adjust_sdk_key"]?.let { CoreModule.setAdjustSdkKey(it) }
        configMap["onesignal_sdk_key"]?.let { CoreModule.setOneSignalSdkKey(it) }
        configMap["revenuecat_api_key"]?.let { CoreModule.setRevenueCatApiKey(it) }
    }

    private fun areKeysPresentInCoreModule(): Boolean {
        return !CoreModule.getAdjustSdkKey().isNullOrEmpty() &&
                !CoreModule.getOneSignalSdkKey().isNullOrEmpty() &&
                !CoreModule.getRevenueCatApiKey().isNullOrEmpty()
    }

    private fun logInitializationStatuses() {
        listOf("Adjust", "OneSignal", "RevenueCat").forEach { sdkName ->
            val isInitialized = CoreModule.isSdkInitialized(sdkName)
            Log.d(LOG_TAG, "$sdkName SDK initialized: $isInitialized")
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any>) {
        analyticsModule?.trackEvent(eventName, params)
    }

    fun getRemoteConfigValue(key: String, type: Class<*>): Any? {
        return RemoteConfigModule.getConfigValue(key, type)
    }
}