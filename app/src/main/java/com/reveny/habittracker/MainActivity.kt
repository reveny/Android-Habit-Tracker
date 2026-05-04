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
import com.reveny.habittracker.ui.screen.monthlyreview.MonthlyReviewScreen
import com.reveny.habittracker.ui.theme.HabitTrackerTheme
import com.reveny.habittracker.ui.screen.weeklyreview.WeeklyReviewScreen
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
                val weeklyReview by viewModel.weeklyReview.collectAsState()
                val monthlyReview by viewModel.monthlyReview.collectAsState()

                val milestone = congratulationsMilestone
                val review = weeklyReview
                val monthReview = monthlyReview
                when {
                    milestone != null -> {
                        CongratulationsScreen(
                            streakDays = milestone.streakDays,
                            habitName = milestone.habitName,
                            onContinue = viewModel::markCongratulationsShown,
                        )
                    }
                    review != null -> {
                        WeeklyReviewScreen(
                            review = review,
                            onContinue = viewModel::markWeeklyReviewShown,
                        )
                    }
                    monthReview != null -> {
                        MonthlyReviewScreen(
                            review = monthReview,
                            onContinue = viewModel::markMonthlyReviewShown,
                        )
                    }
                    else -> NavGraph()
                }
            }
        }
    }
}
