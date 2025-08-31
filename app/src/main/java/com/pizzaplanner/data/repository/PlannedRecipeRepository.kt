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
        private const val KEY_ACTIVE_RECIPE = "active_recipe"
        private const val KEY_ACTIVE_TIMELINE = "active_timeline"
        private const val KEY_CURRENT_STEP_INDEX = "current_step_index"
        private const val KEY_RECIPE_STATUS = "recipe_status"
        private const val KEY_IS_PAUSED = "is_paused"
    }
    
    fun saveActiveRecipe(
        plannedRecipe: PlannedRecipe,
        recipeTimeline: RecipeTimeline,
        currentStepIndex: Int = 0,
        status: RecipeStatus = RecipeStatus.IN_PROGRESS,
        isPaused: Boolean = false
    ) {
        with(sharedPreferences.edit()) {
            // Convert LocalDateTime to string for storage
            val recipeJson = gson.toJson(plannedRecipe.copy(
                startTime = plannedRecipe.startTime,
                targetCompletionTime = plannedRecipe.targetCompletionTime
            ))
            
            val timelineJson = gson.toJson(recipeTimeline)
            
            putString(KEY_ACTIVE_RECIPE, recipeJson)
            putString(KEY_ACTIVE_TIMELINE, timelineJson)
            putInt(KEY_CURRENT_STEP_INDEX, currentStepIndex)
            putString(KEY_RECIPE_STATUS, status.name)
            putBoolean(KEY_IS_PAUSED, isPaused)
            apply()
        }
    }
    
    fun getActiveRecipe(): PlannedRecipe? {
        val recipeJson = sharedPreferences.getString(KEY_ACTIVE_RECIPE, null) ?: return null
        return try {
            gson.fromJson(recipeJson, PlannedRecipe::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getActiveRecipeTimeline(): RecipeTimeline? {
        val timelineJson = sharedPreferences.getString(KEY_ACTIVE_TIMELINE, null) ?: return null
        return try {
            gson.fromJson(timelineJson, RecipeTimeline::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentStepIndex(): Int {
        return sharedPreferences.getInt(KEY_CURRENT_STEP_INDEX, 0)
    }
    
    fun getRecipeStatus(): RecipeStatus {
        val statusString = sharedPreferences.getString(KEY_RECIPE_STATUS, RecipeStatus.SCHEDULED.name)
        return try {
            RecipeStatus.valueOf(statusString ?: RecipeStatus.SCHEDULED.name)
        } catch (e: Exception) {
            RecipeStatus.SCHEDULED
        }
    }
    
    fun isRecipePaused(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PAUSED, false)
    }
    
    fun updateCurrentStepIndex(index: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_STEP_INDEX, index).apply()
    }
    
    fun updateRecipeStatus(status: RecipeStatus) {
        sharedPreferences.edit().putString(KEY_RECIPE_STATUS, status.name).apply()
    }
    
    fun updatePausedStatus(isPaused: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PAUSED, isPaused).apply()
    }
    
    fun clearActiveRecipe() {
        with(sharedPreferences.edit()) {
            remove(KEY_ACTIVE_RECIPE)
            remove(KEY_ACTIVE_TIMELINE)
            remove(KEY_CURRENT_STEP_INDEX)
            remove(KEY_RECIPE_STATUS)
            remove(KEY_IS_PAUSED)
            apply()
        }
    }
    
    fun hasActiveRecipe(): Boolean {
        return sharedPreferences.contains(KEY_ACTIVE_RECIPE)
    }
    
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
