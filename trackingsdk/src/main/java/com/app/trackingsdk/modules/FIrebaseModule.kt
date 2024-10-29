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

    fun initialize(context: Context, firebaseOptions: FirebaseOptions, onConfigFetched: () -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        // Setup and fetch Remote Config
        setupRemoteConfig(onConfigFetched)

        isInitialized = true
        CoreModule.setSdkInitialized("Firebase", isInitialized)
    }

    private fun setupRemoteConfig(onConfigFetched: () -> Unit) {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
            )
            fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoreModule.storeApiKey("Adjust", getString("adjust_api_key"))
                    CoreModule.storeApiKey("OneSignal", getString("onesignal_api_key"))
                    CoreModule.storeApiKey("RevenueCat", getString("revenuecat_api_key"))
                    CoreModule.setTermsLinkUrl(getString("terms_link_url"))
                    CoreModule.setPrivacyPolicyUrl(getString("privacy_policy_link_url"))
                }
                onConfigFetched() // Notify that Remote Config is fetched
            }
        }
    }

    fun logEvent(event: String, params: Bundle) {
        firebaseAnalytics.logEvent(event, params)
    }
}
