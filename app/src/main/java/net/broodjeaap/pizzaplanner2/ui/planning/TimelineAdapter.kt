package net.broodjeaap.pizzaplanner2.ui.planning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.broodjeaap.pizzaplanner2.databinding.ItemTimelineStepBinding
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimelineAdapter : ListAdapter<TimelineStepItem, TimelineAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimelineStepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == 0, position == itemCount - 1)
    }

    class ViewHolder(
        private val binding: ItemTimelineStepBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

        fun bind(item: TimelineStepItem, isFirst: Boolean, isLast: Boolean) {
            binding.apply {
                // Configure timeline line visibility
                viewTimelineTop.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
                viewTimelineBottom.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
                
                // Set step information
                textViewStepDate.text = formatDate(item.startTime)
                textViewStepTime.text = item.startTime.format(timeFormatter)
                textViewStepName.text = item.stepName
                
                // Show duration if available
                if (item.durationMinutes > 0) {
                    textViewStepDuration.text = "(${formatDuration(item.durationMinutes)})"
                    textViewStepDuration.visibility = View.VISIBLE
                } else {
                    textViewStepDuration.visibility = View.GONE
                }
                
                // Show description if available
                if (item.description.isNotBlank()) {
                    MarkdownUtils.setMarkdownText(textViewStepDescription, item.description)
                    textViewStepDescription.visibility = View.VISIBLE
                } else {
                    textViewStepDescription.visibility = View.GONE
                }
                
                // Show alarm info if step has alarms
                if (item.hasAlarm) {
                    layoutAlarmInfo.visibility = View.VISIBLE
                    textViewAlarmInfo.text = "Alarm set"
                } else {
                    layoutAlarmInfo.visibility = View.GONE
                }
            }
        }
        
        private fun formatDuration(minutes: Int): String {
            return when {
                minutes < 60 -> "${minutes}m"
                minutes % 60 == 0 -> "${minutes / 60}h"
                else -> "${minutes / 60}h ${minutes % 60}m"
            }
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

    private class DiffCallback : DiffUtil.ItemCallback<TimelineStepItem>() {
        override fun areItemsTheSame(oldItem: TimelineStepItem, newItem: TimelineStepItem): Boolean {
            return oldItem.stepId == newItem.stepId
        }

        override fun areContentsTheSame(oldItem: TimelineStepItem, newItem: TimelineStepItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class TimelineStepItem(
    val stepId: String,
    val stepName: String,
    val description: String,
    val startTime: LocalDateTime,
    val durationMinutes: Int,
    val hasAlarm: Boolean
)
