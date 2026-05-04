package com.reveny.habittracker.util

import com.reveny.habittracker.data.model.HabitType

data class CsvRow(
    val habitName: String,
    val habitType: HabitType,
    val failureDate: String,
    val failureTime: String? = null,
    val note: String? = null,
)

object CsvImporter {

    private val SUPPORTED_HEADERS = setOf(
        "habit_id,habit_name,type,failure_date",
        "habit_id,habit_name,type,failure_date,note",
        "habit_id,habit_name,type,failure_date,failure_time,note",
    )

    fun parse(csv: String): List<CsvRow>? {
        val lines = csv.lines()
        val header = lines.firstOrNull()?.trim() ?: return null
        if (header !in SUPPORTED_HEADERS) return null

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
        val hasFailureTime = fields.size >= 6
        val failureTime = if (hasFailureTime) {
            fields.getOrNull(4)?.trim()?.takeIf { it.matches(TIME_PATTERN) }
        } else {
            null
        }
        val noteIndex = if (hasFailureTime) 5 else 4
        val note = fields.getOrNull(noteIndex)?.trim()?.takeIf { it.isNotEmpty() }

        return CsvRow(habitName, habitType, date, failureTime, note)
    }

    private fun splitCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '"' && inQuotes && line.getOrNull(index + 1) == '"' -> {
                    current.append('"')
                    index++
                }
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            index++
        }
        fields.add(current.toString())
        return fields
    }

    private val TIME_PATTERN = Regex("""\d{2}:\d{2}""")
}
