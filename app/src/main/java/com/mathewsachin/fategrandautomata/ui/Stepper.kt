package com.mathewsachin.fategrandautomata.ui

import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.ui.prefs.StatusWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.milliseconds

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
    val scope = rememberCoroutineScope()
    val canDelta = (currentValue + delta) in valueRange
    val isEnabled = enabled && canDelta

    // rememberUpdatedState is needed on State objects accessed from the gesture callback,
    // otherwise the callback itself would get recomposed.
    val rememberedCanDelta by rememberUpdatedState(canDelta)
    val rememberedIsEnabled by rememberUpdatedState(isEnabled)

    val rememberedCurrentValue by rememberUpdatedState(currentValue)
    val onCurrentValueDelta by rememberUpdatedState {
        onCurrentValueChange(rememberedCurrentValue + delta)
    }
    val onPerformCommit by rememberUpdatedState {
        onCommit(rememberedCurrentValue.coerceIn(valueRange))
    }

    val longPressTimeout = ViewConfiguration.getLongPressTimeout().milliseconds
    val repeatIntervalDelta = 2.milliseconds
    val minRepeatInterval = 10.milliseconds

    Surface(
        modifier = Modifier
            .pointerInput(true) {
                detectTapGestures(onPress = {
                    if (rememberedIsEnabled) {
                        val currentJob = scope.launch {
                            var first = false

                            try {
                                delay(longPressTimeout)
                                var repeatInterval = 100.milliseconds

                                while (rememberedCanDelta) {
                                    onCurrentValueDelta()
                                    first = true

                                    delay(repeatInterval)

                                    repeatInterval = (repeatInterval - repeatIntervalDelta)
                                        .coerceAtLeast(minRepeatInterval)
                                }
                            } catch (e: Exception) {
                                // Ignore
                            }

                            if (!first) {
                                onCurrentValueDelta()
                            }
                        }

                        tryAwaitRelease()

                        currentJob.cancel()
                        currentJob.join()

                        // Some delay otherwise value won't update on every other single tap
                        delay(10.milliseconds)

                        onPerformCommit()
                    }
                })
            }
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
    enabled: Boolean = true
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
            delta = -1,
            text = "-",
            enabled = enabled
        )

        StatusWrapper(enabled = enabled) {
            Text(
                currentValue.toString(),
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
            delta = 1,
            text = "+",
            enabled = enabled
        )
    }
}