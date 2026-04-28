package com.reveny.habittracker.ui.screen.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private fun loadTrends() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)
            val lastMonth = currentMonth.minusMonths(1)

            val (thisStart, thisEnd) = DateUtils.monthStartEnd(currentMonth)
            val (lastStart, lastEnd) = DateUtils.monthStartEnd(lastMonth)

            val thisMonthLogs = repository.getLogsInRange(thisStart, thisEnd)
            val lastMonthLogs = repository.getLogsInRange(lastStart, lastEnd)
            val habits = repository.getAllHabits().first()

            val comparisons = habits
                .filter { it.archivedAt == null }
                .map { habit ->
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
                activeHabitCount = habits.count { it.archivedAt == null },
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
)
