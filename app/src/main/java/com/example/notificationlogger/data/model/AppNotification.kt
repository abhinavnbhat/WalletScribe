package com.example.notificationlogger.data.model

import java.util.Date

/**
 * Base sealed class for all types of notifications.
 * Sealed classes are used to represent restricted class hierarchies.
 */
sealed class AppNotification {
    abstract val id: String
    abstract val timestamp: Long
    abstract val packageName: String
    abstract val title: String
    abstract val text: String
    abstract val rawData: String

    // Format timestamp as date string (e.g., "2025-06-15")
    val formattedDate: String
        get() = android.text.format.DateFormat
            .format("yyyy-MM-dd", Date(timestamp)).toString()

    // Format timestamp as time string (e.g., "14:30:45")
    val formattedTime: String
        get() = android.text.format.DateFormat
            .format("HH:mm:ss", Date(timestamp)).toString()
}