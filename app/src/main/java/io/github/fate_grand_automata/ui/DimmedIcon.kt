package io.github.fate_grand_automata.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun DimmedIcon(
    icon: VectorIcon,
    modifier: Modifier = Modifier,
    contentDescription: String = "icon",
    tint: Color? = null
) {
    if (tint != null) {
        Icon(
            icon.asPainter(),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )

    } else {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Icon(
                icon.asPainter(),
                contentDescription = contentDescription,
                modifier = modifier,
            )
        }
    }

}