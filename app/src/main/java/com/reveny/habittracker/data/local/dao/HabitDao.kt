package com.reveny.habittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.reveny.habittracker.data.local.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Query("SELECT * FROM habits WHERE archivedAt IS NULL ORDER BY createdAt DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): Habit?

    @Query("UPDATE habits SET archivedAt = :archivedAt WHERE id = :id")
    suspend fun archive(id: Long, archivedAt: String)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: Long)
}
