package com.reveny.habittracker.ui.screen.congratulations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reveny.habittracker.ui.components.PaperBackground

@Composable
fun CongratulationsScreen(
    streakDays: Int,
    habitName: String,
    onContinue: () -> Unit,
) {
    val milestone = rememberMilestone(streakDays)

    PaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = milestone.icon,
                contentDescription = null,
                tint = milestone.accentColor,
                modifier = Modifier.size(milestone.iconSize),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = milestone.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = milestone.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$streakDays",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    color = milestone.accentColor,
                )
                Text(
                    text = " days",
                    style = MaterialTheme.typography.headlineMedium,
                    color = milestone.accentColor,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            Text(
                text = habitName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = milestone.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun rememberMilestone(streakDays: Int): MilestoneCopy {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        streakDays >= 90 -> MilestoneCopy(
            title = "Legendary streak",
            subtitle = "This habit is part of who you are now.",
            message = "Ninety days of consistency is rare. Protect the rhythm you built.",
            icon = Icons.Default.WorkspacePremium,
            iconSize = 54.dp,
            accentColor = colorScheme.secondary,
        )
        streakDays >= 60 -> MilestoneCopy(
            title = "Sixty days strong",
            subtitle = "You doubled down and kept going.",
            message = "Two months in, this is more than momentum. It is discipline.",
            icon = Icons.Default.LocalFireDepartment,
            iconSize = 52.dp,
            accentColor = colorScheme.primary,
        )
        else -> MilestoneCopy(
            title = "Congratulations",
            subtitle = "You kept your streak alive.",
            message = "Thirty days is no accident. Keep showing up.",
            icon = Icons.Default.EmojiEvents,
            iconSize = 48.dp,
            accentColor = colorScheme.primary,
        )
    }
}

private data class MilestoneCopy(
    val title: String,
    val subtitle: String,
    val message: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconSize: androidx.compose.ui.unit.Dp,
    val accentColor: androidx.compose.ui.graphics.Color,
)
