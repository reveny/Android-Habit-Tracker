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
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import kotlin.math.abs

@Composable
fun KeyInsightsGrid(
    totalThisMonth: Int,
    totalLastMonth: Int,
    overallChangePercent: Int,
    activeHabits: Int,
    modifier: Modifier = Modifier,
) {
    HandDrawnCard(modifier = modifier) {
        Text(
            "Overview",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InsightItem(
                label = "This month",
                value = totalThisMonth.toString(),
                color = if (totalThisMonth == 0) Sage else Terracotta,
            )
            InsightItem(
                label = "Last month",
                value = totalLastMonth.toString(),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val isBetter = totalThisMonth <= totalLastMonth
            val pct = abs(overallChangePercent)
            val changeLabel = when {
                pct == 0 -> "Same"
                isBetter -> "$pct% better"
                else -> "$pct% worse"
            }
            InsightItem(
                label = "vs last month",
                value = changeLabel,
                color = if (isBetter) Sage else Terracotta,
            )
            InsightItem(
                label = "Tracking",
                value = "$activeHabits habits",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun InsightItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
