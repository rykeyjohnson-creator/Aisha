package com.example.jarvisalexa.aisha.companion

import android.content.Context
import java.util.UUID

class CompanionManager(context: Context) {

    private val store = CompanionConfigStore(context)

    fun config(): CompanionConfig = store.load()

    fun isPaired(): Boolean = store.isPaired()

    fun isEnabled(): Boolean = store.isEnabled()

    fun pair(
        phoneId: String,
        secret: String,
        broker: String = "broker.hivemq.com",
        port: Int = 1883
    ) {
        store.save(
            CompanionConfig(
                phoneId = phoneId.trim(),
                broker = broker.trim(),
                port = port,
                secret = secret.trim(),
                timeoutSeconds = 20,
                useNative = true
            )
        )
    }

    fun unpair() {
        store.clear()
    }

    fun createRequest(
        action: String,
        params: Map<String, String> = emptyMap()
    ): CompanionRequest {
        require(action.isNotBlank()) { "Action cannot be empty" }

        return CompanionRequest(
            id = UUID.randomUUID().toString().replace("-", "").take(12),
            action = action.trim(),
            params = params
        )
    }
}
