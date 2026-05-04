package com.reveny.habittracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.reveny.habittracker.ui.screen.addhabit.AddHabitScreen
import com.reveny.habittracker.ui.screen.calendar.CalendarScreen
import com.reveny.habittracker.ui.screen.calendar.CalendarViewModel
import com.reveny.habittracker.ui.screen.settings.SettingsScreen
import com.reveny.habittracker.ui.screen.settings.SettingsViewModel
import com.reveny.habittracker.ui.screen.today.TodayScreen
import com.reveny.habittracker.ui.screen.today.TodayViewModel
import com.reveny.habittracker.ui.screen.trends.TrendsScreen
import com.reveny.habittracker.ui.screen.trends.TrendsViewModel
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Cream

@Composable
fun NavGraph(
    startOnAddHabit: Boolean = false,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val todayViewModel: TodayViewModel = hiltViewModel()
    var prewarmTabs by remember { mutableStateOf(false) }
    val trendsViewModel: TrendsViewModel? = if (prewarmTabs) hiltViewModel() else null
    val calendarViewModel: CalendarViewModel? = if (prewarmTabs) hiltViewModel() else null
    val settingsViewModel: SettingsViewModel? = if (prewarmTabs) hiltViewModel() else null

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        prewarmTabs = true
    }

    LaunchedEffect(startOnAddHabit) {
        if (startOnAddHabit) {
            navController.navigate(Screen.AddHabit.route)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddHabit.route) },
                    containerColor = Sage,
                    contentColor = Cream,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Today.route) { TodayScreen(todayViewModel) }
            composable(Screen.Trends.route) { TrendsScreen(trendsViewModel ?: hiltViewModel()) }
            composable(Screen.Calendar.route) { CalendarScreen(calendarViewModel ?: hiltViewModel()) }
            composable(Screen.Settings.route) { SettingsScreen(settingsViewModel ?: hiltViewModel()) }
            composable(Screen.AddHabit.route) {
                AddHabitScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
