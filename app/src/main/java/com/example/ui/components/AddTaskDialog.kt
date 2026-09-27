package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TaskEntity
import com.example.model.Priority
import com.example.ui.theme.Indigo600
import com.example.util.DateTimeUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    initialTask: TaskEntity?,
    initialDateMs: Long,
    onDismiss: () -> Unit,
    onOpenAiScheduler: (() -> Unit)? = null,
    onSave: (
        id: Long,
        title: String,
        subject: String,
        description: String,
        notes: String,
        dueDateMs: Long,
        priority: Priority,
        estimatedHours: Float
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var subject by remember { mutableStateOf(initialTask?.subject ?: "Computer Science") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var notes by remember { mutableStateOf(initialTask?.notes ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.MEDIUM) }
    var estimatedHours by remember { mutableFloatStateOf(initialTask?.estimatedHours ?: 2.0f) }

    // Due date state
    var dueDateMs by remember {
        mutableLongStateOf(
            initialTask?.dueDateEpochMs ?: run {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = initialDateMs
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 0)
                }
                cal.timeInMillis
            }
        )
    }

    var titleError by remember { mutableStateOf(false) }

    val categoryPresets = listOf(
        "Computer Science", "Physics", "Mathematics", "UI/UX Design",
        "Economics", "Engineering", "Literature", "Chemistry"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("add_task_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Indigo600.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (initialTask != null) Icons.Filled.Edit else Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = if (initialTask != null) "Edit Task" else "Add New Task",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional AI Shortcut
                if (onOpenAiScheduler != null && initialTask == null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Indigo600.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Indigo600.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onOpenAiScheduler()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Need a full schedule? Plan with Gemini AI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Indigo600
                                )
                            }
                            Text("Try ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Indigo600)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Task Name
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    label = { Text("Task / Assignment Name *") },
                    placeholder = { Text("e.g., Final Physics Lab Report") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Task name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject / Category
                Text(
                    text = "Subject / Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryPresets.forEach { cat ->
                        val isSelected = subject.equals(cat, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Indigo600 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { subject = cat }
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smart Priority Selection
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { p ->
                        val isSelected = priority == p
                        val pColor = p.composeColor
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) pColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { priority = p }
                                .testTag("priority_option_${p.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) pColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(pColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = p.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Deadline Date Presets & Custom
                Text(
                    text = "Deadline Date: ${DateTimeUtils.formatShortDate(dueDateMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val nowCal = Calendar.getInstance()
                    val dateOptions = listOf(
                        "Today" to 0,
                        "Tomorrow" to 1,
                        "In 3 Days" to 3,
                        "In 1 Week" to 7
                    )

                    dateOptions.forEach { (label, daysAhead) ->
                        val targetCal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, daysAhead)
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 0)
                        }
                        val isSelected = DateTimeUtils.isSameDay(dueDateMs, targetCal.timeInMillis)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Indigo600 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    dueDateMs = targetCal.timeInMillis
                                }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deadline Time Presets
                Text(
                    text = "Deadline Time: ${DateTimeUtils.formatFriendlyDate(dueDateMs).substringAfter("• ").ifBlank { "Selected Time" }}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val timePresets = listOf(
                        "9:00 AM" to (9 to 0),
                        "12:00 PM" to (12 to 0),
                        "5:00 PM" to (17 to 0),
                        "11:59 PM" to (23 to 59)
                    )

                    timePresets.forEach { (timeLabel, pair) ->
                        val (hour, minute) = pair
                        val currentCal = Calendar.getInstance().apply { timeInMillis = dueDateMs }
                        val isSelected = currentCal.get(Calendar.HOUR_OF_DAY) == hour && currentCal.get(Calendar.MINUTE) == minute

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Indigo600 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dueDateMs
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                    }
                                    dueDateMs = newCal.timeInMillis
                                }
                        ) {
                            Text(
                                text = timeLabel,
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Estimated Time Required
                Text(
                    text = "Estimated Time Required: ${estimatedHours}h",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f).forEach { hours ->
                        val isSelected = estimatedHours == hours
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Indigo600 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { estimatedHours = hours }
                        ) {
                            Text(
                                text = "${hours}h",
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("What needs to be accomplished?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Links / Reminders") },
                    placeholder = { Text("e.g. Formula sheet link, submission portal URL") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons: Cancel & Create Task
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            onSave(
                                initialTask?.id ?: 0L,
                                title,
                                subject,
                                description,
                                notes,
                                dueDateMs,
                                priority,
                                estimatedHours
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("create_task_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                    ) {
                        Text(
                            text = if (initialTask != null) "Save Changes" else "Create Task",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
