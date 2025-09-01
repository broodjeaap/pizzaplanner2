package com.pizzaplanner.data.models

import java.time.LocalDateTime

/**
 * Represents an alarm event for a recipe step
 * @property id Unique identifier for the alarm
 * @property stepName Name of the step
 * @property message Notification message
 * @property scheduledTime When the alarm should trigger
 * @property alarmType Type of alarm (notification or full-screen)
 */
data class AlarmEvent(
    val id: String,
    val stepName: String,
    val message: String,
    val scheduledTime: LocalDateTime,
    val alarmType: AlarmType
)

enum class AlarmType {
    NOTIFICATION,
    FULL_SCREEN
}
