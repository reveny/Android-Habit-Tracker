package com.reveny.habittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Sage,
    onPrimary = Cream,
    primaryContainer = SageLight,
    onPrimaryContainer = InkDark,
    secondary = Terracotta,
    onSecondary = Cream,
    secondaryContainer = TerracottaLight,
    onSecondaryContainer = InkDark,
    background = Paper,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = PaperDark,
    onSurfaceVariant = InkLight,
    outline = JournalBorder,
    outlineVariant = JournalBorder.copy(alpha = 0.5f),
)

private val DarkColorScheme = darkColorScheme(
    primary = SageNight,
    onPrimary = Color(0xFF1A3028),
    primaryContainer = SageContainerN,
    onPrimaryContainer = NightOnSurface,
    secondary = TerracottaNight,
    onSecondary = Color(0xFF3D1810),
    secondaryContainer = TerrContainerN,
    onSecondaryContainer = NightOnSurface,
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVar,
    onSurfaceVariant = NightOnSurfaceVar,
    outline = NightBorder,
    outlineVariant = NightBorder.copy(alpha = 0.5f),
)

@Composable
fun HabitTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = HabitTrackerTypography,
        shapes = HabitTrackerShapes,
        content = content,
    )
}
