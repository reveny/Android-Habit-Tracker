package com.reveny.habittracker.ui.screen.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reveny.habittracker.data.model.MonthlyComparison
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import kotlin.math.abs

@Composable
fun MonthlyComparisonCard(
    comparisons: List<MonthlyComparison>,
    modifier: Modifier = Modifier,
) {
    if (comparisons.isEmpty()) return

    HandDrawnCard(modifier = modifier) {
        Text("Per-Habit Breakdown", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Failures: this month vs last",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        comparisons.forEach { comparison ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(comparison.habitName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${comparison.thisMonthFailures} this month / ${comparison.lastMonthFailures} last",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val pct = abs(comparison.changePercent)
                val color = if (comparison.isBetter) Sage else Terracotta
                val label = if (comparison.isBetter) {
                    if (pct > 0) "$pct% better" else "same"
                } else {
                    "$pct% worse"
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                )
            }
        }
    }
}
