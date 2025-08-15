package com.example.basictaskmanagerapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.basictaskmanagerapp.DataModel.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: TextInputEditText
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var rgPriority: RadioGroup
    private lateinit var rbLow: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var rbHigh: RadioButton
    private lateinit var btnSaveTask: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        setupActionBar()
        initViews()
        initFirebase()
        setupClickListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Add New Task"
        }
    }

    private fun initViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        rgPriority = findViewById(R.id.rgPriority)
        rbLow = findViewById(R.id.rbLow)
        rbMedium = findViewById(R.id.rbMedium)
        rbHigh = findViewById(R.id.rbHigh)
        btnSaveTask = findViewById(R.id.btnSaveTask)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initFirebase() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupClickListeners() {
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun saveTask() {
        val title = etTaskTitle.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            etTaskTitle.error = "Please enter a task title"
            etTaskTitle.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etTaskDescription.error = "Please enter a task description"
            etTaskDescription.requestFocus()
            return
        }

        val priority = when (rgPriority.checkedRadioButtonId) {
            R.id.rbLow -> "Low"
            R.id.rbMedium -> "Medium"
            R.id.rbHigh -> "High"
            else -> "Medium"
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            title = title,
            description = description,
            priority = priority,
            createdAt = Date(),
            userId = currentUser.uid,
            isCompleted = false
        )

        showLoading(true)

        firestore.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                // update generated id into the doc
                documentReference.update("id", documentReference.id)
                showLoading(false)
                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                showLoading(false)
                Toast.makeText(this, "Error adding task: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            btnSaveTask.isEnabled = false
            btnSaveTask.text = "Saving..."
        } else {
            progressBar.visibility = View.GONE
            btnSaveTask.isEnabled = true
            btnSaveTask.text = "Save Task"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
