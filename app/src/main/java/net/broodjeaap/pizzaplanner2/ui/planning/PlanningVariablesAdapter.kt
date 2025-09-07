package net.broodjeaap.pizzaplanner2.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.broodjeaap.pizzaplanner2.data.models.RecipeVariable
import net.broodjeaap.pizzaplanner2.databinding.ItemPlanningVariableBinding

class PlanningVariablesAdapter(
    private val onVariableChanged: (String, Double) -> Unit
) : ListAdapter<PlanningVariableItem, PlanningVariablesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlanningVariableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPlanningVariableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlanningVariableItem) {
            binding.apply {
                textViewVariableName.text = item.variable.displayName
                textViewVariableDescription.text = item.variable.displayName // Use displayName as description
                
                // Set up slider
                sliderVariable.valueFrom = item.variable.minValue.toFloat()
                sliderVariable.valueTo = item.variable.maxValue.toFloat()
                sliderVariable.stepSize = 1.0f // Use fixed step size
                sliderVariable.value = item.currentValue.toFloat()
                
                // Update min/max labels
                textViewMinValue.text = formatValue(item.variable.minValue, item.variable.unit ?: "")
                textViewMaxValue.text = formatValue(item.variable.maxValue, item.variable.unit ?: "")
                
                // Update current value
                updateCurrentValue(item.currentValue, item.variable.unit ?: "")
                
                // Set up slider listener
                sliderVariable.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        val newValue = value.toDouble()
                        updateCurrentValue(newValue, item.variable.unit ?: "")
                        onVariableChanged(item.variable.name, newValue)
                    }
                }
            }
        }
        
        private fun updateCurrentValue(value: Double, unit: String) {
            binding.textViewCurrentValue.text = formatValue(value, unit)
        }
        
        private fun formatValue(value: Double, unit: String): String {
            return when (unit) {
                "hours" -> "${value.toInt()}h"
                "minutes" -> "${value.toInt()}m"
                "grams" -> "${value.toInt()}g"
                "pieces" -> "${value.toInt()}"
                "°C" -> "${value.toInt()}°C"
                "°F" -> "${value.toInt()}°F"
                else -> "${value.toInt()}${if (unit.isNotEmpty()) unit else ""}"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlanningVariableItem>() {
        override fun areItemsTheSame(oldItem: PlanningVariableItem, newItem: PlanningVariableItem): Boolean {
            return oldItem.variable.name == newItem.variable.name
        }

        override fun areContentsTheSame(oldItem: PlanningVariableItem, newItem: PlanningVariableItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class PlanningVariableItem(
    val variable: RecipeVariable,
    val currentValue: Double
)
