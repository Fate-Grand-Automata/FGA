package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GroupSelectorItem(
    item: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(end = 5.dp),
        onClick = onSelect
    ) {
        Text(
            item,
            color = if (isSelected) MaterialTheme.colors.onPrimary else Color.Unspecified,
            modifier = Modifier.padding(5.dp, 2.dp)
        )
    }
}