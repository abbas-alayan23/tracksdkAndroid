package com.app.trackingsdk.modules

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings



object RemoteConfigModule {
    private const val TAG = "RemoteConfigModule"
    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun initialize(onComplete: (Map<String, String>) -> Unit) {
        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf(
            "adjust_sdk_key" to "default_adjust_key",
            "onesignal_sdk_key" to "default_onesignal_key",
            "revenuecat_api_key" to "default_revenuecat_key"
        ))

        fetchAndActivateConfig(onComplete)
    }

    private fun fetchAndActivateConfig(onComplete: (Map<String, String>) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
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
        return mapOf(
            "adjust_sdk_key" to remoteConfig.getString("adjust_sdk_key"),
            "onesignal_sdk_key" to remoteConfig.getString("onesignal_sdk_key"),
            "revenuecat_api_key" to remoteConfig.getString("revenuecat_api_key")
        )
    }

    // New method to retrieve a specific parameter by key and type
    fun getConfigValue(key: String, type: Class<*>): Any? {
        return when (type) {
            Boolean::class.java -> remoteConfig.getBoolean(key)
            String::class.java -> remoteConfig.getString(key)
            Int::class.java -> remoteConfig.getLong(key).toInt()
            Long::class.java -> remoteConfig.getLong(key)
            Double::class.java -> remoteConfig.getDouble(key)
            else -> null
        }
    }
}
