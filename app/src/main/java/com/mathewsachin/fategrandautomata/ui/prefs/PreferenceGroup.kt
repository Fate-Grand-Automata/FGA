package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceGroupHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(
            start = 16.dp,
            top = 24.dp,
            bottom = 8.dp
        )
    )
}