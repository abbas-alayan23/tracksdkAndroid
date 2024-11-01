package com.app.trackingsdk.modules

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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