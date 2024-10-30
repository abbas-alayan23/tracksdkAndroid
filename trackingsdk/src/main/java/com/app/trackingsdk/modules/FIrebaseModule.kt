package com.app.trackingsdk.modules

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseModule {
    private const val LOG_TAG = "FirebaseModule"
    private var isInitialized = false

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    fun initialize(context: Context, firebaseOptions: FirebaseOptions, onConfigFetched: (Map<String, String>) -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
            Log.d(LOG_TAG, "Firebase initialized with custom options.")
        }

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        setupRemoteConfig { fetchedConfig ->
            isInitialized = true
            CoreModule.setSdkInitialized("Firebase", isInitialized)
            onConfigFetched(fetchedConfig)
        }
    }

    private fun setupRemoteConfig(onConfigFetched: (Map<String, String>) -> Unit) {
        firebaseRemoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)  // Set to 0 for debugging
                .build()
        )

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(LOG_TAG, "Remote Config fetch and activate succeeded")

                // Retrieve each value and log it for debugging
                val adjustApiKey = firebaseRemoteConfig.getString("adjust_api_key")
                val oneSignalApiKey = firebaseRemoteConfig.getString("onesignal_api_key")
                val revenueCatApiKey = firebaseRemoteConfig.getString("revenuecat_api_key")

                if (adjustApiKey.isEmpty() || oneSignalApiKey.isEmpty() || revenueCatApiKey.isEmpty()) {
                    Log.e(LOG_TAG, "Some Remote Config values are missing or empty!")
                } else {
                    Log.d(LOG_TAG, "Remote Config - Adjust API Key: $adjustApiKey")
                    Log.d(LOG_TAG, "Remote Config - OneSignal API Key: $oneSignalApiKey")
                    Log.d(LOG_TAG, "Remote Config - RevenueCat API Key: $revenueCatApiKey")
                }

                // Pass the fetched values in a map
                onConfigFetched(
                    mapOf(
                        "adjust_api_key" to adjustApiKey,
                        "onesignal_api_key" to oneSignalApiKey,
                        "revenuecat_api_key" to revenueCatApiKey
                    )
                )
            } else {
                Log.e(LOG_TAG, "Remote Config fetch failed: ${task.exception?.message}")
                onConfigFetched(emptyMap())  // Return empty config on failure
            }
        }
    }
}
