package com.reveny.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.reveny.habittracker.data.local.entity.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: HabitLog): Long

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun countFailuresInRange(habitId: Long, startDate: String, endDate: String): Int

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getLogsForHabitInRange(habitId: Long, startDate: String, endDate: String): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getLogsInRange(startDate: String, endDate: String): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getLogsInRangeFlow(startDate: String, endDate: String): Flow<List<HabitLog>>

    @Query("SELECT EXISTS(SELECT 1 FROM habit_logs WHERE habitId = :habitId AND date = :date)")
    suspend fun exists(habitId: Long, date: String): Boolean

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("DELETE FROM habit_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
