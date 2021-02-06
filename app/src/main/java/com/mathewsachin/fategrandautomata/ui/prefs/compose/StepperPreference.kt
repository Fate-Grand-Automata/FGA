package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import com.mathewsachin.fategrandautomata.prefs.core.Pref

@Composable
fun Pref<Int>.StepperPreference(
    title: String,
    icon: ImageVector? = null,
    valueRange: IntRange = 0..100,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() }
) {
    val state by collect()

    Preference(
        title = { Text(title) },
        summary = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { set((state - 1).coerceIn(valueRange)) },
                    enabled = enabled && state > valueRange.first
                ) {
                    Text("-")
                }

                Text(valueRepresentation(state))

                TextButton(
                    onClick = { set((state + 1).coerceIn(valueRange)) },
                    enabled = enabled && state < valueRange.last
                ) {
                    Text("+")
                }
            }
        },
        icon = icon,
        enabled = enabled
    )
}