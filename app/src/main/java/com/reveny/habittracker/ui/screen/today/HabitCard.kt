package com.reveny.habittracker.ui.screen.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.ui.components.FailureDatePickerDialog
import com.reveny.habittracker.ui.components.FailureNoteDialog
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Cream
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habitWithLogs: HabitWithLogs,
    cleanStreak: Int,
    onLogFailure: (String, String?) -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = habitWithLogs.habit
    var showDatePicker by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var failureDate by remember { mutableStateOf<String?>(null) }
    var renameText by remember(habit.name) { mutableStateOf(habit.name) }

    HandDrawnCard(
        modifier = modifier.combinedClickable(
            onClick = {},
            onLongClick = { showActionMenu = true },
        )
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
            onConfirm = { note ->
                onLogFailure(selectedDate, note)
                failureDate = null
            },
            onDismiss = { failureDate = null },
        )
    }

    if (showActionMenu) {
        Dialog(onDismissRequest = { showActionMenu = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    IconButton(onClick = {
                        showActionMenu = false
                        renameText = habit.name
                        showRenameDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Sage,
                        )
                    }
                    IconButton(onClick = {
                        showActionMenu = false
                        showDeleteDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Terracotta,
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        val focusRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            HandDrawnCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Sage,
                        modifier = Modifier.size(20.dp),
                    )
                    Text("Rename habit", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { focusRequester.requestFocus() },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = renameText.trim()
                            if (trimmed.isNotEmpty()) {
                                onRename(trimmed)
                                showRenameDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Sage,
                            contentColor = Cream,
                        ),
                        enabled = renameText.trim().isNotEmpty() && renameText.trim() != habit.name,
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            HandDrawnCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(20.dp),
                    )
                    Text("Delete habit?", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "\"${habit.name}\" and all its failure logs will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Terracotta,
                            contentColor = Cream,
                        ),
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
