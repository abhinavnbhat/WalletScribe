package com.example.walletscribe

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.ByteArrayInputStream

object SheetsServiceFactory {

    private const val APPLICATION_NAME = "WalletScribe"

    fun create(context: Context): Sheets {
        // 1) Load the JSON string the user pasted in Settings
        val prefs     = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val credsJson = prefs.getString("CREDS_JSON", "") ?: ""
        require(credsJson.isNotBlank()) { "No credentials JSON in preferences" }

        // 2) Wrap it as an InputStream
        val stream = ByteArrayInputStream(credsJson.toByteArray(Charsets.UTF_8))

        // 3) Prepare HTTP transport & the default GsonFactory
        val transport   = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        // 4) Parse service-account credentials via GsonFactory
        val credential = GoogleCredential
            .fromStream(stream, transport, jsonFactory)
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))

        // 5) Build and return the Sheets client
        return Sheets.Builder(transport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}
