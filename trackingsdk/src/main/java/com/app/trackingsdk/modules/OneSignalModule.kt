package com.app.trackingsdk.modules

import android.content.Context
import com.app.trackingsdk.cores.CoreModule
import com.onesignal.OneSignal

// OneSignalModule.kt
object OneSignalModule {
    private var isInitialized = false

    fun initialize(context: Context) {
        val oneSignalApiKey = CoreModule.getApiKey("OneSignal") ?: return
        OneSignal.initWithContext(context)
        OneSignal.initWithContext(context,oneSignalApiKey)
        isInitialized = true
        CoreModule.setSdkInitialized("OneSignal", isInitialized)

    }
}
