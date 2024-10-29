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
        onInitializationComplete: () -> Unit
    ) {
        CoreModule.setUserId(userId)
        CoreModule.setPackageName(packageName)

        Log.d(LOG_TAG, "Starting SDK Initialization...")

        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        initializeFirebase(context, firebaseOptions) {
            Log.d(LOG_TAG, "Firebase initialized successfully.")

            initializeAdjust(context) {
                Log.d(LOG_TAG, "Adjust initialized successfully.")

                initializeOneSignal(context) {
                    Log.d(LOG_TAG, "OneSignal initialized successfully.")

                    initializeRevenueCat(context) {
                        Log.d(LOG_TAG, "RevenueCat initialized successfully.")

                        logInitializationStatuses()
                        onInitializationComplete()
                    }
                }
            }
        }
    }

    private fun initializeFirebase(context: Context, options: FirebaseOptions, onNext: () -> Unit) {
        FirebaseModule.initialize(context, options) {
            CoreModule.setSdkInitialized("Firebase", true)
            Log.d(LOG_TAG, "Firebase setup and Remote Config fetched.")
            onNext()
        }
    }

    private fun initializeAdjust(context: Context, onNext: () -> Unit) {
        AdjustModule.initialize(context)
        CoreModule.setSdkInitialized("Adjust", true)
        Log.d(LOG_TAG, "Adjust setup complete.")
        onNext()
    }

    private fun initializeOneSignal(context: Context, onNext: () -> Unit) {
        OneSignalModule.initialize(context)
        CoreModule.setSdkInitialized("OneSignal", true)
        Log.d(LOG_TAG, "OneSignal setup complete.")
        onNext()
    }

    private fun initializeRevenueCat(context: Context, onNext: () -> Unit) {
        val revenueCatApiKey = CoreModule.getApiKey("RevenueCat")
        if (revenueCatApiKey.isNullOrEmpty()) {
            Log.e(LOG_TAG, "RevenueCat API key not available. Delaying initialization.")
            return
        }
        RevenueCatModule.initialize(context, revenueCatApiKey)
        CoreModule.setSdkInitialized("RevenueCat", true)
        Log.d(LOG_TAG, "RevenueCat setup complete.")
        onNext()
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
