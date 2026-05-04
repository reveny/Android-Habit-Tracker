package com.reveny.habittracker.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.CalendarDay
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.DateUtils
import com.reveny.habittracker.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val habits = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMonth(YearMonth.now())
    }

    fun selectHabit(habit: Habit?) {
        _uiState.update { it.copy(selectedHabit = habit) }
        loadMonth(_uiState.value.currentMonth)
    }

    fun previousMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = prev) }
        loadMonth(prev)
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = next) }
        loadMonth(next)
    }

    fun toggleFailure(habitId: Long, day: Int) {
        val date = _uiState.value.currentMonth.atDay(day).toString()
        viewModelScope.launch {
            repository.toggleFailure(habitId, date)
            loadMonth(_uiState.value.currentMonth)
            widgetUpdater.updateAll()
        }
    }

    fun logFailure(habitId: Long, day: Int, note: String?) {
        val date = _uiState.value.currentMonth.atDay(day).toString()
        viewModelScope.launch {
            repository.logFailure(habitId, date, note)
            loadMonth(_uiState.value.currentMonth)
            widgetUpdater.updateAll()
        }
    }

    fun removeFailure(habitId: Long, day: Int) {
        val date = _uiState.value.currentMonth.atDay(day).toString()
        viewModelScope.launch {
            repository.removeFailure(habitId, date)
            loadMonth(_uiState.value.currentMonth)
            widgetUpdater.updateAll()
        }
    }

    suspend fun getFailedHabitIds(day: Int): Set<Long> {
        val date = _uiState.value.currentMonth.atDay(day).toString()
        val (start, end) = date to date
        val logs = repository.getLogsInRange(start, end)
        return logs.map { it.habitId }.toSet()
    }

    private fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            val (startDate, endDate) = DateUtils.monthStartEnd(yearMonth)
            val logs = repository.getLogsInRange(startDate, endDate)
            val selectedHabitId = _uiState.value.selectedHabit?.id
            val filteredLogs = if (selectedHabitId != null) {
                logs.filter { it.habitId == selectedHabitId }
            } else {
                logs
            }

            val today = LocalDate.now()
            val daysInMonth = DateUtils.daysInMonth(yearMonth)
            val calendarDays = (1..daysInMonth).map { day ->
                val dateStr = yearMonth.atDay(day).toString()
                val dayFailures = filteredLogs.count { it.date == dateStr }
                CalendarDay(
                    day = day,
                    failureCount = dayFailures,
                    isToday = yearMonth.atDay(day) == today,
                )
            }

            val failureDays = calendarDays.count { it.failureCount > 0 }

            // Clean streak: consecutive days without failure counting back from today
            val cleanStreak = if (yearMonth == YearMonth.from(today)) {
                calendarDays
                    .take(today.dayOfMonth)
                    .reversed()
                    .takeWhile { it.failureCount == 0 }
                    .size
            } else {
                calendarDays.reversed().takeWhile { it.failureCount == 0 }.size
            }

            _uiState.update {
                it.copy(
                    calendarDays = calendarDays,
                    failureDays = failureDays,
                    cleanStreak = cleanStreak,
                    monthLabel = DateUtils.formatMonthYear(yearMonth),
                )
            }
        }
    }
}

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val failureDays: Int = 0,
    val cleanStreak: Int = 0,
    val monthLabel: String = "",
    val selectedHabit: Habit? = null,
)
