package com.example.notificationlogger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationlogger.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val logAdapter = LogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup
        binding.rvLog.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = logAdapter
        }

        // Prompt for notification-listener access if not already granted
        if (!NotificationManagerCompat.getEnabledListenerPackages(this)
                .contains(packageName)
        ) {
            startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            Toast.makeText(
                this,
                "Enable Notification Access for this app, then press Sync",
                Toast.LENGTH_LONG
            ).show()
        }

        // Sync button click
        binding.btnSync.setOnClickListener {
            // 3.1 Load saved settings
            val prefs     = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val sheetId   = prefs.getString("SHEET_ID", "") ?: ""
            val credsJson = prefs.getString("CREDS_JSON", "") ?: ""

            // — Debug logging of what you just loaded —
            Log.d("PrefsDebug", "Loaded SHEET_ID   → '$sheetId'")
            Log.d("PrefsDebug", "Loaded CREDS_JSON → first 50 chars: '${credsJson.take(50)}…'")
            Log.d("Diag", "sheetId        = '*$sheetId*' (length=${sheetId.length})")
            Log.d("Diag", "credsJson      = '*${credsJson.take(60)
                .replace("\n","\\n")}*' (length=${credsJson.length})")
            Log.d("Diag", "sheetBlank?    = ${sheetId.isBlank()}")
            Log.d("Diag", "credsJsonBlank?= ${credsJson.isBlank()}")

            // 3.2 If missing, show message and bail out
            if (sheetId.isBlank() || credsJson.isBlank()) {
                Toast.makeText(
                    this,
                    "Provide details on the Settings screen on where to sync the notifications",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // 3.3 Check service ready
            val service = NLService.INSTANCE
            Log.d("Diag", "NLService.INSTANCE = $service")
            if (service == null) {
                Toast.makeText(
                    this,
                    "Service not ready—please enable notification access and try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 3.4 Disable button & launch sync
            binding.btnSync.isEnabled = false
            lifecycleScope.launch {
                try {
                    Log.d("Diag", "About to call doOneShotSync()")
                    val res = service.doOneShotSync()
                    Log.d("Diag", "doOneShotSync() returned successfully")

                    binding.tvCounts.text =
                        "Captured: ${res.captured} • Written: ${res.written} • Failed: ${res.failed}"
                    logAdapter.submitList(res.logLines.reversed())
                } catch (e: Exception) {
                    Log.e("SyncError", "Sync failed with exception", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Sync failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    binding.btnSync.isEnabled = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Ensure the service is running so that onListenerConnected() will fire
        startService(Intent(this, NLService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
