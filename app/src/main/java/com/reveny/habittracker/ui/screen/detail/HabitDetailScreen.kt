package com.reveny.habittracker.ui.screen.detail

import android.text.format.DateFormat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Cream
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import java.util.Calendar

@Composable
fun HabitDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var renameOpen by remember { mutableStateOf(false) }
    var archiveOpen by remember { mutableStateOf(false) }

    if (uiState.isMissing) {
        MissingHabit(onNavigateBack = onNavigateBack)
        return
    }

    val habit = uiState.habit
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit?.name.orEmpty(), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Habit detail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { DetailStatsCard(uiState) }
        item { HistoryCard(uiState.historyDays) }
        item {
            FailureTimeChartCard(
                days = uiState.timeChartDays,
                points = uiState.timeChartPoints,
            )
        }
        item { MonthlyFailuresCard(uiState.monthlyFailures) }
        if (!habit?.motivation.isNullOrBlank()) {
            item { MotivationCard(habit?.motivation.orEmpty()) }
        }
        item {
            EditActionsCard(
                onRename = { renameOpen = true },
                onArchive = { archiveOpen = true },
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (renameOpen && habit != null) {
        RenameHabitDialog(
            currentName = habit.name,
            onDismiss = { renameOpen = false },
            onSave = { name ->
                viewModel.renameHabit(name)
                renameOpen = false
            },
        )
    }

    if (archiveOpen && habit != null) {
        ArchiveHabitDialog(
            habitName = habit.name,
            onDismiss = { archiveOpen = false },
            onArchive = { viewModel.archiveHabit(onNavigateBack) },
        )
    }

}

@Composable
private fun DetailStatsCard(uiState: HabitDetailUiState) {
    HandDrawnCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "${uiState.cleanStreak}d clean",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (uiState.cleanStreak == 0) Terracotta else Sage,
                )
                Text(
                    "current streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${uiState.failuresThisMonth}", style = MaterialTheme.typography.titleLarge, color = Terracotta)
                Text("this month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${uiState.totalFailures}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("total failures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HistoryCard(days: List<HabitHistoryDay>) {
    HandDrawnCard {
        Text("Calendar history", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    day.isFailure -> Terracotta.copy(alpha = 0.28f)
                                    day.isToday -> Sage.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                },
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (day.isFailure) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun MonthlyFailuresCard(monthlyFailures: List<HabitMonthlyFailure>) {
    val maxValue = maxOf(monthlyFailures.maxOfOrNull { it.failures } ?: 0, 1)
    HandDrawnCard {
        Text("Failures by month", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(14.dp))
        monthlyFailures.forEachIndexed { index, month ->
            MonthFailureBar(month, maxValue)
            if (index != monthlyFailures.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MonthFailureBar(month: HabitMonthlyFailure, maxValue: Int) {
    val fraction by animateFloatAsState(
        targetValue = month.failures.toFloat() / maxValue,
        animationSpec = tween(durationMillis = 550),
        label = "habit-detail-month-${month.label}",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(month.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (month.isCurrentMonth) Sage else Terracotta.copy(alpha = 0.75f)),
            )
        }
        Text("${month.failures}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MotivationCard(motivation: String) {
    HandDrawnCard {
        Text("Motivation", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            motivation.ifBlank { "No motivation added yet." },
            style = MaterialTheme.typography.bodyMedium,
            color = if (motivation.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FailureTimeChartCard(
    days: List<HabitTimeChartDay>,
    points: List<HabitTimeFailurePoint>,
) {
    val context = LocalContext.current
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pointColor = Terracotta
    val todayColor = Sage
    val gridLabels = listOf(
        formatChartTimeLabel(context, 0, 0) to 0f,
        formatChartTimeLabel(context, 6, 0) to 0.25f,
        formatChartTimeLabel(context, 12, 0) to 0.5f,
        formatChartTimeLabel(context, 18, 0) to 0.75f,
        formatChartTimeLabel(context, 23, 59) to 1f,
    )

    HandDrawnCard {
        Text("Failure times", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Last 7 days",
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (points.isEmpty()) {
            Text(
                "No timed failures in the last 7 days.",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .height(170.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    gridLabels.forEach { (label, _) ->
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp),
                ) {
                    gridLabels.forEach { (_, fraction) ->
                        val y = size.height * fraction
                        drawLine(
                            color = axisColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    days.forEachIndexed { index, day ->
                        val x = if (days.size == 1) {
                            size.width / 2f
                        } else {
                            size.width * (index.toFloat() / (days.size - 1))
                        }
                        if (day.isToday) {
                            drawLine(
                                color = todayColor.copy(alpha = 0.45f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                            )
                        }
                    }
                    points.forEach { point ->
                        val x = if (days.size == 1) {
                            size.width / 2f
                        } else {
                            size.width * (point.dayIndex.toFloat() / (days.size - 1))
                        }
                        val y = size.height * (point.minuteOfDay / 1440f)
                        drawCircle(
                            color = pointColor.copy(alpha = 0.2f),
                            radius = 9.dp.toPx(),
                            center = Offset(x, y),
                        )
                        drawCircle(
                            color = pointColor,
                            radius = 4.5.dp.toPx(),
                            center = Offset(x, y),
                        )
                        drawCircle(
                            color = pointColor,
                            radius = 9.dp.toPx(),
                            center = Offset(x, y),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                days.forEach { day ->
                    Text(
                        day.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day.isToday) Sage else labelColor,
                    )
                }
            }
        }
    }
}

private fun formatChartTimeLabel(context: android.content.Context, hour: Int, minute: Int): String {
    if (!DateFormat.is24HourFormat(context)) {
        val suffix = if (hour < 12) "AM" else "PM"
        val hour12 = when (val normalized = hour % 12) {
            0 -> 12
            else -> normalized
        }
        return if (minute == 0) "$hour12 $suffix" else "$hour12:${"%02d".format(minute)} $suffix"
    }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
}

@Composable
private fun EditActionsCard(
    onRename: () -> Unit,
    onArchive: () -> Unit,
) {
    HandDrawnCard {
        Text("Edit actions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onRename,
                colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Rename")
            }
            Button(onClick = onArchive) {
                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Archive")
            }
        }
    }
}

@Composable
private fun RenameHabitDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        HandDrawnCard {
            Text("Rename habit", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(name.trim()) },
                    enabled = name.trim().isNotEmpty() && name.trim() != currentName,
                    colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream),
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun ArchiveHabitDialog(
    habitName: String,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        HandDrawnCard {
            Text("Archive habit?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"$habitName\" will be hidden from active tracking. You can restore it later from Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onArchive,
                    colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Cream),
                ) { Text("Archive") }
            }
        }
    }
}

@Composable
private fun DeleteHabitDialog(
    habitName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        HandDrawnCard {
            Text("Delete habit?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\"$habitName\" and all failure logs will be permanently deleted.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = Cream),
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun MissingHabit(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Habit not found", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateBack) { Text("Go back") }
    }
}
