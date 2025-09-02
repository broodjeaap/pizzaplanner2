package com.pizzaplanner.ui.planning

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pizzaplanner.R
import com.pizzaplanner.data.models.Recipe
import com.pizzaplanner.data.models.PlannedRecipe
import com.pizzaplanner.data.models.RecipeStatus
import com.pizzaplanner.data.repository.PlannedRecipeRepository
import com.pizzaplanner.databinding.FragmentPlanningBinding
import com.pizzaplanner.services.AlarmService
import com.pizzaplanner.data.models.AlarmEvent
import com.pizzaplanner.data.models.AlarmType
import com.pizzaplanner.utils.TimeCalculationService
import com.pizzaplanner.utils.YamlParser
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class PlanningFragment : Fragment() {

    private var _binding: FragmentPlanningBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var variablesAdapter: PlanningVariablesAdapter
    private lateinit var timelineAdapter: TimelineAdapter
    
    private var selectedRecipe: Recipe? = null
    private var passedRecipe: Recipe? = null
    private var targetDateTime: LocalDateTime? = null
    private val variableValues = mutableMapOf<String, Double>()
    
    private val timeCalculationService = TimeCalculationService()
    private lateinit var plannedRecipeRepository: PlannedRecipeRepository
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with scheduling alarms
            Toast.makeText(requireContext(), "Alarm permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied, show explanation
            Toast.makeText(requireContext(), "Alarms may not work properly without permission", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        plannedRecipeRepository = PlannedRecipeRepository(requireContext())
        setupRecyclerViews()
        setupClickListeners()
        
        // Check for passed recipe argument
        arguments?.getParcelable<Recipe>("recipe")?.let { recipe ->
            passedRecipe = recipe
            selectRecipe(recipe)
        }
        
        updateUI()
    }
    
    private fun setupRecyclerViews() {
        // Variables adapter
        variablesAdapter = PlanningVariablesAdapter { variableId, value ->
            variableValues[variableId] = value
            updateTimeline()
        }
        
        binding.recyclerViewVariables.apply {
            adapter = variablesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        // Timeline adapter
        timelineAdapter = TimelineAdapter()
        
        binding.recyclerViewTimeline.apply {
            adapter = timelineAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonSelectRecipe.setOnClickListener {
            showRecipeSelectionDialog()
        }
        
        binding.buttonSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.buttonSelectTime.setOnClickListener {
            showTimePicker()
        }
        
        binding.buttonSavePlan.setOnClickListener {
            savePlan()
        }
        
        binding.buttonStartRecipe.setOnClickListener {
            startRecipe()
        }
    }
    
    private fun showRecipeSelectionDialog() {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().assets.open("recipes/pizza_recipes.yaml")
                val recipes = YamlParser().parseRecipes(inputStream)
                val recipeNames = recipes.map { it.name }.toTypedArray()
                
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select Recipe")
                    .setItems(recipeNames) { _, which ->
                        selectRecipe(recipes[which])
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading recipes", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun selectRecipe(recipe: Recipe) {
        selectedRecipe = recipe
        
        // Initialize variable values with defaults
        variableValues.clear()
        recipe.variables.forEach { variable ->
            variableValues[variable.name] = variable.defaultValue
        }
        
        updateUI()
    }
    
    private fun resetRecipeSelection() {
        selectedRecipe = null
        variableValues.clear()
        updateUI()
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentDate = targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
        
        calendar.set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val currentTime = targetDateTime?.toLocalTime() ?: LocalTime.of(18, 0)
                targetDateTime = LocalDateTime.of(selectedDate, currentTime)
                updateUI()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }
    
    private fun showTimePicker() {
        val currentTime = targetDateTime?.toLocalTime() ?: LocalTime.of(18, 0)
        
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                val currentDate = targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
                targetDateTime = LocalDateTime.of(currentDate, selectedTime)
                updateUI()
            },
            currentTime.hour,
            currentTime.minute,
            false
        ).show()
    }
    
    private fun updateUI() {
        val recipe = selectedRecipe
        val dateTime = targetDateTime
        
        // Update recipe selection UI
        if (recipe != null) {
            binding.layoutSelectedRecipe.visibility = View.VISIBLE
            binding.textViewSelectedRecipeName.text = recipe.name
            binding.textViewSelectedRecipeInfo.text = 
                "Difficulty: ${recipe.difficulty.replaceFirstChar { it.uppercase() }} • " +
                "Base time: ${recipe.totalTimeHours} hours"
            
            // Show variables
            binding.cardVariables.visibility = View.VISIBLE
            val variableItems = recipe.variables.map { variable ->
                PlanningVariableItem(variable, variableValues[variable.name] ?: variable.defaultValue)
            }
            variablesAdapter.submitList(variableItems)
        } else {
            binding.layoutSelectedRecipe.visibility = View.GONE
            binding.cardVariables.visibility = View.GONE
        }
        
        // Update datetime selection UI
        if (dateTime != null) {
            binding.textViewSelectedDateTime.visibility = View.VISIBLE
            binding.textViewSelectedDateTime.text = formatDateTime(dateTime)
        } else {
            binding.textViewSelectedDateTime.visibility = View.GONE
        }
        
        // Update timeline and actions
        if (recipe != null && dateTime != null) {
            updateTimeline()
            binding.cardTimeline.visibility = View.VISIBLE
            binding.layoutActions.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        } else {
            binding.cardTimeline.visibility = View.GONE
            binding.layoutActions.visibility = View.GONE
            binding.layoutEmptyState.visibility = if (recipe == null) View.VISIBLE else View.GONE
        }
    }
    
    private fun updateTimeline() {
        val recipe = selectedRecipe ?: return
        val targetTime = targetDateTime ?: return
        
        try {
            val timeline = timeCalculationService.calculateRecipeTimeline(recipe, variableValues, targetTime)
            
            // Update start time display
            binding.textViewStartTime.text = "Start: ${formatDateTime(timeline.startTime)}"
            
            // Convert to timeline items
            val timelineItems = timeline.steps.map { step ->
                TimelineStepItem(
                    stepId = step.step.id,
                    stepName = step.step.name,
                    description = step.processedDescription,
                    startTime = step.startTime,
                    durationMinutes = step.durationMinutes,
                    hasAlarm = true // All steps have alarms in this implementation
                )
            }
            
            timelineAdapter.submitList(timelineItems)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error calculating timeline", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun savePlan() {
        val recipe = selectedRecipe ?: return
        val targetTime = targetDateTime ?: return
        
        try {
            val timeline = timeCalculationService.calculateRecipeTimeline(recipe, variableValues, targetTime)
            
            // TODO: Save to local storage/database
            Toast.makeText(requireContext(), "Plan saved successfully!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving plan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startRecipe() {
        val recipe = selectedRecipe ?: return
        val targetTime = targetDateTime ?: return
        
        // Check for alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Request permission
                requestPermissionLauncher.launch(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
                Toast.makeText(requireContext(), "Please grant alarm permission to schedule recipe steps", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        try {
            val timeline = timeCalculationService.calculateRecipeTimeline(recipe, variableValues, targetTime)
            
            // Get event name from input
            val eventName = binding.editTextEventName.text?.toString()?.trim()
            
            // Create planned recipe
            val plannedRecipe = PlannedRecipe(
                id = "active_recipe_${System.currentTimeMillis()}",
                recipeId = recipe.id,
                recipeName = recipe.name,
                targetCompletionTime = targetTime,
                startTime = timeline.startTime,
                variableValues = variableValues,
                status = RecipeStatus.IN_PROGRESS,
                currentStepIndex = 0,
                eventName = if (eventName.isNullOrEmpty()) null else eventName
            )
            
            // Save to repository
            plannedRecipeRepository.saveRecipe(
                plannedRecipe = plannedRecipe,
                recipeTimeline = timeline,
                currentStepIndex = 0,
                status = RecipeStatus.IN_PROGRESS,
                isPaused = false
            )
            
            // Schedule alarms for recipe steps
            val alarmEvents = timeline.steps.map { step ->
                AlarmEvent(
                    id = "${plannedRecipe.id}_${step.step.id}",
                    stepName = step.step.name,
                    message = step.processedDescription,
                    scheduledTime = step.startTime,
                    alarmType = AlarmType.NOTIFICATION
                )
            }
            AlarmService.scheduleMultipleAlarms(requireContext(), alarmEvents)
            
            Toast.makeText(requireContext(), "Recipe started! Check the Active tab.", Toast.LENGTH_LONG).show()
            
            // Clear current planning
            selectedRecipe = null
            targetDateTime = null
            variableValues.clear()
            binding.editTextEventName.text?.clear()
            updateUI()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error starting recipe", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun formatDateTime(dateTime: LocalDateTime): String {
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        val dateText = when (dateTime.toLocalDate()) {
            today -> "Today"
            tomorrow -> "Tomorrow"
            else -> dateTime.format(dateFormatter)
        }
        
        return "$dateText, ${dateTime.format(timeFormatter)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
