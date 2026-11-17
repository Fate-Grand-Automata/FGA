package io.github.fate_grand_automata.ui.fine_tune

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.FGASliderColors
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun FineTuneItem.FineTuneSetter() {
    val defaultString = stringResource(
        R.string.p_fine_tune_default_value,
        valueRepresentation(pref.defaultValue)
    )

    val hintDialog = FgaDialog()
    hintDialog.build {
        title(
            text = stringResource(name),
            icon = icon
        )

        message("$defaultString\n\n${stringResource(hint)}")

        buttons(
            onSubmit = { reset() },
            okLabel = stringResource(R.string.reset_to_default)
        )
    }

    Column {
        Row {
            ListItem(
                headlineContent = { Text(stringResource(name)) },
                supportingContent = { Text(defaultString) },
                modifier = Modifier.weight(1f),
                colors = FGAListItemColors()
            )

            IconButton(
                onClick = { hintDialog.show() }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_info),
                    contentDescription = "Info"
                )
            }
        }

        var value by pref.remember()

        ValueSlider(
            value = value,
            onValueChange = { value = it }
        )
    }
}

@Composable
private fun FineTuneItem.ValueSlider(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    /*
     * The slider moves continuously but only writes on release, so a drag doesn't hit storage on
     * every frame. Re-keying on `value` snaps the thumb to what was committed.
     */
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    val colors = FGASliderColors()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(snapToStep(sliderValue)) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            /*
             * `steps` counts the positions between the ends, not the intervals, and Slider
             * rejects a negative count for a range narrower than two steps.
             */
            steps = ((valueRange.last - valueRange.first) / step - 1).coerceAtLeast(0),
            colors = colors,
            interactionSource = interactionSource,
            // The default thumb is 44dp tall, which overhangs this row and crowds the card edge.
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(4.dp, 24.dp)
                )
            },
            /*
             * A tick per step turns a 40- or 120-position track into a dotted line and swamps the
             * filled/empty contrast. The value label already says where the thumb landed.
             */
            track = { state ->
                SliderDefaults.Track(
                    sliderState = state,
                    colors = colors,
                    drawTick = { _, _ -> }
                )
            },
            modifier = Modifier.weight(1f)
        )

        Text(
            valueRepresentation(snapToStep(sliderValue)),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 16.dp)
                .widthIn(min = 64.dp),
            textAlign = TextAlign.End
        )
    }
}
