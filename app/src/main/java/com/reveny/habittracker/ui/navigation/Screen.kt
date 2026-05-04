package com.reveny.habittracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    data object Today : Screen("today", "Today", Icons.Default.Today)
    data object Trends : Screen("trends", "Trends", Icons.Default.BarChart)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object AddHabit : Screen("add_habit", "Add Habit", null)
    data object HabitDetail : Screen("habit_detail/{habitId}", "Habit Detail", null) {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}

val bottomNavItems = listOf(Screen.Today, Screen.Trends, Screen.Calendar, Screen.Settings)
