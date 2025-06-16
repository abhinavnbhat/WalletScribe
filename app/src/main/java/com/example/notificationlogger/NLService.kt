package com.example.notificationlogger

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NLService : NotificationListenerService() {

    companion object {
        /** Set by the system when the listener is connected so Activities can reach us. */
        var INSTANCE: NLService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Diag", "NLService onCreate()")
        // No repo initialization here—defer until we sync
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("Diag", "NLService onListenerConnected()")
        INSTANCE = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (INSTANCE === this) INSTANCE = null
    }

    /** One-shot sync. Parses, dedupes, writes, and builds a log. */
    suspend fun doOneShotSync(): SyncResult = withContext(Dispatchers.IO) {
        Log.d("Diag", "doOneShotSync ▶ start")

        // 1) Load prefs
        val prefs   = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sheetId = prefs.getString("SHEET_ID", "") ?: ""
        require(sheetId.isNotBlank()) { "No Sheet ID in preferences" }
        Log.d("Diag", "doOneShotSync ▶ prefs loaded sheetId='$sheetId'")

        // 2) Build SheetsRepository
        val sheetsClient = SheetsServiceFactory.create(this@NLService)
        val repo         = SheetsRepository(sheetsClient, sheetId)
        Log.d("Diag", "doOneShotSync ▶ SheetsRepository ready")

        // 3) Fetch & parse notifications
        val active     = activeNotifications.toList()
        val walletRows = mutableListOf<WalletTxn>()
        val otherRows  = mutableListOf<OtherNotif>()
        active.forEach { sbn ->
            val (wallet, other) = Parser.parse(sbn)
            wallet?.let { walletRows += it }
            other?.let { otherRows += it }
        }
        Log.d("Diag", "doOneShotSync ▶ parsed ${walletRows.size} wallet + ${otherRows.size} other notifications")

        // 4) Dedupe
        Log.d("Diag", "doOneShotSync ▶ fetching existing hashes")
        val txnHashes   = repo.fetchHashes("Transactions")
        val otherHashes = repo.fetchHashes("Other Notifications")
        Log.d("Diag", "doOneShotSync ▶ fetched hashes: ${txnHashes.size} txns, ${otherHashes.size} others")

        // 5) Prepare timestamp
        val nowDate = LocalDate.now().toString()
        val nowTime = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        // 6) Build new rows
        val newTxnRows = walletRows
            .filterNot { it.hash in txnHashes }
            .map {
                listOf(
                    it.amount, it.card, it.date, it.time, it.where,
                    it.raw, "", nowDate, nowTime, it.hash
                )
            }
        val newOtherRows = otherRows
            .filterNot { it.hash in otherHashes }
            .map { listOf(it.raw, nowDate, nowTime, it.hash) }
        Log.d("Diag", "doOneShotSync ▶ writing ${newTxnRows.size} txn rows, ${newOtherRows.size} other rows")

        // 7) Append
        repo.appendRows("Transactions", newTxnRows)
        repo.appendRows("Other Notifications", newOtherRows)
        Log.d("Diag", "doOneShotSync ▶ appendRows called")

        // 8) Build the per-notification log
        val logLines = buildList<String> {
            walletRows.forEach {
                add(
                    if (it.hash in txnHashes)
                        "♻️ ${it.amount} ${it.where} — duplicate"
                    else
                        "✅ ${it.amount} ${it.where} — written"
                )
            }
            otherRows.forEach {
                add(
                    if (it.hash in otherHashes)
                        "♻️ ${it.raw.take(32)}… — duplicate"
                    else
                        "✅ ${it.raw.take(32)}… — written"
                )
            }
        }

        Log.d(
            "Diag", "doOneShotSync ▶ returning result " +
                    "captured=${active.size}, written=${newTxnRows.size + newOtherRows.size}, " +
                    "failed=${active.size - (newTxnRows.size + newOtherRows.size)}"
        )

        SyncResult(
            captured = active.size,
            written  = newTxnRows.size + newOtherRows.size,
            failed   = active.size - (newTxnRows.size + newOtherRows.size),
            logLines = logLines
        )
    }
}
