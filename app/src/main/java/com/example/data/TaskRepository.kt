package com.example.data

import com.example.model.NotificationType
import com.example.model.Priority
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class TaskRepository(
    private val taskDao: TaskDao,
    private val notificationDao: NotificationDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(id, completed, completedAt)
    }

    suspend fun updatePriority(id: Long, priority: Priority) {
        taskDao.updateTaskPriority(id, priority.name)
    }

    suspend fun addNotification(title: String, message: String, type: NotificationType) {
        notificationDao.insertNotification(
            NotificationItem(
                title = title,
                message = message,
                type = type,
                timestampEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun dismissNotification(id: Long) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearNotifications() {
        notificationDao.clearAllNotifications()
    }

    suspend fun ensureInitialData() {
        val count = taskDao.getTaskCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val sampleTasks = listOf(
                TaskEntity(
                    title = "Physics Assignment: Thermodynamics",
                    subject = "Physics",
                    description = "Complete problem sets 4 & 5 on heat engines and entropy calculations.",
                    notes = "Check formula sheet page 12.",
                    estimatedHours = 2.5f,
                    dueDateEpochMs = now + TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(15),
                    priority = Priority.HIGH,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "Web Development Project: REST API",
                    subject = "Computer Science",
                    description = "Build deadline management backend endpoints with automated test suites.",
                    notes = "Push clean commit before 6 PM.",
                    estimatedHours = 4.0f,
                    dueDateEpochMs = now + TimeUnit.HOURS.toMillis(7) + TimeUnit.MINUTES.toMillis(45),
                    priority = Priority.HIGH,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "Math Quiz Preparation: Linear Algebra",
                    subject = "Mathematics",
                    description = "Review eigenvalues, eigenvectors, and orthogonal projections.",
                    notes = "Practice 10 sample problems from chapter 6.",
                    estimatedHours = 2.0f,
                    dueDateEpochMs = now + TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(4),
                    priority = Priority.MEDIUM,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "UI/UX Interactive Prototype Review",
                    subject = "Design",
                    description = "Flesh out mobile navigation components and user onboarding flow.",
                    notes = "Export high fidelity interactive preview link.",
                    estimatedHours = 3.0f,
                    dueDateEpochMs = now + TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(2),
                    priority = Priority.MEDIUM,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "Economics Term Paper Outline",
                    subject = "Economics",
                    description = "Draft structured thesis and primary bibliographic citations.",
                    notes = "Include recent inflation and monetary policy charts.",
                    estimatedHours = 1.5f,
                    dueDateEpochMs = now + TimeUnit.DAYS.toMillis(5),
                    priority = Priority.LOW,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "Software Engineering Final Sprint Demo",
                    subject = "Engineering",
                    description = "Prepare slide deck and live user story walkthrough for jury.",
                    notes = "Coordinate with team on presentation sequence.",
                    estimatedHours = 5.0f,
                    dueDateEpochMs = now + TimeUnit.DAYS.toMillis(8),
                    priority = Priority.HIGH,
                    isCompleted = false
                ),
                TaskEntity(
                    title = "Database Systems Lab 3 (Normalization)",
                    subject = "Computer Science",
                    description = "Resolve 3NF anomalies and construct indexing benchmarks.",
                    notes = "Submit PDF report with queries.",
                    estimatedHours = 2.0f,
                    dueDateEpochMs = now - TimeUnit.HOURS.toMillis(14), // Overdue
                    priority = Priority.HIGH,
                    isCompleted = false
                ),
                // Completed items for rich statistics
                TaskEntity(
                    title = "Algorithms Problem Set 2",
                    subject = "Computer Science",
                    description = "Dynamic programming coin change and longest common subsequence.",
                    notes = "All test cases passed with O(N) memory.",
                    estimatedHours = 3.0f,
                    dueDateEpochMs = now - TimeUnit.DAYS.toMillis(1),
                    priority = Priority.HIGH,
                    isCompleted = true,
                    completedAtEpochMs = now - TimeUnit.DAYS.toMillis(1)
                ),
                TaskEntity(
                    title = "Technical Writing Executive Summary",
                    subject = "Communications",
                    description = "Summarize renewable energy research papers into 1 page brief.",
                    notes = "Submitted to portal.",
                    estimatedHours = 1.0f,
                    dueDateEpochMs = now - TimeUnit.DAYS.toMillis(2),
                    priority = Priority.LOW,
                    isCompleted = true,
                    completedAtEpochMs = now - TimeUnit.DAYS.toMillis(2)
                ),
                TaskEntity(
                    title = "Calculus Weekly Quiz 4",
                    subject = "Mathematics",
                    description = "Multi-variable integrals and Green's theorem.",
                    notes = "Grade: 96%",
                    estimatedHours = 1.5f,
                    dueDateEpochMs = now - TimeUnit.DAYS.toMillis(3),
                    priority = Priority.MEDIUM,
                    isCompleted = true,
                    completedAtEpochMs = now - TimeUnit.DAYS.toMillis(3)
                )
            )
            taskDao.insertTasks(sampleTasks)

            // Seed initial notifications
            notificationDao.insertNotification(
                NotificationItem(
                    title = "Deadline Approaching",
                    message = "Physics Assignment is due in less than 4 hours.",
                    type = NotificationType.DEADLINE_APPROACHING,
                    timestampEpochMs = now - TimeUnit.MINUTES.toMillis(20)
                )
            )
            notificationDao.insertNotification(
                NotificationItem(
                    title = "Task Overdue",
                    message = "Database Systems Lab 3 requires immediate attention.",
                    type = NotificationType.OVERDUE,
                    timestampEpochMs = now - TimeUnit.HOURS.toMillis(2)
                )
            )
            notificationDao.insertNotification(
                NotificationItem(
                    title = "High Priority Submission",
                    message = "Web Development Project deadline is scheduled for tonight.",
                    type = NotificationType.IMPORTANT_REMINDER,
                    timestampEpochMs = now - TimeUnit.HOURS.toMillis(5)
                )
            )
        }
    }
}
