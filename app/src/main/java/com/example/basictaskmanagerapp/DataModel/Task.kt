package com.example.basictaskmanagerapp.DataModel

import java.util.Date

data class Task(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var priority: String = "Medium", // High, Medium, Low
    var createdAt: Date = Date(),
    var userId: String = "",
    var isCompleted: Boolean = false
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", "", "Medium", Date(), "", false)
}
