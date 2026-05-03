package com.reveny.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reveny.habittracker.data.repository.HabitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ReminderBootEntryPoint::class.java,
                )
                val scheduler = entryPoint.habitReminderScheduler()
                entryPoint.habitRepository()
                    .getActiveHabitsWithFailures()
                    .first()
                    .forEach { scheduler.schedule(it.habit) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderBootEntryPoint {
    fun habitRepository(): HabitRepository
    fun habitReminderScheduler(): HabitReminderScheduler
}
