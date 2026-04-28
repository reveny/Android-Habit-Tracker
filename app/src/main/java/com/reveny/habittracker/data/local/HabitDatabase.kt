package com.reveny.habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.reveny.habittracker.data.local.converter.Converters
import com.reveny.habittracker.data.local.dao.HabitDao
import com.reveny.habittracker.data.local.dao.HabitLogDao
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
}
