package com.pizzaplanner.ui.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizzaplanner.R
import com.pizzaplanner.data.repository.PlannedRecipeRepository.ActiveRecipeData
import com.pizzaplanner.databinding.ItemActiveRecipeBinding
import com.pizzaplanner.databinding.ItemInactiveRecipeBinding
import com.pizzaplanner.data.models.RecipeStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ActiveRecipesAdapter(
    private val onRecipeClick: (ActiveRecipeData) -> Unit
) : ListAdapter<ActiveRecipeData, RecyclerView.ViewHolder>(ActiveRecipeDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ACTIVE = 0
        private const val VIEW_TYPE_INACTIVE = 1
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item.status) {
            RecipeStatus.IN_PROGRESS, RecipeStatus.SCHEDULED -> VIEW_TYPE_ACTIVE
            else -> VIEW_TYPE_INACTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ACTIVE -> {
                val binding = ItemActiveRecipeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActiveRecipeViewHolder(binding)
            }
            else -> {
                val binding = ItemInactiveRecipeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                InactiveRecipeViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ActiveRecipeViewHolder -> holder.bind(item)
            is InactiveRecipeViewHolder -> holder.bind(item)
        }
    }

    inner class ActiveRecipeViewHolder(
        private val binding: ItemActiveRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activeRecipe: ActiveRecipeData) {
            with(binding) {
                // Recipe info
                textViewRecipeName.text = activeRecipe.recipe.recipeName
                textViewRecipeStatus.text = when (activeRecipe.status) {
                    RecipeStatus.IN_PROGRESS -> if (activeRecipe.isPaused) "Paused" else "In Progress"
                    RecipeStatus.SCHEDULED -> "Scheduled"
                    else -> "Unknown" // Shouldn't happen for active view
                }
                
                // Event name
                if (!activeRecipe.recipe.eventName.isNullOrEmpty()) {
                    textViewEventName.text = activeRecipe.recipe.eventName
                    textViewEventName.visibility = View.VISIBLE
                } else {
                    textViewEventName.visibility = View.GONE
                }

                // Progress
                val totalSteps = activeRecipe.timeline.steps.size
                val completedSteps = activeRecipe.currentStepIndex
                val progressPercent = if (totalSteps > 0) {
                    (completedSteps.toFloat() / totalSteps * 100).toInt()
                } else 0

                progressIndicator.progress = progressPercent
                textViewProgress.text = "${progressPercent}%"
                chipCurrentStep.text = "Step ${activeRecipe.currentStepIndex + 1}/$totalSteps"

                // Current step
                val currentStep = activeRecipe.timeline.steps.getOrNull(activeRecipe.currentStepIndex)
                if (currentStep != null) {
                    textViewCurrentStepName.text = currentStep.step.name
                    
                    // Show timer if step has duration and recipe is in progress
                    if (currentStep.durationMinutes > 0 && activeRecipe.status == RecipeStatus.IN_PROGRESS && !activeRecipe.isPaused) {
                        val now = LocalDateTime.now()
                        val stepEndTime = currentStep.endTime
                        if (stepEndTime != null) {
                            try {
                                val timeRemaining = ChronoUnit.MINUTES.between(now, stepEndTime)
                                if (timeRemaining > 0) {
                                    textViewStepTimer.text = formatTimeRemaining(timeRemaining)
                                    textViewStepTimer.visibility = android.view.View.VISIBLE
                                } else {
                                    textViewStepTimer.visibility = android.view.View.GONE
                                }
                            } catch (e: Exception) {
                                textViewStepTimer.visibility = android.view.View.GONE
                            }
                        } else {
                            textViewStepTimer.visibility = android.view.View.GONE
                        }
                    } else {
                        textViewStepTimer.visibility = android.view.View.GONE
                    }
                } else {
                    textViewCurrentStepName.text = "Recipe Complete"
                    textViewStepTimer.visibility = android.view.View.GONE
                }

                // Time info
                textViewStartTime.text = formatDateTime(activeRecipe.recipe.startTime)
                textViewCompletionTime.text = formatDateTime(activeRecipe.recipe.targetCompletionTime)

                // Click listener
                root.setOnClickListener {
                    onRecipeClick(activeRecipe)
                }
            }
        }

        private fun formatDateTime(dateTime: LocalDateTime): String {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            val tomorrow = today.plusDays(1)
            val yesterday = today.minusDays(1)

            return when (dateTime.toLocalDate()) {
                today -> "Today, ${dateTime.format(timeFormatter)}"
                tomorrow -> "Tomorrow, ${dateTime.format(timeFormatter)}"
                yesterday -> "Yesterday, ${dateTime.format(timeFormatter)}"
                else -> dateTime.format(dateTimeFormatter)
            }
        }
        
        private fun formatTimeRemaining(minutes: Long): String {
            return when {
                minutes >= 60 -> {
                    val hours = minutes / 60
                    val remainingMinutes = minutes % 60
                    "${hours}h ${remainingMinutes}m remaining"
                }
                else -> "${minutes}m remaining"
            }
        }
    }

    inner class InactiveRecipeViewHolder(
        private val binding: ItemInactiveRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

    fun bind(recipeData: ActiveRecipeData) {
        with(binding) {
            val displayName = if (!recipeData.recipe.eventName.isNullOrEmpty()) {
                recipeData.recipe.eventName
            } else {
                recipeData.recipe.recipeName
            }
            textViewRecipeName.text = displayName
                textViewStatus.text = when (recipeData.status) {
                    RecipeStatus.COMPLETED -> "Completed"
                    RecipeStatus.CANCELLED -> "Cancelled"
                    else -> "Unknown"
                }
                
                // Set different colors based on status
                when (recipeData.status) {
                    RecipeStatus.COMPLETED -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                    }
                    RecipeStatus.CANCELLED -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                    }
                    RecipeStatus.SCHEDULED -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
                    }
                    RecipeStatus.IN_PROGRESS -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_blue_dark))
                    }
                    RecipeStatus.PAUSED -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                    }
                    else -> {
                        textViewStatus.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                    }
                }

                textViewStartTime.text = formatDateTime(recipeData.recipe.startTime)
                
                // End time - use last step end time or current time
                val endTime = if (recipeData.timeline.steps.isNotEmpty()) {
                    recipeData.timeline.steps.last().endTime ?: LocalDateTime.now()
                } else {
                    LocalDateTime.now()
                }
                textViewEndTime.text = formatDateTime(endTime)

                root.setOnClickListener {
                    onRecipeClick(recipeData)
                }
            }
        }

        private fun formatDateTime(dateTime: LocalDateTime): String {
            return dateTime.format(dateTimeFormatter)
        }
    }

    private class ActiveRecipeDiffCallback : DiffUtil.ItemCallback<ActiveRecipeData>() {
        override fun areItemsTheSame(oldItem: ActiveRecipeData, newItem: ActiveRecipeData): Boolean {
            return oldItem.recipe.id == newItem.recipe.id
        }

        override fun areContentsTheSame(oldItem: ActiveRecipeData, newItem: ActiveRecipeData): Boolean {
            return oldItem == newItem
        }
    }
}
