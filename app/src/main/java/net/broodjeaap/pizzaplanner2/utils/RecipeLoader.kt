package net.broodjeaap.pizzaplanner2.utils

import android.content.Context
import net.broodjeaap.pizzaplanner2.data.models.Recipe
import java.io.File
import java.io.InputStream

object RecipeLoader {
    
    /**
     * Loads recipes from the best available source:
     * 1. Downloaded recipes from internal storage (if available)
     * 2. Fallback to bundled recipes from assets
     */
    fun loadRecipes(context: Context): List<Recipe> {
        val yamlParser = YamlParser()
        
        // First, try to load downloaded recipes from internal storage
        val downloadedRecipesFile = File(File(context.filesDir, "recipes"), "downloaded_recipes.yaml")
        if (downloadedRecipesFile.exists()) {
            try {
                return yamlParser.parseRecipes(downloadedRecipesFile.inputStream())
            } catch (e: Exception) {
                // If downloaded recipes are corrupted, fall back to assets
                android.util.Log.w("RecipeLoader", "Failed to load downloaded recipes, falling back to assets: ${e.message}")
            }
        }
        
        // Fallback to bundled recipes from assets
        return context.assets.open("recipes/pizza_recipes_converted.yaml").use { inputStream ->
            yamlParser.parseRecipes(inputStream)
        }
    }
    
    /**
     * Gets the source of the currently loaded recipes
     */
    fun getRecipeSource(context: Context): String {
        val downloadedRecipesFile = File(File(context.filesDir, "recipes"), "downloaded_recipes.yaml")
        return if (downloadedRecipesFile.exists()) {
            "Downloaded recipes (${downloadedRecipesFile.lastModified()})"
        } else {
            "Bundled recipes"
        }
    }
    
    /**
     * Check if downloaded recipes are newer than when an active recipe was created
     */
    fun hasNewerRecipes(context: Context, activeRecipeCreatedAt: java.time.LocalDateTime): Boolean {
        val downloadedRecipesFile = File(File(context.filesDir, "recipes"), "downloaded_recipes.yaml")
        if (!downloadedRecipesFile.exists()) return false
        
        val downloadTime = java.time.Instant.ofEpochMilli(downloadedRecipesFile.lastModified())
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
        
        return downloadTime.isAfter(activeRecipeCreatedAt)
    }
    
    /**
     * Find a recipe by ID from current available recipes
     */
    fun findRecipeById(context: Context, recipeId: String): Recipe? {
        return try {
            loadRecipes(context).find { it.id == recipeId }
        } catch (e: Exception) {
            null
        }
    }
}