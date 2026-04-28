package com.reveny.habittracker.ui.screen.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.ui.theme.Sage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitFocusChips(
    habits: List<Habit>,
    selectedHabit: Habit?,
    onSelect: (Habit?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedHabit == null,
            onClick = { onSelect(null) },
            label = { Text("All", style = MaterialTheme.typography.labelMedium) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                selectedLabelColor = Sage,
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = Sage,
                enabled = true,
                selected = selectedHabit == null,
            ),
        )
        habits.forEach { habit ->
            FilterChip(
                selected = selectedHabit?.id == habit.id,
                onClick = { onSelect(if (selectedHabit?.id == habit.id) null else habit) },
                label = { Text(habit.name, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    selectedLabelColor = Sage,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Sage,
                    enabled = true,
                    selected = selectedHabit?.id == habit.id,
                ),
            )
        }
    }
}
