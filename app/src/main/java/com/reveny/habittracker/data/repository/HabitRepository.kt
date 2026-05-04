package com.reveny.habittracker.data.repository

import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.data.model.MonthlyComparison
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getActiveHabitsWithFailures(): Flow<List<HabitWithLogs>>
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun createHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun archiveHabit(id: Long, archivedAt: String)
    suspend fun deleteHabit(id: Long)
    suspend fun logFailure(habitId: Long, date: String, note: String? = null)
    suspend fun insertLogRaw(habitId: Long, date: String, note: String? = null): Long
    suspend fun removeFailure(habitId: Long, date: String)
    suspend fun toggleFailure(habitId: Long, date: String): Boolean
    suspend fun hasFailure(habitId: Long, date: String): Boolean
    suspend fun getLogsInRange(startDate: String, endDate: String): List<HabitLog>
    fun getLogsInRangeFlow(startDate: String, endDate: String): Flow<List<HabitLog>>
    suspend fun getMonthlyComparisons(): List<MonthlyComparison>
    suspend fun getAllHabitLogs(): List<HabitLog>
}
