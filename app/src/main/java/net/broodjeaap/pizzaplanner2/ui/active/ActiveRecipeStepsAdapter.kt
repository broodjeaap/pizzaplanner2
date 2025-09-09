package net.broodjeaap.pizzaplanner2.ui.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.databinding.ItemTimelineStepBinding
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils
import net.broodjeaap.pizzaplanner2.data.models.StepTimeline
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActiveRecipeStepsAdapter(
    private val currentStepIndex: Int
) : ListAdapter<StepTimeline, ActiveRecipeStepsAdapter.StepViewHolder>(StepDiffCallback()) {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemTimelineStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class StepViewHolder(
        private val binding: ItemTimelineStepBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stepTimeline: StepTimeline, position: Int) {
            binding.apply {
                // Step information
                textViewStepName.text = stepTimeline.step.name
                MarkdownUtils.setMarkdownText(textViewStepDescription, stepTimeline.processedDescription)
                textViewStepDescription.visibility = View.VISIBLE
                
                // Timing information
                textViewStepDate.text = formatDate(stepTimeline.startTime)
                textViewStepTime.text = stepTimeline.startTime.format(timeFormatter)
                
                // Duration formatting
                val durationText = when {
                    stepTimeline.durationMinutes < 60 -> "${stepTimeline.durationMinutes}m"
                    stepTimeline.durationMinutes % 60 == 0 -> "${stepTimeline.durationMinutes / 60}h"
                    else -> "${stepTimeline.durationMinutes / 60}h ${stepTimeline.durationMinutes % 60}m"
                }
                textViewStepDuration.text = "($durationText)"
                
                // Alarm information
                layoutAlarmInfo.visibility = View.GONE
                
                // Step status and visual indicators
                val now = LocalDateTime.now()
                val stepStatus = when {
                    position < currentStepIndex -> StepStatus.COMPLETED
                    position == currentStepIndex -> {
                        if (now.isBefore(stepTimeline.startTime)) StepStatus.UPCOMING
                        else if (now.isAfter(stepTimeline.endTime)) StepStatus.OVERDUE
                        else StepStatus.IN_PROGRESS
                    }
                    else -> StepStatus.UPCOMING
                }
                
                updateStepVisuals(stepStatus, position)
            }
        }
        
        private fun updateStepVisuals(status: StepStatus, position: Int) {
            val context = binding.root.context
            
            // Update status colors
            when (status) {
                StepStatus.COMPLETED -> {
                    binding.textViewStepName.setTextColor(
                        ContextCompat.getColor(context, R.color.difficulty_easy)
                    )
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.difficulty_easy)
                }
                StepStatus.IN_PROGRESS -> {
                    binding.textViewStepName.setTextColor(
                        ContextCompat.getColor(context, R.color.pizza_orange)
                    )
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.pizza_orange)
                }
                StepStatus.OVERDUE -> {
                    binding.textViewStepName.setTextColor(
                        ContextCompat.getColor(context, R.color.difficulty_hard)
                    )
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.difficulty_hard)
                }
                StepStatus.UPCOMING -> {
                    binding.textViewStepName.setTextColor(
                        ContextCompat.getColor(context, R.color.dark_gray)
                    )
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.dark_gray)
                }
            }
            
            // Handle timeline lines visibility
            binding.viewTimelineTop.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.viewTimelineBottom.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
        }
        
        private fun formatDate(dateTime: LocalDateTime): String {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            val stepDate = dateTime.toLocalDate()
            
            return when {
                stepDate == today -> "Today"
                stepDate == today.plusDays(1) -> "Tomorrow"
                stepDate == today.minusDays(1) -> "Yesterday"
                else -> dateTime.format(dateFormatter)
            }
        }
    }
    
    private enum class StepStatus {
        COMPLETED, IN_PROGRESS, OVERDUE, UPCOMING
    }
}

class StepDiffCallback : DiffUtil.ItemCallback<StepTimeline>() {
    override fun areItemsTheSame(oldItem: StepTimeline, newItem: StepTimeline): Boolean {
        return oldItem.step.id == newItem.step.id
    }

    override fun areContentsTheSame(oldItem: StepTimeline, newItem: StepTimeline): Boolean {
        return oldItem == newItem
    }
}