package com.example

import com.example.data.GeneratedScheduleItem
import com.example.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testGeneratedScheduleItemToTaskEntity() {
        val baseMs = 1700000000000L
        val item = GeneratedScheduleItem(
            title = "Review Machine Learning Loss Functions",
            subject = "Computer Science",
            description = "Deep dive into cross-entropy and hinge loss",
            notes = "Work through chapter 4",
            daysFromNow = 2,
            hourOfDay = 15,
            priority = Priority.HIGH,
            estimatedHours = 2.5f
        )

        val task = item.toTaskEntity(baseMs)
        assertEquals("Review Machine Learning Loss Functions", task.title)
        assertEquals("Computer Science", task.subject)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(2.5f, task.estimatedHours, 0.01f)
        assertNotNull(task.dueDateEpochMs)
    }
}
