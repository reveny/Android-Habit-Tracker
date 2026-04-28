package com.reveny.habittracker.data.model

import com.reveny.habittracker.data.local.entity.Habit

data class HabitWithLogs(
    val habit: Habit,
    val failuresThisMonth: Int = 0,
    val failuresLastMonth: Int = 0,
) {
    val changePercent: Int
        get() = if (failuresLastMonth == 0) {
            if (failuresThisMonth > 0) 100 else 0
        } else {
            ((failuresLastMonth - failuresThisMonth).toFloat() / failuresLastMonth * 100).toInt()
        }

    val isBetter: Boolean get() = failuresThisMonth <= failuresLastMonth
}
