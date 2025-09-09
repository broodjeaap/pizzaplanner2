package net.broodjeaap.pizzaplanner2.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.broodjeaap.pizzaplanner2.data.models.RecipeStep
import net.broodjeaap.pizzaplanner2.data.models.RecipeSubstep
import net.broodjeaap.pizzaplanner2.data.models.StepTiming
import net.broodjeaap.pizzaplanner2.databinding.ItemRecipeStepBinding
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils

class RecipeStepsAdapter(
    private val onStepClickListener: ((RecipeStep) -> Unit)? = null
) : ListAdapter<RecipeStep, RecipeStepsAdapter.StepViewHolder>(StepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRecipeStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class StepViewHolder(
        private val binding: ItemRecipeStepBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: RecipeStep, stepNumber: Int) {
            binding.apply {
                textViewStepNumber.text = stepNumber.toString()
                textViewStepTitle.text = step.name
                MarkdownUtils.setMarkdownText(textViewStepDescription, step.description)
                textViewStepTiming.text = when (step.timing) {
                    StepTiming.START -> "At start"
                    StepTiming.AFTER_PREVIOUS -> "After previous step"
                    StepTiming.PARALLEL -> "In parallel"
                    StepTiming.SCHEDULED -> "Scheduled timing"
                }
                
                // Show duration if available
                step.durationMinutes?.let { duration ->
                    textViewStepDuration.text = "Duration: ${duration} min"
                    textViewStepDuration.visibility = android.view.View.VISIBLE
                } ?: run {
                    textViewStepDuration.visibility = android.view.View.GONE
                }
                
                // Show notes if available
                step.notes?.let { notes ->
                    textViewStepNotesLabel.visibility = android.view.View.VISIBLE
                    textViewStepNotes.visibility = android.view.View.VISIBLE
                    MarkdownUtils.setMarkdownText(textViewStepNotes, notes)
                } ?: run {
                    textViewStepNotesLabel.visibility = android.view.View.GONE
                    textViewStepNotes.visibility = android.view.View.GONE
                }
                
                // Handle substeps
                if (step.substeps.isNotEmpty()) {
                    // Show substeps section
                    textViewSubstepsLabel.visibility = android.view.View.VISIBLE
                    recyclerViewSubsteps.visibility = android.view.View.VISIBLE
                    
                    // Set up substeps adapter
                    val substepsAdapter = SubstepsAdapter()
                    recyclerViewSubsteps.apply {
                        this.adapter = substepsAdapter
                        layoutManager = LinearLayoutManager(context)
                        isNestedScrollingEnabled = false
                    }
                    substepsAdapter.submitList(step.substeps)
                } else {
                    // Hide substeps section
                    textViewSubstepsLabel.visibility = android.view.View.GONE
                    recyclerViewSubsteps.visibility = android.view.View.GONE
                }
                
                // Set click listener
                root.setOnClickListener {
                    onStepClickListener?.invoke(step)
                }
            }
        }
    }

    private class StepDiffCallback : DiffUtil.ItemCallback<RecipeStep>() {
        override fun areItemsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem == newItem
        }
    }
    
    // Adapter for substeps
    class SubstepsAdapter : ListAdapter<RecipeSubstep, SubstepsAdapter.SubstepViewHolder>(SubstepDiffCallback()) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubstepViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(net.broodjeaap.pizzaplanner2.R.layout.item_recipe_substep, parent, false)
            return SubstepViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: SubstepViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
        
        inner class SubstepViewHolder(
            private val view: android.view.View
        ) : RecyclerView.ViewHolder(view) {
            
            private val textViewSubstepTitle = view.findViewById<android.widget.TextView>(net.broodjeaap.pizzaplanner2.R.id.textViewSubstepTitle)
            private val textViewSubstepDescription = view.findViewById<android.widget.TextView>(net.broodjeaap.pizzaplanner2.R.id.textViewSubstepDescription)
            
            fun bind(substep: RecipeSubstep) {
                textViewSubstepTitle.text = substep.name
                MarkdownUtils.setMarkdownText(textViewSubstepDescription, substep.description)
            }
        }
        
        private class SubstepDiffCallback : DiffUtil.ItemCallback<RecipeSubstep>() {
            override fun areItemsTheSame(oldItem: RecipeSubstep, newItem: RecipeSubstep): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: RecipeSubstep, newItem: RecipeSubstep): Boolean {
                return oldItem == newItem
            }
        }
    }
}
