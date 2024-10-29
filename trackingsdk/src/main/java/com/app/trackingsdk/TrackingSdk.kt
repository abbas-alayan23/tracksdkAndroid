package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.app.trackingsdk.modules.AdjustModule
import com.app.trackingsdk.modules.FirebaseModule
import com.app.trackingsdk.modules.OneSignalModule
import com.app.trackingsdk.modules.RevenueCatModule

class TrackingSdk {
    fun initialize(context: Context, userId: String,packageName: String) {
        // Store the user ID in CoreModule
        CoreModule.setUserId(userId)
        CoreModule.setPackageName(packageName)

        // Initialize Firebase and fetch API keys
        FirebaseModule.initialize(context)

        // Call the third-party SDKs initialization with userId
        AdjustModule.initialize(context)
        OneSignalModule.initialize(context)
        RevenueCatModule.initialize(context )

        logInitializationStatuses()

    }

    fun trackAdjustEvent(eventToken: String) {
        AdjustModule.sendEvent(eventToken)
    }
    private fun logInitializationStatuses() {
        val sdkNames = listOf("Firebase", "Adjust", "OneSignal", "RevenueCat")
        sdkNames.forEach { sdk ->
            val isInitialized = CoreModule.isSdkInitialized(sdk)
            Log.d("MySDK", "$sdk initialized: $isInitialized")
        }
    }
}