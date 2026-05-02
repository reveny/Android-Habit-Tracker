package com.reveny.habittracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.reveny.habittracker.data.preferences.WidgetPreferencesStore
import com.reveny.habittracker.util.CleanStreakCalculator
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalFails: Int
        val cleanStreak: Int
        val habitId: Long

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext, WidgetEntryPoint::class.java
            )
            val repository = entryPoint.habitRepository()
            val prefsStore = entryPoint.widgetPreferencesStore()

            habitId = prefsStore.widgetHabitId.first()

            if (habitId == WidgetPreferencesStore.NO_HABIT_SELECTED) {
                provideContent { GlanceTheme { NoHabitSelectedContent() } }
                return
            }

            val habits = repository.getActiveHabitsWithFailures().first()
            val selectedHabit = habits.find { it.habit.id == habitId }
            totalFails = selectedHabit?.failuresThisMonth ?: 0
            cleanStreak = selectedHabit?.let {
                calculateCleanStreak(repository, it.habit.id, LocalDate.parse(it.habit.createdAt))
            } ?: 0
        } catch (e: Exception) {
            provideContent { GlanceTheme { ErrorContent() } }
            return
        }

        provideContent {
            GlanceTheme {
                HabitStatsRow(
                    totalFails = totalFails,
                    cleanStreak = cleanStreak,
                    failLabel = if (totalFails == 1) "fail this month" else "fails this month",
                )
            }
        }
    }

    private suspend fun calculateCleanStreak(
        repository: com.reveny.habittracker.data.repository.HabitRepository,
        habitId: Long,
        startDate: LocalDate,
    ): Int {
        val today = LocalDate.now()
        val logs = repository.getLogsInRange(
            "2000-01-01",
            today.toString(),
        )
        return CleanStreakCalculator.calculate(logs, today, startDate, setOf(habitId))
    }
}
