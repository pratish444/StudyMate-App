package com.example.basictaskmanagerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.basictaskmanagerapp.DataModel.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TaskAdapter(
    private val items: List<Task>,
    private val listener: TaskActionListener
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    interface TaskActionListener {
        fun onToggleComplete(task: Task, isChecked: Boolean)
        fun onDeleteTask(task: Task)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvTaskDescription)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val cbComplete: CheckBox = view.findViewById(R.id.cbTaskComplete)
        val ivDelete: ImageView = view.findViewById(R.id.ivDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = items[position]
        holder.tvTitle.text = task.title
        holder.tvDesc.text = task.description
        holder.tvPriority.text = task.priority

        // Set checkbox from Firestore field first
        holder.cbComplete.isChecked = task.isCompleted

        // Check latest from Realtime DB
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && task.id.isNotEmpty()) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("task_status")
                .child(uid)
                .child(task.id)
            ref.get().addOnSuccessListener { snap ->
                val completed = snap.child("completed").value as? Boolean
                if (completed != null) {
                    holder.cbComplete.isChecked = completed
                }
            }
        }

        // Toggle complete
        holder.cbComplete.setOnCheckedChangeListener { _, isChecked ->
            listener.onToggleComplete(task, isChecked)
        }

        // Delete task
        holder.ivDelete.setOnClickListener {
            listener.onDeleteTask(task)
        }
    }

    override fun getItemCount(): Int = items.size
}
