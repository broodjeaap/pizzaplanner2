package com.pizzaplanner.ui.active

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pizzaplanner.R
import com.pizzaplanner.databinding.ItemTimelineStepDialogBinding
import com.pizzaplanner.utils.MarkdownUtils
import com.pizzaplanner.data.models.StepTimeline
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimelineDialogAdapter(
    private val currentStepIndex: Int
) : ListAdapter<StepTimeline, TimelineDialogAdapter.TimelineStepViewHolder>(TimelineStepDiffCallback()) {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineStepViewHolder {
        val binding = ItemTimelineStepDialogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimelineStepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineStepViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class TimelineStepViewHolder(
        private val binding: ItemTimelineStepDialogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stepTimeline: StepTimeline, position: Int) {
            binding.apply {
                // Step information
                textViewStepName.text = stepTimeline.step.name
                MarkdownUtils.setMarkdownText(textViewStepDescription, stepTimeline.processedDescription)
                
                // Timing information
                textViewStartDate.text = formatDate(stepTimeline.startTime)
                textViewStartTime.text = stepTimeline.startTime.format(timeFormatter)
                textViewEndTime.text = stepTimeline.endTime.format(timeFormatter)
                
                // Duration formatting
                val durationText = when {
                    stepTimeline.durationMinutes < 60 -> "${stepTimeline.durationMinutes}m"
                    stepTimeline.durationMinutes % 60 == 0 -> "${stepTimeline.durationMinutes / 60}h"
                    else -> "${stepTimeline.durationMinutes / 60}h ${stepTimeline.durationMinutes % 60}m"
                }
                textViewDuration.text = durationText
                
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
            
            // Update status text and colors
            when (status) {
                StepStatus.COMPLETED -> {
                    binding.textViewStepStatus.text = "Completed"
                    binding.textViewStepStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.difficulty_easy)
                    )
                    binding.textViewStepStatus.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.light_gray)
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.difficulty_easy)
                }
                StepStatus.IN_PROGRESS -> {
                    binding.textViewStepStatus.text = "In Progress"
                    binding.textViewStepStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.pizza_orange)
                    )
                    binding.textViewStepStatus.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.light_gray)
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.pizza_orange)
                }
                StepStatus.OVERDUE -> {
                    binding.textViewStepStatus.text = "Overdue"
                    binding.textViewStepStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.difficulty_hard)
                    )
                    binding.textViewStepStatus.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.light_gray)
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.difficulty_hard)
                }
                StepStatus.UPCOMING -> {
                    binding.textViewStepStatus.text = "Upcoming"
                    binding.textViewStepStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.dark_gray)
                    )
                    binding.textViewStepStatus.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.medium_gray)
                    binding.viewTimelineDot.backgroundTintList = 
                        ContextCompat.getColorStateList(context, R.color.dark_gray)
                }
            }
            
            // Handle timeline lines visibility
            binding.viewTopLine.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.viewBottomLine.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
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

class TimelineStepDiffCallback : DiffUtil.ItemCallback<StepTimeline>() {
    override fun areItemsTheSame(oldItem: StepTimeline, newItem: StepTimeline): Boolean {
        return oldItem.step.id == newItem.step.id
    }

    override fun areContentsTheSame(oldItem: StepTimeline, newItem: StepTimeline): Boolean {
        return oldItem == newItem
    }
}
