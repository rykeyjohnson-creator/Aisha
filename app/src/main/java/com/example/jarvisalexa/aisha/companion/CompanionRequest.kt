package com.example.jarvisalexa.aisha.companion

data class CompanionRequest(
    val id: String,
    val action: String,
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

data class CompanionResponse(
    val id: String,
    val ok: Boolean,
    val result: String = "",
    val error: String = ""
)
