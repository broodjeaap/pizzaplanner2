package com.pizzaplanner.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pizzaplanner.ui.settings.SettingsFragment

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "pizza_alarm_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received")
        
        val stepName = intent.getStringExtra("step_name") ?: "Recipe Step"
        val message = intent.getStringExtra("message") ?: "Time for next step!"
        val alarmType = intent.getStringExtra("alarm_type") ?: "STEP_START"
        
        // Get notification style from settings
        val sharedPreferences = context.getSharedPreferences("pizza_planner_settings", Context.MODE_PRIVATE)
        val notificationStyle = sharedPreferences.getString("notification_style", "Full Screen Alarm") ?: "Full Screen Alarm"
        
        Log.d("AlarmReceiver", "Notification style: $notificationStyle")
        
        when (notificationStyle) {
            "Silent" -> {
                // Do nothing for silent mode
                Log.d("AlarmReceiver", "Silent mode - no notification shown")
                return
            }
            "Notification Only" -> {
                // Show regular notification only
                Log.d("AlarmReceiver", "Showing regular notification only")
                showRegularNotification(context, stepName, message)
            }
            else -> {
                // Full Screen Alarm (default) - show full-screen intent notification
                Log.d("AlarmReceiver", "Showing full-screen notification")
                showFullScreenNotification(context, stepName, message, alarmType)
            }
        }
    }
    
    private fun showRegularNotification(context: Context, stepName: String, message: String) {
        // Create notification channel if needed
        createNotificationChannel(context)
        
        // Create intent to launch MainActivity when notification is tapped
        val mainIntent = Intent(context, com.pizzaplanner.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build regular notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.pizzaplanner.R.drawable.ic_alarm)
            .setContentTitle(stepName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showFullScreenNotification(context: Context, stepName: String, message: String, alarmType: String) {
        // Create notification channel if needed
        createNotificationChannel(context)
        
        // Create intent to launch AlarmActivity when notification is tapped
        val alarmIntent = Intent(context, com.pizzaplanner.services.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("step_name", stepName)
            putExtra("message", message)
            putExtra("alarm_type", alarmType)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification with full-screen intent
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.pizzaplanner.R.drawable.ic_alarm)
            .setContentTitle(stepName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // This is the key for full-screen display
            .setAutoCancel(true)
            .build()
        
        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Pizza Alarm Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for pizza recipe steps"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
