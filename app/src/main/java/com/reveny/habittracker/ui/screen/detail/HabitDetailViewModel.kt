package com.reveny.habittracker.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.notification.HabitReminderScheduler
import com.reveny.habittracker.util.CleanStreakCalculator
import com.reveny.habittracker.util.DateUtils
import com.reveny.habittracker.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
    private val widgetUpdater: WidgetUpdater,
    private val habitReminderScheduler: HabitReminderScheduler,
) : ViewModel() {
    private val habitId: Long = checkNotNull(savedStateHandle["habitId"])

    val uiState: StateFlow<HabitDetailUiState> = combine(
        repository.getAllHabits(),
        repository.getLogsForHabit(habitId),
    ) { habits, logs ->
        val habit = habits.firstOrNull { it.id == habitId }
        if (habit == null) {
            HabitDetailUiState(isMissing = true)
        } else {
            buildState(habit, logs)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitDetailUiState())

    fun renameHabit(name: String) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            repository.updateHabit(habit.copy(name = name.trim()))
            widgetUpdater.updateAll()
        }
    }

    fun archiveHabit(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.archiveHabit(habitId, DateUtils.todayIso())
            habitReminderScheduler.cancel(habitId)
            widgetUpdater.updateAll()
            onComplete()
        }
    }

    fun deleteHabit(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            habitReminderScheduler.cancel(habitId)
            widgetUpdater.updateAll()
            onComplete()
        }
    }

    private fun buildState(habit: Habit, logs: List<HabitLog>): HabitDetailUiState {
        val today = LocalDate.now()
        val createdAt = LocalDate.parse(habit.createdAt)
        val cleanStreak = CleanStreakCalculator.calculate(
            logs = logs,
            today = today,
            startDate = createdAt,
            habitIds = setOf(habit.id),
        )
        val currentMonth = YearMonth.from(today)
        val (monthStart, monthEnd) = DateUtils.monthStartEnd(currentMonth)
        val failuresThisMonth = logs.count { it.date in monthStart..monthEnd }
        val failureDates = logs.map { it.date }.toSet()
        val historyDays = (34 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            HabitHistoryDay(
                dayLabel = date.dayOfMonth.toString(),
                isFailure = date.toString() in failureDates,
                isToday = date == today,
            )
        }

        val firstHabitMonth = YearMonth.from(createdAt)
        val firstLogMonth = logs.minOfOrNull { YearMonth.from(LocalDate.parse(it.date)) }
        val firstTrackedMonth = listOfNotNull(firstHabitMonth, firstLogMonth).minOrNull()
            ?: currentMonth
        val chartStartMonth = maxOf(firstTrackedMonth, currentMonth.minusMonths(5))
        val months = generateSequence(chartStartMonth) { month ->
            month.plusMonths(1).takeIf { !it.isAfter(currentMonth) }
        }.toList()
        val monthlyFailures = months.map { month ->
            val (start, end) = DateUtils.monthStartEnd(month)
            HabitMonthlyFailure(
                label = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                failures = logs.count { it.date in start..end },
                isCurrentMonth = month == currentMonth,
            )
        }
        val timeChartDates = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
        val timeChartDays = timeChartDates.map { date ->
            HabitTimeChartDay(
                date = date.toString(),
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                isToday = date == today,
            )
        }
        val dayIndexByDate = timeChartDates
            .mapIndexed { index, date -> date.toString() to index }
            .toMap()
        val timeChartPoints = logs.mapNotNull { log ->
            val dayIndex = dayIndexByDate[log.date] ?: return@mapNotNull null
            val time = log.failureTime?.let { parseFailureTime(it) } ?: return@mapNotNull null
            HabitTimeFailurePoint(
                dayIndex = dayIndex,
                minuteOfDay = time.hour * 60 + time.minute,
            )
        }

        return HabitDetailUiState(
            habit = habit,
            cleanStreak = cleanStreak,
            failuresThisMonth = failuresThisMonth,
            totalFailures = logs.size,
            historyDays = historyDays,
            monthlyFailures = monthlyFailures,
            timeChartDays = timeChartDays,
            timeChartPoints = timeChartPoints,
        )
    }

    private fun parseFailureTime(time: String): LocalTime? =
        runCatching { LocalTime.parse(time) }.getOrNull()
}

data class HabitDetailUiState(
    val habit: Habit? = null,
    val cleanStreak: Int = 0,
    val failuresThisMonth: Int = 0,
    val totalFailures: Int = 0,
    val historyDays: List<HabitHistoryDay> = emptyList(),
    val monthlyFailures: List<HabitMonthlyFailure> = emptyList(),
    val timeChartDays: List<HabitTimeChartDay> = emptyList(),
    val timeChartPoints: List<HabitTimeFailurePoint> = emptyList(),
    val isMissing: Boolean = false,
)

data class HabitHistoryDay(
    val dayLabel: String,
    val isFailure: Boolean,
    val isToday: Boolean,
)

data class HabitMonthlyFailure(
    val label: String,
    val failures: Int,
    val isCurrentMonth: Boolean,
)

data class HabitTimeChartDay(
    val date: String,
    val label: String,
    val isToday: Boolean,
)

data class HabitTimeFailurePoint(
    val dayIndex: Int,
    val minuteOfDay: Int,
)
