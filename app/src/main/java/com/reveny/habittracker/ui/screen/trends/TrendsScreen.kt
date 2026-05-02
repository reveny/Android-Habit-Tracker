package com.reveny.habittracker.ui.screen.trends

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.data.model.MonthlyComparison
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.screen.calendar.HabitFocusChips
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import kotlin.math.abs

@Composable
fun TrendsScreen(viewModel: TrendsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val improved = uiState.comparisons
        .filter { it.thisMonthFailures < it.lastMonthFailures }
        .sortedByDescending { abs(it.changePercent) }
    val worse = uiState.comparisons
        .filter { it.thisMonthFailures > it.lastMonthFailures }
        .sortedByDescending { abs(it.changePercent) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Trends", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Failure patterns compared with last month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            HabitFocusChips(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                onSelect = viewModel::selectHabit,
            )
        }

        item {
            TrendSummaryCard(uiState = uiState)
        }

        item {
            SixMonthTrendChart(monthlyTotals = uiState.monthlyTotals)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text("What changed", style = MaterialTheme.typography.headlineMedium)
        }

        if (improved.isNotEmpty()) {
            item {
                HabitDeltaCard(
                    title = "Biggest improvements",
                    comparisons = improved,
                )
            }
        }

        if (worse.isNotEmpty()) {
            item {
                HabitDeltaCard(
                    title = "Needs attention",
                    comparisons = worse,
                )
            }
        }

        if (improved.isEmpty() && worse.isEmpty()) {
            item {
                HandDrawnCard {
                    Text("No changes yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        uiState.selectedHabit?.let { selectedHabit ->
                            "${selectedHabit.name} has the same failure count as last month."
                        } ?: run {
                            "Your active habits have the same failure counts as last month."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun TrendSummaryCard(uiState: TrendsUiState) {
    val isBetter = uiState.totalFailuresThisMonth <= uiState.totalFailuresLastMonth
    val pct = abs(uiState.overallChangePercent)
    val changeLabel = when {
        pct == 0 -> "Same as last month"
        isBetter -> "$pct% better than last month"
        else -> "$pct% worse than last month"
    }

    HandDrawnCard {
        Text(uiState.currentMonthLabel.ifBlank { "This month" }, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${uiState.totalFailuresThisMonth}",
            style = MaterialTheme.typography.displayLarge,
            color = if (uiState.totalFailuresThisMonth == 0) Sage else Terracotta,
        )
        Text(
            text = if (uiState.totalFailuresThisMonth == 1) "failure this month" else "failures this month",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = changeLabel,
            style = MaterialTheme.typography.titleMedium,
            color = if (isBetter) Sage else Terracotta,
        )
        Text(
            text = "${uiState.totalFailuresLastMonth} in ${uiState.previousMonthLabel.ifBlank { "last month" }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SixMonthTrendChart(monthlyTotals: List<MonthlyTotal>) {
    val maxValue = maxOf(monthlyTotals.maxOfOrNull { it.failures } ?: 0, 1)
    val title = if (monthlyTotals.size < 6) "Since you started" else "Last 6 months"
    var animateBars by remember(monthlyTotals) { mutableStateOf(false) }

    LaunchedEffect(monthlyTotals) {
        animateBars = true
    }

    HandDrawnCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(14.dp))
        monthlyTotals.forEachIndexed { index, monthlyTotal ->
            MonthBar(
                label = monthlyTotal.label,
                value = monthlyTotal.failures,
                maxValue = maxValue,
                color = if (monthlyTotal.isCurrentMonth) Sage else MaterialTheme.colorScheme.onSurfaceVariant,
                animate = animateBars,
            )
            if (index != monthlyTotals.lastIndex) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MonthBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    animate: Boolean,
) {
    val targetFraction = if (animate) value.toFloat() / maxValue else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 650),
        label = "month-bar-$label",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
        Text(
            text = "$value",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(28.dp),
        )
    }
}

@Composable
private fun HabitDeltaCard(
    title: String,
    comparisons: List<MonthlyComparison>,
) {
    HandDrawnCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        comparisons.forEach { comparison ->
            HabitDeltaRow(comparison = comparison)
        }
    }
}

@Composable
private fun HabitDeltaRow(comparison: MonthlyComparison) {
    val pct = abs(comparison.changePercent)
    val color = if (comparison.isBetter) Sage else Terracotta
    val label = when {
        pct == 0 -> "same"
        comparison.isBetter -> "$pct% better"
        else -> "$pct% worse"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(comparison.habitName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${comparison.thisMonthFailures} now - ${comparison.lastMonthFailures} last",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}
