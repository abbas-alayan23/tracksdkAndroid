package com.app.trackingsdk.modules

import android.content.Context
import android.os.Bundle
import com.app.trackingsdk.R
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

// FirebaseModule.kt
object FirebaseModule {
    private var isInitialized = false


    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    fun initialize(context: Context) {
        FirebaseApp.initializeApp(context)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        setupRemoteConfig()

        isInitialized = true
        CoreModule.setSdkInitialized("Firebase", isInitialized)

    }

    private fun setupRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
            )
            fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Fetch API keys from remote config
                        CoreModule.storeApiKey("Adjust", getString("adjust_api_key"))
                        CoreModule.storeApiKey("OneSignal", getString("onesignal_api_key"))
                        CoreModule.storeApiKey("RevenueCat", getString("revenuecat_api_key"))
                    }
                }


        }
    }

    fun logEvent(event: String, params: Bundle) {
        firebaseAnalytics.logEvent(event, params)
    }
}
