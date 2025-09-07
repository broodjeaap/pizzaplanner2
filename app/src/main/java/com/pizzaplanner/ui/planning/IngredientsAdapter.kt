package com.pizzaplanner.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizzaplanner.databinding.ItemIngredientBinding
import com.pizzaplanner.data.models.ProcessedIngredient

class IngredientsAdapter : ListAdapter<ProcessedIngredient, IngredientsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: ProcessedIngredient) {
            binding.apply {
                textViewIngredientName.text = ingredient.name
                
                // Format the amount with proper units
                val formattedAmount = when {
                    (ingredient.unit == "g" || ingredient.unit == "ml") && ingredient.amount >= 1000 -> {
                        String.format("%.2f kg", ingredient.amount / 1000.0)
                    }
                    (ingredient.unit == "g" || ingredient.unit == "ml") -> {
                        if (ingredient.amount < 1) {
                            String.format("%.2f %s", ingredient.amount, ingredient.unit)
                        } else {
                            String.format("%.0f %s", ingredient.amount, ingredient.unit)
                        }
                    }
                    else -> {
                        if (ingredient.amount < 1) {
                            String.format("%.2f %s", ingredient.amount, ingredient.unit)
                        } else if (ingredient.amount == ingredient.amount.toInt().toDouble()) {
                            String.format("%.0f %s", ingredient.amount, ingredient.unit)
                        } else {
                            String.format("%.1f %s", ingredient.amount, ingredient.unit)
                        }
                    }
                }
                
                textViewIngredientAmount.text = formattedAmount
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ProcessedIngredient>() {
        override fun areItemsTheSame(oldItem: ProcessedIngredient, newItem: ProcessedIngredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ProcessedIngredient, newItem: ProcessedIngredient): Boolean {
            return oldItem == newItem
        }
    }
}
