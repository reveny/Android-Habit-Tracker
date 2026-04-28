package com.reveny.habittracker.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.reveny.habittracker.MainActivity

@Composable
internal fun ErrorContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(12.dp)
            .padding(8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Tap to open",
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp),
        )
    }
}

@Composable
internal fun NoHabitSelectedContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(12.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Select a habit in Settings",
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp),
        )
    }
}

@Composable
internal fun HabitStatsRow(totalFails: Int, cleanStreak: Int, failLabel: String) {
    val accentColor = if (totalFails == 0) GlanceTheme.colors.primary else GlanceTheme.colors.error

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(12.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$totalFails",
            style = TextStyle(
                color = accentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = failLabel,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 11.sp),
            )
            Text(
                text = if (cleanStreak == 0) "Failed today" else "${cleanStreak}d clean",
                style = TextStyle(
                    color = if (cleanStreak == 0) GlanceTheme.colors.error else GlanceTheme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
internal fun AllHabitsStatsRow(totalFails: Int, habitCount: Int, cleanStreak: Int) {
    val accentColor = if (totalFails == 0) GlanceTheme.colors.primary else GlanceTheme.colors.error

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(12.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = androidx.glance.layout.Alignment.CenterHorizontally) {
            Text(
                text = "$totalFails",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = if (totalFails == 1) "fail" else "fails",
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 10.sp),
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "across $habitCount habit${if (habitCount == 1) "" else "s"}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = if (cleanStreak == 0) "Failed today" else "${cleanStreak}d streak",
                style = TextStyle(
                    color = if (cleanStreak == 0) GlanceTheme.colors.error else GlanceTheme.colors.primary,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}
