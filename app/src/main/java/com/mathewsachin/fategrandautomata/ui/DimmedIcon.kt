package com.mathewsachin.fategrandautomata.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
fun DimmedIcon(
    icon: VectorIcon,
    modifier: Modifier = Modifier,
    contentDescription: String = "icon"
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Icon(
            icon.asPainter(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}