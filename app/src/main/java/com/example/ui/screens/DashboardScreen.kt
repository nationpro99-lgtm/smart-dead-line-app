package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.model.DeadlineUrgency
import com.example.model.Priority
import com.example.ui.components.TaskCard
import com.example.ui.components.WeeklyProgressSection
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    userName: String,
    tasks: List<TaskEntity>,
    currentEpochMs: Long,
    selectedPriority: Priority?,
    onSelectPriority: (Priority?) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onChangePriority: (Long, Priority) -> Unit,
    onAddTask: () -> Unit,
    onOpenAiScheduler: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentEpochMs }
    val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
    val greeting = DateTimeUtils.getGreeting(hourOfDay)
    val formattedCurrentDate = remember(currentEpochMs) {
        DateTimeUtils.formatShortDate(currentEpochMs)
    }

    // Calculations for Top Metric Cards
    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val overdueTasks = pendingTasks.filter {
        DateTimeUtils.getUrgency(it.dueDateEpochMs, currentEpochMs) == DeadlineUrgency.OVERDUE
    }

    val todayTasks = pendingTasks.filter {
        val urg = DateTimeUtils.getUrgency(it.dueDateEpochMs, currentEpochMs)
        urg == DeadlineUrgency.TODAY || urg == DeadlineUrgency.CRITICAL
    }

    val upcomingTasks = pendingTasks.filter {
        val urg = DateTimeUtils.getUrgency(it.dueDateEpochMs, currentEpochMs)
        urg == DeadlineUrgency.TOMORROW || urg == DeadlineUrgency.UPCOMING || urg == DeadlineUrgency.LATER
    }.sortedBy { it.dueDateEpochMs }

    val totalCount = tasks.size
    val completionPercentage = if (totalCount > 0) {
        (completedTasks.size.toFloat() / totalCount.toFloat() * 100).toInt()
    } else 0

    // Filter Today's Tasks by selected priority filter if applied
    val filteredTodayTasks = if (selectedPriority != null) {
        todayTasks.filter { it.priority == selectedPriority }
    } else todayTasks

    val filteredUpcomingTasks = if (selectedPriority != null) {
        upcomingTasks.filter { it.priority == selectedPriority }
    } else upcomingTasks

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Personalized Greeting & Date Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greeting, $userName 👋",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "“Here’s what you need to focus on today.”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formattedCurrentDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Dashboard Metrics Cards Row
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 5
            ) {
                MetricCard(
                    title = "Pending",
                    value = "${pendingTasks.size}",
                    icon = Icons.Filled.HourglassBottom,
                    accentColor = Indigo600,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Due Today",
                    value = "${todayTasks.size}",
                    icon = Icons.Filled.NotificationsActive,
                    accentColor = PriorityMedium,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Overdue",
                    value = "${overdueTasks.size}",
                    icon = Icons.Filled.Warning,
                    accentColor = PriorityHigh,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Completed",
                    value = "${completedTasks.size}",
                    icon = Icons.Filled.CheckCircle,
                    accentColor = PriorityLow,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Completion",
                    value = "$completionPercentage%",
                    icon = Icons.Filled.PieChart,
                    accentColor = Cyan500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gemini AI Schedule Generator Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiScheduler() }
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(Indigo600, Cyan500)),
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("ai_schedule_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Indigo600, Cyan500))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Smart Scheduler",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Indigo600.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Gemini",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Indigo600
                                    )
                                }
                            }
                            Text(
                                text = "Auto-plan study milestones & project deadlines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    ElevatedButton(
                        onClick = onOpenAiScheduler,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Indigo600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Smart Priority Filter Bar (All | High | Medium | Low)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter by Priority",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // All Pill
                    PriorityFilterPill(
                        label = "All",
                        isSelected = selectedPriority == null,
                        color = Indigo600,
                        onClick = { onSelectPriority(null) }
                    )

                    PriorityFilterPill(
                        label = "High",
                        isSelected = selectedPriority == Priority.HIGH,
                        color = PriorityHigh,
                        onClick = { onSelectPriority(Priority.HIGH) }
                    )

                    PriorityFilterPill(
                        label = "Medium",
                        isSelected = selectedPriority == Priority.MEDIUM,
                        color = PriorityMedium,
                        onClick = { onSelectPriority(Priority.MEDIUM) }
                    )

                    PriorityFilterPill(
                        label = "Low",
                        isSelected = selectedPriority == Priority.LOW,
                        color = PriorityLow,
                        onClick = { onSelectPriority(Priority.LOW) }
                    )
                }
            }
        }

        // 4. Today's Focus Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PriorityHigh)
                    )
                    Text(
                        text = "Today's Focus",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                    Surface(
                        shape = CircleShape,
                        color = PriorityHigh.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${filteredTodayTasks.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PriorityHigh
                        )
                    }
                }

                ElevatedButton(
                    onClick = onAddTask,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Indigo600,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dashboard_add_task_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Overdue Alert Banner if any
        if (overdueTasks.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PriorityHigh.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PriorityHigh.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = PriorityHigh,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Attention Needed: ${overdueTasks.size} Overdue Task(s)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PriorityHigh
                            )
                            Text(
                                text = "Complete or reschedule past deadlines to stay on track.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Today's Focus Task List or Empty State
        if (filteredTodayTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = null,
                            tint = PriorityLow,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No deadlines for today 🎉",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Looks like you're all caught up! Great job staying ahead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTodayTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    currentEpochMs = currentEpochMs,
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onChangePriority = { newP -> onChangePriority(task.id, newP) }
                )
            }
        }

        // 5. Compact Weekly Productivity Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            WeeklyProgressSection(tasks = tasks)
        }

        // 6. Upcoming Deadlines Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Indigo600)
                    )
                    Text(
                        text = "Upcoming Deadlines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                }

                Text(
                    text = "${filteredUpcomingTasks.size} upcoming",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredUpcomingTasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No upcoming deadlines matching filters",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredUpcomingTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    currentEpochMs = currentEpochMs,
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onChangePriority = { newP -> onChangePriority(task.id, newP) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PriorityFilterPill(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (label != "All") {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else color)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
