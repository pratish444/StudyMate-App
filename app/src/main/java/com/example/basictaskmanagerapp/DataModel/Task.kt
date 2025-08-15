package com.example.basictaskmanagerapp.DataModel

import com.google.firebase.database.Exclude
import java.util.Date

data class Task(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var priority: String = "Medium", // High, Medium, Low
    var category: String = "Other", // Work, Personal, Shopping, Health, Education, Other
    var createdAt: Date = Date(),
    var dueDate: Date? = null,
    var completedAt: Long = 0,
    var userId: String = "",
    var isCompleted: Boolean = false,
    var tags: List<String> = emptyList(),
    var reminder: Long = 0, // Timestamp for reminder
    var notes: String = "" // Additional notes
) {
    // No-arg constructor for Firebase
    constructor() : this(
        "", "", "", "Medium", "Other",
        Date(), null, 0, "", false,
        emptyList(), 0, ""
    )

    // Helper methods
    @Exclude
    fun isOverdue(): Boolean {
        return dueDate?.let { due ->
            !isCompleted && due.before(Date())
        } ?: false
    }

    @Exclude
    fun isDueToday(): Boolean {
        return dueDate?.let { due ->
            val today = Date()
            val dueCal = java.util.Calendar.getInstance().apply { time = due }
            val todayCal = java.util.Calendar.getInstance().apply { time = today }

            dueCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                    dueCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
        } ?: false
    }

    @Exclude
    fun isDueTomorrow(): Boolean {
        return dueDate?.let { due ->
            val tomorrow = java.util.Calendar.getInstance().apply {
                time = Date()
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }.time

            val dueCal = java.util.Calendar.getInstance().apply { time = due }
            val tomorrowCal = java.util.Calendar.getInstance().apply { time = tomorrow }

            dueCal.get(java.util.Calendar.YEAR) == tomorrowCal.get(java.util.Calendar.YEAR) &&
                    dueCal.get(java.util.Calendar.DAY_OF_YEAR) == tomorrowCal.get(java.util.Calendar.DAY_OF_YEAR)
        } ?: false
    }

    @Exclude
    fun getDaysUntilDue(): Int {
        return dueDate?.let { due ->
            val diffInMillis = due.time - Date().time
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
            diffInDays.toInt()
        } ?: Int.MAX_VALUE
    }

    @Exclude
    fun getPriorityScore(): Int {
        return when (priority) {
            "High" -> 3
            "Medium" -> 2
            "Low" -> 1
            else -> 0
        }
    }

    @Exclude
    fun getStatusText(): String {
        return when {
            isCompleted -> "Completed"
            isOverdue() -> "Overdue"
            isDueToday() -> "Due Today"
            isDueTomorrow() -> "Due Tomorrow"
            dueDate != null -> {
                val days = getDaysUntilDue()
                when {
                    days > 0 -> "Due in $days days"
                    days == 0 -> "Due Today"
                    else -> "Overdue by ${-days} days"
                }
            }
            else -> "No due date"
        }
    }

    @Exclude
    fun getCategoryIcon(): String {
        return when (category.lowercase()) {
            "work" -> "ic_work"
            "personal" -> "ic_person"
            "shopping" -> "ic_shopping"
            "health" -> "ic_health"
            "education" -> "ic_education"
            else -> "ic_category"
        }
    }

    @Exclude
    fun getCategoryColor(): String {
        return when (category.lowercase()) {
            "work" -> "category_work"
            "personal" -> "category_personal"
            "shopping" -> "category_shopping"
            "health" -> "category_health"
            "education" -> "category_education"
            else -> "category_other"
        }
    }

    // Convert to HashMap for Firebase
    @Exclude
    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "priority" to priority,
            "category" to category,
            "createdAt" to createdAt.time,
            "dueDate" to dueDate?.time,
            "completedAt" to completedAt,
            "userId" to userId,
            "isCompleted" to isCompleted,
            "tags" to tags,
            "reminder" to reminder,
            "notes" to notes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, id: String): Task {
            return Task(
                id = id,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                priority = map["priority"] as? String ?: "Medium",
                category = map["category"] as? String ?: "Other",
                createdAt = Date((map["createdAt"] as? Long) ?: System.currentTimeMillis()),
                dueDate = (map["dueDate"] as? Long)?.let { Date(it) },
                completedAt = map["completedAt"] as? Long ?: 0,
                userId = map["userId"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                reminder = map["reminder"] as? Long ?: 0,
                notes = map["notes"] as? String ?: ""
            )
        }

        // Sorting comparators
        val PRIORITY_COMPARATOR = Comparator<Task> { task1, task2 ->
            task2.getPriorityScore().compareTo(task1.getPriorityScore())
        }

        val DUE_DATE_COMPARATOR = Comparator<Task> { task1, task2 ->
            when {
                task1.dueDate == null && task2.dueDate == null -> 0
                task1.dueDate == null -> 1
                task2.dueDate == null -> -1
                else -> task1.dueDate!!.compareTo(task2.dueDate!!)
            }
        }

        val CREATED_DATE_COMPARATOR = Comparator<Task> { task1, task2 ->
            task2.createdAt.compareTo(task1.createdAt)
        }

        val COMPLETION_COMPARATOR = Comparator<Task> { task1, task2 ->
            task1.isCompleted.compareTo(task2.isCompleted)
        }
    }
}