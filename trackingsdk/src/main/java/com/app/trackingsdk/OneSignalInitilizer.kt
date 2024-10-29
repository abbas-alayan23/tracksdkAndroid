package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object OneSignalInitializer {
    private var isInitialized = false

    fun initialize(context: Context, oneSignalKey: String) {
        if (isInitialized || oneSignalKey.isBlank()) return

        OneSignal.initWithContext(context, oneSignalKey)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
        isInitialized = true
    }
}