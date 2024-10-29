package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.app.trackingsdk.modules.AdjustModule
import com.app.trackingsdk.modules.FirebaseModule
import com.app.trackingsdk.modules.OneSignalModule
import com.app.trackingsdk.modules.RevenueCatModule
import com.google.firebase.FirebaseOptions

// TrackingSdk.kt
class TrackingSdk {
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

        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        FirebaseModule.initialize(context, firebaseOptions) {
            // Initialize other SDKs after Firebase and Remote Config are ready
            initializeAdjust(context) {
                initializeOneSignal(context) {
                    initializeRevenueCat(context) {
                        logInitializationStatuses()
                        onInitializationComplete()
                    }
                }
            }
        }
    }

    private fun initializeFirebase(context: Context, options: FirebaseOptions, onNext: () -> Unit) {
        FirebaseModule.initialize(context, options) {
            // Mark Firebase as initialized in CoreModule after Remote Config is fetched
            CoreModule.setSdkInitialized("Firebase", true)
            onNext() // Proceed to the next SDK after Firebase setup and Remote Config fetch
        }
    }


    private fun initializeAdjust(context: Context, onNext: () -> Unit) {
        AdjustModule.initialize(context)
        CoreModule.setSdkInitialized("Adjust", true)
        onNext() // Proceed to the next SDK after Adjust setup
    }

    private fun initializeOneSignal(context: Context, onNext: () -> Unit) {
        OneSignalModule.initialize(context)
        CoreModule.setSdkInitialized("OneSignal", true)
        onNext() // Proceed to the next SDK after OneSignal setup
    }

    private fun initializeRevenueCat(context: Context, onNext: () -> Unit) {
        val revenueCatApiKey = CoreModule.getApiKey("RevenueCat")
        if (revenueCatApiKey.isNullOrEmpty()) {
            Log.e("TrackingSdk", "RevenueCat API key not available. Delaying initialization.")
            return
        }
        RevenueCatModule.initialize(context, revenueCatApiKey)
        CoreModule.setSdkInitialized("RevenueCat", true)
        onNext()
    }

    private fun logInitializationStatuses() {
        val sdkNames = listOf("Firebase", "Adjust", "OneSignal", "RevenueCat")
        sdkNames.forEach { sdk ->
            val isInitialized = CoreModule.isSdkInitialized(sdk)
            Log.d("TrackingSdk", "$sdk initialized: $isInitialized")
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
