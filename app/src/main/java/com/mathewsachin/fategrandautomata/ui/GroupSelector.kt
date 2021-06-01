package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GroupSelectorItem(
    item: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .padding(end = 5.dp)
            .background(
                color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
                shape = shape
            )
            .clip(shape)
            .clickable { onSelect() }
    ) {
        Text(
            item,
            color = if (isSelected) MaterialTheme.colors.onPrimary else Color.Unspecified,
            modifier = Modifier.padding(5.dp, 2.dp)
        )
    }
}