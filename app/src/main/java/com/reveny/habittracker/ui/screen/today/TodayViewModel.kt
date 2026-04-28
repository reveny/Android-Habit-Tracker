package com.reveny.habittracker.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    val habitsWithFailures: StateFlow<List<HabitWithLogs>> = repository
        .getActiveHabitsWithFailures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
