package com.reveny.habittracker.ui.screen.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.Frequency
import com.reveny.habittracker.data.model.HabitType
import com.reveny.habittracker.data.model.TimeOfDay
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateType(type: HabitType) = _uiState.update { it.copy(type = type) }
    fun updateFrequency(frequency: Frequency) = _uiState.update { it.copy(frequency = frequency) }
    fun updateTimeOfDay(timeOfDay: TimeOfDay) = _uiState.update { it.copy(timeOfDay = timeOfDay) }
    fun updateMotivation(motivation: String) = _uiState.update { it.copy(motivation = motivation) }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Please enter a habit name") }
            return
        }
        viewModelScope.launch {
            repository.createHabit(
                Habit(
                    name = state.name.trim(),
                    type = state.type,
                    frequency = state.frequency,
                    timeOfDay = state.timeOfDay,
                    motivation = state.motivation.trim(),
                    reminderEnabled = false,
                    createdAt = DateUtils.todayIso(),
                )
            )
            onSuccess()
        }
    }
}

data class AddHabitUiState(
    val name: String = "",
    val type: HabitType = HabitType.BUILD,
    val frequency: Frequency = Frequency.DAILY,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val motivation: String = "",
    val nameError: String? = null,
)
