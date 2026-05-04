package com.reveny.habittracker.di

import android.content.Context
import androidx.room.Room
import com.reveny.habittracker.data.local.HabitDatabase
import com.reveny.habittracker.data.local.dao.HabitDao
import com.reveny.habittracker.data.local.dao.HabitLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_tracker.db",
        )
            .addMigrations(
                HabitDatabase.MIGRATION_2_3,
                HabitDatabase.MIGRATION_3_4,
                HabitDatabase.MIGRATION_4_5,
            )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitLogDao(database: HabitDatabase): HabitLogDao = database.habitLogDao()
}
