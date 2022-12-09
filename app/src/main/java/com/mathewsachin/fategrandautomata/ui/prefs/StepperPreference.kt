package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.Stepper
import com.mathewsachin.fategrandautomata.ui.VectorIcon

@Composable
fun Pref<Int>.StepperPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    valueRange: IntRange = 0..100,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() }
) {
    var state by remember()

    Preference(
        title = { Text(title) },
        summary = {
            Stepper(
                value = state,
                onValueChange = { state = it },
                valueRange = valueRange,
                valueRepresentation = valueRepresentation
            )
        },
        icon = icon,
        enabled = enabled,
        modifier = modifier
    )
}