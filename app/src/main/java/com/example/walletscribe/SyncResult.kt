package com.example.walletscribe

data class SyncResult(
    val captured: Int,
    val written: Int,
    val failed: Int,
    val logLines: List<String>   // “✅ …”, “♻️ …”, “⚠️ …”
)
