package com.app.trackingsdk.modules

import android.content.Context
import com.app.trackingsdk.cores.CoreModule
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

// RevenueCatModule.kt
object RevenueCatModule {
    private var isInitialized = false


    fun initialize(context: Context, revenueCatApiKey: String) {
        Purchases.configure(PurchasesConfiguration.Builder(context, revenueCatApiKey).build())
        CoreModule.getPackageName()?.let { packageName ->
            Purchases.sharedInstance.setAttributes(
                mapOf("package_name" to packageName)
            )

        }
        isInitialized = true
        CoreModule.setSdkInitialized("RevenueCat", isInitialized)
    }
}
