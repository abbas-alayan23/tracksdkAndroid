// TrackingSdk.kt
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
        onComplete: () -> Unit // Callback for successful SDK initialization
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
            val adjustApiKey = configMap["adjust_api_key"]
            val oneSignalApiKey = configMap["onesignal_api_key"]
            val revenueCatApiKey = configMap["revenuecat_api_key"]

            // Initialize other SDKs that rely on these keys
            if (adjustApiKey != null) {
                AdjustModule.initialize(context)
            }
            if (oneSignalApiKey != null) {
                OneSignalModule.initialize(context)
            }
            if (revenueCatApiKey != null) {
                RevenueCatModule.initialize(context, revenueCatApiKey)
            }

            // Log initialization statuses and invoke the onComplete callback
            logInitializationStatuses()
            onComplete()
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
