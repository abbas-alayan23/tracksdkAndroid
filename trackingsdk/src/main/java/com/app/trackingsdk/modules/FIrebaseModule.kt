package com.app.trackingsdk.modules

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object FirebaseModule {
    private const val TAG = "FirebaseModule"
    private lateinit var remoteConfig: FirebaseRemoteConfig

    // Initialize Firebase with optional custom options and configure Remote Config
    fun initialize(context: Context, firebaseOptions: FirebaseOptions? = null, onComplete: (Map<String, String>) -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            firebaseOptions?.let {
                FirebaseApp.initializeApp(context, it)
            } ?: FirebaseApp.initializeApp(context)
            Log.d(TAG, "Firebase initialized successfully.")
        }

        // Configure Remote Config settings with a low fetch interval for testing
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // Set to 0 for development
        }
        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default values for Remote Config
        remoteConfig.setDefaultsAsync(mapOf(
            "adjust_api_key" to "default_adjust_key",
            "onesignal_api_key" to "default_onesignal_key",
            "revenuecat_api_key" to "default_revenuecat_key"
        ))

        // Fetch and activate Remote Config values
        fetchAndActivateConfig(onComplete)
    }

    private fun fetchAndActivateConfig(onComplete: (Map<String, String>) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Remote Config fetch and activate succeeded")
                    onComplete(retrieveRemoteConfigValues())
                } else {
                    Log.e(TAG, "Fetch failed: ${task.exception?.message}")
                    onComplete(emptyMap())
                }
            }
    }

    private fun retrieveRemoteConfigValues(): Map<String, String> {
        val adjustApiKey = remoteConfig.getString("adjust_api_key")
        val oneSignalApiKey = remoteConfig.getString("onesignal_api_key")
        val revenueCatApiKey = remoteConfig.getString("revenuecat_api_key")

        Log.d(TAG, "Adjust API Key: $adjustApiKey")
        Log.d(TAG, "OneSignal API Key: $oneSignalApiKey")
        Log.d(TAG, "RevenueCat API Key: $revenueCatApiKey")

        return mapOf(
            "adjust_api_key" to adjustApiKey,
            "onesignal_api_key" to oneSignalApiKey,
            "revenuecat_api_key" to revenueCatApiKey
        )
    }
}
