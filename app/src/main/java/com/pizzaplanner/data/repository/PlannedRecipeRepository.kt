package com.pizzaplanner.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.reflect.TypeToken
import com.pizzaplanner.data.models.*
import com.pizzaplanner.data.models.AlarmEvent
import com.pizzaplanner.utils.RecipeTimeline
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PlannedRecipeRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("planned_recipes", Context.MODE_PRIVATE)
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    
    companion object {
        private const val KEY_ACTIVE_RECIPES = "active_recipes"
    }
    
    fun saveRecipe(
        plannedRecipe: PlannedRecipe,
        recipeTimeline: RecipeTimeline,
        alarmEvents: List<AlarmEvent> = emptyList(),
        currentStepIndex: Int = 0,
        status: RecipeStatus = RecipeStatus.IN_PROGRESS,
        isPaused: Boolean = false
    ) {
        val recipeData = ActiveRecipeData(
            recipe = plannedRecipe,
            timeline = recipeTimeline,
            alarmEvents = alarmEvents,
            currentStepIndex = currentStepIndex,
            status = status,
            isPaused = isPaused
        )
        
        val recipes = getAllRecipesList().toMutableList()
        
        // Remove existing recipe with same ID if it exists
        recipes.removeAll { it.recipe.id == plannedRecipe.id }
        
        // Add new recipe
        recipes.add(recipeData)
        
        // Save updated list
        val json = gson.toJson(recipes)
        sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
    }
    
    fun getAllRecipesList(): List<ActiveRecipeData> {
        val json = sharedPreferences.getString(KEY_ACTIVE_RECIPES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ActiveRecipeData>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getRecipe(recipeId: String): ActiveRecipeData? {
        return getAllRecipesList().find { it.recipe.id == recipeId }
    }
    
    fun updateRecipe(recipeId: String, updater: (ActiveRecipeData) -> ActiveRecipeData) {
        val recipes = getAllRecipesList().toMutableList()
        val index = recipes.indexOfFirst { it.recipe.id == recipeId }
        if (index != -1) {
            recipes[index] = updater(recipes[index])
            val json = gson.toJson(recipes)
            sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
        }
    }
    
    fun updateRecipeWithAlarms(recipeId: String, alarmEvents: List<AlarmEvent>, updater: (ActiveRecipeData) -> ActiveRecipeData) {
        val recipes = getAllRecipesList().toMutableList()
        val index = recipes.indexOfFirst { it.recipe.id == recipeId }
        if (index != -1) {
            val updatedRecipe = updater(recipes[index].copy(alarmEvents = alarmEvents))
            recipes[index] = updatedRecipe
            val json = gson.toJson(recipes)
            sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
        }
    }
    
    fun removeRecipe(recipeId: String) {
        val recipes = getAllRecipesList().toMutableList()
        recipes.removeAll { it.recipe.id == recipeId }
        val json = gson.toJson(recipes)
        sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
    }
    
    fun hasRecipes(): Boolean {
        return getAllRecipesList().isNotEmpty()
    }
    
    // Data class for storing active recipe with all its state
    data class ActiveRecipeData(
        val recipe: PlannedRecipe,
        val timeline: RecipeTimeline,
        val currentStepIndex: Int = 0,
        val status: RecipeStatus = RecipeStatus.IN_PROGRESS,
        val isPaused: Boolean = false,
        val alarmEvents: List<AlarmEvent> = emptyList()
    )
    
    // Custom TypeAdapter for LocalDateTime
    private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        
        override fun write(out: JsonWriter, value: LocalDateTime?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.format(formatter))
            }
        }
        
        override fun read(`in`: JsonReader): LocalDateTime? {
            return if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
                `in`.nextNull()
                null
            } else {
                LocalDateTime.parse(`in`.nextString(), formatter)
            }
        }
    }
}
