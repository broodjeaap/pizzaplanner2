package com.pizzaplanner.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.pizzaplanner.data.models.Recipe
import com.pizzaplanner.utils.YamlParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class RecipeDownloadService : Service() {
    
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val httpClient = OkHttpClient()
    
    companion object {
        const val ACTION_DOWNLOAD_RECIPES = "com.pizzaplanner.DOWNLOAD_RECIPES"
        const val EXTRA_RECIPE_URL = "recipe_url"
        const val DEFAULT_RECIPE_URL = "https://raw.githubusercontent.com/example/pizza-recipes/main/recipes.yaml"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DOWNLOAD_RECIPES -> {
                val recipeUrl = intent.getStringExtra(EXTRA_RECIPE_URL) ?: DEFAULT_RECIPE_URL
                downloadRecipes(recipeUrl, startId)
            }
        }
        return START_NOT_STICKY
    }
    
    private fun downloadRecipes(url: String, startId: Int) {
        serviceScope.launch {
            try {
                Log.d("RecipeDownloadService", "Starting recipe download from: $url")
                
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val yamlContent = responseBody.string()
                        
                        // Save to internal storage
                        val recipesDir = File(filesDir, "recipes")
                        if (!recipesDir.exists()) {
                            recipesDir.mkdirs()
                        }
                        
                        val recipesFile = File(recipesDir, "downloaded_recipes.yaml")
                        FileOutputStream(recipesFile).use { output ->
                            output.write(yamlContent.toByteArray())
                        }
                        
                        // Validate the downloaded recipes
                        val yamlParser = YamlParser()
                        val recipes = yamlParser.parseRecipes(recipesFile.inputStream())
                        
                        Log.d("RecipeDownloadService", "Successfully downloaded ${recipes.size} recipes")
                        
                        // Broadcast success
                        val successIntent = Intent("com.pizzaplanner.RECIPES_DOWNLOADED").apply {
                            putExtra("success", true)
                            putExtra("recipe_count", recipes.size)
                        }
                        sendBroadcast(successIntent)
                        
                    } ?: throw Exception("Empty response body")
                } else {
                    throw Exception("HTTP ${response.code}: ${response.message}")
                }
                
            } catch (e: Exception) {
                Log.e("RecipeDownloadService", "Failed to download recipes: ${e.message}")
                
                // Broadcast failure
                val failureIntent = Intent("com.pizzaplanner.RECIPES_DOWNLOADED").apply {
                    putExtra("success", false)
                    putExtra("error", e.message)
                }
                sendBroadcast(failureIntent)
            } finally {
                stopSelf(startId)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
