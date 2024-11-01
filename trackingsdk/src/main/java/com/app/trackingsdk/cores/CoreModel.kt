package com.app.trackingsdk.cores

// CoreModule.kt
object CoreModule {
    private val apiKeys = mutableMapOf<String, String>()
    private var userId: String? = null
    private var packageName : String? = null
    private val sdkInitStatus = mutableMapOf<String, Boolean>()
    private var termsLinkUrl :String? = null
    private var privacyPolicyUrl :String? = null
    private var adjustSdKKey :String? = null
    private var oneSignalSdkKey :String? = null
    private var revenueCatApiKey :String? = null


    fun setAdjustSdkKey(key: String) {
        adjustSdKKey = key
    }
    fun getAdjustSdkKey(): String? = adjustSdKKey

    fun setOneSignalSdkKey(key: String) {
        oneSignalSdkKey = key
    }
    fun getOneSignalSdkKey(): String? = oneSignalSdkKey

    fun setRevenueCatApiKey(key: String) {
        revenueCatApiKey = key
    }
    fun getRevenueCatApiKey(): String? = revenueCatApiKey


    fun setSdkInitialized(sdkName: String, isInitialized: Boolean) {
        sdkInitStatus[sdkName] = isInitialized
    }

    fun isSdkInitialized(sdkName: String): Boolean = sdkInitStatus[sdkName] ?: false

    fun storeApiKey(sdkName: String, apiKey: String) {
        apiKeys[sdkName] = apiKey
    }

    fun getApiKey(sdkName: String): String? = apiKeys[sdkName]

    fun setUserId(id: String) {
        userId = id
    }
    fun setPackageName(name: String) {
        packageName = name
    }

    fun getPackageName(): String? = packageName

    fun getUserId(): String? = userId


    fun setTermsLinkUrl(url: String) {
        termsLinkUrl = url
    }
    fun getTermsLinkUrl(): String? = termsLinkUrl
    fun setPrivacyPolicyUrl(url: String) {
        privacyPolicyUrl = url
    }
    fun getPrivacyPolicyUrl(): String? = privacyPolicyUrl
}
