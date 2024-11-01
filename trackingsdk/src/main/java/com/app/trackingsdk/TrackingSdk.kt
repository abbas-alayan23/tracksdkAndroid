package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.app.trackingsdk.modules.AdjustModule
import com.app.trackingsdk.modules.FirebaseModule
import com.app.trackingsdk.modules.OneSignalModule
import com.app.trackingsdk.modules.RevenueCatModule
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics

class TrackingSdk {

    companion object {
        private const val LOG_TAG = "TrackingSdk"
        private var firebaseAnalytics: FirebaseAnalytics? = null

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
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        FirebaseModule.initialize(context, firebaseOptions) { configMap ->
            firebaseAnalytics = FirebaseAnalytics.getInstance(context) // Initialize Firebase Analytics
            onComplete()

            populateCoreModule(configMap)

            if (configMap.isNotEmpty() && areKeysPresentInCoreModule()) {
                Log.d(LOG_TAG, "Config map and CoreModule populated successfully.")
                initializeSDKs(context, configMap)
            } else {
                Log.e(LOG_TAG, "Config map is empty or CoreModule is missing keys.")
            }

            // Log initialization statuses
            logInitializationStatuses()
            onComplete()
        }
    }

    private fun populateCoreModule(configMap: Map<String, String>) {
        configMap["adjust_sdk_key"]?.let {
            CoreModule.setAdjustSdkKey(it)
            Log.d(LOG_TAG, "Adjust SDK Key set in CoreModule: $it")
        }
        configMap["onesignal_api_key"]?.let {
            CoreModule.setOneSignalSdkKey(it)
            Log.d(LOG_TAG, "OneSignal API Key set in CoreModule: $it")
        }
        configMap["revenuecat_api_key"]?.let {
            CoreModule.setRevenueCatApiKey(it)
            Log.d(LOG_TAG, "RevenueCat API Key set in CoreModule: $it")
        }
    }


    // Check if CoreModule has the necessary keys
    private fun areKeysPresentInCoreModule(): Boolean {
        val isAdjustKeyPresent = !CoreModule.getAdjustSdkKey().isNullOrEmpty()
        val isOneSignalKeyPresent = !CoreModule.getOneSignalSdkKey().isNullOrEmpty()
        val isRevenueCatKeyPresent = !CoreModule.getRevenueCatApiKey().isNullOrEmpty()

        Log.d(LOG_TAG, "CoreModule Adjust SDK Key: ${CoreModule.getAdjustSdkKey()}")
        Log.d(LOG_TAG, "CoreModule OneSignal API Key: ${CoreModule.getOneSignalSdkKey()}")
        Log.d(LOG_TAG, "CoreModule RevenueCat API Key: ${CoreModule.getRevenueCatApiKey()}")

        return isAdjustKeyPresent && isOneSignalKeyPresent && isRevenueCatKeyPresent
    }


    private fun initializeSDKs(context: Context, configMap: Map<String, String>) {
        val adjustApiKey = configMap["adjust_sdk_key"]
        val oneSignalApiKey = configMap["onesignal_sdk_key"]
        val revenueCatApiKey = configMap["revenuecat_api_key"]

        if (!adjustApiKey.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Initializing Adjust with API Key.")
            AdjustModule.initialize(context)
        } else {
            Log.e(LOG_TAG, "Adjust API key is missing or empty.")
        }

        if (!oneSignalApiKey.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Initializing OneSignal with API Key.")
            OneSignalModule.initialize(context)
        } else {
            Log.e(LOG_TAG, "OneSignal API key is missing or empty.")
        }

        if (!revenueCatApiKey.isNullOrEmpty()) {
            Log.d(LOG_TAG, "Initializing RevenueCat with API Key.")
            RevenueCatModule.initialize(context, revenueCatApiKey)
        } else {
            Log.e(LOG_TAG, "RevenueCat API key is missing or empty. Skipping RevenueCat initialization.")
        }
    }

    private fun logInitializationStatuses() {
        val sdkNames = listOf("Firebase", "Adjust", "OneSignal", "RevenueCat")
        sdkNames.forEach { sdk ->
            val isInitialized = CoreModule.isSdkInitialized(sdk)
            Log.d(LOG_TAG, "$sdk initialized: $isInitialized")
        }
    }

    fun trackAdjustEvent(eventToken: String) {
        AdjustModule.sendEvent(eventToken)
    }


    fun logTestEvent(context: Context) {
        firebaseAnalytics?.logEvent("test_event", null)
    }
}