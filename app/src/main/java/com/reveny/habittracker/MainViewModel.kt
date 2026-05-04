package com.reveny.habittracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.Frequency
import com.reveny.habittracker.data.model.TimeOfDay
import com.reveny.habittracker.data.preferences.CongratulationsPreferencesStore
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.CleanStreakCalculator
import com.reveny.habittracker.util.CsvImporter
import com.reveny.habittracker.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val congratulationsPreferencesStore: CongratulationsPreferencesStore,
) : ViewModel() {

    private val _congratulationsMilestone = MutableStateFlow<CongratulationsMilestone?>(null)
    val congratulationsMilestone: StateFlow<CongratulationsMilestone?> =
        _congratulationsMilestone.asStateFlow()

    private val _weeklyReview = MutableStateFlow<WeeklyReview?>(null)
    val weeklyReview: StateFlow<WeeklyReview?> = _weeklyReview.asStateFlow()

    private val _monthlyReview = MutableStateFlow<MonthlyReview?>(null)
    val monthlyReview: StateFlow<MonthlyReview?> = _monthlyReview.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    init {
        checkLaunchOverlays()
    }

    fun markCongratulationsShown() {
        val milestone = _congratulationsMilestone.value ?: return
        viewModelScope.launch {
            congratulationsPreferencesStore.setHighestShownMilestone(milestone.milestoneDays)
            _congratulationsMilestone.value = null
        }
    }

    fun markWeeklyReviewShown() {
        val review = _weeklyReview.value ?: return
        viewModelScope.launch {
            congratulationsPreferencesStore.setLastShownWeeklyReviewEndDate(review.weekEndDate)
            _weeklyReview.value = null
        }
    }

    fun markMonthlyReviewShown() {
        val review = _monthlyReview.value ?: return
        viewModelScope.launch {
            congratulationsPreferencesStore.setLastShownMonthlyReviewMonth(review.monthKey)
            _monthlyReview.value = null
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            congratulationsPreferencesStore.setOnboardingComplete(true)
            _showOnboarding.value = false
        }
    }

    fun importOnboardingData(context: Context, uri: Uri) {
        viewModelScope.launch {
            val csvText = context.contentResolver.openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
                ?: return@launch
            val rows = CsvImporter.parse(csvText) ?: return@launch
            val nameToHabit = repository.getAllHabits().first()
                .associateBy { it.name }
                .toMutableMap()

            rows.forEach { row ->
                val habit = nameToHabit.getOrPut(row.habitName) {
                    val newHabit = Habit(
                        name = row.habitName,
                        type = row.habitType,
                        frequency = Frequency.DAILY,
                        timeOfDay = TimeOfDay.MORNING,
                        createdAt = DateUtils.todayIso(),
                    )
                    val id = repository.createHabit(newHabit)
                    newHabit.copy(id = id)
                }
                repository.insertLogRaw(habit.id, row.failureDate, row.note)
            }
            congratulationsPreferencesStore.setOnboardingComplete(true)
            _showOnboarding.value = false
        }
    }

    private fun checkLaunchOverlays() {
        viewModelScope.launch {
            val allHabits = repository.getAllHabits().first()
            if (allHabits.isNotEmpty()) {
                congratulationsPreferencesStore.setOnboardingComplete(true)
            } else if (!congratulationsPreferencesStore.getOnboardingComplete()) {
                _showOnboarding.value = true
                return@launch
            }
            val activeHabits = allHabits.filter { it.archivedAt == null }
            if (activeHabits.isEmpty()) return@launch

            val today = LocalDate.now()
            val activeHabitIds = activeHabits.map { it.id }.toSet()
            val logs = repository.getLogsInRange("2000-01-01", today.toString())
                .filter { it.habitId in activeHabitIds }
            checkCongratulationsMilestone(activeHabits, logs, today)
            checkWeeklyReview(activeHabits, logs, today)
            checkMonthlyReview(activeHabits, logs, today)
        }
    }

    private suspend fun checkCongratulationsMilestone(
        activeHabits: List<com.reveny.habittracker.data.local.entity.Habit>,
        logs: List<com.reveny.habittracker.data.local.entity.HabitLog>,
        today: LocalDate,
    ) {
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
            } ?: return

            val milestoneDays = milestoneFor(bestStreak.streakDays) ?: return
            val highestShownMilestone = congratulationsPreferencesStore.getHighestShownMilestone()
            if (milestoneDays <= highestShownMilestone) return

            _congratulationsMilestone.value = CongratulationsMilestone(
                milestoneDays = milestoneDays,
                streakDays = bestStreak.streakDays,
                habitName = bestStreak.habitName,
            )
    }

    private suspend fun checkWeeklyReview(
        activeHabits: List<com.reveny.habittracker.data.local.entity.Habit>,
        logs: List<com.reveny.habittracker.data.local.entity.HabitLog>,
        today: LocalDate,
    ) {
        val weekEnd = latestSunday(today)
        val weekStart = weekEnd.minusDays(6)
        val weekEndIso = weekEnd.toString()
        if (congratulationsPreferencesStore.getLastShownWeeklyReviewEndDate() == weekEndIso) return

        val earliestHabitDate = activeHabits.minOfOrNull { LocalDate.parse(it.createdAt) }
        val earliestLogDate = logs.minOfOrNull { LocalDate.parse(it.date) }
        val earliestTrackedDate = listOfNotNull(earliestHabitDate, earliestLogDate).minOrNull() ?: return
        if (earliestTrackedDate.isAfter(weekStart)) return

        val review = withContext(Dispatchers.Default) {
            val weekLogs = logs.filter { it.date in weekStart.toString()..weekEndIso }
            val failureDates = weekLogs.map { it.date }.toSet()
            val failureCount = weekLogs.size
            val failureFreeDays = 7 - failureDates.size
            val habitById = activeHabits.associateBy { it.id }
            val mostLoggedHabit = weekLogs
                .groupingBy { it.habitId }
                .eachCount()
                .maxByOrNull { it.value }
                ?.let { (habitId, count) -> habitById[habitId]?.name to count }

            WeeklyReview(
                weekEndDate = weekEndIso,
                dateRangeLabel = formatWeekRange(weekStart, weekEnd),
                failureCount = failureCount,
                failureFreeDays = failureFreeDays,
                focusText = when {
                    failureCount == 0 -> "No failures logged this week. That is a clean run."
                    mostLoggedHabit?.first != null -> "${mostLoggedHabit.first} came up ${mostLoggedHabit.second} ${if (mostLoggedHabit.second == 1) "time" else "times"} this week."
                    else -> "You logged $failureCount ${if (failureCount == 1) "failure" else "failures"} this week."
                },
            )
        }
        _weeklyReview.value = review
    }

    private suspend fun checkMonthlyReview(
        activeHabits: List<com.reveny.habittracker.data.local.entity.Habit>,
        logs: List<com.reveny.habittracker.data.local.entity.HabitLog>,
        today: LocalDate,
    ) {
        val reviewedMonth = YearMonth.from(today).minusMonths(1)
        val monthKey = reviewedMonth.toString()
        if (congratulationsPreferencesStore.getLastShownMonthlyReviewMonth() == monthKey) return

        val earliestHabitDate = activeHabits.minOfOrNull { LocalDate.parse(it.createdAt) }
        val earliestLogDate = logs.minOfOrNull { LocalDate.parse(it.date) }
        val earliestTrackedDate = listOfNotNull(earliestHabitDate, earliestLogDate).minOrNull() ?: return
        if (YearMonth.from(earliestTrackedDate).isAfter(reviewedMonth)) return

        val review = withContext(Dispatchers.Default) {
            val monthStart = reviewedMonth.atDay(1)
            val monthEnd = reviewedMonth.atEndOfMonth()
            val monthLogs = logs.filter { it.date in monthStart.toString()..monthEnd.toString() }
            val failureDates = monthLogs.map { it.date }.toSet()
            val failureCount = monthLogs.size
            val failureFreeDays = reviewedMonth.lengthOfMonth() - failureDates.size
            val habitById = activeHabits.associateBy { it.id }
            val mostLoggedHabit = monthLogs
                .groupingBy { it.habitId }
                .eachCount()
                .maxByOrNull { it.value }
                ?.let { (habitId, count) -> habitById[habitId]?.name to count }

            MonthlyReview(
                monthKey = monthKey,
                monthLabel = reviewedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                failureCount = failureCount,
                failureFreeDays = failureFreeDays,
                focusText = when {
                    failureCount == 0 -> "No failures logged this month. That is a strong reset."
                    mostLoggedHabit?.first != null -> "${mostLoggedHabit.first} was the main pattern with ${mostLoggedHabit.second} ${if (mostLoggedHabit.second == 1) "failure" else "failures"}."
                    else -> "You logged $failureCount ${if (failureCount == 1) "failure" else "failures"} this month."
                },
            )
        }
        _monthlyReview.value = review
    }

    private fun milestoneFor(streakDays: Int): Int? {
        return when {
            streakDays >= 90 -> 90
            streakDays >= 60 -> 60
            streakDays >= 30 -> 30
            else -> null
        }
    }

    private fun latestSunday(today: LocalDate): LocalDate {
        val daysSinceSunday = if (today.dayOfWeek == DayOfWeek.SUNDAY) 0L else today.dayOfWeek.value.toLong()
        return today.minusDays(daysSinceSunday)
    }

    private fun formatWeekRange(start: LocalDate, end: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        return "${start.format(formatter)} - ${end.format(formatter)}"
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

data class WeeklyReview(
    val weekEndDate: String,
    val dateRangeLabel: String,
    val failureCount: Int,
    val failureFreeDays: Int,
    val focusText: String,
)

data class MonthlyReview(
    val monthKey: String,
    val monthLabel: String,
    val failureCount: Int,
    val failureFreeDays: Int,
    val focusText: String,
)
