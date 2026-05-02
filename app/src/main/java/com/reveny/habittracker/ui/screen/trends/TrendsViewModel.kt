package com.reveny.habittracker.ui.screen.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.MonthlyComparison
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        loadTrends()
    }

    fun selectHabit(habit: Habit?) {
        _uiState.value = _uiState.value.copy(selectedHabit = habit)
        loadTrends()
    }

    private fun loadTrends() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)
            val lastMonth = currentMonth.minusMonths(1)

            val (thisStart, thisEnd) = DateUtils.monthStartEnd(currentMonth)
            val (lastStart, lastEnd) = DateUtils.monthStartEnd(lastMonth)

            val habits = repository.getAllHabits().first()
            val activeHabits = habits.filter { it.archivedAt == null }
            val selectedHabit = _uiState.value.selectedHabit
                ?.takeIf { selected -> activeHabits.any { it.id == selected.id } }
            val focusedHabits = selectedHabit?.let { listOf(it) } ?: activeHabits
            val focusedHabitIds = focusedHabits.map { it.id }.toSet()
            val allActiveLogs = repository.getLogsInRange("2000-01-01", today.toString())
                .filter { it.habitId in focusedHabitIds }
            val firstHabitMonth = focusedHabits
                .minOfOrNull { YearMonth.from(LocalDate.parse(it.createdAt)) }
            val firstLogMonth = allActiveLogs
                .minOfOrNull { YearMonth.from(LocalDate.parse(it.date)) }
            val firstTrackedMonth = listOfNotNull(firstHabitMonth, firstLogMonth)
                .minOrNull()
                ?: currentMonth
            val chartStartMonth = maxOf(firstTrackedMonth, currentMonth.minusMonths(5))
            val displayedMonths = generateSequence(chartStartMonth) { month ->
                month.plusMonths(1).takeIf { !it.isAfter(currentMonth) }
            }.toList()
            val thisMonthLogs = repository.getLogsInRange(thisStart, thisEnd)
                .filter { it.habitId in focusedHabitIds }
            val lastMonthLogs = repository.getLogsInRange(lastStart, lastEnd)
                .filter { it.habitId in focusedHabitIds }
            val monthlyTotals = displayedMonths.map { month ->
                val (start, end) = DateUtils.monthStartEnd(month)
                MonthlyTotal(
                    label = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    failures = allActiveLogs.count { it.date in start..end },
                    isCurrentMonth = month == currentMonth,
                )
            }

            val comparisons = focusedHabits.map { habit ->
                MonthlyComparison(
                    habitName = habit.name,
                    thisMonthFailures = thisMonthLogs.count { it.habitId == habit.id },
                    lastMonthFailures = lastMonthLogs.count { it.habitId == habit.id },
                )
            }

            val totalThisMonth = thisMonthLogs.size
            val totalLastMonth = lastMonthLogs.size
            val overallChange = if (totalLastMonth == 0) {
                if (totalThisMonth > 0) 100 else 0
            } else {
                ((totalLastMonth - totalThisMonth).toFloat() / totalLastMonth * 100).toInt()
            }

            _uiState.value = TrendsUiState(
                comparisons = comparisons,
                totalFailuresThisMonth = totalThisMonth,
                totalFailuresLastMonth = totalLastMonth,
                overallChangePercent = overallChange,
                activeHabitCount = activeHabits.size,
                currentMonthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                previousMonthLabel = lastMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                monthlyTotals = monthlyTotals,
                habits = activeHabits,
                selectedHabit = selectedHabit,
            )
        }
    }
}

data class TrendsUiState(
    val comparisons: List<MonthlyComparison> = emptyList(),
    val totalFailuresThisMonth: Int = 0,
    val totalFailuresLastMonth: Int = 0,
    val overallChangePercent: Int = 0,
    val activeHabitCount: Int = 0,
    val currentMonthLabel: String = "",
    val previousMonthLabel: String = "",
    val monthlyTotals: List<MonthlyTotal> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val selectedHabit: Habit? = null,
)

data class MonthlyTotal(
    val label: String,
    val failures: Int,
    val isCurrentMonth: Boolean,
)
