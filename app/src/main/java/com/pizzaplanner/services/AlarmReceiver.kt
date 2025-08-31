package com.pizzaplanner.services

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
        
        // Create and show notification
        val notificationService = NotificationService(context)
        notificationService.showStepNotification(stepName, message, alarmType)
        
        // If this is a critical alarm, also start the alarm activity
        if (alarmType == "STEP_START" || alarmType == "FINAL_COMPLETION") {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("step_name", stepName)
                putExtra("message", message)
                putExtra("alarm_type", alarmType)
            }
            context.startActivity(alarmIntent)
        }
    }
}
