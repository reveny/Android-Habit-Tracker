package com.reveny.habittracker.data.model

data class MonthlyComparison(
    val habitName: String,
    val thisMonthFailures: Int,
    val lastMonthFailures: Int,
) {
    val changePercent: Int
        get() = if (lastMonthFailures == 0) {
            if (thisMonthFailures > 0) 100 else 0
        } else {
            ((lastMonthFailures - thisMonthFailures).toFloat() / lastMonthFailures * 100).toInt()
        }

    val isBetter: Boolean get() = thisMonthFailures <= lastMonthFailures
}
