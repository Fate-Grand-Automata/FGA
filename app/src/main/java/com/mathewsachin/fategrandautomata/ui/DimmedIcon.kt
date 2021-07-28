package com.mathewsachin.fategrandautomata.ui

import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
fun DimmedIcon(
    icon: VectorIcon,
    modifier: Modifier = Modifier,
    contentDescription: String = "icon"
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Icon(
            icon.asPainter(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}