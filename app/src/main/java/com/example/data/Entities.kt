package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.NotificationType
import com.example.model.Priority

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val description: String = "",
    val notes: String = "",
    val estimatedHours: Float = 1.5f,
    val dueDateEpochMs: Long,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAtEpochMs: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class UserProfile(
    val name: String = "Anshuman",
    val email: String = "anshuman@example.com",
    val avatarUrl: String = "",
    val isLoggedIn: Boolean = true
)
