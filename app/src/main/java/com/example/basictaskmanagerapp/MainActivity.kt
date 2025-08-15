package com.example.basictaskmanagerapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basictaskmanagerapp.DataModel.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddTask: FloatingActionButton

    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    private var tasksListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBarMain)
        fabAddTask = findViewById(R.id.fabAddTask)

        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = TaskAdapter(tasks, object : TaskAdapter.TaskActionListener {
            override fun onToggleComplete(task: Task, isChecked: Boolean) {
                toggleTaskCompletion(task, isChecked)
            }

            override fun onDeleteTask(task: Task) {
                showDeleteConfirmation(task)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadTasks()
        }
    }

    override fun onStop() {
        super.onStop()
        tasksListener?.remove()
    }

    private fun loadTasks() {
        val uid = auth.currentUser?.uid ?: return
        showLoading(true)
        tasks.clear()
        adapter.notifyDataSetChanged()

        tasksListener = firestore.collection("tasks")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                showLoading(false)
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                tasks.clear()
                for (doc in snapshot.documents) {
                    val task = doc.toObject(Task::class.java)
                    if (task != null) {
                        if (task.id.isEmpty()) task.id = doc.id
                        tasks.add(task)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun toggleTaskCompletion(task: Task, isChecked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference("task_status").child(uid).child(task.id)
        val status = mapOf(
            "completed" to isChecked,
            "completedAt" to if (isChecked) System.currentTimeMillis() else null
        )
        ref.setValue(status).addOnFailureListener {
            Toast.makeText(this, "Failed to update status: ${it.message}", Toast.LENGTH_LONG).show()
        }

        firestore.collection("tasks").document(task.id)
            .update("isCompleted", isChecked)
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_LONG).show()
            }

        database.getReference("task_status").child(uid).child(task.id)
            .removeValue()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
