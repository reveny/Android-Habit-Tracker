package com.reveny.habittracker.util

import android.content.Context
import android.net.Uri
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog

object CsvExporter {

    fun buildCsv(habits: List<Habit>, logs: List<HabitLog>): String {
        val sb = StringBuilder()
        sb.appendLine("habit_id,habit_name,type,failure_date")
        val habitMap = habits.associateBy { it.id }
        logs.sortedBy { it.date }.forEach { log ->
            val habit = habitMap[log.habitId]
            sb.appendLine("${log.habitId},\"${habit?.name ?: "Unknown"}\",${habit?.type ?: ""},${log.date}")
        }
        return sb.toString()
    }

    fun writeToUri(context: Context, csv: String, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(csv.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
