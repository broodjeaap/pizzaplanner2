package com.pizzaplanner.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

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
        
        // Create notification channel if needed
        createNotificationChannel(context)
        
        // Create intent to launch AlarmActivity when notification is tapped
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
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
