package com.app.trackingsdk.cores

// CoreModule.kt
object CoreModule {
    private val apiKeys = mutableMapOf<String, String>()
    private var userId: String? = null
    private var packageName : String? = null
    private val sdkInitStatus = mutableMapOf<String, Boolean>()

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
}
