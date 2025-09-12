package net.broodjeaap.pizzaplanner2.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import net.broodjeaap.pizzaplanner2.data.models.AlarmEvent
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // This service doesn't need to do anything when started
        return START_NOT_STICKY
    }
    
    companion object {
        fun scheduleAlarm(context: Context, alarmEvent: AlarmEvent) {
            // Parse recipeId and stepId from alarmEvent.id (format: recipeId::stepId)
            val parts = alarmEvent.id.split("::")
            val recipeId = if (parts.size > 1) parts[0] else ""
            val stepId = if (parts.size > 1) parts[1] else ""
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("step_name", alarmEvent.stepName)
                putExtra("message", alarmEvent.message)
                putExtra("alarm_type", alarmEvent.alarmType.name)
                putExtra("alarm_id", alarmEvent.id)
                putExtra("recipe_id", recipeId)
                putExtra("step_id", stepId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmEvent.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
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
        
        fun cancelAlarm(context: Context, alarmId: String) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmService", "Cancelled alarm with ID: $alarmId")
        }
        
        fun scheduleMultipleAlarms(context: Context, alarmEvents: List<AlarmEvent>) {
            alarmEvents.forEach { alarmEvent ->
                if (alarmEvent.scheduledTime.isAfter(LocalDateTime.now())) {
                    scheduleAlarm(context, alarmEvent)
                }
            }
        }
        
        fun cancelAllAlarms(context: Context, alarmEvents: List<AlarmEvent>) {
            alarmEvents.forEach { alarmEvent ->
                cancelAlarm(context, alarmEvent.id)
            }
        }
    }
}
