package com.pizzaplanner.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Entity(tableName = "planned_recipes")
@Parcelize
data class PlannedRecipe(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val recipeName: String,
    val targetCompletionTime: LocalDateTime,
    val startTime: LocalDateTime,
    val variableValues: Map<String, Double>,
    val status: RecipeStatus = RecipeStatus.SCHEDULED,
    val currentStepIndex: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val notes: String? = null
) : Parcelable

@Parcelize
enum class RecipeStatus : Parcelable {
    SCHEDULED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED
}

@Parcelize
data class AlarmEvent(
    val id: String,
    val plannedRecipeId: String,
    val stepId: String,
    val stepName: String,
    val scheduledTime: LocalDateTime,
    val alarmType: AlarmType,
    val isActive: Boolean = true,
    val message: String
) : Parcelable

@Parcelize
enum class AlarmType : Parcelable {
    STEP_START,
    STEP_END,
    REMINDER,
    FINAL_COMPLETION
}

@Parcelize
data class RecipeProgress(
    val plannedRecipeId: String,
    val stepId: String,
    val stepName: String,
    val scheduledStartTime: LocalDateTime,
    val actualStartTime: LocalDateTime? = null,
    val estimatedEndTime: LocalDateTime,
    val actualEndTime: LocalDateTime? = null,
    val status: StepStatus = StepStatus.PENDING,
    val notes: String? = null
) : Parcelable

@Parcelize
enum class StepStatus : Parcelable {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}
