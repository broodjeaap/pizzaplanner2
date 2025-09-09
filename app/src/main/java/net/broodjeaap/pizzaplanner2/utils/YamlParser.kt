package net.broodjeaap.pizzaplanner2.utils

import net.broodjeaap.pizzaplanner2.data.models.*
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class YamlParser {
    
    private val yaml = Yaml()
    
    fun parseRecipes(inputStream: InputStream): List<Recipe> {
        val data = yaml.load<Map<String, Any>>(inputStream)
        val recipesData = data["recipes"] as? List<Map<String, Any>> ?: return emptyList()
        
        return recipesData.mapNotNull { recipeMap ->
            try {
                parseRecipe(recipeMap)
            } catch (e: Exception) {
                // Log error and skip malformed recipe
                null
            }
        }
    }
    
    private fun parseRecipe(recipeMap: Map<String, Any>): Recipe {
        val id = recipeMap["id"] as? String ?: generateId(recipeMap["name"] as String)
        val name = recipeMap["name"] as String
        val description = recipeMap["description"] as? String ?: ""
        val difficulty = recipeMap["difficulty"] as? String ?: "Medium"
        val totalTimeHours = (recipeMap["total_time_hours"] as? Number)?.toInt() ?: 24
        val imageUrl = recipeMap["image_url"] as? String
        
        val variables = parseVariables(recipeMap["variables"] as? List<Map<String, Any>> ?: emptyList())
        val steps = parseSteps(recipeMap["steps"] as? List<Map<String, Any>> ?: emptyList())
        val ingredients = parseIngredients(recipeMap["ingredients"] as? List<Map<String, Any>> ?: emptyList())
        
        return Recipe(
            id = id,
            name = name,
            description = description,
            variables = variables,
            steps = steps,
            ingredients = ingredients,
            imageUrl = imageUrl,
            difficulty = difficulty,
            totalTimeHours = totalTimeHours
        )
    }
    
    private fun parseVariables(variablesData: List<Map<String, Any>>): List<RecipeVariable> {
        return variablesData.map { varMap ->
            val name = varMap["name"] as String
            val displayName = varMap["display"] as? String ?: name.replace("_", " ").capitalize()
            val defaultValue = (varMap["default"] as Number).toDouble()
            val minValue = (varMap["min"] as Number).toDouble()
            val maxValue = (varMap["max"] as Number).toDouble()
            val typeString = varMap["type"] as? String ?: "integer"
            val unit = varMap["unit"] as? String
            
            val type = when (typeString.lowercase()) {
                "decimal", "float", "double" -> VariableType.DECIMAL
                "boolean", "bool" -> VariableType.BOOLEAN
                else -> VariableType.INTEGER
            }
            
            RecipeVariable(
                name = name,
                displayName = displayName,
                defaultValue = defaultValue,
                minValue = minValue,
                maxValue = maxValue,
                type = type,
                unit = unit
            )
        }
    }
    
    private fun parseSteps(stepsData: List<Map<String, Any>>): List<RecipeStep> {
        return stepsData.mapIndexed { index, stepMap ->
            val id = stepMap["id"] as? String ?: "step_$index"
            val name = stepMap["name"] as String
            val description = stepMap["description"] as? String ?: ""
            val durationMinutes = (stepMap["duration_minutes"] as? Number)?.toInt()
            val durationFormula = stepMap["duration_formula"] as? String
            val timingString = stepMap["timing"] as? String ?: "after_previous"
            val isOptional = stepMap["optional"] as? Boolean ?: false
            val temperature = stepMap["temperature"] as? String
            val notes = stepMap["notes"] as? String
            
            // Parse substeps if they exist
            val substeps = parseSubsteps(stepMap["substeps"] as? List<Map<String, Any>> ?: emptyList())
            
            val timing = when (timingString.lowercase()) {
                "start" -> StepTiming.START
                "parallel" -> StepTiming.PARALLEL
                "scheduled" -> StepTiming.SCHEDULED
                else -> StepTiming.AFTER_PREVIOUS
            }
            
            RecipeStep(
                id = id,
                name = name,
                description = description,
                durationMinutes = durationMinutes,
                durationFormula = durationFormula,
                timing = timing,
                isOptional = isOptional,
                temperature = temperature,
                notes = notes,
                substeps = substeps
            )
        }
    }
    
    private fun parseSubsteps(substepsData: List<Map<String, Any>>): List<RecipeSubstep> {
        return substepsData.map { substepMap ->
            val name = substepMap["name"] as String
            val description = substepMap["description"] as? String ?: ""
            
            RecipeSubstep(
                name = name,
                description = description
            )
        }
    }
    
    private fun parseIngredients(ingredientsData: List<Map<String, Any>>): List<Ingredient> {
        return ingredientsData.map { ingredientMap ->
            val name = ingredientMap["name"] as String
            val amount = (ingredientMap["amount"] as Number).toDouble()
            val unit = ingredientMap["unit"] as String
            val category = ingredientMap["category"] as? String
            
            Ingredient(
                name = name,
                amount = amount,
                unit = unit,
                category = category
            )
        }
    }
    
    private fun generateId(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
    }
}
