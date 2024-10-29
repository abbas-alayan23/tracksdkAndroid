package com.app.trackingsdk.modules

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.app.trackingsdk.R
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

// FirebaseModule.kt
// FirebaseModule.kt
// FirebaseModule.kt
// FirebaseModule.kt
object FirebaseModule {
    private var isInitialized = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun initialize(
        context: Context,
        firebaseOptions: FirebaseOptions
    ) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
        } else {
            FirebaseApp.initializeApp(context)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        // Immediately set up and fetch Remote Config
        setupRemoteConfig()

        isInitialized = true
        CoreModule.setSdkInitialized("Firebase", isInitialized)
    }

    private fun setupRemoteConfig() {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600) // or other interval
                    .build()
            )
            fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Fetch API keys from remote config
                        CoreModule.storeApiKey("Adjust", getString("adjust_api_key"))
                        CoreModule.storeApiKey("OneSignal", getString("onesignal_api_key"))
                        CoreModule.storeApiKey("RevenueCat", getString("revenuecat_api_key"))
                        CoreModule.setTermsLinkUrl(getString("terms_link_url"))
                        CoreModule.setPrivacyPolicyUrl(getString("privacy_policy_link_url"))
                    } else {
                        // Log or handle the failure if fetch failed
                        Log.e("FirebaseModule", "Remote Config fetch failed")
                    }
                }
        }
    }

    fun logEvent(event: String, params: Bundle) {
        firebaseAnalytics.logEvent(event, params)
    }
}
