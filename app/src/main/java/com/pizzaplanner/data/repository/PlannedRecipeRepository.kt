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
        private const val KEY_ACTIVE_RECIPES = "active_recipes"
    }
    
    fun saveActiveRecipe(
        plannedRecipe: PlannedRecipe,
        recipeTimeline: RecipeTimeline,
        currentStepIndex: Int = 0,
        status: RecipeStatus = RecipeStatus.IN_PROGRESS,
        isPaused: Boolean = false
    ) {
        val activeRecipeData = ActiveRecipeData(
            recipe = plannedRecipe,
            timeline = recipeTimeline,
            currentStepIndex = currentStepIndex,
            status = status,
            isPaused = isPaused
        )
        
        val activeRecipes = getActiveRecipesList().toMutableList()
        
        // Remove existing recipe with same ID if it exists
        activeRecipes.removeAll { it.recipe.id == plannedRecipe.id }
        
        // Add new recipe
        activeRecipes.add(activeRecipeData)
        
        // Save updated list
        val json = gson.toJson(activeRecipes)
        sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
    }
    
    fun getActiveRecipesList(): List<ActiveRecipeData> {
        val json = sharedPreferences.getString(KEY_ACTIVE_RECIPES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ActiveRecipeData>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getActiveRecipe(recipeId: String): ActiveRecipeData? {
        return getActiveRecipesList().find { it.recipe.id == recipeId }
    }
    
    fun updateActiveRecipe(recipeId: String, updater: (ActiveRecipeData) -> ActiveRecipeData) {
        val activeRecipes = getActiveRecipesList().toMutableList()
        val index = activeRecipes.indexOfFirst { it.recipe.id == recipeId }
        if (index != -1) {
            activeRecipes[index] = updater(activeRecipes[index])
            val json = gson.toJson(activeRecipes)
            sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
        }
    }
    
    fun removeActiveRecipe(recipeId: String) {
        val activeRecipes = getActiveRecipesList().toMutableList()
        activeRecipes.removeAll { it.recipe.id == recipeId }
        val json = gson.toJson(activeRecipes)
        sharedPreferences.edit().putString(KEY_ACTIVE_RECIPES, json).apply()
    }
    
    fun hasActiveRecipes(): Boolean {
        return getActiveRecipesList().isNotEmpty()
    }
    
    // Data class for storing active recipe with all its state
    data class ActiveRecipeData(
        val recipe: PlannedRecipe,
        val timeline: RecipeTimeline,
        val currentStepIndex: Int = 0,
        val status: RecipeStatus = RecipeStatus.IN_PROGRESS,
        val isPaused: Boolean = false
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
