package com.reveny.habittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reveny.habittracker.data.model.Frequency
import com.reveny.habittracker.data.model.HabitType
import com.reveny.habittracker.data.model.TimeOfDay

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: HabitType,
    val frequency: Frequency,
    val timeOfDay: TimeOfDay,
    val motivation: String = "",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val createdAt: String,
    val archivedAt: String? = null,
)
