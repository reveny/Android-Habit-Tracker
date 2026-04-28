package com.reveny.habittracker.ui.screen.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val habits by viewModel.habits.collectAsState()
    var dayForDialog by remember { mutableStateOf<Int?>(null) }
    var failedHabitIds by remember { mutableStateOf(emptySet<Long>()) }

    LaunchedEffect(dayForDialog) {
        failedHabitIds = dayForDialog?.let { viewModel.getFailedHabitIds(it) } ?: emptySet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Calendar", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Tap a day to toggle failures",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats card
        HandDrawnCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiState.cleanStreak}",
                        style = MaterialTheme.typography.displayMedium,
                        color = Sage,
                    )
                Text("day streak", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${uiState.failureDays}",
                        style = MaterialTheme.typography.displayMedium,
                        color = Terracotta,
                    )
                Text("failure days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous month")
            }
            Text(uiState.monthLabel, style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HandDrawnCard {
            MonthCalendarGrid(
                yearMonth = uiState.currentMonth,
                days = uiState.calendarDays,
                onDayClick = { day ->
                    val selected = uiState.selectedHabit
                    if (selected != null) {
                        viewModel.toggleFailure(selected.id, day)
                    } else if (habits.size == 1) {
                        viewModel.toggleFailure(habits.first().id, day)
                    } else if (habits.isNotEmpty()) {
                        dayForDialog = day
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Filter by Habit", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        HabitFocusChips(
            habits = habits,
            selectedHabit = uiState.selectedHabit,
            onSelect = { viewModel.selectHabit(it) },
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    dayForDialog?.let { day ->
        val dateLabel = uiState.currentMonth.atDay(day).toString()
        AlertDialog(
            onDismissRequest = { dayForDialog = null },
            title = { Text(dateLabel) },
            text = {
                Column {
                    Text(
                        "Tap a habit to toggle failure",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    habits.filter { it.archivedAt == null }.forEach { habit ->
                        val isFailed = habit.id in failedHabitIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.toggleFailure(habit.id, day)
                                    failedHabitIds = if (isFailed) {
                                        failedHabitIds - habit.id
                                    } else {
                                        failedHabitIds + habit.id
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (isFailed) Terracotta else Sage.copy(alpha = 0.3f),
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isFailed) Terracotta else MaterialTheme.colorScheme.onSurface,
                            )
                            if (isFailed) {
                                Text(
                                    "failed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Terracotta,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dayForDialog = null }) { Text("Done") }
            },
            dismissButton = {},
        )
    }
}
