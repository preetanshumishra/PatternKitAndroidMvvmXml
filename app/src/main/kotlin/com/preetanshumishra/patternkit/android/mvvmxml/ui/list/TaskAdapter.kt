package com.preetanshumishra.patternkit.android.mvvmxml.ui.list

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.preetanshumishra.patternkit.android.mvvmxml.R
import com.preetanshumishra.patternkit.android.mvvmxml.databinding.ItemTaskBinding
import com.preetanshumishra.patternkit.android.mvvmxml.model.Priority
import com.preetanshumishra.patternkit.android.mvvmxml.model.TaskItem
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * RecyclerView adapter using [ListAdapter] + DiffUtil — the XML-stack
 * equivalent of the Compose `LazyColumn` keyed `items`. Identity is the task
 * id; content changes (e.g. a completion toggle) are caught by the data-class
 * equality check in [Diff].
 */
class TaskAdapter(
    private val onClick: (TaskItem) -> Unit,
    private val onToggle: (TaskItem) -> Unit
) : ListAdapter<TaskItem, TaskAdapter.TaskViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dueFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

        fun bind(task: TaskItem) {
            binding.root.setOnClickListener { onClick(task) }
            binding.toggleButton.setOnClickListener { onToggle(task) }

            binding.toggleButton.setImageResource(
                if (task.isCompleted) R.drawable.ic_check_circle else R.drawable.ic_circle
            )

            binding.titleText.text = task.title
            binding.titleText.paintFlags = if (task.isCompleted) {
                binding.titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            val color = colorFor(task.priority)
            binding.priorityText.text = task.priority.displayName
            binding.priorityText.setTextColor(color)
            binding.priorityText.setBackgroundColor(ColorUtils.setAlphaComponent(color, 40))

            if (task.dueDate != null) {
                binding.dueText.visibility = android.view.View.VISIBLE
                binding.dueText.text = dueFormatter.format(task.dueDate)
            } else {
                binding.dueText.visibility = android.view.View.GONE
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(old: TaskItem, new: TaskItem) = old.id == new.id
        override fun areContentsTheSame(old: TaskItem, new: TaskItem) = old == new
    }

    companion object {
        fun colorFor(priority: Priority): Int = when (priority) {
            Priority.HIGH   -> Color.parseColor("#E53935")
            Priority.MEDIUM -> Color.parseColor("#FB8C00")
            Priority.LOW    -> Color.parseColor("#1E88E5")
        }
    }
}
