package com.app.trackingsdk.modules

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.app.trackingsdk.cores.CoreModule
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
object FirebaseModule {
    private const val TAG = "FirebaseModule"

    fun initialize(context: Context, firebaseOptions: FirebaseOptions) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
            Log.d(TAG, "Firebase initialized successfully.")
        } else {
            Log.d(TAG, "Firebase already initialized.")
        }
    }
}