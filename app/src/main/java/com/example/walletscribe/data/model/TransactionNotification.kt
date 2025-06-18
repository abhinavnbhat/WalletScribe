package com.example.walletscribe.data.model

/**
 * Represents a transaction notification (e.g., from Google Wallet or banking apps).
 * Extends the base AppNotification class with transaction-specific fields.
 */
data class TransactionNotification(
    override val id: String,
    override val timestamp: Long,
    override val packageName: String,
    override val title: String,
    override val text: String,
    override val rawData: String,
    val amount: Double,          // Transaction amount (e.g., 29.48)
    val card: String,            // Card info (e.g., "Amex •••• 1005")
    val merchant: String,        // Merchant name (e.g., "INDIA METRO HYPERMARKET")
    val transactionDate: String, // Date of transaction (e.g., "2025-06-15")
    val transactionTime: String  // Time of transaction (e.g., "14:30:45")
) : AppNotification() {
    // Format amount as currency string (e.g., "$29.48")
    val formattedAmount: String
        get() = "$${"%.2f".format(amount)}"
}