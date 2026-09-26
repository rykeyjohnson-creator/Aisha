package com.example.jarvisalexa.aisha.actions

class AishaActionEngine {

    fun classify(command: String): String {
        val q = command.trim().lowercase()

        return when {
            q.contains("open youtube") || q.contains("youtube kholo") -> "OPEN_YOUTUBE"
            q.contains("open whatsapp") || q.contains("whatsapp kholo") -> "OPEN_WHATSAPP"
            q.contains("open chrome") || q.contains("chrome kholo") -> "OPEN_CHROME"
            q.contains("open termux") || q.contains("termux kholo") -> "OPEN_TERMUX"
            q.contains("weather") || q.contains("mausam") -> "WEATHER"
            q == "time" || q.contains("what time") || q.contains("kitne baje") -> "TIME"
            q.contains("date") || q.contains("tarikh") -> "DATE"
            q.contains("1 se 100") || q.contains("count 1 to 100") -> "COUNT"
            q.startsWith("search ") -> "SEARCH"
            else -> "CHAT"
        }
    }
}
