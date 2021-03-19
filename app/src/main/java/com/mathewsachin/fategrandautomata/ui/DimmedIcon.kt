package com.mathewsachin.fategrandautomata.ui

import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun DimmedIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String = "icon"
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Icon(
            painter,
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}