package com.app.trackingsdk

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneSignalInitializer {

    fun initialize(context: Context, oneSignalKey: String) {
        OneSignal.Debug.logLevel = LogLevel.DEBUG
        if (oneSignalKey.isBlank()) {
            Log.e("OneSignalInitializer", "OneSignal key is blank or missing")
            return
        }
        OneSignal.initWithContext(context, oneSignalKey)


        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }

}