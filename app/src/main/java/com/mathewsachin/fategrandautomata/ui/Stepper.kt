package com.mathewsachin.fategrandautomata.ui.prefs

import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.milliseconds

@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    var currentJob by remember { mutableStateOf<Job?>(null) }

    var currentValue by remember(value) { mutableStateOf(value) }

    val onComplete = {
        scope.launch {
            currentJob?.cancel()
            currentJob?.join()

            onValueChange(currentValue.coerceIn(valueRange))
        }
    }

    fun makeModifier(delta: Int, enabled: Boolean) =
        Modifier
            .pressIndicatorGestureFilter(
                onStart = {
                    currentJob?.cancel()
                    currentJob = scope.launch {
                        var first = false
                        val onCurrentValueChange = { currentValue += delta }

                        try {
                            delay(ViewConfiguration.getLongPressTimeout().milliseconds)

                            var repeatInterval = 100.milliseconds
                            val repeatIntervalDelta = 2.milliseconds
                            val minRepeatInterval = 10.milliseconds

                            while (true) {
                                onCurrentValueChange()
                                first = true

                                delay(repeatInterval)

                                repeatInterval = (repeatInterval - repeatIntervalDelta)
                                    .coerceAtLeast(minRepeatInterval)
                            }
                        } catch (e: Exception) {
                            if (!first) {
                                onCurrentValueChange()
                            }
                        }
                    }
                },
                onStop = { onComplete() },
                onCancel = { onComplete() },
                enabled = enabled && (currentValue + delta) in valueRange
            )

    @Composable
    fun DeltaButton(delta: Int, text: String, enabled: Boolean) {
        Surface(
            modifier = makeModifier(delta, enabled)
        ) {
            StatusWrapper(enabled = enabled && (currentValue + delta) in valueRange) {
                Text(
                    text,
                    modifier = Modifier
                        .padding(20.dp, 10.dp)
                )
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeltaButton(delta = -1, text = "-", enabled = enabled)

        StatusWrapper(enabled = enabled) {
            Text(
                currentValue.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
            )
        }

        DeltaButton(delta = 1, text = "+", enabled = enabled)
    }
}