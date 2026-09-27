package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.model.Priority
import com.example.ui.components.WeeklyProgressSection
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium

@Composable
fun ProgressScreen(
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val pending = total - completed
    val highPriorityTotal = tasks.count { it.priority == Priority.HIGH }
    val highPriorityDone = tasks.count { it.priority == Priority.HIGH && it.isCompleted }

    val totalHours = tasks.sumOf { it.estimatedHours.toDouble() }.toFloat()
    val completedHours = tasks.filter { it.isCompleted }.sumOf { it.estimatedHours.toDouble() }.toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "Productivity & Insights",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Analyze your deadline performance and completion trends.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Hours Done",
                    value = String.format("%.1fh", completedHours),
                    icon = Icons.Filled.Speed,
                    accentColor = Indigo600,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Hours Left",
                    value = String.format("%.1fh", totalHours - completedHours),
                    icon = Icons.Filled.HourglassTop,
                    accentColor = PriorityMedium,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Success Rate",
                    value = if (total > 0) "${(completed * 100 / total)}%" else "100%",
                    icon = Icons.Filled.TrendingUp,
                    accentColor = PriorityLow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Weekly Chart
        item {
            WeeklyProgressSection(tasks = tasks)
        }

        // Subject Workload Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Workload by Subject",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val subjects = tasks.groupBy { it.subject }
                    subjects.forEach { (subj, list) ->
                        val subTotal = list.size
                        val subDone = list.count { it.isCompleted }
                        val pct = if (subTotal > 0) subDone.toFloat() / subTotal.toFloat() else 0f

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$subDone / $subTotal tasks (${(pct * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Indigo600,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
