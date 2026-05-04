package com.reveny.habittracker.util

import android.content.Context
import android.net.Uri
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog

object CsvExporter {

    fun buildCsv(habits: List<Habit>, logs: List<HabitLog>): String {
        val sb = StringBuilder()
        sb.appendLine("habit_id,habit_name,type,failure_date,note")
        val habitMap = habits.associateBy { it.id }
        logs.sortedBy { it.date }.forEach { log ->
            val habit = habitMap[log.habitId]
            sb.appendLine(
                listOf(
                    log.habitId.toString(),
                    escapeCsv(habit?.name ?: "Unknown"),
                    habit?.type?.name.orEmpty(),
                    log.date,
                    escapeCsv(log.note.orEmpty()),
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"$escaped\""
        } else {
            escaped
        }
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
