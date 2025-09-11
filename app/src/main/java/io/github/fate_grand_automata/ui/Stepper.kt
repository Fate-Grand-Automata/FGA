package io.github.fate_grand_automata.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.ui.prefs.StatusWrapper

@Composable
private fun DeltaButton(
    currentValue: Int,
    onCurrentValueChange: (Int) -> Unit,
    onCommit: () -> Unit,
    valueRange: IntRange,
    delta: Int,
    text: String,
    enabled: Boolean,
) {
    val canDelta = (currentValue + delta) in valueRange
    val isEnabled = enabled && canDelta

    Surface(
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = CircleShape,
    ) {
        StatusWrapper(enabled = isEnabled) {
            Text(
                text,
                modifier = Modifier
                    .holdRepeatClickable(
                        onRepeat = {
                            onCurrentValueChange((currentValue + delta).coerceIn(valueRange))
                        },
                        onEnd = onCommit,
                        enabled = isEnabled,
                    )
                    .padding(20.dp, 10.dp),
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
    valueRepresentation: (Int) -> String = { it.toString() },
) {
    var currentValue by remember(value) { mutableStateOf(value) }

    val onCommit = { onValueChange(currentValue.coerceIn(valueRange)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeltaButton(
            currentValue = currentValue,
            onCurrentValueChange = { currentValue = it },
            onCommit = onCommit,
            valueRange = valueRange,
            delta = -delta,
            text = "-",
            enabled = enabled,
        )

        StatusWrapper(enabled = enabled) {
            Text(
                valueRepresentation(currentValue),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 5.dp),
            )
        }

        DeltaButton(
            currentValue = currentValue,
            onCurrentValueChange = { currentValue = it },
            onCommit = onCommit,
            valueRange = valueRange,
            delta = delta,
            text = "+",
            enabled = enabled,
        )
    }
}
