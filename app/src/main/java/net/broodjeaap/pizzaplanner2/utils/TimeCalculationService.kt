package net.broodjeaap.pizzaplanner2.utils

import net.broodjeaap.pizzaplanner2.data.models.*
import java.time.LocalDateTime
import kotlin.math.roundToInt

class TimeCalculationService {
    
    fun calculateRecipeTimeline(
        recipe: Recipe,
        variableValues: Map<String, Double>,
        targetCompletionTime: LocalDateTime
    ): RecipeTimeline {
        val processedSteps = processSteps(recipe.steps, variableValues)
        val totalDurationMinutes = calculateTotalDuration(processedSteps)
        val startTime = targetCompletionTime.minusMinutes(totalDurationMinutes.toLong())
        
        val stepTimeline = calculateStepTimeline(processedSteps, startTime)
        val processedIngredients = calculateIngredients(recipe.ingredients, variableValues)
        
        return RecipeTimeline(
            recipe = recipe,
            variableValues = variableValues,
            startTime = startTime,
            targetCompletionTime = targetCompletionTime,
            totalDurationMinutes = totalDurationMinutes,
            steps = stepTimeline,
            ingredients = processedIngredients
        )
    }
    
    
    private fun processSteps(
        steps: List<RecipeStep>,
        variableValues: Map<String, Double>
    ): List<ProcessedStep> {
        return steps.map { step ->
            val duration = calculateStepDuration(step, variableValues)
            val processedDescription = substituteVariables(step.description, variableValues)
            val processedTemperature = step.temperature?.let { substituteVariables(it, variableValues) }
            
            ProcessedStep(
                step = step,
                durationMinutes = duration,
                processedDescription = processedDescription,
                processedTemperature = processedTemperature
            )
        }
    }
    
    private fun calculateStepDuration(
        step: RecipeStep,
        variableValues: Map<String, Double>
    ): Int {
        return when {
            step.durationMinutes != null -> step.durationMinutes
            step.durationFormula != null -> {
                evaluateFormula(step.durationFormula, variableValues).roundToInt()
            }
            else -> 0 // Default for steps without duration
        }
    }
    
    private fun evaluateFormula(formula: String, variables: Map<String, Double>): Double {
        var expression = formula
        
        // Replace variables with their values
        variables.forEach { (name, value) ->
            expression = expression.replace(name, value.toString())
        }
        
        // Simple expression evaluator for basic arithmetic
        return try {
            evaluateSimpleExpression(expression)
        } catch (e: Exception) {
            0.0 // Return 0 if formula evaluation fails
        }
    }
    
    private fun evaluateSimpleExpression(expression: String): Double {
        // Remove spaces
        val expr = expression.replace(" ", "")
        
        // Handle multiplication and division first
        var result = expr
        val multiplyDivideRegex = Regex("([0-9.]+)\\s*([*/])\\s*([0-9.]+)")
        
        while (multiplyDivideRegex.containsMatchIn(result)) {
            result = multiplyDivideRegex.replace(result) { matchResult ->
                val left = matchResult.groupValues[1].toDouble()
                val operator = matchResult.groupValues[2]
                val right = matchResult.groupValues[3].toDouble()
                
                when (operator) {
                    "*" -> (left * right).toString()
                    "/" -> (left / right).toString()
                    else -> matchResult.value
                }
            }
        }
        
        // Handle addition and subtraction
        val addSubtractRegex = Regex("([0-9.]+)\\s*([+-])\\s*([0-9.]+)")
        
        while (addSubtractRegex.containsMatchIn(result)) {
            result = addSubtractRegex.replace(result) { matchResult ->
                val left = matchResult.groupValues[1].toDouble()
                val operator = matchResult.groupValues[2]
                val right = matchResult.groupValues[3].toDouble()
                
                when (operator) {
                    "+" -> (left + right).toString()
                    "-" -> (left - right).toString()
                    else -> matchResult.value
                }
            }
        }
        
        return result.toDoubleOrNull() ?: 0.0
    }
    
    private fun substituteVariables(text: String, variables: Map<String, Double>): String {
        var result = text
        variables.forEach { (name, value) ->
            val placeholder = "{$name}"
            val displayValue = if (value == value.toInt().toDouble()) {
                value.toInt().toString()
            } else {
                String.format("%.1f", value)
            }
            result = result.replace(placeholder, displayValue)
        }
        return result
    }
    
    private fun calculateTotalDuration(steps: List<ProcessedStep>): Int {
        return steps.sumOf { it.durationMinutes }
    }
    
    
    private fun calculateStepTimeline(
        steps: List<ProcessedStep>,
        startTime: LocalDateTime
    ): List<StepTimeline> {
        val timeline = mutableListOf<StepTimeline>()
        var currentTime = startTime
        
        steps.forEach { processedStep ->
            val stepStartTime = currentTime
            val stepEndTime = currentTime.plusMinutes(processedStep.durationMinutes.toLong())
            
            timeline.add(
                StepTimeline(
                    step = processedStep.step,
                    processedDescription = processedStep.processedDescription,
                    processedTemperature = processedStep.processedTemperature,
                    durationMinutes = processedStep.durationMinutes,
                    startTime = stepStartTime,
                    endTime = stepEndTime
                )
            )
            
            currentTime = stepEndTime
        }
        
        return timeline
    }
    
    private fun calculateIngredients(
        ingredients: List<Ingredient>,
        variableValues: Map<String, Double>
    ): List<ProcessedIngredient> {
        val doughBalls = variableValues["dough_balls"] ?: 1.0
        val doughBallSize = variableValues["dough_ball_size_g"] ?: 250.0
        
        // Base reference: 1 dough ball of 250g = 250g total
        val baseTotalDoughWeight = 1.0 * 250.0
        val currentTotalDoughWeight = doughBalls * doughBallSize
        val scalingFactor = currentTotalDoughWeight / baseTotalDoughWeight
        
        return ingredients.map { ingredient ->
            // Scale all ingredients based on total dough weight
            val scaledAmount = ingredient.amount * scalingFactor
            
            ProcessedIngredient(
                name = ingredient.name,
                amount = scaledAmount,
                unit = ingredient.unit,
                category = ingredient.category
            )
        }
    }
    
}
