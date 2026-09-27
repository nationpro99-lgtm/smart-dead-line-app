package com.example.util

import com.example.model.DeadlineUrgency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    fun formatFriendlyDate(epochMs: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = epochMs }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(epochMs))

        val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Today at $timeStr"
            isTomorrow -> "Tomorrow at $timeStr"
            isYesterday -> "Yesterday at $timeStr"
            else -> {
                val dateFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                dateFormat.format(Date(epochMs))
            }
        }
    }

    fun formatShortDate(epochMs: Long): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(Date(epochMs))
    }

    fun getRemainingTimeText(dueEpochMs: Long, currentEpochMs: Long): String {
        val diff = dueEpochMs - currentEpochMs
        if (diff <= 0) {
            val overdueDiff = -diff
            val hours = TimeUnit.MILLISECONDS.toHours(overdueDiff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(overdueDiff) % 60
            val days = TimeUnit.MILLISECONDS.toDays(overdueDiff)
            return if (days > 0) {
                "Overdue by ${days}d ${hours % 24}h"
            } else {
                "Overdue by ${hours}h ${minutes}m"
            }
        }

        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return when {
            days > 7 -> "Due in $days days"
            days > 0 -> "${days}d ${hours}h remaining"
            hours > 0 -> "${hours}h ${minutes}m remaining"
            minutes > 0 -> "${minutes}m ${seconds}s remaining"
            else -> "${seconds}s remaining"
        }
    }

    fun getUrgency(dueEpochMs: Long, currentEpochMs: Long): DeadlineUrgency {
        val diff = dueEpochMs - currentEpochMs
        if (diff < 0) return DeadlineUrgency.OVERDUE
        if (diff <= TimeUnit.HOURS.toMillis(3)) return DeadlineUrgency.CRITICAL

        val now = Calendar.getInstance().apply { timeInMillis = currentEpochMs }
        val target = Calendar.getInstance().apply { timeInMillis = dueEpochMs }

        val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) return DeadlineUrgency.TODAY

        val tomorrow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        if (isTomorrow) return DeadlineUrgency.TOMORROW

        if (diff <= TimeUnit.DAYS.toMillis(7)) return DeadlineUrgency.UPCOMING
        return DeadlineUrgency.LATER
    }

    fun isSameDay(epochMs1: Long, epochMs2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = epochMs1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = epochMs2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getGreeting(hourOfDay: Int): String {
        return when (hourOfDay) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Hello"
        }
    }
}
