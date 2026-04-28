package com.reveny.habittracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.reveny.habittracker.ui.screen.addhabit.AddHabitScreen
import com.reveny.habittracker.ui.screen.calendar.CalendarScreen
import com.reveny.habittracker.ui.screen.settings.SettingsScreen
import com.reveny.habittracker.ui.screen.today.TodayScreen
import com.reveny.habittracker.ui.screen.trends.TrendsScreen
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Cream

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

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
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.Trends.route) { TrendsScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.AddHabit.route) {
                AddHabitScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
