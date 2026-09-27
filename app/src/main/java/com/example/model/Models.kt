package com.example.model

import androidx.compose.ui.graphics.Color

enum class Priority(val label: String, val colorHex: Long) {
    HIGH("High Importance", 0xFFEF4444),
    MEDIUM("Medium Importance", 0xFFF59E0B),
    LOW("Low Importance", 0xFF10B981);

    val composeColor: Color
        get() = Color(colorHex)
}

enum class DeadlineUrgency {
    OVERDUE,
    CRITICAL,    // Due in < 3 hours
    TODAY,       // Due today
    TOMORROW,    // Due tomorrow
    UPCOMING,    // Due in 2-7 days
    LATER        // Due in > 7 days
}

enum class NavDestination(val label: String) {
    DASHBOARD("Dashboard"),
    MY_TASKS("My Tasks"),
    CALENDAR("Calendar"),
    AI_SCHEDULE("AI Schedule"),
    PROGRESS("Progress"),
    IMPORTANT("Important"),
    COMPLETED("Completed"),
    SETTINGS("Settings")
}

enum class NotificationType {
    DEADLINE_APPROACHING,
    OVERDUE,
    COMPLETED,
    TASK_CREATED,
    IMPORTANT_REMINDER
}
