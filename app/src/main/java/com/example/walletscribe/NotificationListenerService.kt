package com.example.walletscribe

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.walletscribe.data.model.TransactionNotification
import com.example.walletscribe.data.model.GeneralNotification
import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets

class NotificationListenerService : NotificationListenerService() {
    private val TAG = "NotificationListener"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            processNotification(sbn)?.let { (transaction, general) ->
                transaction?.let { handleTransaction(it) }
                general?.let { handleGeneralNotification(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun processNotification(sbn: StatusBarNotification): Pair<TransactionNotification?, GeneralNotification?> {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title", "") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val rawData = "Title: $title\nText: $text\nExtras: $extras"
        val timestamp = System.currentTimeMillis()

        return try {
            // Try to parse as transaction first
            parseTransaction(packageName, title, text, rawData, timestamp)?.let {
                Pair(it, null)
            } ?: run {
                // If not a transaction, create general notification
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                } catch (e: Exception) {
                    packageName
                }

                Pair(null, GeneralNotification(
                    id = generateId("$packageName$title$text$timestamp"),
                    timestamp = timestamp,
                    packageName = packageName,
                    title = title,
                    text = text,
                    rawData = rawData,
                    appName = appName
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in processNotification", e)
            Pair(null, null)
        }
    }

    private fun parseTransaction(
        packageName: String,
        title: String,
        text: String,
        rawData: String,
        timestamp: Long
    ): TransactionNotification? {
        // Pattern for Google Wallet transactions like:
        // "$29.48 with Amex •••• 1005 at INDIA METRO HYPERMARKET"
        val walletPattern = "\\$([\\d.]+)\\s+with\\s+([A-Za-z]+)\\s+•+\\s+(\\d{4})\\s+at\\s+(.+)".toRegex()
        val walletMatch = walletPattern.find(text) ?: return null

        val amount = walletMatch.groupValues[1].toDoubleOrNull() ?: return null
        val cardType = walletMatch.groupValues[2]
        val lastFour = walletMatch.groupValues[3]
        val merchant = walletMatch.groupValues[4].trim()
        val card = "$cardType •••• $lastFour"

        // Format date and time
        val date = android.text.format.DateFormat.format("yyyy-MM-dd", timestamp).toString()
        val time = android.text.format.DateFormat.format("HH:mm:ss", timestamp).toString()

        return TransactionNotification(
            id = generateId("$amount$card$merchant$date$time"),
            timestamp = timestamp,
            packageName = packageName,
            title = title,
            text = text,
            rawData = rawData,
            amount = amount,
            card = card,
            merchant = merchant,
            transactionDate = date,
            transactionTime = time
        )
    }

    private fun handleTransaction(transaction: TransactionNotification) {
        // TODO: Save to database or send to Google Sheets
        Log.d(TAG, "Transaction detected: ${transaction.merchant} - ${transaction.formattedAmount}")
    }

    private fun handleGeneralNotification(notification: GeneralNotification) {
        // TODO: Save to database or send to Google Sheets
        Log.d(TAG, "General notification from ${notification.appName}: ${notification.title}")
    }

    private fun generateId(input: String): String {
        return Hashing.sha256()
            .hashString(input, StandardCharsets.UTF_8)
            .toString()
    }
}