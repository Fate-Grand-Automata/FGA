package io.github.fate_grand_automata.ui.prefs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.VectorIcon

@Composable
fun Pref<Int>.StepperPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    valueRange: IntRange = 0..100,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() },
) {
    var state by remember()

    Preference(
        title = { Text(title) },
        summary = {
            Stepper(
                value = state,
                onValueChange = { state = it },
                valueRange = valueRange,
                valueRepresentation = valueRepresentation,
            )
        },
        icon = icon,
        enabled = enabled,
        modifier = modifier,
    )
}
