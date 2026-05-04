package com.reveny.habittracker

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.reveny.habittracker.ui.navigation.NavGraph
import com.reveny.habittracker.ui.screen.congratulations.CongratulationsScreen
import com.reveny.habittracker.ui.screen.monthlyreview.MonthlyReviewScreen
import com.reveny.habittracker.ui.screen.onboarding.OnboardingScreen
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
                val context = LocalContext.current
                var startOnAddHabit by remember { mutableStateOf(false) }
                val showOnboarding by viewModel.showOnboarding.collectAsState()
                val congratulationsMilestone by viewModel.congratulationsMilestone.collectAsState()
                val weeklyReview by viewModel.weeklyReview.collectAsState()
                val monthlyReview by viewModel.monthlyReview.collectAsState()
                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                ) { uri ->
                    uri?.let { viewModel.importOnboardingData(context, it) }
                }

                val milestone = congratulationsMilestone
                val review = weeklyReview
                val monthReview = monthlyReview
                when {
                    showOnboarding -> {
                        OnboardingScreen(
                            onCreateHabit = {
                                startOnAddHabit = true
                                viewModel.completeOnboarding()
                            },
                            onImportData = {
                                importLauncher.launch(
                                    arrayOf(
                                        "text/csv",
                                        "text/comma-separated-values",
                                        "application/octet-stream",
                                        "*/*",
                                    )
                                )
                            },
                        )
                    }
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
                    else -> NavGraph(startOnAddHabit = startOnAddHabit)
                }
            }
        }
    }
}
