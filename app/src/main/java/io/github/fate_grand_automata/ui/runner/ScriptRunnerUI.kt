package io.github.fate_grand_automata.ui.runner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun ScriptRunnerUI(
    state: ScriptRunnerUIState,
    prefsCore: PrefsCore,
    updateState: (ScriptRunnerUIAction) -> Unit,
    onDrag: (Float, Float) -> Unit,
    enabled: Boolean,
    isRecording: Boolean
) {
    val hidePlayButtonForScreenshot by prefsCore.hidePlayButtonForScreenshot.remember()

    FGATheme(
        darkTheme = true,
        background = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
        ) {
            val dragModifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }

            Surface(
                color = when (state) {
                    ScriptRunnerUIState.Running ->
                        MaterialTheme.colorScheme.surface.copy(alpha = if (hidePlayButtonForScreenshot) 0f else 0.5f)
                    else -> MaterialTheme.colorScheme.surface
                },
                contentColor = when (state) {
                    ScriptRunnerUIState.Running -> {
                        val color = if (isRecording) MaterialTheme.colorScheme.error else Color.White
                        color.copy(alpha = if (hidePlayButtonForScreenshot) 0f else 0.5f)
                    }
                    else -> Color.White
                },
                tonalElevation = 5.dp,
                shape = CircleShape,
                onClick = {
                    val action = when (state) {
                        ScriptRunnerUIState.Idle -> ScriptRunnerUIAction.Start
                        is ScriptRunnerUIState.Paused -> ScriptRunnerUIAction.Resume
                        ScriptRunnerUIState.Running -> ScriptRunnerUIAction.Pause
                    }

                    updateState(action)
                },
                enabled = enabled,
                modifier = dragModifier
            ) {
                Icon(
                    painter = when (state) {
                        ScriptRunnerUIState.Idle, is ScriptRunnerUIState.Paused -> painterResource(R.drawable.ic_play)
                        ScriptRunnerUIState.Running -> painterResource(R.drawable.ic_pause)
                    },
                    contentDescription = when (state) {
                        ScriptRunnerUIState.Idle -> "start"
                        is ScriptRunnerUIState.Paused -> "resume"
                        ScriptRunnerUIState.Running -> "pause"
                    },
                    modifier = Modifier
                        .padding(18.dp, 10.dp)
                )
            }

            AnimatedVisibility(
                state is ScriptRunnerUIState.Paused,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally(),
                modifier = Modifier
                    .offset(x = (-18).dp)
                    .zIndex(-1f)
            ) {
                Row {
                    val shape = RoundedCornerShape(0, 50, 50, 0)

                    Surface(
                        color = Color(0xFFCF6679),
                        contentColor = Color.White,
                        shape = shape,
                        onClick = { updateState(ScriptRunnerUIAction.Stop) },
                        modifier = dragModifier
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_stop),
                            contentDescription = "stop",
                            modifier = Modifier
                                .padding(18.dp, 10.dp)
                                .padding(start = 8.dp)
                        )
                    }

                    state.let {
                        if (it is ScriptRunnerUIState.Paused && it.pausedStatus != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                tonalElevation = 5.dp,
                                shape = shape,
                                onClick = { updateState(ScriptRunnerUIAction.Status(it.pausedStatus)) },
                                modifier = dragModifier
                                    .offset(x = (-18).dp)
                                    .zIndex(-2f)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_info),
                                    contentDescription = "status",
                                    modifier = Modifier
                                        .padding(18.dp, 10.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}