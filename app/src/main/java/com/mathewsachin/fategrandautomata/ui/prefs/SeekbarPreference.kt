package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import kotlin.math.roundToInt

@Composable
fun Pref<Int>.SeekBarPreference(
    title: String,
    modifier: Modifier = Modifier,
    state: MutableState<Float> = remember { mutableStateOf(get().toFloat()) },
    summary: String = "",
    singleLineTitle: Boolean = true,
    icon: VectorIcon? = null,
    valueRange: IntRange = 0..100,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() }
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
        modifier = modifier
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
                onValueChangeFinished = { set(state.value.roundToInt()) },
                valueRange = valueRange.first.toFloat() .. valueRange.last.toFloat()
            )
        }
    }
}