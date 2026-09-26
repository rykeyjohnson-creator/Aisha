package com.example.jarvisalexa.aisha.companion

data class CompanionConfig(
    val phoneId: String = "",
    val broker: String = "broker.hivemq.com",
    val port: Int = 1883,
    val secret: String = "",
    val timeoutSeconds: Int = 20,
    val useNative: Boolean = false
) {
    fun isPaired(): Boolean =
        phoneId.isNotBlank() && secret.isNotBlank()

    fun isEnabled(): Boolean =
        useNative && isPaired()
}
