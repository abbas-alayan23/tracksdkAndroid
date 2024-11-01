package com.app.trackingsdk.modules


import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsModule(context: Context) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun trackEvent(eventName: String, eventParams: Map<String, Any>) {
        val bundle = Bundle().apply {
            for ((key, value) in eventParams) {
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> throw IllegalArgumentException("Unsupported value type for key: $key")
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
