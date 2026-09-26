package com.example.jarvisalexa.aisha.companion

interface CompanionTransport {

    suspend fun send(
        request: CompanionRequest,
        config: CompanionConfig
    ): CompanionResponse

    suspend fun close()
}
