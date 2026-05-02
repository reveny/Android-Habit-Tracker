package com.reveny.habittracker.util

import com.reveny.habittracker.data.local.entity.HabitLog
import java.time.LocalDate

object CleanStreakCalculator {
    fun calculate(
        logs: List<HabitLog>,
        today: LocalDate = LocalDate.now(),
        startDate: LocalDate,
        habitIds: Set<Long>,
    ): Int {
        val relevantLogs = logs
            .asSequence()
            .filter { it.habitId in habitIds }
            .toList()

        val failureDates = relevantLogs
            .asSequence()
            .map { it.date }
            .toSet()
        val earliestLoggedFailure = relevantLogs.minOfOrNull { LocalDate.parse(it.date) }
        val firstTrackedDate = earliestLoggedFailure
            ?.takeIf { it.isBefore(startDate) }
            ?: startDate

        var streak = 0
        var date = today
        while (!date.isBefore(firstTrackedDate)) {
            if (date.toString() in failureDates) break
            streak++
            date = date.minusDays(1)
        }
        return streak
    }
}
