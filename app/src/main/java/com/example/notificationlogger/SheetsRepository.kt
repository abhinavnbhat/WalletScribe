package com.example.notificationlogger

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.*

class SheetsRepository(
    private val sheets: Sheets,
    private val sheetId: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Returns all existing hashes from the given tab so we can dedupe. */
    suspend fun fetchHashes(tabName: String): Set<String> = withContext(scope.coroutineContext) {
        val hashCol = if (tabName == "Transactions") "J" else "D"   // J for txns, D for other
        val range = "'$tabName'!${hashCol}2:${hashCol}"
        val resp = sheets.spreadsheets().values()
            .get(sheetId, range)
            .execute()
        resp.getValues()
            ?.mapNotNull { it.firstOrNull()?.toString() }
            ?.toSet()
            ?: emptySet()
    }

    /** Appends the provided rows to the bottom of the tab (RAW input). */
    fun appendRows(tabName: String, rows: List<List<Any>>) = scope.launch {
        if (rows.isEmpty()) return@launch
        val body = ValueRange().setValues(rows)
        sheets.spreadsheets().values()
            .append(sheetId, "'$tabName'!A2", body)
            .setValueInputOption("RAW")
            .setInsertDataOption("INSERT_ROWS")
            .execute()
    }
}
