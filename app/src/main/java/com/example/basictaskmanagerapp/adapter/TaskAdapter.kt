package com.example.basictaskmanagerapp

import android.animation.ObjectAnimator
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.basictaskmanagerapp.DataModel.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val items: List<Task>,
    private val listener: TaskActionListener
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    interface TaskActionListener {
        fun onToggleComplete(task: Task, isChecked: Boolean)
        fun onDeleteTask(task: Task)
        fun onTaskClick(task: Task)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardTask)
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvTaskDescription)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val cbComplete: CheckBox = view.findViewById(R.id.cbTaskComplete)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteTask)
        val ivPriorityIcon: ImageView = view.findViewById(R.id.ivPriorityIcon)
        val viewPriorityLine: View = view.findViewById(R.id.viewPriorityLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = items[position]
        val context = holder.itemView.context

        // Animate card entry
        animateCardEntry(holder.cardView, position)

        // Set basic info
        holder.tvTitle.text = task.title
        holder.tvDesc.text = task.description

        // Format and set creation date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        holder.tvCreatedAt.text = dateFormat.format(task.createdAt)

        // Set priority with colors and icons
        setupPriority(holder, task.priority)

        // Set completion status
        setupCompletionStatus(holder, task)

        // Click listeners
        holder.cardView.setOnClickListener {
            listener.onTaskClick(task)
        }

        holder.cbComplete.setOnCheckedChangeListener { _, isChecked ->
            listener.onToggleComplete(task, isChecked)
            animateCompletion(holder, isChecked)
        }

        holder.ivDelete.setOnClickListener {
            animateDelete(holder.cardView) {
                listener.onDeleteTask(task)
            }
        }

        // Long press for additional options
        holder.cardView.setOnLongClickListener {
            // Show context menu or additional options
            true
        }
    }

    private fun setupPriority(holder: VH, priority: String) {
        val context = holder.itemView.context

        when (priority) {
            "High" -> {
                holder.tvPriority.text = "HIGH PRIORITY"
                holder.tvPriority.setBackgroundResource(R.drawable.priority_high_bg)
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high_text))
                holder.ivPriorityIcon.setImageResource(R.drawable.ic_priority_high)
                holder.ivPriorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_high))
                holder.viewPriorityLine.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high))
            }
            "Medium" -> {
                holder.tvPriority.text = "MEDIUM"
                holder.tvPriority.setBackgroundResource(R.drawable.priority_medium_bg)
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium_text))
                holder.ivPriorityIcon.setImageResource(R.drawable.ic_priority_medium)
                holder.ivPriorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium))
                holder.viewPriorityLine.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium))
            }
            "Low" -> {
                holder.tvPriority.text = "LOW"
                holder.tvPriority.setBackgroundResource(R.drawable.priority_low_bg)
                holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low_text))
                holder.ivPriorityIcon.setImageResource(R.drawable.ic_priority_low)
                holder.ivPriorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.priority_low))
                holder.viewPriorityLine.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low))
            }
        }
    }

    private fun setupCompletionStatus(holder: VH, task: Task) {
        val context = holder.itemView.context

        holder.cbComplete.isChecked = task.isCompleted

        if (task.isCompleted) {
            // Completed task styling
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvDesc.paintFlags = holder.tvDesc.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.cardView.alpha = 0.7f
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.completed_task_bg))

            // Show completion checkmark
            holder.cbComplete.setButtonDrawable(R.drawable.ic_check_completed)
        } else {
            // Active task styling
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvDesc.paintFlags = holder.tvDesc.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.cardView.alpha = 1.0f
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.active_task_bg))

            // Show regular checkbox
            holder.cbComplete.setButtonDrawable(R.drawable.ic_check_uncompleted)
        }
    }

    private fun animateCardEntry(cardView: CardView, position: Int) {
        cardView.alpha = 0f
        cardView.translationY = 100f
        cardView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .start()
    }

    private fun animateCompletion(holder: VH, isCompleted: Boolean) {
        val context = holder.itemView.context

        // Scale animation for checkbox
        holder.cbComplete.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction {
                holder.cbComplete.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        // Card animation
        if (isCompleted) {
            // Completed animation
            ObjectAnimator.ofFloat(holder.cardView, "alpha", 1f, 0.7f).apply {
                duration = 300
                start()
            }

            // Strike through animation
            holder.tvTitle.animate()
                .alpha(0.6f)
                .setDuration(300)
                .start()

            holder.tvDesc.animate()
                .alpha(0.6f)
                .setDuration(300)
                .start()
        } else {
            // Uncompleted animation
            ObjectAnimator.ofFloat(holder.cardView, "alpha", 0.7f, 1f).apply {
                duration = 300
                start()
            }

            holder.tvTitle.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

            holder.tvDesc.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }

    private fun animateDelete(cardView: CardView, onAnimationEnd: () -> Unit) {
        cardView.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .translationX(-cardView.width.toFloat())
            .setDuration(300)
            .withEndAction(onAnimationEnd)
            .start()
    }

    override fun getItemCount(): Int = items.size

    // Method to update task completion status with animation
    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        val position = items.indexOfFirst { it.id == taskId }
        if (position != -1) {
            items[position].isCompleted = isCompleted
            notifyItemChanged(position)
        }
    }

    // Method to remove task with animation
    fun removeTask(taskId: String) {
        val position = items.indexOfFirst { it.id == taskId }
        if (position != -1) {
            notifyItemRemoved(position)
        }
    }
}