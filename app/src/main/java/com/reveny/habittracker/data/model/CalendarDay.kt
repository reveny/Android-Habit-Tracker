package com.reveny.habittracker.data.model

data class CalendarDay(
    val day: Int,
    val failureCount: Int = 0,
    val isToday: Boolean = false,
)
