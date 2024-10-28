package com.app.trackingsdk

import android.content.Context
import com.android.billingclient.api.*

class BillingManager : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private var onPurchaseSuccess: ((String, String) -> Unit)? = null // Callback for orderId, purchaseToken

    fun initialize(context: Context, onPurchaseSuccessCallback: (String, String) -> Unit) {
        onPurchaseSuccess = onPurchaseSuccessCallback

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Optionally, query for existing purchases
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle disconnection if needed
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                val orderId = purchase.orderId          // Capture order ID
                val purchaseToken = purchase.purchaseToken  // Capture purchase token
                if (orderId != null) {
                    onPurchaseSuccess?.invoke(orderId, purchaseToken)
                } // Pass to callback
            }
        }
    }
}
