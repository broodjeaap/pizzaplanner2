package net.broodjeaap.pizzaplanner2.services

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Trigger recipe download
            val intent = Intent(applicationContext, RecipeDownloadService::class.java).apply {
                action = RecipeDownloadService.ACTION_DOWNLOAD_RECIPES
                putExtra(RecipeDownloadService.EXTRA_RECIPE_URL, RecipeDownloadService.DEFAULT_RECIPE_URL)
            }
            
            applicationContext.startService(intent)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORKER_TAG = "recipe_update_worker"
    }
}