package com.mathewsachin.fategrandautomata.accessibility

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    object Status: ScriptRunnerUIAction()
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
                        change.consumeAllChanges()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }

            Surface(
                color = MaterialTheme.colors.surface,
                contentColor = if (isRecording) MaterialTheme.colors.error else Color.White,
                elevation = 5.dp,
                shape = CircleShape,
                onClick = {
                    val action = when (state) {
                        ScriptRunnerUIState.Idle -> ScriptRunnerUIAction.Start
                        ScriptRunnerUIState.Paused -> ScriptRunnerUIAction.Resume
                        ScriptRunnerUIState.Running -> ScriptRunnerUIAction.Pause
                    }

                    updateState(action)
                },
                enabled = enabled,
                modifier = dragModifier
            ) {
                Icon(
                    painter = when (state) {
                        ScriptRunnerUIState.Idle, ScriptRunnerUIState.Paused -> painterResource(R.drawable.ic_play)
                        ScriptRunnerUIState.Running -> painterResource(R.drawable.ic_pause)
                    },
                    contentDescription = when (state) {
                        ScriptRunnerUIState.Idle -> "start"
                        ScriptRunnerUIState.Paused -> "resume"
                        ScriptRunnerUIState.Running -> "pause"
                    },
                    modifier = Modifier
                        .padding(18.dp, 10.dp)
                )
            }

            AnimatedVisibility (
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

                    Surface(
                        color = MaterialTheme.colors.surface,
                        contentColor = Color.White,
                        elevation = 5.dp,
                        shape = shape,
                        onClick = { updateState(ScriptRunnerUIAction.Status) },
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