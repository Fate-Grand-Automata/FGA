package io.github.fate_grand_automata.ui.prefs

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.ui.FGATheme

enum class SwitchState {
    ON, OFF, UNKNOWN
}

@Composable
fun TriStateSwitch(
    onCheckedChange: ((Boolean) -> Unit)?,
    checked: SwitchState = SwitchState.UNKNOWN,
    width: Dp = 52.dp,
    height: Dp = 30.dp,
    strokeWidth: Dp = 2.dp,
    gapBetweenThumbAndTrackEdge: Dp = 6.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val onOutlineColor = colorScheme.primary
    val onFillColor = colorScheme.primary
    val onThumbColor = colorScheme.onPrimary
    val offOutlineColor = colorScheme.outline
    val offThumbColor = colorScheme.outline
    val offFillColor = colorScheme.surfaceVariant

    val switchState = remember {
        mutableStateOf(checked)
    }

    val thumbRadius = (height / 2) - gapBetweenThumbAndTrackEdge

    // To move thumb, we need to calculate the position (along x axis)
    val animatePosition = animateFloatAsState(
        targetValue = when (switchState.value) {
            SwitchState.ON -> with(LocalDensity.current) { (width - thumbRadius - gapBetweenThumbAndTrackEdge).toPx() }
            SwitchState.OFF -> with(LocalDensity.current) { (thumbRadius + gapBetweenThumbAndTrackEdge).toPx() }
            SwitchState.UNKNOWN -> with(LocalDensity.current) { width.toPx() / 2 }
        }
    )

    Canvas(
        modifier = Modifier
            .size(width = width, height = height)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // This is called when the user taps on the canvas
                        when (switchState.value) {
                            SwitchState.ON -> {
                                switchState.value = SwitchState.OFF
                                onCheckedChange?.invoke(false)
                            }
                            SwitchState.UNKNOWN, SwitchState.OFF -> {
                                switchState.value = SwitchState.ON
                                onCheckedChange?.invoke(true)
                            }
                        }
                    }
                )
            }
            .padding(strokeWidth / 2)
    ) {
        val halfWidth = width.toPx() / 2

        // Track
        drawRoundRect(
            brush = Brush.horizontalGradient(when (switchState.value) {
                SwitchState.ON -> List(2) { onFillColor }
                SwitchState.OFF -> List(2) { offFillColor }
                SwitchState.UNKNOWN -> listOf(offFillColor, onFillColor)
            }, halfWidth - 1, halfWidth + 1),
            cornerRadius = CornerRadius(x = 50f, y = 50f),
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(when (switchState.value) {
                SwitchState.ON -> List(2) { onOutlineColor }
                SwitchState.OFF -> List(2) { offOutlineColor }
                SwitchState.UNKNOWN -> listOf(offOutlineColor, onOutlineColor)
            }, halfWidth - 1, halfWidth + 1),
            cornerRadius = CornerRadius(x = 50f, y = 50f),
            style = Stroke(width = strokeWidth.toPx())
        )


        // Thumb
        drawCircle(
            brush = Brush.horizontalGradient(when (switchState.value) {
                SwitchState.ON -> List(2) { onThumbColor }
                SwitchState.OFF -> List(2) { offThumbColor }
                SwitchState.UNKNOWN -> listOf(offThumbColor, onThumbColor)
            }, halfWidth - 1, halfWidth + 1),
            radius = thumbRadius.toPx(),
            center = Offset(
                x = animatePosition.value,
                y = size.height / 2
            )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestTriStateSwitch() {
    FGATheme {
        Column(

        ) {
            TriStateSwitch(checked = SwitchState.ON, onCheckedChange = null)
            TriStateSwitch(checked = SwitchState.OFF, onCheckedChange = null)
            TriStateSwitch(checked = SwitchState.UNKNOWN, onCheckedChange = null)
            Switch(checked = true, onCheckedChange = null)
            Switch(checked = false, onCheckedChange = null)
        }
    }
}