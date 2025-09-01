package com.pizzaplanner.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.pizzaplanner.data.models.AlarmEvent
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmService(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleAlarm(alarmEvent: AlarmEvent) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("step_name", alarmEvent.stepName)
            putExtra("message", alarmEvent.message)
            putExtra("alarm_type", alarmEvent.alarmType.name)
            putExtra("alarm_id", alarmEvent.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmEvent.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = alarmEvent.scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            
            Log.d("AlarmService", "Scheduled alarm for ${alarmEvent.stepName} at ${alarmEvent.scheduledTime}")
        } catch (e: SecurityException) {
            Log.e("AlarmService", "Failed to schedule exact alarm: ${e.message}")
            // Fallback to inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelAlarm(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmService", "Cancelled alarm with ID: $alarmId")
    }
    
    fun scheduleMultipleAlarms(alarmEvents: List<AlarmEvent>) {
        alarmEvents.forEach { alarmEvent ->
            if (alarmEvent.scheduledTime.isAfter(LocalDateTime.now())) {
                scheduleAlarm(alarmEvent)
            }
        }
    }
    
    fun cancelAllAlarms(alarmEvents: List<AlarmEvent>) {
        alarmEvents.forEach { alarmEvent ->
            cancelAlarm(alarmEvent.id)
        }
    }
}
