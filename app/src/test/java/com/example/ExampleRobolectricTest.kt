package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DeadlineUrgency
import com.example.model.Priority
import com.example.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Deadline", appName)
    }

    @Test
    fun `test deadline urgency calculation`() {
        val now = 1000000000L
        val past = now - TimeUnit.HOURS.toMillis(2)
        val critical = now + TimeUnit.HOURS.toMillis(1)

        assertEquals(DeadlineUrgency.OVERDUE, DateTimeUtils.getUrgency(past, now))
        assertEquals(DeadlineUrgency.CRITICAL, DateTimeUtils.getUrgency(critical, now))
    }

    @Test
    fun `test remaining time text formatting`() {
        val now = 1000000000L
        val future = now + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(30)
        val remaining = DateTimeUtils.getRemainingTimeText(future, now)
        assertTrue(remaining.contains("5h 30m"))
    }
}
