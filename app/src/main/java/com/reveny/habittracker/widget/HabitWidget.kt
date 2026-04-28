package com.reveny.habittracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.reveny.habittracker.data.preferences.WidgetPreferencesStore
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
            totalFails = habits.find { it.habit.id == habitId }?.failuresThisMonth ?: 0
            cleanStreak = calculateCleanStreak(repository, habitId)
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
    ): Int {
        val today = LocalDate.now()
        val logs = repository.getLogsInRange(today.minusDays(90).toString(), today.toString())
        val failureDates = logs.filter { it.habitId == habitId }.map { it.date }.toSet()
        var streak = 0
        var date = today
        while (streak < 90) {
            if (date.toString() in failureDates) break
            streak++
            date = date.minusDays(1)
        }
        return streak
    }
}
