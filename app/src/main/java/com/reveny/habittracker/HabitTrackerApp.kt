package com.reveny.habittracker

import android.app.Application
import com.reveny.habittracker.notification.HabitReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApp : Application() {
    @Inject lateinit var habitReminderScheduler: HabitReminderScheduler

    override fun onCreate() {
        super.onCreate()
        habitReminderScheduler.ensureChannel()
    }
}
