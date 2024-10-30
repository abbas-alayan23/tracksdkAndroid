package com.app.trackingsdk.modules

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseModule {
    private const val LOG_TAG = "FirebaseModule"
    private var isInitialized = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    // Initialize Firebase with service account and setup Remote Config
    fun initialize(context: Context, firebaseOptions: FirebaseOptions, onConfigFetched: (Map<String, String>) -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        setupRemoteConfig { fetchedConfig ->
            isInitialized = true
            CoreModule.setSdkInitialized("Firebase", isInitialized)
            onConfigFetched(fetchedConfig)  // Pass fetched config to proceed with other SDKs
        }
    }

    // Fetch remote config and return values in a callback
    private fun setupRemoteConfig(onConfigFetched: (Map<String, String>) -> Unit) {
        firebaseRemoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)  // Set to 0 for debugging
                .build()
        )

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(LOG_TAG, "Remote Config fetch and activate succeeded")
                val configMap = mapOf(
                    "adjust_api_key" to firebaseRemoteConfig.getString("adjust_api_key"),
                    "onesignal_api_key" to firebaseRemoteConfig.getString("onesignal_api_key"),
                    "revenuecat_api_key" to firebaseRemoteConfig.getString("revenuecat_api_key"),
                    "terms_link_url" to firebaseRemoteConfig.getString("terms_link_url"),
                    "privacy_policy_link_url" to firebaseRemoteConfig.getString("privacy_policy_link_url")
                )
                onConfigFetched(configMap)  // Return fetched config values
            } else {
                Log.e(LOG_TAG, "Remote Config fetch failed.")
                onConfigFetched(emptyMap())  // Return empty config on failure
            }
        }
    }

    fun logEvent(event: String, params: Bundle) {
        if (isInitialized) {
            firebaseAnalytics.logEvent(event, params)
        }
    }
}
