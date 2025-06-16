package com.example.notificationlogger

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

object NotificationHelper {
    fun isNotificationAccessGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(packageName) == true
    }

    fun showNotificationAccessDialog(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Notification Access Required")
            .setMessage("This app needs notification access to read transaction notifications.")
            .setPositiveButton("Open Settings") { _, _ ->
                activity.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}