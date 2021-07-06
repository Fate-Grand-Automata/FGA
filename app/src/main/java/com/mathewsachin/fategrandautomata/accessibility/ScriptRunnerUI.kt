package com.mathewsachin.fategrandautomata.accessibility

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.FGATheme

sealed class ScriptRunnerUIState {
    object Running: ScriptRunnerUIState()
    object Paused: ScriptRunnerUIState()
    object Idle: ScriptRunnerUIState()
}

sealed class ScriptRunnerUIAction {
    object Start: ScriptRunnerUIAction()
    object Pause: ScriptRunnerUIAction()
    object Resume: ScriptRunnerUIAction()
    object Stop: ScriptRunnerUIAction()
}

@Composable
fun ScriptRunnerUI(
    state: ScriptRunnerUIState,
    updateState: (ScriptRunnerUIAction) -> Unit,
    onDrag: (Float, Float) -> Unit,
    enabled: Boolean,
    isRecording: Boolean
) {
    FGATheme(
        background = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
        ) {
            Button(
                onClick = {
                    val action = when (state) {
                        ScriptRunnerUIState.Idle -> ScriptRunnerUIAction.Start
                        ScriptRunnerUIState.Paused -> ScriptRunnerUIAction.Stop
                        ScriptRunnerUIState.Running -> ScriptRunnerUIAction.Pause
                    }

                    updateState(action)
                },
                enabled = enabled,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consumeAllChanges()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
            ) {
                val tint = if (isRecording)
                    MaterialTheme.colors.error
                else LocalContentColor.current

                CompositionLocalProvider(LocalContentColor provides tint) {
                    Icon(
                        painter = when (state) {
                            ScriptRunnerUIState.Idle -> painterResource(R.drawable.ic_play)
                            ScriptRunnerUIState.Paused -> painterResource(R.drawable.ic_stop)
                            ScriptRunnerUIState.Running -> painterResource(R.drawable.ic_pause)
                        },
                        contentDescription = when (state) {
                            ScriptRunnerUIState.Idle -> "start"
                            ScriptRunnerUIState.Paused -> "stop"
                            ScriptRunnerUIState.Running -> "pause"
                        }
                    )
                }
            }

            if (state is ScriptRunnerUIState.Paused) {
                Button(
                    onClick = { updateState(ScriptRunnerUIAction.Resume) },
                    modifier = Modifier
                        .padding(start = 5.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_play),
                        contentDescription = "resume"
                    )
                }
            }
        }
    }
}