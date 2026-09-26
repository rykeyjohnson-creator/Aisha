package com.example.jarvisalexa.aisha.companion

import android.content.Context

class CompanionConfigStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "aisha_companion",
        Context.MODE_PRIVATE
    )

    fun load(): CompanionConfig {
        return CompanionConfig(
            phoneId = prefs.getString("phone_id", "") ?: "",
            broker = prefs.getString("broker", "broker.hivemq.com")
                ?: "broker.hivemq.com",
            port = prefs.getInt("port", 1883),
            secret = prefs.getString("secret", "") ?: "",
            timeoutSeconds = prefs.getInt("timeout", 20),
            useNative = prefs.getBoolean("use_native", false)
        )
    }

    fun save(config: CompanionConfig) {
        prefs.edit()
            .putString("phone_id", config.phoneId)
            .putString("broker", config.broker)
            .putInt("port", config.port)
            .putString("secret", config.secret)
            .putInt("timeout", config.timeoutSeconds)
            .putBoolean("use_native", config.useNative)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isPaired(): Boolean = load().isPaired()

    fun isEnabled(): Boolean = load().isEnabled()
}
