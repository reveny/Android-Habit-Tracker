package com.reveny.habittracker.ui.screen.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.JournalQuote

@Composable
fun TrendsScreen(viewModel: TrendsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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
                "Month-over-month comparison",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            JournalQuote("Progress is not always linear, but every step counts.")
        }

        item {
            KeyInsightsGrid(
                totalThisMonth = uiState.totalFailuresThisMonth,
                totalLastMonth = uiState.totalFailuresLastMonth,
                overallChangePercent = uiState.overallChangePercent,
                activeHabits = uiState.activeHabitCount,
            )
        }

        item {
            MonthlyComparisonCard(comparisons = uiState.comparisons)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
