package com.reveny.habittracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reveny.habittracker.ui.theme.PlayfairDisplay

@Composable
fun JournalQuote(quote: String, modifier: Modifier = Modifier) {
    Text(
        text = "\"$quote\"",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = PlayfairDisplay,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}
