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
        onInitializationComplete: () -> Unit // Callback for when all SDKs are initialized
    ) {
        // Store the user ID in CoreModule
        CoreModule.setUserId(userId)
        CoreModule.setPackageName(packageName)

        // Create FirebaseOptions with provided parameters
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(firebaseApiKey)
            .setApplicationId(firebaseAppId)
            .setProjectId(firebaseProjectId)
            .setDatabaseUrl(firebaseDatabaseUrl)
            .setStorageBucket(firebaseStorageBucket)
            .setGcmSenderId(firebaseMessagingSenderId)
            .build()

        // Initialize SDKs sequentially
        initializeFirebase(context, firebaseOptions) {
            initializeAdjust(context) {
                initializeOneSignal(context) {
                    initializeRevenueCat(context) {
                        // All SDKs initialized, log statuses and execute the final callback
                        logInitializationStatuses()
                        onInitializationComplete()
                    }
                }
            }
        }
    }

    private fun initializeFirebase(context: Context, options: FirebaseOptions, onNext: () -> Unit) {
        FirebaseModule.initialize(context, options)
        CoreModule.setSdkInitialized("Firebase", true)
        onNext() // Proceed to the next SDK after Firebase setup
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
        RevenueCatModule.initialize(context)
        CoreModule.setSdkInitialized("RevenueCat", true)
        onNext() // All SDKs are now initialized
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
