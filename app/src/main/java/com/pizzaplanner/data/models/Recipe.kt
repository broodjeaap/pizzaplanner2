package com.pizzaplanner.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val variables: List<RecipeVariable>,
    val steps: List<RecipeStep>,
    val ingredients: List<Ingredient> = emptyList(),
    val imageUrl: String? = null,
    val difficulty: String = "Medium",
    val totalTimeHours: Int = 24
) : Parcelable

@Parcelize
data class RecipeVariable(
    val name: String,
    val displayName: String,
    val defaultValue: Double,
    val minValue: Double,
    val maxValue: Double,
    val type: VariableType,
    val unit: String? = null
) : Parcelable

@Parcelize
enum class VariableType : Parcelable {
    INTEGER,
    DECIMAL,
    BOOLEAN
}

@Parcelize
data class RecipeStep(
    val id: String,
    val name: String,
    val description: String,
    val durationMinutes: Int? = null,
    val durationFormula: String? = null,
    val timing: StepTiming,
    val isOptional: Boolean = false,
    val temperature: String? = null,
    val notes: String? = null
) : Parcelable

@Parcelize
enum class StepTiming : Parcelable {
    START,
    AFTER_PREVIOUS,
    PARALLEL,
    SCHEDULED
}

@Parcelize
data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String? = null
) : Parcelable
