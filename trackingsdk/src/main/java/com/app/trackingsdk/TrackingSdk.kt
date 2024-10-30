package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.app.trackingsdk.modules.AdjustModule
import com.app.trackingsdk.modules.FirebaseModule
import com.app.trackingsdk.modules.OneSignalModule
import com.app.trackingsdk.modules.RevenueCatModule
import com.google.firebase.FirebaseOptions

class TrackingSdk {

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
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        FirebaseModule.initialize(context, firebaseOptions) { configMap ->
            if (configMap.isNotEmpty()) {
                Log.d(LOG_TAG, "Config map fetched successfully.")
                initializeSDKs(context, configMap)
            } else {
                Log.e(LOG_TAG, "Config map is empty. Unable to initialize all SDKs.")
            }
            logInitializationStatuses()
            onComplete()
        }
    }

    private fun initializeSDKs(context: Context, configMap: Map<String, String>) {
        val adjustApiKey = configMap["adjust_api_key"]
        val oneSignalApiKey = configMap["onesignal_api_key"]
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

    fun getPrivacyPolicyLink(): String {
        return CoreModule.getPrivacyPolicyUrl() ?: ""
    }

    fun getTermsLink(): String {
        return CoreModule.getTermsLinkUrl() ?: ""
    }
}
