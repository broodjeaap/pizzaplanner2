package com.pizzaplanner.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pizzaplanner.MainActivity
import com.pizzaplanner.R

class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "pizza_planner_alarms"
        const val NOTIFICATION_ID_BASE = 1000
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("pizza_planner_settings", Context.MODE_PRIVATE)
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.alarm_channel_description)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showStepNotification(stepName: String, message: String, alarmType: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get alarm sound from settings
        val alarmSoundUri = getAlarmSoundUri()
        
        // Get vibration setting
        val vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_recipes)
            .setContentTitle(stepName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(alarmSoundUri)
        
        // Add vibration if enabled
        if (vibrationEnabled) {
            notificationBuilder.setVibrate(longArrayOf(0, 500, 200, 500))
        }
        
        val notification = notificationBuilder.build()
        
        val notificationId = NOTIFICATION_ID_BASE + stepName.hashCode()
        notificationManager.notify(notificationId, notification)
    }
    
    private fun getAlarmSoundUri(): Uri? {
        val uriString = sharedPreferences.getString("alarm_sound_uri", null)
        return if (uriString != null) {
            Uri.parse(uriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }
    
    fun cancelNotification(stepName: String) {
        val notificationId = NOTIFICATION_ID_BASE + stepName.hashCode()
        notificationManager.cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
