package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
    summary: String,
    singleLineTitle: Boolean,
    icon: ImageVector,
    valueRange: IntRange = 0..100,
    steps: Int = 0,
    enabled: Boolean = true,
    valueRepresentation: (Int) -> String = { it.toString() },
    hint: String = ""
) {
    Preference(
        title = { Text(text = title, maxLines = if (singleLineTitle) 1 else Int.MAX_VALUE) },
        summary = {
            PreferenceSummary(
                summary,
                valueRepresentation,
                valueRange,
                steps,
                enabled
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
    valueRepresentation: (Int) -> String,
    valueRange: IntRange,
    steps: Int,
    enabled: Boolean,
) {
    var sliderValue by remember { mutableStateOf(get()) }
    asFlow().onEach { sliderValue = it }.collectAsState(defaultValue)

    Column {
        Text(text = summary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = valueRepresentation(sliderValue))
            Spacer(modifier = Modifier.width(16.dp))
            Slider(
                value = sliderValue.toFloat(),
                onValueChange = { if (enabled) sliderValue = it.roundToInt() },
                valueRange = valueRange.first.toFloat() .. valueRange.last.toFloat(),
                steps = steps,
                onValueChangeEnd = {
                    set(sliderValue)
                }
            )
        }
    }
}