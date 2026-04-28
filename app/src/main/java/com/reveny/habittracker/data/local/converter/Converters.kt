package com.reveny.habittracker.data.local.converter

import androidx.room.TypeConverter
import com.reveny.habittracker.data.model.Frequency
import com.reveny.habittracker.data.model.HabitType
import com.reveny.habittracker.data.model.TimeOfDay

class Converters {
    @TypeConverter fun fromHabitType(value: HabitType): String = value.name
    @TypeConverter fun toHabitType(value: String): HabitType = HabitType.valueOf(value)

    @TypeConverter fun fromFrequency(value: Frequency): String = value.name
    @TypeConverter fun toFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter fun fromTimeOfDay(value: TimeOfDay): String = value.name
    @TypeConverter fun toTimeOfDay(value: String): TimeOfDay = TimeOfDay.valueOf(value)
}
