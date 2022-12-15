package com.mathewsachin.fategrandautomata.ui

import android.view.ViewConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun Modifier.holdRepeatClickable(
    onRepeat: () -> Unit,
    onEnd: () -> Unit,
    enabled: Boolean = true
) = composed {
    // rememberUpdatedState is needed on State objects accessed from the gesture callback,
    // otherwise the callback would only get the outdated values.
    val rememberedIsEnabled by rememberUpdatedState(enabled)
    val rememberedOnRepeat by rememberUpdatedState(onRepeat)
    val rememberedOnEnd by rememberUpdatedState(onEnd)

    val interactionSource = remember { MutableInteractionSource() }

    val longPressTimeout = ViewConfiguration.getLongPressTimeout().milliseconds
    val repeatIntervalDelta = 2.milliseconds
    val minRepeatInterval = 10.milliseconds

    val scope = rememberCoroutineScope()

    Modifier
        .pointerInput(true) {
            detectTapGestures(onPress = { offset ->
                if (rememberedIsEnabled) {
                    try {
                        val currentJob = scope.launch {
                            var first = false

                            try {
                                delay(longPressTimeout)
                                var repeatInterval = 100.milliseconds

                                while (rememberedIsEnabled) {
                                    rememberedOnRepeat()
                                    first = true

                                    delay(repeatInterval)

                                    repeatInterval = (repeatInterval - repeatIntervalDelta)
                                        .coerceAtLeast(minRepeatInterval)
                                }
                            } finally {
                                if (!first) {
                                    rememberedOnRepeat()
                                }
                            }
                        }

                        val pressInteraction = PressInteraction.Press(offset)
                        interactionSource.emit(pressInteraction)

                        val success = tryAwaitRelease()

                        val endInteraction =
                            if (success) {
                                PressInteraction.Release(pressInteraction)
                            } else {
                                PressInteraction.Cancel(pressInteraction)
                            }

                        interactionSource.emit(endInteraction)

                        currentJob.cancelAndJoin()
                    } finally {
                        rememberedOnEnd()
                    }
                }
            })
        }
        .indication(
            interactionSource = interactionSource,
            indication = rememberRipple()
        )
}