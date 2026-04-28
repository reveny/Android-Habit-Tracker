package com.reveny.habittracker.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reveny.habittracker.data.model.CalendarDay
import com.reveny.habittracker.ui.theme.Sage
import com.reveny.habittracker.ui.theme.Terracotta
import java.time.YearMonth

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    days: List<CalendarDay>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val cells = buildList {
            repeat(firstDayOffset) { add(null) }
            addAll(days)
        }
        val rows = cells.chunked(7)

        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (i in 0 until 7) {
                    val calDay = week.getOrNull(i)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (calDay != null) {
                            DayCell(calDay, onClick = { onDayClick(calDay.day) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, onClick: () -> Unit) {
    val bgColor = when {
        day.failureCount > 0 -> Terracotta.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val borderMod = if (day.isToday) {
        Modifier.border(1.5.dp, Sage, CircleShape)
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(2.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bgColor, CircleShape)
                .then(borderMod),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (day.failureCount > 0) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Terracotta),
            )
        }
    }
}
