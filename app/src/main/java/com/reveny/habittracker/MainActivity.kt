package com.reveny.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.reveny.habittracker.ui.navigation.NavGraph
import com.reveny.habittracker.ui.screen.congratulations.CongratulationsScreen
import com.reveny.habittracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                val congratulationsMilestone by viewModel.congratulationsMilestone.collectAsState()

                val milestone = congratulationsMilestone
                if (milestone != null) {
                    CongratulationsScreen(
                        streakDays = milestone.streakDays,
                        habitName = milestone.habitName,
                        onContinue = viewModel::markCongratulationsShown,
                    )
                } else {
                    NavGraph()
                }
            }
        }
    }
}
