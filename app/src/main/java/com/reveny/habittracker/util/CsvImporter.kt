package com.reveny.habittracker.util

import com.reveny.habittracker.data.model.HabitType

data class CsvRow(
    val habitName: String,
    val habitType: HabitType,
    val failureDate: String,
)

object CsvImporter {

    private val EXPECTED_HEADER = "habit_id,habit_name,type,failure_date"

    fun parse(csv: String): List<CsvRow>? {
        val lines = csv.lines()
        val header = lines.firstOrNull()?.trim() ?: return null
        if (header != EXPECTED_HEADER) return null

        return lines.drop(1).mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            parseLine(line)
        }
    }

    private fun parseLine(line: String): CsvRow? {
        val fields = splitCsvLine(line)
        if (fields.size < 4) return null

        val habitName = fields[1].trim().removeSurrounding("\"").ifBlank { return null }
        val habitType = try {
            HabitType.valueOf(fields[2].trim())
        } catch (_: IllegalArgumentException) {
            HabitType.QUIT
        }
        val date = fields[3].trim().ifBlank { return null }

        return CsvRow(habitName, habitType, date)
    }

    private fun splitCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        fields.add(current.toString())
        return fields
    }
}
