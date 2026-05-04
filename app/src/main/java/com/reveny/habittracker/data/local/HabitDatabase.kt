package com.reveny.habittracker.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reveny.habittracker.data.local.converter.Converters
import com.reveny.habittracker.data.local.dao.HabitDao
import com.reveny.habittracker.data.local.dao.HabitLogDao
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habit_logs ADD COLUMN note TEXT")
            }
        }
    }
}
