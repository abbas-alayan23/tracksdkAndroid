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
    private const val LOG_TAG = "FirebaseModule"

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
                    // Fetch and log each key value from Remote Config
                    val adjustApiKey = getString("adjust_api_key")
                    val oneSignalApiKey = getString("onesignal_api_key")
                    val revenueCatApiKey = getString("revenuecat_api_key")
                    val termsLinkUrl = getString("terms_link_url")
                    val privacyPolicyUrl = getString("privacy_policy_link_url")

                    // Store each key in CoreModule
                    CoreModule.storeApiKey("Adjust", adjustApiKey)
                    CoreModule.storeApiKey("OneSignal", oneSignalApiKey)
                    CoreModule.storeApiKey("RevenueCat", revenueCatApiKey)
                    CoreModule.setTermsLinkUrl(termsLinkUrl)
                    CoreModule.setPrivacyPolicyUrl(privacyPolicyUrl)

                    // Log each key value
                    Log.d(LOG_TAG, "Remote Config - Adjust API Key: $adjustApiKey")
                    Log.d(LOG_TAG, "Remote Config - OneSignal API Key: $oneSignalApiKey")
                    Log.d(LOG_TAG, "Remote Config - RevenueCat API Key: $revenueCatApiKey")
                    Log.d(LOG_TAG, "Remote Config - Terms Link URL: $termsLinkUrl")
                    Log.d(LOG_TAG, "Remote Config - Privacy Policy URL: $privacyPolicyUrl")
                } else {
                    Log.e(LOG_TAG, "Remote Config fetch failed.")
                }
                onConfigFetched() // Notify that Remote Config is fetched
            }
        }
    }

    fun logEvent(event: String, params: Bundle) {
        firebaseAnalytics.logEvent(event, params)
    }
}
