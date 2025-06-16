package com.example.notificationlogger

data class SyncResult(
    val captured: Int,
    val written: Int,
    val failed: Int,
    val logLines: List<String>   // “✅ …”, “♻️ …”, “⚠️ …”
)
