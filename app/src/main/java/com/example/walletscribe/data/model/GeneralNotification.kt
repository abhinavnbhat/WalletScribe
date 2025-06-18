package com.example.walletscribe.data.model

/**
 * Represents a general notification (non-transaction).
 * Used for all notifications that don't match the transaction pattern.
 */
data class GeneralNotification(
    override val id: String,
    override val timestamp: Long,
    override val packageName: String,
    override val title: String,
    override val text: String,
    override val rawData: String,
    val appName: String  // Name of the app that sent the notification
) : AppNotification()