package io.github.fate_grand_automata.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fate_grand_automata.ui.FGATheme
import kotlin.math.roundToInt

/**
 * A custom slider composable that allows selecting a value within a given range.
 * https://gist.github.com/MansuriYamin/b648db1ad62012c634a9d0011300e6d4
 *
 * @param value The current value of the slider.
 * @param onValueChange The callback invoked when the value of the slider changes.
 * @param modifier The modifier to be applied to the slider.
 * @param valueRange The range of values the slider can represent.
 * @param gap The spacing between indicators on the slider.
 * @param showIndicator Determines whether to show indicators on the slider.
 * @param showLabel Determines whether to show a label above the slider.
 * @param enabled Determines whether the slider is enabled for interaction.
 * @param thumb The composable used to display the thumb of the slider.
 * @param track The composable used to display the track of the slider.
 * @param indicator The composable used to display the indicators on the slider.
 * @param label The composable used to display the label above the slider.
 */
@Composable
fun CustomSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 1..10,
    gap: Int = GAP,
    showIndicator: Boolean = false,
    showLabel: Boolean = false,
    enabled: Boolean = true,
    thumb: @Composable (thumbValue: Int) -> Unit = {
        CustomSliderDefaults.Thumb(
            thumbValue = it.toString(),
            enabled = enabled
        )
    },
    track: @Composable (slideState: SliderState) -> Unit = { sliderState ->
        CustomSliderDefaults.Track(
            sliderState = sliderState,
            enabled = enabled
        )
    },
    indicator: @Composable (indicatorValue: Int) -> Unit = { indicatorValue ->
        CustomSliderDefaults.Indicator(
            indicatorValue = indicatorValue.toString(),
            enabled = enabled
        )
    },
    label: @Composable (labelValue: Int) -> Unit = { labelValue ->
        CustomSliderDefaults.Label(
            labelValue = labelValue.toString(),
            enabled = enabled
        )
    }
) {
    CustomSliderImpl(
        valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        gap = gap,
        modifier = modifier,
        value = value.toFloat(),
        showLabel = showLabel,
        label = label,
        thumb = thumb,
        onValueChange = {
            onValueChange(it.roundToInt())
        },
        track = track,
        enabled = enabled,
        showIndicator = showIndicator,
        indicator = indicator
    )
}

/**
 * A custom slider composable that allows selecting a value within a given range.
 * https://gist.github.com/MansuriYamin/b648db1ad62012c634a9d0011300e6d4
 *
 * @param value The current value of the slider.
 * @param onValueChange The callback invoked when the value of the slider changes.
 * @param modifier The modifier to be applied to the slider.
 * @param valueRange The range of values the slider can represent.
 * @param gap The spacing between indicators on the slider.
 * @param showIndicator Determines whether to show indicators on the slider.
 * @param showLabel Determines whether to show a label above the slider.
 * @param enabled Determines whether the slider is enabled for interaction.
 * @param thumb The composable used to display the thumb of the slider.
 * @param track The composable used to display the track of the slider.
 * @param indicator The composable used to display the indicators on the slider.
 * @param label The composable used to display the label above the slider.
 */
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 1f..10f,
    gap: Int = GAP,
    showIndicator: Boolean = false,
    showLabel: Boolean = false,
    enabled: Boolean = true,
    thumb: @Composable (thumbValue: Int) -> Unit = {
        CustomSliderDefaults.Thumb(
            thumbValue = it.toString(),
            enabled = enabled
        )
    },
    track: @Composable (slideState: SliderState) -> Unit = { sliderState ->
        CustomSliderDefaults.Track(
            sliderState = sliderState,
            enabled = enabled
        )
    },
    indicator: @Composable (indicatorValue: Int) -> Unit = { indicatorValue ->
        CustomSliderDefaults.Indicator(
            indicatorValue = indicatorValue.toString(),
            enabled = enabled
        )
    },
    label: @Composable (labelValue: Int) -> Unit = { labelValue ->
        CustomSliderDefaults.Label(
            labelValue = labelValue.toString(),
            enabled = enabled
        )
    }
) {
    CustomSliderImpl(
        valueRange = valueRange,
        gap = gap,
        modifier = modifier,
        value = value,
        showLabel = showLabel,
        label = label,
        thumb = thumb,
        onValueChange = onValueChange,
        track = track,
        enabled = enabled,
        showIndicator = showIndicator,
        indicator = indicator
    )
}

@Composable
private fun CustomSliderImpl(
    valueRange: ClosedFloatingPointRange<Float>,
    gap: Int,
    modifier: Modifier,
    value: Float,
    showLabel: Boolean,
    label: @Composable (labelValue: Int) -> Unit,
    thumb: @Composable (thumbValue: Int) -> Unit,
    onValueChange: (Float) -> Unit,
    track: @Composable (slideState: SliderState) -> Unit,
    enabled: Boolean,
    showIndicator: Boolean,
    indicator: @Composable (indicatorValue: Int) -> Unit
) {
    val itemCount = (valueRange.endInclusive - valueRange.start).roundToInt()
    val steps = if (gap == 1) 0 else (itemCount / gap - 1)

    Box(modifier = modifier) {
        Layout(
            measurePolicy = customSliderMeasurePolicy(
                itemCount = itemCount,
                gap = gap,
                value = value,
                startValue = valueRange.start
            ),
            content = {
                if (showLabel)
                    Label(
                        modifier = Modifier.layoutId(CustomSliderComponents.LABEL),
                        value = value,
                        label = label
                    )

                Box(modifier = Modifier.layoutId(CustomSliderComponents.THUMB)) {
                    thumb(value.roundToInt())
                }

                Slider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layoutId(CustomSliderComponents.SLIDER),
                    value = value,
                    valueRange = valueRange,
                    steps = steps,
                    onValueChange = { onValueChange(it) },
                    thumb = {
                        thumb(value.roundToInt())
                    },
                    track = {
                        track(it)
                    },
                    enabled = enabled
                )

                if (showIndicator)
                    Indicator(
                        modifier = Modifier.layoutId(CustomSliderComponents.INDICATOR),
                        valueRange = valueRange,
                        gap = gap,
                        indicator = indicator
                    )
            })
    }
}

@Composable
private fun Label(
    modifier: Modifier = Modifier,
    value: Float,
    label: @Composable (labelValue: Int) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        label(value.roundToInt())
    }
}

@Composable
private fun Indicator(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    gap: Int,
    indicator: @Composable (indicatorValue: Int) -> Unit
) {
    // Iterate over the value range and display indicators at regular intervals.
    for (i in valueRange.start.roundToInt()..valueRange.endInclusive.roundToInt() step gap) {
        Box(
            modifier = modifier
        ) {
            indicator(i)
        }
    }
}

private fun customSliderMeasurePolicy(
    itemCount: Int,
    gap: Int,
    value: Float,
    startValue: Float
) = MeasurePolicy { measurables, constraints ->
    // Measure the thumb component and calculate its radius.
    val thumbPlaceable = measurables.first {
        it.layoutId == CustomSliderComponents.THUMB
    }.measure(constraints)
    val thumbRadius = (thumbPlaceable.width / 2).toFloat()

    val indicatorPlaceables = measurables.filter {
        it.layoutId == CustomSliderComponents.INDICATOR
    }.map { measurable ->
        measurable.measure(constraints)
    }
    val indicatorHeight = indicatorPlaceables.maxByOrNull { it.height }?.height ?: 0

    val sliderPlaceable = measurables.first {
        it.layoutId == CustomSliderComponents.SLIDER
    }.measure(constraints)
    val sliderHeight = sliderPlaceable.height

    val labelPlaceable = measurables.find {
        it.layoutId == CustomSliderComponents.LABEL
    }?.measure(constraints)
    val labelHeight = labelPlaceable?.height ?: 0

    // Calculate the total width and height of the custom slider layout
    val width = sliderPlaceable.width
    val height = labelHeight + sliderHeight + indicatorHeight

    // Calculate the available width for the track (excluding thumb radius on both sides).
    val trackWidth = width - (2 * thumbRadius)

    // Calculate the width of each section in the track.
    val sectionWidth = trackWidth / itemCount
    // Calculate the horizontal spacing between indicators.
    val indicatorSpacing = sectionWidth * gap

    // To calculate offset of the label, first we will calculate the progress of the slider
    // by subtracting startValue from the current value.
    // After that we will multiply this progress by the sectionWidth.
    // Add thumb radius to this resulting value.
    val labelOffset = (sectionWidth * (value - startValue)) + thumbRadius

    layout(width = width, height = height) {
        var indicatorOffsetX = thumbRadius
        // Place label at top.
        // We have to subtract the half width of the label from the labelOffset,
        // to place our label at the center.
        labelPlaceable?.placeRelative(
            x = (labelOffset - (labelPlaceable.width / 2)).roundToInt(),
            y = 0
        )

        // Place slider placeable below the label.
        sliderPlaceable.placeRelative(x = 0, y = labelHeight)

        // Place indicators below the slider.
        indicatorPlaceables.forEach { placeable ->
            // We have to subtract the half width of the each indicator from the indicatorOffset,
            // to place our indicators at the center.
            placeable.placeRelative(
                x = (indicatorOffsetX - (placeable.width / 2)).roundToInt(),
                y = labelHeight + sliderHeight
            )
            indicatorOffsetX += indicatorSpacing
        }
    }
}

/**
 * Object to hold defaults used by [CustomSlider]
 */
object CustomSliderDefaults {

    /**
     * Composable function that represents the thumb of the slider.
     *
     * @param thumbValue The value to display on the thumb.
     * @param modifier The modifier for styling the thumb.
     * @param color The color of the thumb.
     * @param size The size of the thumb.
     * @param shape The shape of the thumb.
     */
    @Composable
    fun Thumb(
        thumbValue: String,
        modifier: Modifier = Modifier,
        size: Dp = ThumbSize,
        shape: Shape = CircleShape,
        enabled: Boolean = true,
        color: Color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.7f),
        content: @Composable () -> Unit = {
            Text(
                text = thumbValue,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (enabled) 1f else 0.4f),
                textAlign = TextAlign.Center
            )
        }
    ) {
        Box(
            modifier = modifier
                .thumb(size = size, shape = shape)
                .background(color)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }

    /**
     * Composable function that represents the track of the slider.
     *
     * @param sliderState The State of Slider
     * @param modifier The modifier for styling the track.
     * @param trackColor The color of the track.
     * @param progressColor The color of the progress.
     * @param height The height of the track.
     * @param shape The shape of the track.
     */
    @Composable
    fun Track(
        sliderState: SliderState,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        trackColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (enabled) 1f else 0.4f),
        progressColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f),
        height: Dp = TrackHeight,
        shape: Shape = CircleShape
    ) {
        Box(
            modifier = modifier
                .track(height = height, shape = shape)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .progress(
                        sliderState = sliderState,
                        height = height,
                        shape = shape
                    )
                    .background(progressColor)
            )
        }
    }

    /**
     * Composable function that represents the indicator of the slider.
     *
     * @param indicatorValue The value to display as the indicator.
     * @param modifier The modifier for styling the indicator.
     * @param style The style of the indicator text.
     */
    @Composable
    fun Indicator(
        indicatorValue: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (enabled) 1f else 0.4f),
        style: TextStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal)
    ) {
        Box(modifier = modifier) {
            Text(
                text = indicatorValue,
                style = style,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }

    /**
     * Composable function that represents the label of the slider.
     *
     * @param labelValue The value to display as the label.
     * @param modifier The modifier for styling the label.
     * @param style The style of the label text.
     */
    @Composable
    fun Label(
        labelValue: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (enabled) 1f else 0.4f),
        style: TextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
    ) {
        Box(modifier = modifier) {
            Text(
                text = labelValue,
                style = style,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}

fun Modifier.track(
    height: Dp = TrackHeight,
    shape: Shape = CircleShape
) = fillMaxWidth()
    .heightIn(min = height)
    .clip(shape)

fun Modifier.progress(
    sliderState: SliderState,
    height: Dp = TrackHeight,
    shape: Shape = CircleShape
) = fillMaxWidth(
    fraction =
    (sliderState.value.coerceIn(sliderState.valueRange) - sliderState.valueRange.start) /
            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
)
    .heightIn(min = height)
    .clip(shape)


fun Modifier.thumb(size: Dp = ThumbSize, shape: Shape = CircleShape) =
    this
        .defaultMinSize(minWidth = size, minHeight = size)
        .clip(shape)

private enum class CustomSliderComponents {
    SLIDER, LABEL, INDICATOR, THUMB
}

private const val GAP = 1
private val TrackHeight = 8.dp
private val ThumbSize = 30.dp


@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomSliderPreview() {
    FGATheme {
        var value by remember { mutableIntStateOf(5) }
        CustomSlider(
            value = value,
            onValueChange = {
                value = it
            },
            showIndicator = true,
            showLabel = true
        )
    }
}