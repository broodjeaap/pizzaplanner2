package net.broodjeaap.pizzaplanner2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    
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
                // Show regular notification only using NotificationService
                Log.d("AlarmReceiver", "Showing regular notification only")
                showRegularNotification(context, stepName, message, alarmType)
            }
            else -> {
                // Full Screen Alarm (default) - show full-screen intent notification
                Log.d("AlarmReceiver", "Showing full-screen notification")
                showFullScreenNotification(context, stepName, message, alarmType)
            }
        }
    }
    
    private fun showRegularNotification(context: Context, stepName: String, message: String, alarmType: String) {
        // Use NotificationService to show notification
        val notificationService = NotificationService(context)
        notificationService.showStepNotification(stepName, message, alarmType)
    }
    
    private fun showFullScreenNotification(context: Context, stepName: String, message: String, alarmType: String) {
        // Create intent to launch AlarmActivity when notification is tapped
        val alarmIntent = Intent(context, net.broodjeaap.pizzaplanner2.services.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("step_name", stepName)
            putExtra("message", message)
            putExtra("alarm_type", alarmType)
        }
        
        // Use NotificationService to show notification with full-screen intent
        val notificationService = NotificationService(context)
        // We still need to create the notification with full-screen intent manually
        // because NotificationService doesn't handle full-screen intents
        
        // Create notification channel if needed (NotificationService handles this)
        // For full-screen intent, we need to create the notification manually
        val notification = androidx.core.app.NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(net.broodjeaap.pizzaplanner2.R.drawable.ic_alarm)
            .setContentTitle(stepName)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(
                android.app.PendingIntent.getActivity(
                    context,
                    System.currentTimeMillis().toInt(),
                    alarmIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                ),
                true
            )
            .setAutoCancel(true)
            .build()
        
        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationService.NOTIFICATION_ID_BASE + stepName.hashCode(), notification)
    }
}
