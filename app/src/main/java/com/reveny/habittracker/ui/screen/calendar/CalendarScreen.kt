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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.FailureNoteDialog
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val habits by viewModel.habits.collectAsState()
    var dayForDialog by remember { mutableStateOf<Int?>(null) }
    var failedHabitIds by remember { mutableStateOf(emptySet<Long>()) }
    var pendingFailure by remember { mutableStateOf<PendingCalendarFailure?>(null) }

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
                    val hasFailure = uiState.calendarDays
                        .firstOrNull { it.day == day }
                        ?.failureCount
                        ?.let { it > 0 }
                        ?: false
                    if (selected != null) {
                        if (hasFailure) {
                            viewModel.removeFailure(selected.id, day)
                        } else {
                            pendingFailure = PendingCalendarFailure(selected.id, selected.name, day)
                        }
                    } else if (habits.size == 1) {
                        val habit = habits.first()
                        if (hasFailure) {
                            viewModel.removeFailure(habit.id, day)
                        } else {
                            pendingFailure = PendingCalendarFailure(habit.id, habit.name, day)
                        }
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
        Dialog(
            onDismissRequest = { dayForDialog = null },
        ) {
            HandDrawnCard {
                Text("Which habit failed?", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap a habit to add a note.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                habits.forEach { habit ->
                    val isFailed = habit.id in failedHabitIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isFailed) {
                                    viewModel.removeFailure(habit.id, day)
                                    failedHabitIds = failedHabitIds - habit.id
                                } else {
                                    pendingFailure = PendingCalendarFailure(
                                        habitId = habit.id,
                                        habitName = habit.name,
                                        day = day,
                                    )
                                    dayForDialog = null
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { dayForDialog = null }) {
                        Text("Cancel", color = Sage)
                    }
                }
            }
        }
    }

    pendingFailure?.let { pending ->
        val date = uiState.currentMonth.atDay(pending.day).toString()
        FailureNoteDialog(
            date = date,
            habitName = pending.habitName,
            onConfirm = { note, failureTime ->
                viewModel.logFailure(pending.habitId, pending.day, note, failureTime)
                pendingFailure = null
            },
            onDismiss = { pendingFailure = null },
        )
    }
}

private data class PendingCalendarFailure(
    val habitId: Long,
    val habitName: String,
    val day: Int,
)
