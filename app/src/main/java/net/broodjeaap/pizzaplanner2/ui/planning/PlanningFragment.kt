package net.broodjeaap.pizzaplanner2.ui.planning

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.broodjeaap.pizzaplanner2.data.models.Recipe
import net.broodjeaap.pizzaplanner2.data.models.PlannedRecipe
import net.broodjeaap.pizzaplanner2.data.models.RecipeStatus
import net.broodjeaap.pizzaplanner2.data.repository.PlannedRecipeRepository
import net.broodjeaap.pizzaplanner2.databinding.FragmentPlanningBinding
import net.broodjeaap.pizzaplanner2.services.AlarmService
import net.broodjeaap.pizzaplanner2.data.models.AlarmEvent
import net.broodjeaap.pizzaplanner2.data.models.AlarmType
import net.broodjeaap.pizzaplanner2.utils.TimeCalculationService
import net.broodjeaap.pizzaplanner2.utils.YamlParser
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
    private lateinit var ingredientsAdapter: IngredientsAdapter
    
    private var selectedRecipe: Recipe? = null
    private var passedRecipe: Recipe? = null
    private var targetDateTime: LocalDateTime? = null
    private val variableValues = mutableMapOf<String, Double>()
    
    // Dough ball configuration values
    private var numberOfDoughBalls: Int = 1
    private var doughBallSize: Int = 250
    
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
        setupDoughConfigurationListeners()
        
        // Check for passed recipe argument
        arguments?.getParcelable<Recipe>("recipe")?.let { recipe ->
            passedRecipe = recipe
            selectRecipe(recipe)
        } ?: run {
            // No recipe passed, clear any previously selected recipe
            // This handles the case when navigating directly to the planning tab
            if (passedRecipe != null) {
                resetRecipeSelection()
                passedRecipe = null
            }
        }
        
        // Clear arguments after processing to prevent navigation issues
        arguments = null
        
        updateUI()
        
        // Ensure the fragment doesn't block navigation
        // This is a workaround for the bottom navigation issue
        binding.root.isFocusable = false
        binding.root.isClickable = false
    }
    
    private fun setupRecyclerViews() {
        // Variables adapter
        variablesAdapter = PlanningVariablesAdapter { variableId, value ->
            // Constrain the value to the valid range for this variable
            val constrainedValue = constrainVariableValue(variableId, value)
            variableValues[variableId] = constrainedValue
            
            // Check if this is a time-related variable that affects the recipe duration
            if (isTimeVariable(variableId)) {
                updateTargetTimeForTimeVariables()
            }
            
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
        
        // Ingredients adapter
        ingredientsAdapter = IngredientsAdapter()
        
        binding.recyclerViewIngredients.apply {
            adapter = ingredientsAdapter
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
    
    private fun setupDoughConfigurationListeners() {
        // Number of dough balls slider
        binding.sliderDoughBalls.addOnChangeListener { _, value, _ ->
            numberOfDoughBalls = value.toInt()
            binding.textViewDoughBallsValue.text = "$numberOfDoughBalls"
            updateIngredients()
            if (targetDateTime != null) {
                updateTimeline()
            }
        }
        
        // Dough ball size slider
        binding.sliderDoughBallSize.addOnChangeListener { _, value, _ ->
            doughBallSize = value.toInt()
            binding.textViewDoughBallSizeValue.text = "${doughBallSize}g"
            updateIngredients()
            if (targetDateTime != null) {
                updateTimeline()
            }
        }
    }
    
    private fun showRecipeSelectionDialog() {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().assets.open("recipes/pizza_recipes_converted.yaml")
                val recipes = YamlParser().parseRecipes(inputStream)
                val recipeNames = recipes.map { it.name }.toTypedArray()
                
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select Recipe")
                    .setItems(recipeNames) { _, which ->
                        selectRecipe(recipes[which])
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading recipes: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun selectRecipe(recipe: Recipe) {
        selectedRecipe = recipe
        
        // Initialize variable values with defaults
        variableValues.clear()
        recipe.variables.forEach { variable ->
            variableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
        }
        
        // Set default target time to earliest possible time
        targetDateTime = calculateEarliestTargetTime(recipe)
        
        updateUI()
    }
    
    private fun resetRecipeSelection() {
        selectedRecipe = null
        variableValues.clear()
        updateUI()
    }
    
    private fun showDatePicker() {
        val recipe = selectedRecipe ?: return
        
        val calendar = Calendar.getInstance()
        val currentDate = targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
        
        calendar.set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
        
        // Calculate minimum date based on recipe duration
        val earliestTargetTime = calculateEarliestTargetTime(recipe)
        val minDateMillis = earliestTargetTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val currentTime = targetDateTime?.toLocalTime() ?: LocalTime.of(18, 0)
                val newTargetDateTime = LocalDateTime.of(selectedDate, currentTime)
                
                // Validate the selected date
                if (isTargetTimeValid(newTargetDateTime, recipe)) {
                    targetDateTime = newTargetDateTime
                    updateUI()
                } else {
                    // Show error message
                    Toast.makeText(requireContext(), "Selected time would result in steps in the past. Please choose a later time.", Toast.LENGTH_LONG).show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = minDateMillis
        }.show()
    }
    
    private fun showTimePicker() {
        val recipe = selectedRecipe ?: return
        val currentDate = targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
        
        val currentTime = targetDateTime?.toLocalTime() ?: LocalTime.of(18, 0)
        
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                val newTargetDateTime = LocalDateTime.of(currentDate, selectedTime)
                
                // Validate the selected time
                if (isTargetTimeValid(newTargetDateTime, recipe)) {
                    targetDateTime = newTargetDateTime
                    updateUI()
                } else {
                    // Show error message
                    Toast.makeText(requireContext(), "Selected time would result in steps in the past. Please choose a later time.", Toast.LENGTH_LONG).show()
                }
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
            
            // Show dough configuration
            binding.cardDoughConfig.visibility = View.VISIBLE
            
            // Show ingredients as soon as recipe is selected
            updateIngredients()
            
            // Show variables
            binding.cardVariables.visibility = View.VISIBLE
            val variableItems = recipe.variables.map { variable ->
                val currentValue = variableValues[variable.name] ?: variable.defaultValue
                val constrainedValue = constrainVariableValue(variable.name, currentValue)
                // Update the variableValues map with the constrained value
                variableValues[variable.name] = constrainedValue
                PlanningVariableItem(variable, constrainedValue)
            }
            variablesAdapter.submitList(variableItems)
        } else {
            binding.layoutSelectedRecipe.visibility = View.GONE
            binding.cardDoughConfig.visibility = View.GONE
            binding.cardIngredients.visibility = View.GONE
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
            // Update variable values with dough ball configuration
            val updatedVariableValues = variableValues.toMutableMap().apply {
                put("dough_balls", numberOfDoughBalls.toDouble())
                put("dough_ball_size_g", doughBallSize.toDouble())
            }
            
            // Ensure all recipe variables are present in the map with valid values
            recipe.variables.forEach { variable ->
                if (!updatedVariableValues.containsKey(variable.name)) {
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
                } else {
                    // Constrain the value to valid range
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, updatedVariableValues[variable.name] ?: variable.defaultValue)
                }
            }
            
            val timeline = timeCalculationService.calculateRecipeTimeline(
                recipe,
                updatedVariableValues,
                targetTime
            )
            
            // Check if any steps are in the past
            val hasPastSteps = timeline.steps.any { step ->
                step.startTime.isBefore(LocalDateTime.now())
            }
            
            // Show warning if there are past steps
            if (hasPastSteps) {
                binding.textViewTimelineWarning.visibility = View.VISIBLE
                binding.textViewTimelineWarning.text = "Warning: Some steps would start in the past. Please select a later completion time."
            } else {
                binding.textViewTimelineWarning.visibility = View.GONE
            }
            
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
            
            // Update ingredients
            if (timeline.ingredients.isNotEmpty()) {
                binding.cardIngredients.visibility = View.VISIBLE
                ingredientsAdapter.submitList(timeline.ingredients)
            } else {
                binding.cardIngredients.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error calculating timeline", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun savePlan() {
        val recipe = selectedRecipe ?: return
        val targetTime = targetDateTime ?: return
        
        // Validate the target time
        if (!isTargetTimeValid(targetTime, recipe)) {
            Toast.makeText(requireContext(), "Selected time would result in steps in the past. Please choose a later time.", Toast.LENGTH_LONG).show()
            return
        }
        
        try {
            // Update variable values with dough ball configuration
            val updatedVariableValues = variableValues.toMutableMap().apply {
                put("dough_balls", numberOfDoughBalls.toDouble())
                put("dough_ball_size_g", doughBallSize.toDouble())
            }
            
            // Ensure all recipe variables are present in the map with valid values
            recipe.variables.forEach { variable ->
                if (!updatedVariableValues.containsKey(variable.name)) {
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
                } else {
                    // Constrain the value to valid range
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, updatedVariableValues[variable.name] ?: variable.defaultValue)
                }
            }
            
            timeCalculationService.calculateRecipeTimeline(recipe, updatedVariableValues, targetTime)
            
            // TODO: Save to local storage/database
            Toast.makeText(requireContext(), "Plan saved successfully!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving plan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateIngredients() {
        val recipe = selectedRecipe ?: return
        
        try {
            // Update variable values with dough ball configuration
            val updatedVariableValues = variableValues.toMutableMap().apply {
                put("dough_balls", numberOfDoughBalls.toDouble())
                put("dough_ball_size_g", doughBallSize.toDouble())
            }
            
            // Ensure all recipe variables are present in the map with valid values
            recipe.variables.forEach { variable ->
                if (!updatedVariableValues.containsKey(variable.name)) {
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
                } else {
                    // Constrain the value to valid range
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, updatedVariableValues[variable.name] ?: variable.defaultValue)
                }
            }
            
            // Calculate ingredients using current time as target (doesn't matter for ingredients)
            val timeline = timeCalculationService.calculateRecipeTimeline(
                recipe, 
                updatedVariableValues, 
                LocalDateTime.now()
            )
            
            // Update ingredients
            if (timeline.ingredients.isNotEmpty()) {
                binding.cardIngredients.visibility = View.VISIBLE
                ingredientsAdapter.submitList(timeline.ingredients)
            } else {
                binding.cardIngredients.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error calculating ingredients", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startRecipe() {
        val recipe = selectedRecipe ?: return
        val targetTime = targetDateTime ?: return
        
        // Validate the target time
        if (!isTargetTimeValid(targetTime, recipe)) {
            Toast.makeText(requireContext(), "Selected time would result in steps in the past. Please choose a later time.", Toast.LENGTH_LONG).show()
            return
        }
        
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
            // Update variable values with dough ball configuration
            val updatedVariableValues = variableValues.toMutableMap().apply {
                put("dough_balls", numberOfDoughBalls.toDouble())
                put("dough_ball_size_g", doughBallSize.toDouble())
            }
            
            // Ensure all recipe variables are present in the map with valid values
            recipe.variables.forEach { variable ->
                if (!updatedVariableValues.containsKey(variable.name)) {
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
                } else {
                    // Constrain the value to valid range
                    updatedVariableValues[variable.name] = constrainVariableValue(variable.name, updatedVariableValues[variable.name] ?: variable.defaultValue)
                }
            }
            
            val timeline = timeCalculationService.calculateRecipeTimeline(
                recipe,
                updatedVariableValues,
                targetTime
            )
            
            // Get event name from input
            val eventName = binding.editTextEventName.text?.toString()?.trim()
            
            // Create planned recipe
            val plannedRecipe = PlannedRecipe(
                id = "active_recipe_${System.currentTimeMillis()}",
                recipeId = recipe.id,
                recipeName = recipe.name,
                targetCompletionTime = targetTime,
                startTime = timeline.startTime,
                variableValues = updatedVariableValues,
                status = RecipeStatus.IN_PROGRESS,
                currentStepIndex = 0,
                eventName = if (eventName.isNullOrEmpty()) null else eventName
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
            
            // Save recipe with alarm events
            plannedRecipeRepository.saveRecipe(
                plannedRecipe = plannedRecipe,
                recipeTimeline = timeline,
                alarmEvents = alarmEvents,
                currentStepIndex = 0,
                status = RecipeStatus.IN_PROGRESS,
                isPaused = false
            )
            
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
    
    private fun isTargetTimeValid(targetTime: LocalDateTime, recipe: Recipe): Boolean {
        // Calculate the timeline to get the start time
        val updatedVariableValues = variableValues.toMutableMap().apply {
            put("dough_balls", numberOfDoughBalls.toDouble())
            put("dough_ball_size_g", doughBallSize.toDouble())
        }
        
        // Ensure all recipe variables are present in the map with valid values
        recipe.variables.forEach { variable ->
            if (!updatedVariableValues.containsKey(variable.name)) {
                updatedVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
            } else {
                // Constrain the value to valid range
                updatedVariableValues[variable.name] = constrainVariableValue(variable.name, updatedVariableValues[variable.name] ?: variable.defaultValue)
            }
        }
        
        val timeline = timeCalculationService.calculateRecipeTimeline(
            recipe,
            updatedVariableValues,
            targetTime
        )
        
        // Check if the start time is in the past
        return timeline.startTime.isAfter(LocalDateTime.now()) || timeline.startTime.isEqual(LocalDateTime.now())
    }
    
    private fun calculateEarliestTargetTime(recipe: Recipe): LocalDateTime {
        // Initialize variable values with defaults if not already set
        val tempVariableValues = variableValues.toMutableMap()
        recipe.variables.forEach { variable ->
            if (!tempVariableValues.containsKey(variable.name)) {
                tempVariableValues[variable.name] = variable.defaultValue
            } else {
                // Constrain the value to valid range
                tempVariableValues[variable.name] = constrainVariableValue(variable.name, tempVariableValues[variable.name] ?: variable.defaultValue)
            }
        }
        
        // Ensure all recipe variables are present in the map with valid values
        recipe.variables.forEach { variable ->
            if (!tempVariableValues.containsKey(variable.name)) {
                tempVariableValues[variable.name] = constrainVariableValue(variable.name, variable.defaultValue)
            } else {
                // Constrain the value to valid range
                tempVariableValues[variable.name] = constrainVariableValue(variable.name, tempVariableValues[variable.name] ?: variable.defaultValue)
            }
        }
        
        // Add dough ball configuration values
        tempVariableValues["dough_balls"] = numberOfDoughBalls.toDouble()
        tempVariableValues["dough_ball_size_g"] = doughBallSize.toDouble()
        
        // Calculate the timeline with current variable values
        val timeline = timeCalculationService.calculateRecipeTimeline(
            recipe,
            tempVariableValues,
            LocalDateTime.now()
        )
        
        // Calculate how much time we need to add to make the start time valid
        val currentTime = LocalDateTime.now()
        val startTime = timeline.startTime
        
        // If start time is already in the future or now, return current time
        if (startTime.isAfter(currentTime) || startTime.isEqual(currentTime)) {
            return LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(1)
        }
        
        // Calculate the difference and add it to current time
        val duration = java.time.Duration.between(startTime, currentTime)
        return currentTime.plus(duration).withSecond(0).withNano(0).plusMinutes(1)
    }
    
    private fun isTimeVariable(variableId: String): Boolean {
        // Check if the variable name contains time-related keywords
        val timeKeywords = listOf("time", "duration", "hours", "minutes", "rise", "proof", "ferment", "cold", "warm")
        
        // Check if the variable is in the current recipe's variables
        val recipe = selectedRecipe ?: return false
        val variable = recipe.variables.find { it.name == variableId } ?: return false
        
        // Check if the variable unit is time-related
        val timeUnits = listOf("hours", "minutes", "days")
        val hasTimeUnit = variable.unit?.let { unit ->
            timeUnits.any { timeUnit -> unit.contains(timeUnit, ignoreCase = true) }
        } ?: false
        
        // Return true if either the name contains time keywords or the unit is time-related
        return timeKeywords.any { keyword ->
            variableId.lowercase().contains(keyword)
        } || hasTimeUnit
    }
    
    private fun updateTargetTimeForTimeVariables() {
        val recipe = selectedRecipe ?: return
        
        // Calculate new earliest target time with updated variables
        val newTargetTime = calculateEarliestTargetTime(recipe)
        
        // Always update the target time when time variables change
        // This ensures the schedule remains valid even if the user increases time variables
        targetDateTime = newTargetTime
        updateUI()
    }
    
    private fun constrainVariableValue(variableName: String, value: Double): Double {
        val recipe = selectedRecipe ?: return value
        val variable = recipe.variables.find { it.name == variableName } ?: return value
        
        // Constrain the value to be within the variable's min and max values
        return value.coerceIn(variable.minValue, variable.maxValue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure that the fragment doesn't block navigation
        // This might help with the bottom navigation issue
    }
}
