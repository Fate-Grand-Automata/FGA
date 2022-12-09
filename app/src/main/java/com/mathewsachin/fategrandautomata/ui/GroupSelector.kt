package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GroupSelectorItem(
    item: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(end = 5.dp),
        onClick = onSelect,
        enabled = enabled
    ) {
        Text(
            item,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified,
            modifier = Modifier.padding(5.dp, 2.dp)
        )
    }
}