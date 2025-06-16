package com.example.notificationlogger

data class WalletTxn(
    val amount: String,     // e.g. "$12.34"
    val card: String,       // e.g. "**** 1234"
    val date: String,       // yyyy-MM-dd from notification timestamp
    val time: String,       // HH:mm:ss from notification timestamp
    val where: String,      // merchant name after "at "
    val raw: String,        // full notification text
    val hash: String        // SHA-256 of raw text (lower-cased)
)
