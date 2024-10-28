package com.app.trackingsdk

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.app.trackingsdk.IapObject
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdjustManager {

    fun initialize(context: Context, adjustKey: String, isDebug: Boolean, userId: String) {
        val config = AdjustConfig(
            context,
            adjustKey,
            if (isDebug) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        )
        Adjust.addGlobalCallbackParameter("user_uuid", userId)
        Adjust.initSdk(config)

        CoroutineScope(Dispatchers.IO).launch {
            adjustFirebaseIdEvent("yourAdjustEventToken", userId)
        }
    }

    fun getParamsToAdjust(
        adjustSubscriptionEventToken: String,
        iapObject: IapObject?,
        context: Context
    ): AdjustEvent {
        val adjustEvent = AdjustEvent(adjustSubscriptionEventToken)

        // Add parameters from IapObject and context to the Adjust event
        adjustEvent.addCallbackParameter("user_uuid", iapObject?.originalTransactionId ?: "")
        adjustEvent.addCallbackParameter("eventValue", "OK")
        adjustEvent.addCallbackParameter("inAppProductId", iapObject?.value)
        adjustEvent.addCallbackParameter("inAppPackageName", context.packageName)
        adjustEvent.addCallbackParameter("inAppPurchaseToken", iapObject?.purchaseToken ?: "")
        adjustEvent.addCallbackParameter("inAppOrderId", iapObject?.orderId ?: "")
        adjustEvent.addCallbackParameter("transactionId", iapObject?.transactionId ?: "")

        return adjustEvent
    }



    private suspend fun adjustFirebaseIdEvent(adjustEventToken: String, userId: String) {
        val instanceId = withContext(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token
        } ?: "Unknown installation id"

        val adjustEvent = AdjustEvent(adjustEventToken)
        adjustEvent.addCallbackParameter("eventValue", instanceId.toString())
        adjustEvent.addCallbackParameter("user_uuid", userId)

        Adjust.trackEvent(adjustEvent)
    }
}

