package com.pizzaplanner.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizzaplanner.data.models.RecipeVariable
import com.pizzaplanner.data.models.VariableType
import com.pizzaplanner.databinding.ItemRecipeVariableBinding

class RecipeVariablesAdapter : ListAdapter<RecipeVariable, RecipeVariablesAdapter.VariableViewHolder>(VariableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariableViewHolder {
        val binding = ItemRecipeVariableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VariableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VariableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VariableViewHolder(
        private val binding: ItemRecipeVariableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(variable: RecipeVariable) {
            binding.apply {
                textViewVariableName.text = variable.displayName
                textViewVariableDescription.text = variable.displayName // Using displayName as description
                textViewVariableType.text = variable.type.name
                textViewVariableDefault.text = "Default: ${variable.defaultValue}"
                
                // Show min/max for numeric types
                if (variable.type == VariableType.INTEGER || 
                    variable.type == VariableType.DECIMAL) {
                    textViewVariableRange.text = "Range: ${variable.minValue} - ${variable.maxValue}"
                    textViewVariableRange.visibility = android.view.View.VISIBLE
                } else {
                    textViewVariableRange.visibility = android.view.View.GONE
                }
            }
        }
    }

    private class VariableDiffCallback : DiffUtil.ItemCallback<RecipeVariable>() {
        override fun areItemsTheSame(oldItem: RecipeVariable, newItem: RecipeVariable): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecipeVariable, newItem: RecipeVariable): Boolean {
            return oldItem == newItem
        }
    }
}
