package com.reveny.habittracker.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayIso(): String = LocalDate.now().format(isoFormatter)

    fun today(): LocalDate = LocalDate.now()

    fun formatDisplay(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "$dayOfWeek, $month ${date.dayOfMonth}"
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return "$month ${yearMonth.year}"
    }

    fun monthStartEnd(yearMonth: YearMonth): Pair<String, String> {
        val start = yearMonth.atDay(1).format(isoFormatter)
        val end = yearMonth.atEndOfMonth().format(isoFormatter)
        return start to end
    }

    fun daysInMonth(yearMonth: YearMonth): Int = yearMonth.lengthOfMonth()

    fun firstDayOfWeekOffset(yearMonth: YearMonth): Int {
        // Sunday = 0, Monday = 1, ...
        val firstDay = yearMonth.atDay(1).dayOfWeek.value % 7
        return firstDay
    }
}
