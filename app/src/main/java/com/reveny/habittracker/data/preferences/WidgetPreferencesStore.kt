package com.reveny.habittracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_preferences",
)

@Singleton
class WidgetPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        val WIDGET_HABIT_ID_KEY = longPreferencesKey("widget_habit_id")
        const val NO_HABIT_SELECTED = -1L
    }

    val widgetHabitId: Flow<Long> = context.widgetDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[WIDGET_HABIT_ID_KEY] ?: NO_HABIT_SELECTED }

    suspend fun setWidgetHabitId(id: Long) {
        context.widgetDataStore.edit { prefs ->
            prefs[WIDGET_HABIT_ID_KEY] = id
        }
    }
}
