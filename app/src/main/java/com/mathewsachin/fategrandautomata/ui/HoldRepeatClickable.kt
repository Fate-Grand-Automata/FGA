package com.mathewsachin.fategrandautomata.ui

import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun Modifier.holdRepeatClickable(
    onRepeat: () -> Unit,
    onEnd: () -> Unit,
    enabled: Boolean = true
) = composed {
    // rememberUpdatedState is needed on State objects accessed from the gesture callback,
    // otherwise the callback itself would get recomposed.
    val rememberedIsEnabled by rememberUpdatedState(enabled)
    val rememberedOnRepeat by rememberUpdatedState(onRepeat)
    val rememberedOnEnd by rememberUpdatedState(onEnd)

    val interactionSource = remember { MutableInteractionSource() }
    var pressedInteraction by remember { mutableStateOf<PressInteraction.Press?>(null) }
    DisposableEffect(interactionSource) {
        onDispose {
            pressedInteraction?.let {
                interactionSource.tryEmit(PressInteraction.Cancel(it))
            }
        }
    }

    val longPressTimeout = Duration.milliseconds(ViewConfiguration.getLongPressTimeout())
    val repeatIntervalDelta = Duration.milliseconds(2)
    val minRepeatInterval = Duration.milliseconds(10)

    val scope = rememberCoroutineScope()

    Modifier
        .pointerInput(true) {
            detectTapGestures(onPress = { offset ->
                if (rememberedIsEnabled) {
                    val currentJob = scope.launch {
                        var first = false

                        try {
                            delay(longPressTimeout)
                            var repeatInterval = Duration.milliseconds(100)

                            while (true) {
                                rememberedOnRepeat()
                                @Suppress("UNUSED_VALUE")
                                first = true

                                delay(repeatInterval)

                                repeatInterval = (repeatInterval - repeatIntervalDelta)
                                    .coerceAtLeast(minRepeatInterval)
                            }
                        } catch (e: Exception) {
                            // Ignore
                        }

                        if (!first) {
                            rememberedOnRepeat()
                        }
                    }

                    val pressInteraction = PressInteraction.Press(offset)
                    interactionSource.emit(pressInteraction)
                    pressedInteraction = pressInteraction

                    val success = tryAwaitRelease()

                    val endInteraction =
                        if (success) {
                            PressInteraction.Release(pressInteraction)
                        } else {
                            PressInteraction.Cancel(pressInteraction)
                        }

                    interactionSource.emit(endInteraction)
                    pressedInteraction = null

                    currentJob.cancel()
                    currentJob.join()

                    rememberedOnEnd()
                }
            })
        }
        .indication(
            interactionSource = interactionSource,
            indication = rememberRipple()
        )
}