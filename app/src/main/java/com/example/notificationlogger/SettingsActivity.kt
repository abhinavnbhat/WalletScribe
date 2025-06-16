package com.example.notificationlogger

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notificationlogger.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Load existing values under the same keys MainActivity expects:
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        binding.etSheetId.setText(prefs.getString("SHEET_ID", ""))
        binding.etCredentials.setText(prefs.getString("CREDS_JSON", ""))

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val sheetId = binding.etSheetId.text.toString().trim()
            val creds   = binding.etCredentials.text.toString().trim()

            if (sheetId.isEmpty() || creds.isEmpty()) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Very basic JSON sanity check
            if (!creds.startsWith("{") || !creds.endsWith("}")) {
                Toast.makeText(this, "Credentials must be valid JSON", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2) Persist under uppercase keys:
            prefs.edit()
                .putString("SHEET_ID"    , sheetId)
                .putString("CREDS_JSON"  , creds)
                .apply()

            // 3) Debug log to confirm what you just saved:
            Log.d("PrefsDebug", "just saved SHEET_ID → '$sheetId'")
            Log.d("PrefsDebug", "just saved CREDS_JSON → ${creds.take(50)}…")

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
