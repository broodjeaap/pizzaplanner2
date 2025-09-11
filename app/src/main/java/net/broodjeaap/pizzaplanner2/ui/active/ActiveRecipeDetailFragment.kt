package net.broodjeaap.pizzaplanner2.ui.active

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.data.models.*
import net.broodjeaap.pizzaplanner2.data.repository.PlannedRecipeRepository
import net.broodjeaap.pizzaplanner2.databinding.FragmentActiveBinding
import net.broodjeaap.pizzaplanner2.databinding.DialogTimelineBinding
import net.broodjeaap.pizzaplanner2.services.AlarmService
import net.broodjeaap.pizzaplanner2.ui.active.ActiveRecipeStepsAdapter
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils
import net.broodjeaap.pizzaplanner2.ui.active.ActiveRecipeDetailFragmentDirections
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ActiveRecipeDetailFragment : Fragment() {

    private var _binding: FragmentActiveBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: PlannedRecipeRepository
    private var activeRecipeData: PlannedRecipeRepository.ActiveRecipeData? = null
    private var recipeId: String = ""
    
    // Adapter for displaying all steps
    private lateinit var stepsAdapter: ActiveRecipeStepsAdapter
    
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
        
        // Initialize steps adapter
        stepsAdapter = ActiveRecipeStepsAdapter(0)
        
        // Get recipe ID from arguments
        recipeId = arguments?.getString("recipeId") ?: ""
        
        // Set up fragment result listener for substeps completion
        parentFragmentManager.setFragmentResultListener("substepsResult", this) { _, result ->
            if (result.getBoolean("stepCompleted", false)) {
                completeCurrentStep()
            }
        }
        
        setupClickListeners()
        loadRecipe()
        updateUI()
    }
    
    private fun setupClickListeners() {
        binding.buttonPauseResume.setOnClickListener {
            togglePauseResume()
        }
        
        binding.buttonStartStep.setOnClickListener {
            showSubsteps()
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
    
    private fun loadRecipe() {
        activeRecipeData = repository.getRecipe(recipeId)
        
        // Start timer if recipe is in progress and not paused
        activeRecipeData?.let { data ->
            if (data.status == RecipeStatus.IN_PROGRESS && !data.isPaused) {
                startStepTimer()
            }
            // Update the steps adapter with the current step index
            stepsAdapter = ActiveRecipeStepsAdapter(data.currentStepIndex)
            binding.recyclerViewAllSteps.adapter = stepsAdapter
            stepsAdapter.submitList(data.timeline.steps)
        }
    }
    
    private fun updateUI() {
        val data = activeRecipeData
        
        if (data == null) {
            showEmptyState()
            return
        }
        
        showActiveRecipe()
        updateRecipeInfo(data)
        updateCurrentStep(data)
        updateAllSteps(data)
        updateProgress(data)
    }
    
    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.cardActiveRecipe.visibility = View.GONE
        binding.cardCurrentStep.visibility = View.GONE
        binding.recyclerViewAllSteps.visibility = View.GONE
        binding.layoutRecipeActions.visibility = View.GONE
    }
    
    private fun showActiveRecipe() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.cardActiveRecipe.visibility = View.VISIBLE
        binding.cardCurrentStep.visibility = View.VISIBLE
        binding.recyclerViewAllSteps.visibility = View.VISIBLE
        binding.layoutRecipeActions.visibility = View.VISIBLE
    }
    
    private fun updateRecipeInfo(data: PlannedRecipeRepository.ActiveRecipeData) {
        binding.textViewRecipeName.text = data.recipe.recipeName
        binding.textViewRecipeStatus.text = when (data.status) {
            RecipeStatus.IN_PROGRESS -> if (data.isPaused) "Paused" else "In Progress"
            RecipeStatus.SCHEDULED -> "Scheduled"
            RecipeStatus.COMPLETED -> "Completed"
            RecipeStatus.CANCELLED -> "Cancelled"
            else -> "Unknown"
        }
        
        // Event name
        if (!data.recipe.eventName.isNullOrEmpty()) {
            binding.textViewEventName.text = data.recipe.eventName
            binding.textViewEventName.visibility = View.VISIBLE
        } else {
            binding.textViewEventName.visibility = View.GONE
        }
        
        // Update pause/resume button
        binding.buttonPauseResume.text = if (data.isPaused) getString(R.string.resume) else getString(R.string.pause)
        binding.buttonPauseResume.setIconResource(if (data.isPaused) R.drawable.ic_time else R.drawable.ic_pause)
    }
    
    
    private fun updateCurrentStep(data: PlannedRecipeRepository.ActiveRecipeData) {
        if (data.currentStepIndex >= data.timeline.steps.size) return
        
        val currentStep = data.timeline.steps.getOrNull(data.currentStepIndex) ?: return
        binding.textViewCurrentStepName.text = currentStep.step.name
        binding.textViewCurrentStepDescription.text = currentStep.processedDescription
        
        // Show timer if step has duration
        if (currentStep.durationMinutes > 0) {
            binding.layoutStepTimer.visibility = View.VISIBLE
            updateStepTimer()
        } else {
            binding.layoutStepTimer.visibility = View.GONE
        }
        
        // Update substeps button visibility
        binding.layoutSubstepsContainer.visibility = View.GONE
        binding.buttonCompleteStep.visibility = if (currentStep.step.substeps.isEmpty()) View.GONE else View.VISIBLE
        binding.buttonStartStep.text = if (currentStep.step.substeps.isEmpty()) getString(R.string.step_complete) else getString(R.string.start_step)
    }
    private fun updateProgress(data: PlannedRecipeRepository.ActiveRecipeData) {
        val totalSteps = data.timeline.steps.size
        val completedSteps = data.currentStepIndex
        val progressPercent = if (totalSteps > 0) {
            (completedSteps.toFloat() / totalSteps * 100).toInt()
        } else 0
        
        binding.textViewProgress.text = "Step ${data.currentStepIndex + 1} of $totalSteps"
        binding.progressIndicator.progress = progressPercent
        
        // Calculate time remaining
        val now = LocalDateTime.now()
        val targetTime = data.timeline.targetCompletionTime
        
        if (targetTime != null) {
            try {
                val timeRemaining = ChronoUnit.MINUTES.between(now, targetTime)
                binding.textViewTimeRemaining.text = when {
                    timeRemaining <= 0 -> "Ready!"
                    else -> "${formatTimeRemaining(timeRemaining)} remaining"
                }
            } catch (e: Exception) {
                binding.textViewTimeRemaining.text = "Time calculation error"
            }
        } else {
            binding.textViewTimeRemaining.text = "Unknown"
        }
    }
    
    private fun updateAllSteps(data: PlannedRecipeRepository.ActiveRecipeData) {
        // Set up the RecyclerView for all steps
        binding.recyclerViewAllSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepsAdapter
        }
        
        // Update the adapter with the current step index
        stepsAdapter = ActiveRecipeStepsAdapter(data.currentStepIndex)
        binding.recyclerViewAllSteps.adapter = stepsAdapter
        
        // Submit the list of steps
        stepsAdapter.submitList(data.timeline.steps)
    }
    
    private fun startStepTimer() {
        val data = activeRecipeData ?: return
        if (data.currentStepIndex >= data.timeline.steps.size) return
        
        val currentStep = data.timeline.steps.getOrNull(data.currentStepIndex) ?: return
        if (currentStep.durationMinutes <= 0) return
        
        val now = LocalDateTime.now()
        val stepEndTime = currentStep.endTime ?: return
        
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
                    showStepCompletionNotification()
                }
            }.start()
        } catch (e: Exception) {
            return
        }
    }
    
    private fun updateStepTimer() {
        if (stepTimeRemainingMs <= 0) {
            binding.textViewStepTimer.text = "00:00"
            return
        }
        
        val totalSeconds = stepTimeRemainingMs / 1000
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        
        if (hours > 0) {
            binding.textViewStepTimer.text = String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            binding.textViewStepTimer.text = String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    private fun formatTimeRemaining(minutes: Long): String {
        return when {
            minutes >= 60 -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "${hours}h ${remainingMinutes}m"
            }
            else -> "${minutes}m"
        }
    }
    
    private fun togglePauseResume() {
        val data = activeRecipeData ?: return
        val newPausedState = !data.isPaused
        
        repository.updateRecipe(recipeId) { currentData ->
            currentData.copy(isPaused = newPausedState)
        }
        
        if (newPausedState) {
            stepTimer?.cancel()
        } else {
            startStepTimer()
        }
        
        loadRecipe()
        updateUI()
        
        val message = if (newPausedState) "Recipe paused" else "Recipe resumed"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun completeCurrentStep() {
        val data = activeRecipeData ?: return
        stepTimer?.cancel()
        
        // Cancel alarm for the completed step
        if (data.currentStepIndex < data.alarmEvents.size) {
            val alarmEvent = data.alarmEvents[data.currentStepIndex]
            AlarmService.cancelAlarm(requireContext(), alarmEvent.id)
        }
        
        val newStepIndex = data.currentStepIndex + 1
        
        repository.updateRecipe(recipeId) { currentData ->
            currentData.copy(currentStepIndex = newStepIndex)
        }
        
        if (newStepIndex >= data.timeline.steps.size) {
            completeRecipe()
        } else {
            loadRecipe()
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
        
        // Cancel all alarms for this recipe
        activeRecipeData?.let { data ->
            AlarmService.cancelAllAlarms(requireContext(), data.alarmEvents)
        }
        
        repository.updateRecipe(recipeId) { currentData ->
            currentData.copy(status = RecipeStatus.CANCELLED)
        }
        
        Toast.makeText(requireContext(), "Recipe cancelled", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
    
    private fun completeRecipe() {
        stepTimer?.cancel()
        
        // Cancel all remaining alarms for this recipe
        activeRecipeData?.let { data ->
            AlarmService.cancelAllAlarms(requireContext(), data.alarmEvents)
        }
        
        repository.updateRecipe(recipeId) { currentData ->
            currentData.copy(status = RecipeStatus.COMPLETED)
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Recipe Complete!")
            .setMessage("Congratulations! Your pizza dough is ready.")
            .setPositiveButton("Great!") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showStepCompletionNotification() {
        val data = activeRecipeData ?: return
        if (data.currentStepIndex >= data.timeline.steps.size) return
        
        val currentStep = data.timeline.steps.getOrNull(data.currentStepIndex) ?: return
        
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
        val data = activeRecipeData ?: return
        
        val dialogBinding = DialogTimelineBinding.inflate(layoutInflater)
        
        dialogBinding.textViewRecipeNameTimeline.text = data.recipe.recipeName
        dialogBinding.textViewStartTimeTimeline.text = data.recipe.startTime.format(dateTimeFormatter)
        dialogBinding.textViewCompletionTimeTimeline.text = data.recipe.targetCompletionTime.format(dateTimeFormatter)
        
        val adapter = TimelineDialogAdapter(data.currentStepIndex)
        dialogBinding.recyclerViewTimelineSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
        
        adapter.submitList(data.timeline.steps)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.buttonCloseTimeline.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showSubsteps() {
        val data = activeRecipeData ?: return
        if (data.currentStepIndex >= data.timeline.steps.size) return
        
        val currentStep = data.timeline.steps.getOrNull(data.currentStepIndex) ?: return
        
        // Navigate to substeps fragment
        val action = ActiveRecipeDetailFragmentDirections.actionActiveRecipeDetailToSubsteps(
            currentStep.step.name,
            currentStep.step.substeps.toTypedArray(),
            HashMap(data.timeline.variableValues)
        )
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stepTimer?.cancel()
        _binding = null
    }
}
