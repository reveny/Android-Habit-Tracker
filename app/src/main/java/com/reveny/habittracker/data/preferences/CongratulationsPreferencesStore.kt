package com.reveny.habittracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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

    private val lastShownWeeklyReviewEndDate: Flow<String?> = context.congratulationsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[LAST_SHOWN_WEEKLY_REVIEW_END_DATE_KEY] }

    private val lastShownMonthlyReviewMonth: Flow<String?> = context.congratulationsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[LAST_SHOWN_MONTHLY_REVIEW_MONTH_KEY] }

    val onboardingComplete: Flow<Boolean?> = context.congratulationsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[ONBOARDING_COMPLETE_KEY] }

    suspend fun getOnboardingComplete(): Boolean {
        return onboardingComplete.first() ?: false
    }

    suspend fun getHighestShownMilestone(): Int {
        return highestShownMilestone.first()
    }

    suspend fun setHighestShownMilestone(milestone: Int) {
        context.congratulationsDataStore.edit { prefs ->
            prefs[HIGHEST_SHOWN_MILESTONE_KEY] = milestone
        }
    }

    suspend fun getLastShownWeeklyReviewEndDate(): String? {
        return lastShownWeeklyReviewEndDate.first()
    }

    suspend fun setLastShownWeeklyReviewEndDate(date: String) {
        context.congratulationsDataStore.edit { prefs ->
            prefs[LAST_SHOWN_WEEKLY_REVIEW_END_DATE_KEY] = date
        }
    }

    suspend fun getLastShownMonthlyReviewMonth(): String? {
        return lastShownMonthlyReviewMonth.first()
    }

    suspend fun setLastShownMonthlyReviewMonth(month: String) {
        context.congratulationsDataStore.edit { prefs ->
            prefs[LAST_SHOWN_MONTHLY_REVIEW_MONTH_KEY] = month
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.congratulationsDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = complete
        }
    }

    private companion object {
        val HIGHEST_SHOWN_MILESTONE_KEY = intPreferencesKey("highest_shown_milestone")
        val LAST_SHOWN_WEEKLY_REVIEW_END_DATE_KEY = stringPreferencesKey("last_shown_weekly_review_end_date")
        val LAST_SHOWN_MONTHLY_REVIEW_MONTH_KEY = stringPreferencesKey("last_shown_monthly_review_month")
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    }
}
