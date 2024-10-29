package com.app.trackingsdk.modules

import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// AdjustModule.kt
object AdjustModule {
    private var isInitialized = false


    fun initialize(context: Context) {
        val adjustApiKey = CoreModule.getApiKey("Adjust") ?: return
        val config = AdjustConfig(context, adjustApiKey, AdjustConfig.ENVIRONMENT_PRODUCTION)
        CoreModule.getUserId()?.let { userId ->
            Adjust.addGlobalCallbackParameter("user_uuid", userId)
        }
        Adjust.initSdk(config)
        isInitialized = true
        CoreModule.setSdkInitialized("Adjust", isInitialized)
    }

    fun sendEvent(eventToken: String) {
        if (isInitialized) {
            val adjustEvent = AdjustEvent(eventToken)
            Adjust.trackEvent(adjustEvent)
        } else {
            Log.e("AdjustModule", "Adjust SDK not initialized. Event not sent.")
        }
    }

    private suspend fun adjustFirebaseIdEvent(adjustEventToken: String, userId: String) {
        val instanceId = withContext(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token
        }

        val adjustEvent = AdjustEvent(adjustEventToken)
        adjustEvent.addCallbackParameter("eventValue", instanceId.toString())
        adjustEvent.addCallbackParameter("user_uuid", userId)

        Adjust.trackEvent(adjustEvent)
    }

}
