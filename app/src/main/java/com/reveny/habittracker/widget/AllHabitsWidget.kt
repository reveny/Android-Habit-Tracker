package com.reveny.habittracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.reveny.habittracker.util.CleanStreakCalculator
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class AllHabitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalFails: Int
        val habitCount: Int
        val cleanStreak: Int

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext, WidgetEntryPoint::class.java
            )
            val repository = entryPoint.habitRepository()

            val habits = repository.getActiveHabitsWithFailures().first()
            totalFails = habits.sumOf { it.failuresThisMonth }
            habitCount = habits.size
            cleanStreak = calculateCleanStreak(repository, habits)
        } catch (e: Exception) {
            provideContent { GlanceTheme { ErrorContent() } }
            return
        }

        provideContent {
            GlanceTheme {
                AllHabitsStatsRow(
                    totalFails = totalFails,
                    habitCount = habitCount,
                    cleanStreak = cleanStreak,
                )
            }
        }
    }

    private suspend fun calculateCleanStreak(
        repository: com.reveny.habittracker.data.repository.HabitRepository,
        habits: List<com.reveny.habittracker.data.model.HabitWithLogs>,
    ): Int {
        if (habits.isEmpty()) return 0

        val today = LocalDate.now()
        val activeHabitIds = habits.map { it.habit.id }.toSet()
        val startDate = habits.minOf { LocalDate.parse(it.habit.createdAt) }
        val logs = repository.getLogsInRange(
            "2000-01-01",
            today.toString(),
        )
        return CleanStreakCalculator.calculate(logs, today, startDate, activeHabitIds)
    }
}
