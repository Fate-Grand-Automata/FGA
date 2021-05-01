package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.ui.prefs.StatusWrapper

@Composable
private fun DeltaButton(
    currentValue: Int,
    onCurrentValueChange: (Int) -> Unit,
    onCommit: (Int) -> Unit,
    valueRange: IntRange,
    delta: Int,
    text: String,
    enabled: Boolean
) {
    val canDelta = (currentValue + delta) in valueRange
    val isEnabled = enabled && canDelta

    Surface(
        color = Color.Transparent,
        contentColor = MaterialTheme.colors.onBackground,
        modifier = Modifier
            .clip(CircleShape)
            .holdRepeatClickable(
                onRepeat = {
                    onCurrentValueChange((currentValue + delta).coerceIn(valueRange))
                },
                onEnd = { onCommit(currentValue.coerceIn(valueRange)) },
                enabled = isEnabled
            )
    ) {
        StatusWrapper(enabled = isEnabled) {
            Text(
                text,
                modifier = Modifier
                    .padding(20.dp, 10.dp)
            )
        }
    }
}

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    enabled: Boolean = true,
    delta: Int = 1,
    valueRepresentation: (Int) -> String = { it.toString() }
) {
    var currentValue by remember(value) { mutableStateOf(value) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeltaButton(
            currentValue = currentValue,
            onCurrentValueChange = { currentValue = it },
            onCommit = onValueChange,
            valueRange = valueRange,
            delta = -delta,
            text = "-",
            enabled = enabled
        )

        StatusWrapper(enabled = enabled) {
            Text(
                valueRepresentation(currentValue),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
            )
        }

        DeltaButton(
            currentValue = currentValue,
            onCurrentValueChange = { currentValue = it },
            onCommit = onValueChange,
            valueRange = valueRange,
            delta = delta,
            text = "+",
            enabled = enabled
        )
    }
}