package com.reveny.habittracker.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.model.HabitType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily reminders for habits you want to build"
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun schedule(habit: Habit) {
        if (
            habit.type != HabitType.BUILD ||
            !habit.reminderEnabled ||
            habit.reminderHour == null ||
            habit.reminderMinute == null
        ) {
            cancel(habit.id)
            return
        }

        ensureChannel()

        val triggerAtMillis = nextReminderTimeMillis(habit.reminderHour, habit.reminderMinute)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            reminderIntent(habit.id, habit.name),
        )
    }

    fun cancel(habitId: Long) {
        alarmManager.cancel(reminderIntent(habitId, ""))
    }

    private fun reminderIntent(habitId: Long, habitName: String): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
        }
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextReminderTimeMillis(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }
}
