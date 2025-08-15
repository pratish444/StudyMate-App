package com.example.basictaskmanagerapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.basictaskmanagerapp.DataModel.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var tilTaskTitle: TextInputLayout
    private lateinit var etTaskTitle: TextInputEditText
    private lateinit var tilTaskDescription: TextInputLayout
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var chipGroupPriority: ChipGroup
    private lateinit var chipLow: Chip
    private lateinit var chipMedium: Chip
    private lateinit var chipHigh: Chip
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var btnSelectDueDate: MaterialButton
    private lateinit var btnSelectTime: MaterialButton
    private lateinit var btnSaveTask: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvSelectedDateTime: TextView

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var selectedDueDate: Date? = null
    private var isEditMode = false
    private var editTaskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        setupActionBar()
        initViews()
        initFirebase()
        setupClickListeners()
        setupAnimations()

        // Check if editing existing task
        editTaskId = intent.getStringExtra("task_id")
        isEditMode = editTaskId != null

        if (isEditMode) {
            loadTaskForEditing()
        }
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (isEditMode) "Edit Task" else "Add New Task"
            elevation = 0f
        }

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color)
    }

    private fun initViews() {
        tilTaskTitle = findViewById(R.id.tilTaskTitle)
        etTaskTitle = findViewById(R.id.etTaskTitle)
        tilTaskDescription = findViewById(R.id.tilTaskDescription)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        chipGroupPriority = findViewById(R.id.chipGroupPriority)
        chipLow = findViewById(R.id.chipLow)
        chipMedium = findViewById(R.id.chipMedium)
        chipHigh = findViewById(R.id.chipHigh)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        btnSelectDueDate = findViewById(R.id.btnSelectDueDate)
        btnSelectTime = findViewById(R.id.btnSelectTime)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime)

        // Set default priority selection
        chipMedium.isChecked = true

        // Setup category chips
        setupCategoryChips()
    }

    private fun setupCategoryChips() {
        val categories = listOf("Work", "Personal", "Shopping", "Health", "Education", "Other")

        categories.forEach { category ->
            val chip = Chip(this)
            chip.text = category
            chip.isCheckable = true
            chip.setChipBackgroundColorResource(R.color.chip_background)
            chip.setTextColor(ContextCompat.getColor(this, R.color.chip_text))
            chip.chipStrokeColor = ContextCompat.getColorStateList(this, R.color.chip_stroke)
            chip.chipStrokeWidth = 2f
            chipGroupCategory.addView(chip)
        }
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupClickListeners() {
        btnSaveTask.setOnClickListener {
            if (validateInputs()) {
                saveTask()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSelectDueDate.setOnClickListener {
            showDatePicker()
        }

        btnSelectTime.setOnClickListener {
            if (selectedDueDate != null) {
                showTimePicker()
            } else {
                Snackbar.make(btnSelectTime, "Please select a date first", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Priority chip selection
        chipGroupPriority.setOnCheckedStateChangeListener { _, checkedIds ->
            updatePriorityUI(checkedIds)
        }
    }

    private fun setupAnimations() {
        // Animate views on create
        val views = listOf(
            tilTaskTitle, tilTaskDescription, chipGroupPriority,
            chipGroupCategory, btnSelectDueDate, btnSelectTime, btnSaveTask
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }

    private fun updatePriorityUI(checkedIds: List<Int>) {
        val priorityColors = mapOf(
            R.id.chipHigh to R.color.priority_high,
            R.id.chipMedium to R.color.priority_medium,
            R.id.chipLow to R.color.priority_low
        )

        checkedIds.forEach { id ->
            val color = priorityColors[id] ?: R.color.priority_medium
            findViewById<Chip>(id).chipBackgroundColor =
                ContextCompat.getColorStateList(this, color)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Due Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDueDate = Date(selection)
            updateDateTimeDisplay()

            // Animate button
            btnSelectDueDate.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    btnSelectDueDate.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Due Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDueDate!!
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            selectedDueDate = calendar.time
            updateDateTimeDisplay()
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun updateDateTimeDisplay() {
        selectedDueDate?.let { date ->
            val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            tvSelectedDateTime.text = "Due: ${formatter.format(date)}"
            tvSelectedDateTime.visibility = View.VISIBLE

            // Enable time button
            btnSelectTime.isEnabled = true
            btnSelectTime.alpha = 1f
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val title = etTaskTitle.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            tilTaskTitle.error = "Please enter a task title"
            tilTaskTitle.isErrorEnabled = true
            etTaskTitle.requestFocus()
            shakeView(tilTaskTitle)
            isValid = false
        } else {
            tilTaskTitle.error = null
            tilTaskTitle.isErrorEnabled = false
        }

        if (description.isEmpty()) {
            tilTaskDescription.error = "Please enter a task description"
            tilTaskDescription.isErrorEnabled = true
            if (isValid) etTaskDescription.requestFocus()
            shakeView(tilTaskDescription)
            isValid = false
        } else {
            tilTaskDescription.error = null
            tilTaskDescription.isErrorEnabled = false
        }

        return isValid
    }

    private fun shakeView(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).apply {
            duration = 600
            start()
        }
    }

    private fun saveTask() {
        val title = etTaskTitle.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        val priority = when (chipGroupPriority.checkedChipId) {
            R.id.chipLow -> "Low"
            R.id.chipHigh -> "High"
            else -> "Medium"
        }

        // Get selected category
        val selectedCategoryChip = chipGroupCategory.findViewById<Chip>(chipGroupCategory.checkedChipId)
        val category = selectedCategoryChip?.text?.toString() ?: "Other"

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Snackbar.make(btnSaveTask, "User not authenticated", Snackbar.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = editTaskId ?: "",
            title = title,
            description = description,
            priority = priority,
            category = category,
            createdAt = if (isEditMode) Date() else Date(), // Keep original date if editing
            dueDate = selectedDueDate,
            userId = currentUser.uid,
            isCompleted = false
        )

        showLoading(true)

        val tasksRef = database.getReference("users").child(currentUser.uid).child("tasks")

        if (isEditMode && editTaskId != null) {
            // Update existing task
            tasksRef.child(editTaskId!!).setValue(task)
                .addOnSuccessListener {
                    showLoading(false)
                    showSuccessMessage("Task updated successfully! ✨")
                    finish()
                }
                .addOnFailureListener { error ->
                    showLoading(false)
                    showErrorMessage("Error updating task: ${error.message}")
                }
        } else {
            // Create new task
            val newTaskRef = tasksRef.push()
            task.id = newTaskRef.key ?: ""

            newTaskRef.setValue(task)
                .addOnSuccessListener {
                    showLoading(false)
                    showSuccessMessage("Task created successfully! 🎉")
                    finish()
                }
                .addOnFailureListener { error ->
                    showLoading(false)
                    showErrorMessage("Error creating task: ${error.message}")
                }
        }
    }

    private fun loadTaskForEditing() {
        // Implementation to load existing task data
        // This would fetch the task from database and populate the UI
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            btnSaveTask.isEnabled = false
            btnSaveTask.text = "Saving..."
            btnSaveTask.icon = null
        } else {
            progressBar.visibility = View.GONE
            btnSaveTask.isEnabled = true
            btnSaveTask.text = if (isEditMode) "Update Task" else "Save Task"
            btnSaveTask.setIconResource(R.drawable.ic_save)
        }
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Add confirmation dialog if user has entered data
        val hasData = etTaskTitle.text?.isNotEmpty() == true ||
                etTaskDescription.text?.isNotEmpty() == true

        if (hasData) {
            androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Discard Changes?")
                .setMessage("You have unsaved changes. Are you sure you want to go back?")
                .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Keep Editing", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}