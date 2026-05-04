package com.reveny.habittracker.ui.screen.today

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reveny.habittracker.ui.components.HandDrawnCard
import com.reveny.habittracker.ui.components.JournalQuote
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta

@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val habits by viewModel.habitsWithFailures.collectAsState()
    val totalCleanStreak by viewModel.totalCleanStreak.collectAsState()
    val cleanStreaks by viewModel.cleanStreaks.collectAsState()

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

        item {
            TotalStreakCard(
                cleanStreak = totalCleanStreak,
                habitCount = habits.size,
            )
        }

        items(
            items = habits,
            key = { it.habit.id },
        ) { habitWithLogs ->
            val index = habits.indexOfFirst { it.habit.id == habitWithLogs.habit.id }
            AnimatedHabitCard(index = index.coerceAtLeast(0)) {
                HabitCard(
                    habitWithLogs = habitWithLogs,
                    cleanStreak = cleanStreaks[habitWithLogs.habit.id] ?: 0,
                    onLogFailure = { date, note ->
                        viewModel.logFailure(habitWithLogs.habit.id, date, note)
                    },
                    onDelete = { viewModel.deleteHabit(habitWithLogs.habit.id) },
                    onRename = { newName -> viewModel.renameHabit(habitWithLogs.habit.id, newName) },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AnimatedHabitCard(
    index: Int,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(18f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 45L).coerceAtMost(220L))
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 260))
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 45L).coerceAtMost(220L))
        offsetY.animateTo(0f, animationSpec = tween(durationMillis = 260))
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .alpha(alpha.value)
            .graphicsLayer { translationY = offsetY.value },
    ) {
        content()
    }
}

@Composable
private fun TotalStreakCard(
    cleanStreak: Int,
    habitCount: Int,
    modifier: Modifier = Modifier,
) {
    HandDrawnCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Total streak",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Across $habitCount active habit${if (habitCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = cleanStreak.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (cleanStreak == 0) Terracotta else Sage,
                )
                Text(
                    text = if (cleanStreak == 1) "day clean" else "days clean",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
