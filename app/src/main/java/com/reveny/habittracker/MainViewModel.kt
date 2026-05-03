package com.reveny.habittracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.preferences.CongratulationsPreferencesStore
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.CleanStreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val congratulationsPreferencesStore: CongratulationsPreferencesStore,
) : ViewModel() {

    private val _congratulationsMilestone = MutableStateFlow<CongratulationsMilestone?>(null)
    val congratulationsMilestone: StateFlow<CongratulationsMilestone?> =
        _congratulationsMilestone.asStateFlow()

    init {
        checkCongratulationsMilestone()
    }

    fun markCongratulationsShown() {
        val milestone = _congratulationsMilestone.value ?: return
        viewModelScope.launch {
            congratulationsPreferencesStore.setHighestShownMilestone(milestone.milestoneDays)
            _congratulationsMilestone.value = null
        }
    }

    private fun checkCongratulationsMilestone() {
        viewModelScope.launch {
            val activeHabits = repository.getActiveHabitsWithFailures()
                .first()
                .map { it.habit }
            if (activeHabits.isEmpty()) return@launch

            val today = LocalDate.now()
            val activeHabitIds = activeHabits.map { it.id }.toSet()
            val logs = repository.getLogsInRange("2000-01-01", today.toString())
                .filter { it.habitId in activeHabitIds }
            val bestStreak = withContext(Dispatchers.Default) {
                activeHabits
                    .map { habit ->
                        CongratulationsMilestoneCandidate(
                            habitName = habit.name,
                            streakDays = CleanStreakCalculator.calculate(
                                logs = logs,
                                today = today,
                                startDate = LocalDate.parse(habit.createdAt),
                                habitIds = setOf(habit.id),
                            ),
                        )
                    }
                    .maxByOrNull { it.streakDays }
            } ?: return@launch

            val milestoneDays = milestoneFor(bestStreak.streakDays) ?: return@launch
            val highestShownMilestone = congratulationsPreferencesStore.getHighestShownMilestone()
            if (milestoneDays <= highestShownMilestone) return@launch

            _congratulationsMilestone.value = CongratulationsMilestone(
                milestoneDays = milestoneDays,
                streakDays = bestStreak.streakDays,
                habitName = bestStreak.habitName,
            )
        }
    }

    private fun milestoneFor(streakDays: Int): Int? {
        return when {
            streakDays >= 90 -> 90
            streakDays >= 60 -> 60
            streakDays >= 30 -> 30
            else -> null
        }
    }
}

data class CongratulationsMilestone(
    val milestoneDays: Int,
    val streakDays: Int,
    val habitName: String,
)

private data class CongratulationsMilestoneCandidate(
    val habitName: String,
    val streakDays: Int,
)
