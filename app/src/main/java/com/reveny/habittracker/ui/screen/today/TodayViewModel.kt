package com.reveny.habittracker.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.CleanStreakCalculator
import com.reveny.habittracker.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    val habitsWithFailures: StateFlow<List<HabitWithLogs>> = repository
        .getActiveHabitsWithFailures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val today = LocalDate.now()
    private val allLogs = repository.getLogsInRangeFlow("2000-01-01", today.toString())

    val cleanStreaks: StateFlow<Map<Long, Int>> = combine(
        habitsWithFailures,
        allLogs,
    ) { habits, logs ->
        habits.associate { habitWithLogs ->
            val habit = habitWithLogs.habit
            habit.id to CleanStreakCalculator.calculate(
                logs = logs,
                today = today,
                startDate = LocalDate.parse(habit.createdAt),
                habitIds = setOf(habit.id),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalCleanStreak: StateFlow<Int> = combine(
        habitsWithFailures,
        allLogs,
    ) { habits, logs ->
        if (habits.isEmpty()) {
            0
        } else {
            CleanStreakCalculator.calculate(
                logs = logs,
                today = today,
                startDate = habits.minOf { LocalDate.parse(it.habit.createdAt) },
                habitIds = habits.map { it.habit.id }.toSet(),
            )
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logFailure(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.logFailure(habitId, date)
            widgetUpdater.updateAll()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            widgetUpdater.updateAll()
        }
    }

    fun renameHabit(habitId: Long, newName: String) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            repository.updateHabit(habit.copy(name = newName))
            widgetUpdater.updateAll()
        }
    }
}
