package com.pizzaplanner.ui.active

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pizzaplanner.R
import com.pizzaplanner.data.models.*
import com.pizzaplanner.databinding.FragmentActiveBinding
import com.pizzaplanner.utils.RecipeTimeline
import com.pizzaplanner.utils.StepTimeline
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ActiveFragment : Fragment() {

    private var _binding: FragmentActiveBinding? = null
    private val binding get() = _binding!!
    
    // Mock active recipe data - in a real app this would come from a repository/database
    private var activeRecipe: PlannedRecipe? = null
    private var recipeTimeline: RecipeTimeline? = null
    private var currentStepIndex: Int = 0
    private var isPaused: Boolean = false
    
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
            // TODO: Navigate to timeline view or show timeline dialog
            Toast.makeText(requireContext(), "Timeline view coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadActiveRecipe() {
        // Mock data - in a real app this would load from database/repository
        // For demo purposes, create a sample active recipe
        createMockActiveRecipe()
    }
    
    private fun createMockActiveRecipe() {
        // This is mock data for demonstration
        // In a real app, this would be loaded from storage when a recipe is started from planning
        
        val mockRecipe = Recipe(
            id = "neapolitan",
            name = "Classic Neapolitan Pizza",
            description = "Traditional Neapolitan pizza dough",
            variables = listOf(
                RecipeVariable("rise_time", "Rise Time", 24.0, 1.0, 48.0, VariableType.INTEGER, "hours")
            ),
            steps = listOf(
                RecipeStep("mix", "Mix ingredients", "Combine flour, water, salt, and yeast", 30, timing = StepTiming.START),
                RecipeStep("knead", "Knead dough", "Knead until smooth and elastic", 15, timing = StepTiming.AFTER_PREVIOUS),
                RecipeStep("rise", "First rise", "Let dough rise in covered bowl", null, "rise_time * 60", StepTiming.AFTER_PREVIOUS),
                RecipeStep("divide", "Divide dough", "Divide into portions", 10, timing = StepTiming.AFTER_PREVIOUS),
                RecipeStep("final_rise", "Final rise", "Let portions rise", 120, timing = StepTiming.AFTER_PREVIOUS)
            ),
            difficulty = "Medium",
            totalTimeHours = 25
        )
        
        activeRecipe = PlannedRecipe(
            id = "active_recipe_1",
            recipeId = mockRecipe.id,
            recipeName = mockRecipe.name,
            targetCompletionTime = LocalDateTime.now().plusHours(2),
            startTime = LocalDateTime.now().minusMinutes(45),
            variableValues = mapOf("rise_time" to 24.0),
            status = RecipeStatus.IN_PROGRESS,
            currentStepIndex = 1 // Currently on "knead" step
        )
        
        // Create mock timeline
        val now = LocalDateTime.now()
        recipeTimeline = RecipeTimeline(
            recipe = mockRecipe,
            variableValues = mapOf("rise_time" to 24.0),
            startTime = now.minusMinutes(45),
            targetCompletionTime = now.plusHours(2),
            totalDurationMinutes = 165,
            steps = listOf(
                StepTimeline(
                    step = mockRecipe.steps[0],
                    processedDescription = "Combine flour, water, salt, and yeast",
                    processedTemperature = null,
                    durationMinutes = 30,
                    startTime = now.minusMinutes(45),
                    endTime = now.minusMinutes(15)
                ),
                StepTimeline(
                    step = mockRecipe.steps[1],
                    processedDescription = "Knead until smooth and elastic",
                    processedTemperature = null,
                    durationMinutes = 15,
                    startTime = now.minusMinutes(15),
                    endTime = now
                ),
                StepTimeline(
                    step = mockRecipe.steps[2],
                    processedDescription = "Let dough rise in covered bowl",
                    processedTemperature = null,
                    durationMinutes = 1440, // 24 hours
                    startTime = now,
                    endTime = now.plusHours(24)
                ),
                StepTimeline(
                    step = mockRecipe.steps[3],
                    processedDescription = "Divide into portions",
                    processedTemperature = null,
                    durationMinutes = 10,
                    startTime = now.plusHours(24),
                    endTime = now.plusHours(24).plusMinutes(10)
                ),
                StepTimeline(
                    step = mockRecipe.steps[4],
                    processedDescription = "Let portions rise",
                    processedTemperature = null,
                    durationMinutes = 120,
                    startTime = now.plusHours(24).plusMinutes(10),
                    endTime = now.plusHours(26).plusMinutes(10)
                )
            )
        )
        
        currentStepIndex = 1 // Currently on knead step
        startStepTimer()
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
        
        val currentStep = timeline.steps[currentStepIndex]
        if (currentStep.durationMinutes <= 0) return
        
        val now = LocalDateTime.now()
        val stepEndTime = currentStep.endTime
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
        activeRecipe = null
        recipeTimeline = null
        updateUI()
        Toast.makeText(requireContext(), "Recipe cancelled", Toast.LENGTH_SHORT).show()
    }
    
    private fun completeRecipe() {
        stepTimer?.cancel()
        activeRecipe = activeRecipe?.copy(status = RecipeStatus.COMPLETED)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Recipe Complete!")
            .setMessage("Congratulations! Your pizza dough is ready.")
            .setPositiveButton("Great!") { _, _ ->
                // Clear active recipe
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        stepTimer?.cancel()
        _binding = null
    }
}
