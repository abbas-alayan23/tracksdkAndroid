package com.app.trackingsdk.modules

import android.content.Context
import android.util.Log
import com.app.trackingsdk.cores.CoreModule

object SdkInitializerModule {
    private const val TAG = "SdkInitializerModule"

    fun initializeSDKs(context: Context) {
        CoreModule.getAdjustSdkKey()?.let {
            AdjustModule.initialize(context)
            Log.d(TAG, "Adjust SDK initialized.")
            CoreModule.setSdkInitialized("Adjust", true)
        } ?: Log.e(TAG, "Adjust SDK Key missing, initialization skipped.")

        CoreModule.getOneSignalSdkKey()?.let {
            OneSignalModule.initialize(context)
            Log.d(TAG, "OneSignal SDK initialized.")
            CoreModule.setSdkInitialized("OneSignal", true)
        } ?: Log.e(TAG, "OneSignal SDK Key missing, initialization skipped.")

        CoreModule.getRevenueCatApiKey()?.let {
            RevenueCatModule.initialize(context, it)
            Log.d(TAG, "RevenueCat SDK initialized.")
            CoreModule.setSdkInitialized("RevenueCat", true)
        } ?: Log.e(TAG, "RevenueCat SDK Key missing, initialization skipped.")
    }
}