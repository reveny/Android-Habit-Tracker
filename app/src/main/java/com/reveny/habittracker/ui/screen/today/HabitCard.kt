package com.reveny.habittracker.ui.screen.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.ui.components.FailureDatePickerDialog
import com.reveny.habittracker.ui.components.FailureNoteDialog
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Cream
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithLogs: HabitWithLogs,
    cleanStreak: Int,
    onLogFailure: (String, String?, String) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithLogs.habit
    var showDatePicker by remember { mutableStateOf(false) }
    var failureDate by remember { mutableStateOf<String?>(null) }

    HandDrawnCard(
        modifier = modifier.clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
            }
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = Cream,
                ),
                modifier = Modifier.size(height = 36.dp, width = 120.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Log Fail", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = "${habitWithLogs.failuresThisMonth}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                    color = if (habitWithLogs.failuresThisMonth == 0) Sage else Terracotta,
                )
                Text(
                    text = "failures this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = if (cleanStreak == 0) "Failed today" else "${cleanStreak}d clean",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (cleanStreak == 0) Terracotta else Sage,
                )
                Text(
                    text = "current streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (habitWithLogs.failuresLastMonth > 0 || habitWithLogs.failuresThisMonth > 0) {
                    Spacer(modifier = Modifier.height(10.dp))

                    val pct = abs(habitWithLogs.changePercent)
                    val color = if (habitWithLogs.isBetter) Sage else Terracotta
                    val label = if (habitWithLogs.isBetter) "better" else "worse"

                    Text(
                        text = if (pct > 0) "$pct% $label" else "same",
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                    )
                    Text(
                        text = "vs ${habitWithLogs.failuresLastMonth} last month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        FailureDatePickerDialog(
            onDateSelected = { date ->
                failureDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    failureDate?.let { selectedDate ->
        FailureNoteDialog(
            date = selectedDate,
            habitName = habit.name,
            onConfirm = { note, failureTime ->
                onLogFailure(selectedDate, note, failureTime)
                failureDate = null
            },
            onDismiss = { failureDate = null },
        )
    }
}
