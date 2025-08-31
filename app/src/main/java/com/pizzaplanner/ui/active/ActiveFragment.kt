package com.pizzaplanner.ui.active

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pizzaplanner.R
import com.pizzaplanner.data.models.*
import com.pizzaplanner.data.repository.PlannedRecipeRepository
import com.pizzaplanner.databinding.FragmentActiveBinding
import com.pizzaplanner.databinding.DialogTimelineBinding
import com.pizzaplanner.utils.RecipeTimeline
import com.pizzaplanner.utils.StepTimeline
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ActiveFragment : Fragment() {

    private var _binding: FragmentActiveBinding? = null
    private val binding get() = _binding!!
    
    private var activeRecipe: PlannedRecipe? = null
    private var recipeTimeline: RecipeTimeline? = null
    private var currentStepIndex: Int = 0
    private var isPaused: Boolean = false
    
    private lateinit var repository: PlannedRecipeRepository
    
    // Timer for step countdown
    private var stepTimer: CountDownTimer? = null
    private var stepTimeRemainingMs: Long = 0
    
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = PlannedRecipeRepository(requireContext())
        setupClickListeners()
        loadActiveRecipe()
        updateUI()
    }
    
    private fun setupClickListeners() {
        binding.buttonPauseResume.setOnClickListener {
            togglePauseResume()
        }
        
        binding.buttonCompleteStep.setOnClickListener {
            completeCurrentStep()
        }
        
        binding.buttonSkipStep.setOnClickListener {
            skipCurrentStep()
        }
        
        binding.buttonCancelRecipe.setOnClickListener {
            showCancelRecipeDialog()
        }
        
        binding.buttonViewTimeline.setOnClickListener {
            showTimelineDialog()
        }
    }
    
    private fun loadActiveRecipe() {
        if (repository.hasActiveRecipe()) {
            activeRecipe = repository.getActiveRecipe()
            recipeTimeline = repository.getActiveRecipeTimeline()
            currentStepIndex = repository.getCurrentStepIndex()
            isPaused = repository.isRecipePaused()
            
            // Update the status from repository
            activeRecipe?.let { recipe ->
                activeRecipe = recipe.copy(status = repository.getRecipeStatus())
            }
            
            // Start timer if recipe is in progress and not paused
            if (activeRecipe?.status == RecipeStatus.IN_PROGRESS && !isPaused) {
                startStepTimer()
            }
        } else {
            // No active recipe
            activeRecipe = null
            recipeTimeline = null
        }
    }
    
    
    private fun updateUI() {
        val recipe = activeRecipe
        val timeline = recipeTimeline
        
        if (recipe == null || timeline == null) {
            showEmptyState()
            return
        }
        
        showActiveRecipe()
        updateRecipeInfo(recipe, timeline)
        updateCurrentStep(timeline)
        updateNextStep(timeline)
        updateProgress(timeline)
    }
    
    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.cardActiveRecipe.visibility = View.GONE
        binding.cardCurrentStep.visibility = View.GONE
        binding.cardNextStep.visibility = View.GONE
        binding.layoutRecipeActions.visibility = View.GONE
    }
    
    private fun showActiveRecipe() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.cardActiveRecipe.visibility = View.VISIBLE
        binding.cardCurrentStep.visibility = View.VISIBLE
        binding.cardNextStep.visibility = View.VISIBLE
        binding.layoutRecipeActions.visibility = View.VISIBLE
    }
    
    private fun updateRecipeInfo(recipe: PlannedRecipe, timeline: RecipeTimeline) {
        binding.textViewRecipeName.text = recipe.recipeName
        binding.textViewRecipeStatus.text = when (recipe.status) {
            RecipeStatus.IN_PROGRESS -> if (isPaused) "Paused" else "In Progress"
            RecipeStatus.SCHEDULED -> "Scheduled"
            RecipeStatus.COMPLETED -> "Completed"
            RecipeStatus.CANCELLED -> "Cancelled"
            else -> "Unknown"
        }
        
        // Update pause/resume button
        binding.buttonPauseResume.text = if (isPaused) getString(R.string.resume) else getString(R.string.pause)
        binding.buttonPauseResume.setIconResource(if (isPaused) R.drawable.ic_time else R.drawable.ic_pause)
    }
    
    private fun updateCurrentStep(timeline: RecipeTimeline) {
        if (currentStepIndex >= timeline.steps.size) return
        
        val currentStep = timeline.steps[currentStepIndex]
        binding.textViewCurrentStepName.text = currentStep.step.name
        binding.textViewCurrentStepDescription.text = currentStep.processedDescription
        
        // Show timer if step has duration
        if (currentStep.durationMinutes > 0) {
            binding.layoutStepTimer.visibility = View.VISIBLE
            updateStepTimer()
        } else {
            binding.layoutStepTimer.visibility = View.GONE
        }
    }
    
    private fun updateNextStep(timeline: RecipeTimeline) {
        val nextStepIndex = currentStepIndex + 1
        if (nextStepIndex >= timeline.steps.size) {
            binding.cardNextStep.visibility = View.GONE
            return
        }
        
        val nextStep = timeline.steps[nextStepIndex]
        binding.textViewNextStepName.text = nextStep.step.name
        
        val timeUntilNext = ChronoUnit.MINUTES.between(LocalDateTime.now(), nextStep.startTime)
        binding.textViewNextStepTime.text = when {
            timeUntilNext <= 0 -> "Ready to start"
            timeUntilNext < 60 -> "Starts in ${timeUntilNext}m"
            else -> "Starts in ${timeUntilNext / 60}h ${timeUntilNext % 60}m"
        }
    }
    
    private fun updateProgress(timeline: RecipeTimeline) {
        val totalSteps = timeline.steps.size
        val completedSteps = currentStepIndex
        val progressPercent = (completedSteps.toFloat() / totalSteps * 100).toInt()
        
        binding.textViewProgress.text = "Step ${currentStepIndex + 1} of $totalSteps"
        binding.progressIndicator.progress = progressPercent
        
        // Calculate time remaining
        val now = LocalDateTime.now()
        val timeRemaining = ChronoUnit.MINUTES.between(now, timeline.targetCompletionTime)
        binding.textViewTimeRemaining.text = when {
            timeRemaining <= 0 -> "Ready!"
            timeRemaining < 60 -> "${timeRemaining}m remaining"
            else -> "${timeRemaining / 60}h ${timeRemaining % 60}m remaining"
        }
    }
    
    private fun startStepTimer() {
        val timeline = recipeTimeline ?: return
        if (currentStepIndex >= timeline.steps.size) return
        
        val currentStep = timeline.steps.getOrNull(currentStepIndex) ?: return
        if (currentStep.durationMinutes <= 0) return
        
        val now = LocalDateTime.now()
        val stepEndTime = currentStep.endTime ?: return // Add null check
        
        try {
            stepTimeRemainingMs = ChronoUnit.MILLIS.between(now, stepEndTime)
            
            if (stepTimeRemainingMs <= 0) return
            
            stepTimer?.cancel()
            stepTimer = object : CountDownTimer(stepTimeRemainingMs, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    stepTimeRemainingMs = millisUntilFinished
                    updateStepTimer()
                }
                
                override fun onFinish() {
                    stepTimeRemainingMs = 0
                    updateStepTimer()
                    // Auto-advance to next step or show completion notification
                    showStepCompletionNotification()
                }
            }.start()
        } catch (e: Exception) {
            // If there's any issue with time calculations, just skip the timer
            return
        }
    }
    
    private fun updateStepTimer() {
        if (stepTimeRemainingMs <= 0) {
            binding.textViewStepTimer.text = "00:00"
            return
        }
        
        val minutes = (stepTimeRemainingMs / 1000 / 60).toInt()
        val seconds = ((stepTimeRemainingMs / 1000) % 60).toInt()
        binding.textViewStepTimer.text = String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun togglePauseResume() {
        isPaused = !isPaused
        
        // Update repository
        repository.updatePausedStatus(isPaused)
        
        if (isPaused) {
            stepTimer?.cancel()
        } else {
            startStepTimer()
        }
        
        updateUI()
        
        val message = if (isPaused) "Recipe paused" else "Recipe resumed"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun completeCurrentStep() {
        stepTimer?.cancel()
        currentStepIndex++
        
        // Update repository
        repository.updateCurrentStepIndex(currentStepIndex)
        
        val timeline = recipeTimeline ?: return
        
        if (currentStepIndex >= timeline.steps.size) {
            // Recipe completed
            completeRecipe()
        } else {
            startStepTimer()
            updateUI()
            Toast.makeText(requireContext(), "Step completed!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun skipCurrentStep() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Skip Step")
            .setMessage("Are you sure you want to skip this step?")
            .setPositiveButton("Skip") { _, _ ->
                completeCurrentStep()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCancelRecipeDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Recipe")
            .setMessage("Are you sure you want to cancel this recipe? All progress will be lost.")
            .setPositiveButton("Cancel Recipe") { _, _ ->
                cancelRecipe()
            }
            .setNegativeButton("Keep Going", null)
            .show()
    }
    
    private fun cancelRecipe() {
        stepTimer?.cancel()
        
        // Update repository
        repository.updateRecipeStatus(RecipeStatus.CANCELLED)
        repository.clearActiveRecipe()
        
        activeRecipe = null
        recipeTimeline = null
        updateUI()
        Toast.makeText(requireContext(), "Recipe cancelled", Toast.LENGTH_SHORT).show()
    }
    
    private fun completeRecipe() {
        stepTimer?.cancel()
        
        // Update repository
        repository.updateRecipeStatus(RecipeStatus.COMPLETED)
        
        activeRecipe = activeRecipe?.copy(status = RecipeStatus.COMPLETED)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Recipe Complete!")
            .setMessage("Congratulations! Your pizza dough is ready.")
            .setPositiveButton("Great!") { _, _ ->
                // Clear active recipe
                repository.clearActiveRecipe()
                activeRecipe = null
                recipeTimeline = null
                updateUI()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showStepCompletionNotification() {
        val timeline = recipeTimeline ?: return
        if (currentStepIndex >= timeline.steps.size) return
        
        val currentStep = timeline.steps[currentStepIndex]
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Step Complete")
            .setMessage("Time's up for: ${currentStep.step.name}")
            .setPositiveButton("Next Step") { _, _ ->
                completeCurrentStep()
            }
            .setNegativeButton("Continue", null)
            .show()
    }
    
    private fun showTimelineDialog() {
        val recipe = activeRecipe ?: return
        val timeline = recipeTimeline ?: return
        
        val dialogBinding = DialogTimelineBinding.inflate(layoutInflater)
        
        // Set up dialog content
        dialogBinding.textViewRecipeNameTimeline.text = recipe.recipeName
        dialogBinding.textViewStartTimeTimeline.text = recipe.startTime.format(dateTimeFormatter)
        dialogBinding.textViewCompletionTimeTimeline.text = recipe.targetCompletionTime.format(dateTimeFormatter)
        
        // Set up RecyclerView
        val adapter = TimelineDialogAdapter(currentStepIndex)
        dialogBinding.recyclerViewTimelineSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        
        // Submit timeline steps to adapter
        adapter.submitList(timeline.steps)
        
        // Create and show dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        // Set up close button
        dialogBinding.buttonCloseTimeline.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stepTimer?.cancel()
        _binding = null
    }
}
