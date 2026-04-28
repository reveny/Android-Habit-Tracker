package com.reveny.habittracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
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
            cleanStreak = calculateCleanStreak(repository)
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
    ): Int {
        val today = LocalDate.now()
        val logs = repository.getLogsInRange(today.minusDays(90).toString(), today.toString())
        val failureDates = logs.map { it.date }.toSet()
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
