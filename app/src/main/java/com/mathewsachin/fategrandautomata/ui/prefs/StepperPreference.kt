package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon

@Composable
fun Pref<Int>.StepperPreference(
    title: String,
    icon: VectorIcon? = null,
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