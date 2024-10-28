package com.app.trackingsdk

data class IapObject(
    val value: String,                     // Product ID or SKU
    val transactionId: String? = null,     // Unique transaction ID if available
    val originalTransactionId: String? = null,  // Use originalAppUserId as an identifier
    val purchaseToken: String? = null,     // Purchase token if available
    val orderId: String? = null  ,
    val userId : String // Order ID if available
)
