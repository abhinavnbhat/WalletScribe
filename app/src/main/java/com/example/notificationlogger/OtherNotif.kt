package com.example.notificationlogger

data class OtherNotif(
    val raw: String,   // full notification text
    val hash: String   // SHA-256 of raw text
)
