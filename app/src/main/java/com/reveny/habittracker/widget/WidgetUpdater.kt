package com.reveny.habittracker.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun updateAll() {
        try {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(HabitWidget::class.java)
                .forEach { HabitWidget().update(context, it) }
            manager.getGlanceIds(AllHabitsWidget::class.java)
                .forEach { AllHabitsWidget().update(context, it) }
        } catch (_: Exception) {
        }
    }
}
