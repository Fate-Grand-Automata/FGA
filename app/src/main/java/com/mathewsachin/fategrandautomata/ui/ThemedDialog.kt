package com.mathewsachin.fategrandautomata.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun ThemedDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    Dialog(onDismissRequest = onDismiss) {
        MaterialTheme(
            colors = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}