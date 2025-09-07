package net.broodjeaap.pizzaplanner2.data.models

import java.time.LocalDateTime

data class RecipeTimeline(
    val recipe: Recipe,
    val variableValues: Map<String, Double>,
    val startTime: LocalDateTime,
    val targetCompletionTime: LocalDateTime,
    val totalDurationMinutes: Int,
    val steps: List<StepTimeline>,
    val ingredients: List<ProcessedIngredient> = emptyList()
)

data class ProcessedStep(
    val step: RecipeStep,
    val durationMinutes: Int,
    val processedDescription: String,
    val processedTemperature: String?
)

data class StepTimeline(
    val step: RecipeStep,
    val processedDescription: String,
    val processedTemperature: String?,
    val durationMinutes: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class ProcessedIngredient(
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String?
)