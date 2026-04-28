package com.reveny.habittracker.widget

import com.reveny.habittracker.data.preferences.WidgetPreferencesStore
import com.reveny.habittracker.data.repository.HabitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitRepository(): HabitRepository
    fun widgetPreferencesStore(): WidgetPreferencesStore
}
