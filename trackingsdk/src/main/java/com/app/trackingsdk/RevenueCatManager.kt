package com.app.trackingsdk

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback

import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import java.util.Date

class RevenueCatManager :PaywallResultHandler {
    private var paywallActivityLauncher: PaywallActivityLauncher? = null
    private var paywallResultCallback: ((String, CustomerInfo?) -> Unit)? = null


    fun initialize(context: Context, revenueCatKey: String, userId: String) {
        if (revenueCatKey.isBlank() || userId.isBlank()) {
            Log.e("RevenueCatManager", "RevenueCat key or user ID is blank or missing")
            return
        }

        val purchasesConfiguration = PurchasesConfiguration.Builder(context, revenueCatKey)
            .appUserID(userId)
            .build()

        Purchases.configure(purchasesConfiguration)

        Purchases.sharedInstance.setAttributes(
            mapOf("package_name" to (context.packageName.takeIf { it.isNotBlank() } ?: "unknown"))
        )
    }

    fun isUserSubscribed(context: Context, entitlementId: String, onResult: (Boolean) -> Unit) {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val entitlementInfo = customerInfo.entitlements[entitlementId]

                val isSubscribed = entitlementInfo?.isActive == true

                onResult(isSubscribed)

                // Log the subscription status
                if (isSubscribed) {
                    Log.d("Purchase Status", "User is subscribed (Active)")
                } else {
                    Log.d("Purchase Status", "User is not subscribed (Inactive)")
                }
            }

            override fun onError(error: PurchasesError) {
                // Handle the error case and return false
                Log.e("RevenueCatManager", "Error fetching customer info: ${error.message}")
                onResult(false)
            }
        })
    }

    fun presentPayWall(
        caller: ActivityResultCaller,
        onResult: (String, CustomerInfo?) -> Unit
    ) {
        paywallActivityLauncher = PaywallActivityLauncher(caller, this)
        paywallResultCallback = onResult
        paywallActivityLauncher?.launch()
    }


    override fun onActivityResult(result: PaywallResult) {
        when (result) {
            is PaywallResult.Purchased -> {
                val customerInfo = result.customerInfo
                paywallResultCallback?.invoke("successfully purchased", customerInfo)
            }
            is PaywallResult.Cancelled -> {
                paywallResultCallback?.invoke("canceled", null)
            }
            else -> {
                paywallResultCallback?.invoke("error", null)
            }
        }
        paywallResultCallback = null // Clear callback after result is handled
    }
}