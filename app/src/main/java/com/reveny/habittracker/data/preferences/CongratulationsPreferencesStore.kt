package com.reveny.habittracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.congratulationsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "congratulations_preferences",
)

@Singleton
class CongratulationsPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val highestShownMilestone: Flow<Int> = context.congratulationsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[HIGHEST_SHOWN_MILESTONE_KEY] ?: 0 }

    suspend fun getHighestShownMilestone(): Int {
        return highestShownMilestone.first()
    }

    suspend fun setHighestShownMilestone(milestone: Int) {
        context.congratulationsDataStore.edit { prefs ->
            prefs[HIGHEST_SHOWN_MILESTONE_KEY] = milestone
        }
    }

    private companion object {
        val HIGHEST_SHOWN_MILESTONE_KEY = intPreferencesKey("highest_shown_milestone")
    }
}
