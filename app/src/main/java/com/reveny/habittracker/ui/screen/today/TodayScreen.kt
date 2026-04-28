package com.reveny.habittracker.ui.screen.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.JournalQuote

@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val habits by viewModel.habitsWithFailures.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            DailyIntentionsHeader(habitCount = habits.size)
        }

        item {
            JournalQuote("Awareness of failure is the first step toward change.")
        }

        items(habits, key = { it.habit.id }) { habitWithLogs ->
            HabitCard(
                habitWithLogs = habitWithLogs,
                onLogFailure = { date -> viewModel.logFailure(habitWithLogs.habit.id, date) },
                onDelete = { viewModel.deleteHabit(habitWithLogs.habit.id) },
                onRename = { newName -> viewModel.renameHabit(habitWithLogs.habit.id, newName) },
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
