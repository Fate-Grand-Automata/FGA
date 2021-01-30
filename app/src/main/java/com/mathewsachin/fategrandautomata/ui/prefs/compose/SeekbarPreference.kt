package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@Composable
fun Pref<Int>.SeekBarPreference(
    title: String,
    state: MutableState<Float> = remember { mutableStateOf(get().toFloat()) },
    summary: String = "",
    singleLineTitle: Boolean = true,
    icon: ImageVector? = null,
    valueRange: IntRange = 0..100,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() },
    hint: String = ""
) {
    Preference(
        title = {
            Row {
                Text(
                    text = title,
                    maxLines = if (singleLineTitle) 1 else Int.MAX_VALUE,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = valueRepresentation(state.value.roundToInt())
                )
            }
        },
        summary = {
            PreferenceSummary(
                summary,
                valueRange,
                enabled,
                state
            )
        },
        icon = icon,
        enabled = enabled,
        hint = hint
    )
}

@Composable
private fun Pref<Int>.PreferenceSummary(
    summary: String,
    valueRange: IntRange,
    enabled: Boolean,
    state: MutableState<Float>
) {
    Column {
        Text(text = summary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = state.value,
                onValueChange = { if (enabled) state.value = it },
                onValueChangeEnd = { set(state.value.roundToInt()) },
                valueRange = valueRange.first.toFloat() .. valueRange.last.toFloat()
            )
        }
    }
}