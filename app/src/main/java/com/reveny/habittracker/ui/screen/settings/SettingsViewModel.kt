package com.reveny.habittracker.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.Frequency
import com.reveny.habittracker.data.model.TimeOfDay
import com.reveny.habittracker.data.preferences.WidgetPreferencesStore
import com.reveny.habittracker.data.repository.HabitRepository
import com.reveny.habittracker.util.CsvExporter
import com.reveny.habittracker.util.CsvImporter
import com.reveny.habittracker.util.DateUtils
import com.reveny.habittracker.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val widgetPreferencesStore: WidgetPreferencesStore,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    val activeHabits: StateFlow<List<Habit>> = repository.getAllHabits()
        .map { habits -> habits.filter { it.archivedAt == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val widgetHabitId: StateFlow<Long> = widgetPreferencesStore.widgetHabitId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WidgetPreferencesStore.NO_HABIT_SELECTED)

    fun setWidgetHabit(habitId: Long) {
        viewModelScope.launch {
            widgetPreferencesStore.setWidgetHabitId(habitId)
            widgetUpdater.updateAll()
        }
    }

    private val _operationStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val habits = repository.getAllHabits().first()
                val logs = repository.getAllHabitLogs()
                val csv = CsvExporter.buildCsv(habits, logs)
                val success = CsvExporter.writeToUri(context, csv, uri)
                _operationStatus.value = if (success) "Export successful" else "Export failed"
            } catch (e: Exception) {
                _operationStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvText = context.contentResolver.openInputStream(uri)
                    ?.use { it.bufferedReader().readText() }
                    ?: run { _operationStatus.value = "Import failed: couldn't read file"; return@launch }

                val rows = CsvImporter.parse(csvText)
                    ?: run { _operationStatus.value = "Import failed: unrecognised file format"; return@launch }

                if (rows.isEmpty()) {
                    _operationStatus.value = "Import failed: no data rows found in file"
                    return@launch
                }

                // Build a name → Habit map from what's already in the DB
                val nameToHabit = repository.getAllHabits().first()
                    .associateBy { it.name }
                    .toMutableMap()

                var habitsCreated = 0
                var logsImported = 0

                for (row in rows) {
                    // Reuse existing habit or create a new one
                    val habit = nameToHabit.getOrPut(row.habitName) {
                        val newHabit = Habit(
                            name = row.habitName,
                            type = row.habitType,
                            frequency = Frequency.DAILY,
                            timeOfDay = TimeOfDay.MORNING,
                            createdAt = DateUtils.todayIso(),
                        )
                        val id = repository.createHabit(newHabit)
                        habitsCreated++
                        newHabit.copy(id = id)
                    }

                    // insertLogRaw returns -1 if the (habitId, date) pair already existed
                    val rowId = repository.insertLogRaw(habit.id, row.failureDate, row.note)
                    if (rowId != -1L) logsImported++
                }

                widgetUpdater.updateAll()

                _operationStatus.value = buildString {
                    append("Imported $logsImported log")
                    if (logsImported != 1) append("s")
                    if (habitsCreated > 0) {
                        append(", created $habitsCreated new habit")
                        if (habitsCreated != 1) append("s")
                    }
                }
            } catch (e: Exception) {
                _operationStatus.value = "Import failed: ${e.message}"
            }
        }
    }

    fun clearExportStatus() {
        _operationStatus.value = null
    }
}
