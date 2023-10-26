package io.github.fate_grand_automata.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.prefs.IPreferences

@Composable
fun playButtonDetectionLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_play_button_detection),
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.p_play_button_detection_explanation),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            DrawLocationGuide(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }


    }


    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = { ScriptLauncherResponse.PlayButtonDetection }
    )
}

@Composable
private fun DrawLocationGuide(
    modifier: Modifier = Modifier,
) {
    val colors = listOf(
        MaterialTheme.colorScheme.onBackground,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.onPrimary
    )

    val transition = rememberInfiniteTransition(label = "infinite transition")

    val colorIndex by transition.animateValue(
        initialValue = 0,
        targetValue = colors.size - 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "transition between colors"
    )

    val color = colors[colorIndex % colors.size]

    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .height(150.dp)
                .width(250.dp)
        ) {
            drawRect(color = primaryContainerColor)
            drawPath(
                path = Path().apply {
                    // draw arrow pointing to the bottom left
                    moveTo(size.width * 3 / 4, size.height * 1 / 4)

                    lineTo(size.width * 1 / 8, size.height * 7 / 8)
                    lineTo(size.width * 1 / 8, size.height * 3 / 4)

                    lineTo(size.width * 1 / 8, size.height * 7 / 8)
                    lineTo(size.width * 1 / 4, size.height * 7 / 8)
                },
                color = color,
                style = Stroke(width = 4f)
            )
            // play button
            drawCircle(
                color = onPrimaryContainerColor,
                radius = 10f,
                center = Offset(0 + 10f, size.height - 10f)
            )
        }
    }


}