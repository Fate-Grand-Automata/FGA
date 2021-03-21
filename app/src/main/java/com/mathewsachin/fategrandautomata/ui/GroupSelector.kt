package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> GroupSelector(
    groups: List<T>,
    selected: T,
    onSelectedChange: (T) -> Unit,
    stringify: @Composable (T) -> String = { it.toString() }
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(groups) {
            val isSelected = selected == it

            Box(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { onSelectedChange(it) }
            ) {
                Text(
                    stringify(it),
                    color = if (isSelected) MaterialTheme.colors.onSecondary else Color.Unspecified,
                    modifier = Modifier.padding(5.dp, 2.dp)
                )
            }
        }
    }
}