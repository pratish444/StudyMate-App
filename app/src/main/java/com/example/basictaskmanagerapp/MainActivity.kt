package com.example.basictaskmanagerapp

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout // You may need to add this import if Android Studio doesn't do it automatically
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basictaskmanagerapp.DataModel.Task
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabAddTask: ExtendedFloatingActionButton
    private lateinit var tvEmptyState: LinearLayout // ✅ FIXED: Changed from TextView to LinearLayout
    private lateinit var tvWelcome: TextView
    private lateinit var tvTaskCount: TextView

    private lateinit var database: FirebaseDatabase
    private lateinit var tasksRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private var tasksListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initFirebase()
        setupRecyclerView()
        setupSwipeToDelete()
        setupClickListeners()
        setupWelcomeMessage()

        // Customize LinearProgressIndicator programmatically
        val progressIndicator = findViewById<LinearProgressIndicator>(R.id.progressIndicator)
        progressIndicator.trackCornerRadius = 10
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBarMain)
        fabAddTask = findViewById(R.id.fabAddTask)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvTaskCount = findViewById(R.id.tvTaskCount)
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid ?: return
        tasksRef = database.getReference("users").child(uid).child("tasks")
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(tasks, object : TaskAdapter.TaskActionListener {
            override fun onToggleComplete(task: Task, isChecked: Boolean) {
                toggleTaskCompletion(task, isChecked)
            }

            override fun onDeleteTask(task: Task) {
                showDeleteConfirmation(task)
            }

            override fun onTaskClick(task: Task) {
                showTaskDetails(task)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator?.apply {
            addDuration = 300
            removeDuration = 300
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = tasks[position]
                deleteTaskWithUndo(task, position)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupClickListeners() {
        fabAddTask.setOnClickListener {
            animateFab()
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun setupWelcomeMessage() {
        val user = auth.currentUser
        val email = user?.email?.substringBefore("@") ?: "User"
        tvWelcome.text = "Welcome back, ${email.capitalize()}!"

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }

        val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        supportActionBar?.title = greeting
        supportActionBar?.subtitle = currentDate
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
        tasksListener?.let { tasksRef.removeEventListener(it) }
    }

    private fun loadTasks() {
        showLoading(true)
        tasks.clear()
        adapter.notifyDataSetChanged()

        tasksListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                tasks.clear()

                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null) {
                        task.id = taskSnapshot.key ?: ""
                        tasks.add(task)
                    }
                }

                tasks.sortWith(compareBy<Task> { it.isCompleted }
                    .thenBy {
                        when (it.priority) {
                            "High" -> 0
                            "Medium" -> 1
                            "Low" -> 2
                            else -> 3
                        }
                    }
                    .thenByDescending { it.createdAt })

                adapter.notifyDataSetChanged()
                updateUI()
                animateRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        tasksRef.addValueEventListener(tasksListener!!)
    }

    private fun updateUI() {
        val completedTasks = tasks.count { it.isCompleted }
        val totalTasks = tasks.size

        if (tasks.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvTaskCount.text = "No tasks yet"
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvTaskCount.text = "$completedTasks of $totalTasks completed"
        }

        if (totalTasks > 0) {
            val progress = (completedTasks.toFloat() / totalTasks * 100).toInt()
            animateProgress(progress)
        }
    }

    private fun animateRecyclerView() {
        recyclerView.alpha = 0f
        recyclerView.translationY = 50f
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    private fun animateProgress(progress: Int) {
        ObjectAnimator.ofInt(progress, 0, progress).apply {
            duration = 500
            start()
        }
    }

    private fun animateFab() {
        fabAddTask.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                fabAddTask.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun toggleTaskCompletion(task: Task, isChecked: Boolean) {
        val taskRef = tasksRef.child(task.id)
        val updates = mapOf<String, Any>(
            "isCompleted" to isChecked,
            "completedAt" to if (isChecked) System.currentTimeMillis() else 0
        )

        taskRef.updateChildren(updates)
            .addOnSuccessListener {
                val message = if (isChecked) "Task completed! 🎉" else "Task reopened"
                Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_green_light))
                    .show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to update: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete \"${task.title}\"?")
            .setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        tasksRef.child(task.id).removeValue()
            .addOnSuccessListener {
                Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        tasksRef.child(task.id).setValue(task)
                    }
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_light))
                    .show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to delete: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTaskWithUndo(task: Task, position: Int) {
        val deletedTask = tasks[position]
        tasks.removeAt(position)
        adapter.notifyItemRemoved(position)

        Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                tasks.add(position, deletedTask)
                adapter.notifyItemInserted(position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        tasksRef.child(task.id).removeValue()
                    }
                }
            })
            .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_light))
            .show()
    }

    private fun showTaskDetails(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        fabAddTask.isEnabled = !show
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.menu_clear_completed -> {
                clearCompletedTasks()
                true
            }
            R.id.menu_sort -> {
                showSortOptions()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearCompletedTasks() {
        val completedTasks = tasks.filter { it.isCompleted }
        if (completedTasks.isEmpty()) {
            Toast.makeText(this, "No completed tasks to clear", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Clear Completed Tasks")
            .setMessage("Delete ${completedTasks.size} completed tasks?")
            .setPositiveButton("Clear") { _, _ ->
                completedTasks.forEach { task ->
                    tasksRef.child(task.id).removeValue()
                }
                Toast.makeText(this, "Completed tasks cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSortOptions() {
        val options = arrayOf("Priority", "Date Created", "Completion Status")
        AlertDialog.Builder(this)
            .setTitle("Sort Tasks By")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sortTasksByPriority()
                    1 -> sortTasksByDate()
                    2 -> sortTasksByCompletion()
                }
            }
            .show()
    }

    private fun sortTasksByPriority() {
        tasks.sortWith(compareBy<Task> { it.isCompleted }
            .thenBy {
                when (it.priority) {
                    "High" -> 0
                    "Medium" -> 1
                    "Low" -> 2
                    else -> 3
                }
            })
        adapter.notifyDataSetChanged()
    }

    private fun sortTasksByDate() {
        tasks.sortWith(compareBy<Task> { it.isCompleted }.thenByDescending { it.createdAt })
        adapter.notifyDataSetChanged()
    }

    private fun sortTasksByCompletion() {
        tasks.sortBy { it.isCompleted }
        adapter.notifyDataSetChanged()
    }
}